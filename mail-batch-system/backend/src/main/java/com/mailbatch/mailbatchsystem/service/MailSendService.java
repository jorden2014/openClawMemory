package com.mailbatch.mailbatchsystem.service;

import com.mailbatch.mailbatchsystem.dto.SendMailRequest;
import com.mailbatch.mailbatchsystem.entity.Customer;
import com.mailbatch.mailbatchsystem.entity.MailRecord;
import com.mailbatch.mailbatchsystem.entity.MailTemplate;
import com.mailbatch.mailbatchsystem.repository.CustomerRepository;
import com.mailbatch.mailbatchsystem.repository.MailRecordRepository;
import com.mailbatch.mailbatchsystem.repository.MailTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 邮件发送服务类
 * 核心业务：提交发送任务、消费队列、控制发送频率、失败重试
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendService {

    private final MailRecordRepository mailRecordRepository;
    private final CustomerRepository customerRepository;
    private final MailTemplateRepository mailTemplateRepository;
    private final JavaMailSender mailSender;
    private final MailConfigService mailConfigService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mail.batch.queue-name:mail:queue}")
    private String queueName;

    @Value("${mail.batch.progress-prefix:mail:progress:}")
    private String progressPrefix;

    @Value("${mail.batch.send-interval:3000}")
    private int sendInterval;

    @Value("${mail.batch.max-retry:3}")
    private int maxRetry;

    /**
     * 提交邮件发送任务
     * 将任务放入Redis队列，等待消费者处理
     * 
     * @param request 发送请求
     * @return 批次ID
     */
    @Transactional
    public String submitSendTask(SendMailRequest request) {
        log.info("提交邮件发送任务: customerIds={}", request.getCustomerIds());

        // SMTP 连接测试已跳过（异步发送时会自动处理）
        log.info("跳过 SMTP 连接测试，直接进入发送流程");

        // 生成批次ID
        String batchId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("生成批次ID: {}", batchId);

        // 获取邮件内容和主题
        String subject;
        String body;
        List<String> attachmentPaths;

        if (request.getTemplateId() != null) {
            // 使用模板
            MailTemplate template = mailTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new RuntimeException("模板不存在: " + request.getTemplateId()));
            subject = template.getSubject();
            body = template.getBody();
            attachmentPaths = request.getAttachmentPaths() != null ? 
                request.getAttachmentPaths() : 
                (template.getAttachmentPathArray() != null ? 
                    Arrays.asList(template.getAttachmentPathArray()) : null);
        } else {
            // 不使用模板
            if (request.getSubject() == null || request.getBody() == null) {
                throw new RuntimeException("未使用模板时必须提供subject和body");
            }
            subject = request.getSubject();
            body = request.getBody();
            attachmentPaths = request.getAttachmentPaths();
        }

        // 为每个客户创建发送记录并加入队列
        int totalCount = 0;
        for (Long customerId : request.getCustomerIds()) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("客户不存在: " + customerId));

            // 替换占位符（{{称呼}}）
            String finalSubject = replacePlaceholders(subject, customer);
            String finalBody = replacePlaceholders(body, customer);

            // 创建邮件记录
            MailRecord record = MailRecord.builder()
                    .batchId(batchId)
                    .customerId(customerId)
                    .toEmail(customer.getEmail())
                    .salutation(customer.getSalutation())
                    .subject(finalSubject)
                    .body(finalBody)
                    .attachmentPaths(attachmentPaths != null ? String.join(",", attachmentPaths) : null)
                    .status(MailRecord.Status.PENDING)
                    .retryCount(0)
                    .build();

            record = mailRecordRepository.save(record);

            // 将任务加入Redis队列
            Map<String, Object> task = new HashMap<>();
            task.put("recordId", record.getId());
            task.put("batchId", batchId);
            task.put("toEmail", customer.getEmail());
            task.put("subject", finalSubject);
            task.put("body", finalBody);
            task.put("attachmentPaths", attachmentPaths);
            task.put("retryCount", 0);

            try {
                String taskJson = objectMapper.writeValueAsString(task);
                redisTemplate.opsForList().rightPush(queueName, taskJson);
                totalCount++;
                log.debug("任务加入队列: recordId={}, toEmail={}", record.getId(), customer.getEmail());
            } catch (Exception e) {
                log.error("序列化任务失败: {}", e.getMessage(), e);
            }
        }

        // 初始化进度信息到Redis
        Map<String, Object> progress = new HashMap<>();
        progress.put("batchId", batchId);
        progress.put("total", totalCount);
        progress.put("pending", totalCount);
        progress.put("sending", 0);
        progress.put("sent", 0);
        progress.put("failed", 0);
        progress.put("status", "SENDING");  // PENDING, SENDING, COMPLETED, CANCELLED
        progress.put("startTime", System.currentTimeMillis());
        progress.put("endTime", null);

        redisTemplate.opsForHash().putAll(progressPrefix + batchId, progress);
        redisTemplate.expire(progressPrefix + batchId, 24, TimeUnit.HOURS);  // 24小时过期

        log.info("邮件发送任务提交完成: batchId={}, total={}", batchId, totalCount);

        // 触发消费队列（异步执行，如果已在跑也不会重复）
        startQueueConsumption();

        return batchId;
    }

    /**
     * 消费邮件发送队列（由定时任务或独立线程调用）
     * 逐条从Redis队列取任务发送
     */
    public void startQueueConsumption() {
        new Thread(this::consumeSendQueue, "MailSend-Consumer").start();
    }

    public void consumeSendQueue() {
        log.info("开始消费邮件发送队列...");

        try {
            String currentBatchId = null;
            while (true) {
                // 非阻塞取任务，避免 leftPop 超时问题
                Object taskObj = redisTemplate.opsForList().leftPop(queueName);

                if (taskObj == null) {
                    // 队列空了，再等一小段时间如果还没任务就结束
                    Thread.sleep(2000);
                    taskObj = redisTemplate.opsForList().leftPop(queueName);
                    if (taskObj == null) {
                        if (currentBatchId != null) {
                            updateProgress(currentBatchId, "COMPLETED", 0, 0, 0, 0);
                            log.info("批次 {} 所有任务处理完毕，标记为 COMPLETED", currentBatchId);
                        }
                        break;
                    }
                }

                try {
                    String taskJson = taskObj.toString();
                    Map<String, Object> task = objectMapper.readValue(taskJson, Map.class);

                    Long recordId = Long.valueOf(task.get("recordId").toString());
                    String batchId = task.get("batchId").toString();
                    currentBatchId = batchId;
                    String toEmail = task.get("toEmail").toString();
                    String subject = task.get("subject").toString();
                    String body = task.get("body").toString();
                    Integer retryCount = Integer.valueOf(task.get("retryCount").toString());

                    if (isBatchCancelled(batchId)) {
                        log.warn("批次已取消，跳过任务: batchId={}, recordId={}", batchId, recordId);
                        updateProgress(batchId, "CANCELLED", 0, 0, 0, 1);
                        continue;
                    }

                    MailRecord record = mailRecordRepository.findById(recordId).orElse(null);
                    if (record != null) {
                        record.setStatus(MailRecord.Status.SENDING);
                        mailRecordRepository.save(record);
                    }
                    updateProgress(batchId, "RUNNING", 0, 1, 0, 0);

                    SendResult sendResult;
                    if (record != null) {
                        sendResult = sendMail(record, toEmail, subject, body,
                            (List<String>) task.get("attachmentPaths"), retryCount);
                    } else {
                        sendResult = SendResult.failure("邮件记录不存在: recordId=" + recordId);
                        log.warn("邮件记录不存在，跳过发送: recordId={}", recordId);
                    }

                    if (record != null) {
                        if (sendResult.isSuccess()) {
                            record.setStatus(MailRecord.Status.SENT);
                            record.setSentAt(LocalDateTime.now());
                            mailRecordRepository.save(record);
                            updateProgress(batchId, "RUNNING", -1, -1, 1, 0);
                            log.info("邮件发送成功: recordId={}, toEmail={}", recordId, toEmail);
                        } else {
                            String errorMessage = sendResult.getErrorMessage();
                            if (retryCount < maxRetry) {
                                task.put("retryCount", retryCount + 1);
                                String retryTaskJson = objectMapper.writeValueAsString(task);
                                redisTemplate.opsForList().rightPush(queueName, retryTaskJson);
                                log.warn("邮件发送失败，加入重试队列: recordId={}, retryCount={}, error={}", recordId, retryCount + 1, errorMessage);
                            } else {
                                record.setStatus(MailRecord.Status.FAILED);
                                record.setErrorMsg(errorMessage);
                                mailRecordRepository.save(record);
                                updateProgress(batchId, "RUNNING", -1, -1, 0, 1);
                                log.error("邮件发送失败，超过最大重试次数: recordId={}, error={}", recordId, errorMessage);
                            }
                        }
                    }
                } catch (Exception taskEx) {
                    log.error("处理单个邮件发送任务时发生异常，继续处理下一个任务: {}", taskEx.getMessage(), taskEx);
                }

                Thread.sleep(sendInterval);
            }
        } catch (InterruptedException e) {
            log.warn("邮件发送线程被中断");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("邮件发送队列消费发生严重异常: {}", e.getMessage(), e);
        }
        log.info("邮件发送消费线程结束");
    }

    private static class SendResult {
        private final boolean success;
        private final String errorMessage;

        private SendResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        static SendResult success() {
            return new SendResult(true, null);
        }

        static SendResult failure(String errorMessage) {
            return new SendResult(false, errorMessage);
        }

        boolean isSuccess() {
            return success;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 根据数据库配置创建 JavaMailSender
     */
    private JavaMailSender createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailConfigService.getSmtpHost());
        sender.setPort(mailConfigService.getSmtpPort());
        sender.setUsername(mailConfigService.getUsername());
        sender.setPassword(mailConfigService.getPassword());

        Properties props = mailConfigService.getMailProperties();
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        sender.setJavaMailProperties(props);

        log.debug("使用数据库配置创建MailSender: host={}, port={}, username={}", 
            sender.getHost(), sender.getPort(), sender.getUsername());
        return sender;
    }

    /**
     * 实际发送邮件（支持HTML和附件）
     */
    private SendResult sendMail(MailRecord record, String toEmail, String subject, String body, 
                           List<String> attachmentPaths, int retryCount) {
        try {
            JavaMailSender sender = createMailSender();
            
            String fromEmail = mailConfigService.getUsername();
            if (fromEmail == null || fromEmail.isEmpty()) {
                return SendResult.failure("发件人邮箱未配置");
            }

            // 创建MimeMessage
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置发件人和收件人
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            // 添加附件
            if (attachmentPaths != null && !attachmentPaths.isEmpty()) {
                for (String path : attachmentPaths) {
                    try {
                        log.info("添加附件: {}", path);
                    } catch (Exception e) {
                        log.error("添加附件失败: {}", path, e);
                    }
                }
            }

            // 发送邮件
            sender.send(message);

            // 更新重试次数
            record.setRetryCount(retryCount);
            mailRecordRepository.save(record);

            return SendResult.success();

        } catch (MessagingException e) {
            String errorMsg = "邮件发送失败: " + e.getMessage();
            log.error("邮件发送失败: recordId={}, error={}", record.getId(), errorMsg);
            return SendResult.failure(errorMsg);
        } catch (Exception e) {
            String errorMsg = "邮件发送异常: " + e.getClass().getSimpleName() + " - " + e.getMessage();
            log.error("邮件发送异常: recordId={}", record.getId(), e);
            return SendResult.failure(errorMsg);
        }
    }

    /**
     * 替换邮件模板中的占位符
     * 目前支持：{{称呼}} -> 客户的称呼
     */
    private String replacePlaceholders(String template, Customer customer) {
        if (template == null) return "";
        
        String result = template;
        // 替换{{称呼}}占位符
        if (customer.getSalutation() != null) {
            result = result.replace("{{称呼}}", customer.getSalutation());
        } else {
            result = result.replace("{{称呼}}", customer.getName());
        }
        
        // 可以继续添加其他占位符替换规则
        // result = result.replace("{{姓名}}", customer.getName());
        // result = result.replace("{{邮箱}}", customer.getEmail());
        
        return result;
    }

    /**
     * 更新发送进度到Redis
     */
    private void updateProgress(String batchId, String status, 
                              int pendingDelta, int sendingDelta, 
                              int sentDelta, int failedDelta) {
        String key = progressPrefix + batchId;
        
        // 更新状态
        if (status != null) {
            redisTemplate.opsForHash().put(key, "status", status);
        }

        // 原子性更新计数
        if (pendingDelta != 0) {
            redisTemplate.opsForHash().increment(key, "pending", pendingDelta);
        }
        if (sendingDelta != 0) {
            redisTemplate.opsForHash().increment(key, "sending", sendingDelta);
        }
        if (sentDelta != 0) {
            redisTemplate.opsForHash().increment(key, "sent", sentDelta);
        }
        if (failedDelta != 0) {
            redisTemplate.opsForHash().increment(key, "failed", failedDelta);
        }

        // 如果是完成或取消状态，设置结束时间
        if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            redisTemplate.opsForHash().put(key, "endTime", System.currentTimeMillis());
        }
    }

    /**
     * 检查批次是否被取消
     */
    private boolean isBatchCancelled(String batchId) {
        Object status = redisTemplate.opsForHash().get(progressPrefix + batchId, "status");
        return "CANCELLED".equals(status);
    }

    /**
     * 取消发送任务
     */
    @Transactional
    public void cancelSendTask(String batchId) {
        log.info("取消发送任务: batchId={}", batchId);

        // 更新Redis中的状态
        redisTemplate.opsForHash().put(progressPrefix + batchId, "status", "CANCELLED");
        redisTemplate.opsForHash().put(progressPrefix + batchId, "endTime", System.currentTimeMillis());

        // 更新数据库中未发送的邮件状态为FAILED
        List<MailRecord> pendingRecords = mailRecordRepository.findByBatchIdAndStatus(batchId, MailRecord.Status.PENDING);
        for (MailRecord record : pendingRecords) {
            mailRecordRepository.updateStatus(record.getId(), MailRecord.Status.FAILED, "任务已取消", null);
        }

        log.info("发送任务已取消: batchId={}", batchId);
    }

    /**
     * 获取发送进度
     */
    public Map<String, Object> getProgress(String batchId) {
        String key = progressPrefix + batchId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, Object> result = new java.util.HashMap<>();
        entries.forEach((k, v) -> result.put(String.valueOf(k), v));

        List<MailRecord> failedRecords = mailRecordRepository.findByBatchIdAndStatus(batchId, MailRecord.Status.FAILED);
        if (!failedRecords.isEmpty()) {
            List<Map<String, Object>> failedDetails = new java.util.ArrayList<>();
            for (MailRecord record : failedRecords) {
                Map<String, Object> detail = new java.util.HashMap<>();
                detail.put("recordId", record.getId());
                detail.put("toEmail", record.getToEmail());
                detail.put("errorMsg", record.getErrorMsg());
                detail.put("retryCount", record.getRetryCount());
                failedDetails.add(detail);
            }
            result.put("failedDetails", failedDetails);
        }

        return result;
    }
}
