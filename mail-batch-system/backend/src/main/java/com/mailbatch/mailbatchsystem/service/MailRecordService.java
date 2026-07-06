package com.mailbatch.mailbatchsystem.service;

import com.mailbatch.mailbatchsystem.dto.MailRecordResponse;
import com.mailbatch.mailbatchsystem.dto.PageResponse;
import com.mailbatch.mailbatchsystem.entity.MailRecord;
import com.mailbatch.mailbatchsystem.repository.MailRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件发送记录服务类
 * 处理邮件记录的查询、重发等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailRecordService {

    private final MailRecordRepository mailRecordRepository;

    /**
     * 分页查询邮件记录（支持多条件筛选）
     */
    public PageResponse<MailRecordResponse> listRecords(
            String batchId,
            Long customerId,
            String status,
            String keyword,
            Pageable pageable) {

        log.info("查询邮件记录: batchId={}, customerId={}, status={}", batchId, customerId, status);

        // 构建动态查询条件
        Specification<MailRecord> spec = Specification.where(null);

        // 按批次ID筛选
        if (batchId != null && !batchId.trim().isEmpty()) {
            String finalBatchId = batchId.trim();
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("batchId"), finalBatchId));
        }

        // 按客户ID筛选
        if (customerId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("customerId"), customerId));
        }

        // 按状态筛选
        if (status != null && !status.trim().isEmpty()) {
            String finalStatus = status.trim();
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), MailRecord.Status.valueOf(finalStatus)));
        }

        // 关键词搜索（邮箱、主题）
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                    criteriaBuilder.like(root.get("toEmail"), likeKeyword),
                    criteriaBuilder.like(root.get("subject"), likeKeyword)
                ));
        }

        // 执行查询
        Page<MailRecord> page = mailRecordRepository.findAll(spec, pageable);
        Page<MailRecordResponse> responsePage = page.map(MailRecordResponse::fromEntity);

        return PageResponse.fromPage(responsePage);
    }

    /**
     * 根据ID查询邮件记录
     */
    public MailRecordResponse getRecord(Long id) {
        MailRecord record = mailRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("邮件记录不存在: " + id));
        return MailRecordResponse.fromEntity(record);
    }

    /**
     * 重新发送失败的邮件
     */
    @Transactional
    public void resendMail(Long id) {
        log.info("重新发送邮件: id={}", id);

        MailRecord record = mailRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("邮件记录不存在: " + id));

        // 只有失败状态的邮件才能重发
        if (record.getStatus() != MailRecord.Status.FAILED && record.getStatus() != MailRecord.Status.SENT) {
            throw new RuntimeException("只有失败或未发送的邮件才能重发");
        }

        // 重置状态为待发送
        mailRecordRepository.updateStatus(id, MailRecord.Status.PENDING, null, null);
        mailRecordRepository.updateRetryCount(id, 0);

        log.info("邮件已重置为待发送状态: id={}", id);
    }

    /**
     * 批量重新发送失败的邮件
     */
    @Transactional
    public int batchResendMails(String batchId) {
        log.info("批量重发邮件: batchId={}", batchId);

        List<MailRecord> failedRecords = mailRecordRepository.findByBatchIdAndStatus(batchId, MailRecord.Status.FAILED);
        int count = 0;

        for (MailRecord record : failedRecords) {
            mailRecordRepository.updateStatus(record.getId(), MailRecord.Status.PENDING, null, null);
            mailRecordRepository.updateRetryCount(record.getId(), 0);
            count++;
        }

        log.info("批量重发完成: batchId={}, count={}", batchId, count);
        return count;
    }

    /**
     * 获取批次统计信息
     */
    public List<Object[]> getBatchStatistics(String batchId) {
        return mailRecordRepository.countByBatchIdAndStatus(batchId);
    }

    /**
     * 删除邮件记录
     */
    @Transactional
    public void deleteRecord(Long id) {
        log.info("删除邮件记录: id={}", id);

        if (!mailRecordRepository.existsById(id)) {
            throw new RuntimeException("邮件记录不存在: " + id);
        }

        mailRecordRepository.deleteById(id);
    }

    /**
     * 清空批次的所有记录
     */
    @Transactional
    public void clearBatch(String batchId) {
        log.info("清空批次记录: batchId={}", batchId);
        mailRecordRepository.deleteByBatchId(batchId);
    }
}
