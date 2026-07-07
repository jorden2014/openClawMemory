package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.*;
import com.mailbatch.mailbatchsystem.service.MailRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class MailRecordController {

    private final MailRecordService mailRecordService;

    /**
     * 仪表盘统计
     * GET /api/records/dashboard
     */
    @GetMapping("/dashboard")
    public Result<?> getDashboardStats() {
        log.info("获取仪表盘统计");
        try {
            // TODO: 实现真实的统计查询
            // 暂时返回默认值
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalEmails", 0);
            stats.put("successCount", 0);
            stats.put("failedCount", 0);
            stats.put("pendingCount", 0);
            stats.put("todayCount", 0);
            return Result.success(stats);
        } catch (Exception e) {
            log.warn("获取仪表盘统计失败: {}", e.getMessage());
            return Result.success(new java.util.HashMap<>());
        }
    }

    /**
     * 分页查询邮件记录
     * GET /api/records?batchId=xxx&customerId=xxx&status=PENDING&keyword=xxx&page=0&size=10
     */
    @GetMapping
    public Result<PageResponse<MailRecordResponse>> listRecords(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("分页查询邮件记录: batchId={}, status={}", batchId, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<MailRecordResponse> response = mailRecordService.listRecords(
                batchId, customerId, status, keyword, pageable);

        return Result.success(response);
    }

    /**
     * 查询邮件记录详情
     * GET /api/records/{id}
     * 注意：此接口必须放在 /dashboard 等具体路径之后
     */
    @GetMapping("/{id}")
    public Result<MailRecordResponse> getRecord(@PathVariable Long id) {
        log.info("查询邮件记录详情: id={}", id);
        MailRecordResponse response = mailRecordService.getRecord(id);
        return Result.success(response);
    }

    /**
     * 重新发送邮件
     * POST /api/records/{id}/resend
     */
    @PostMapping("/{id}/resend")
    public Result<?> resendMail(@PathVariable Long id) {
        log.info("重新发送邮件: id={}", id);

        try {
            mailRecordService.resendMail(id);
            return Result.success("邮件已重新加入发送队列");
        } catch (Exception e) {
            log.error("重发失败: {}", e.getMessage(), e);
            return Result.error("重发失败: " + e.getMessage());
        }
    }

    /**
     * 批量重发批次中的失败邮件
     * POST /api/records/batch/{batchId}/resend-failed
     */
    @PostMapping("/batch/{batchId}/resend-failed")
    public Result<?> batchResendFailed(@PathVariable String batchId) {
        log.info("批量重发失败邮件: batchId={}", batchId);

        try {
            int count = mailRecordService.batchResendMails(batchId);
            return Result.success("成功重发 " + count + " 封邮件");
        } catch (Exception e) {
            log.error("批量重发失败: {}", e.getMessage(), e);
            return Result.error("批量重发失败: " + e.getMessage());
        }
    }

    /**
     * 删除邮件记录
     * DELETE /api/records/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteRecord(@PathVariable Long id) {
        log.info("删除邮件记录: id={}", id);

        try {
            mailRecordService.deleteRecord(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除失败: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 清空批次的所有记录
     * DELETE /api/records/batch/{batchId}
     */
    @DeleteMapping("/batch/{batchId}")
    public Result<?> clearBatch(@PathVariable String batchId) {
        log.info("清空批次记录: batchId={}", batchId);

        try {
            mailRecordService.clearBatch(batchId);
            return Result.success("批次记录已清空");
        } catch (Exception e) {
            log.error("清空失败: {}", e.getMessage(), e);
            return Result.error("清空失败: " + e.getMessage());
        }
    }
}
