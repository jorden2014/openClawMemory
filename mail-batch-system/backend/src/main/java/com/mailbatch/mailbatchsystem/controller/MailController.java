package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.*;
import com.mailbatch.mailbatchsystem.service.MailSendService;
import com.mailbatch.mailbatchsystem.service.MailRecordService;
import com.mailbatch.mailbatchsystem.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * 邮件发送控制器
 * 处理邮件发送任务的提交、进度查询、取消等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailSendService mailSendService;
    private final MailRecordService mailRecordService;
    private final TemplateService templateService;

    /**
     * 提交邮件发送任务
     * POST /api/mail/send
     */
    @PostMapping("/send")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<String> sendMail(@RequestBody SendMailRequest request) {
        log.info("提交邮件发送任务: customerIds={}", request.getCustomerIds());

        try {
            String batchId = mailSendService.submitSendTask(request);
            return Result.success("发送任务已提交", batchId);
        } catch (Exception e) {
            log.error("提交发送任务失败: {}", e.getMessage(), e);
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取发送进度（SSE推送）
     * GET /api/mail/progress/{batchId}
     */
    @GetMapping(value = "/progress/{batchId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public SseEmitter getProgress(@PathVariable String batchId) {
        log.info("建立SSE连接，查询发送进度: batchId={}", batchId);

        SseEmitter emitter = new SseEmitter(120000L);  // 2分钟超时

        // 启动一个线程定期推送进度
        new Thread(() -> {
            try {
                while (true) {
                    Map<String, Object> progress = mailSendService.getProgress(batchId);

                    if (progress == null || progress.isEmpty()) {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Result.error("批次不存在")));
                        break;
                    }

                    emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(Result.success(progress)));

                    // 检查是否完成或取消
                    String status = (String) progress.get("status");
                    if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
                        emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(Result.success("发送任务" + ("COMPLETED".equals(status) ? "完成" : "已取消"))));
                        break;
                    }

                    Thread.sleep(2000);  // 每2秒推送一次
                }
            } catch (Exception e) {
                log.error("SSE推送异常: {}", e.getMessage());
            } finally {
                emitter.complete();
            }
        }).start();

        return emitter;
    }

    /**
     * 取消发送任务
     * POST /api/mail/cancel/{batchId}
     */
    @PostMapping("/cancel/{batchId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public Result<?> cancelSend(@PathVariable String batchId) {
        log.info("取消发送任务: batchId={}", batchId);

        try {
            mailSendService.cancelSendTask(batchId);
            return Result.success("发送任务已取消");
        } catch (Exception e) {
            log.error("取消发送任务失败: {}", e.getMessage(), e);
            return Result.error("取消失败: " + e.getMessage());
        }
    }

    /**
     * 获取邮件模板列表（用于发送时选择）
     * GET /api/mail/templates
     */
    @GetMapping("/templates")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<?> getTemplates() {
        return Result.success(templateService.getAllTemplates());
    }

    /**
     * 获取批次统计信息
     * GET /api/mail/batch/{batchId}/statistics
     */
    @GetMapping("/batch/{batchId}/statistics")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<?> getBatchStatistics(@PathVariable String batchId) {
        return Result.success(mailRecordService.getBatchStatistics(batchId));
    }
}
