package com.mailbatch.mailbatchsystem.repository;

import com.mailbatch.mailbatchsystem.entity.MailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件发送记录数据访问接口
 * 支持复杂查询和分页
 */
@Repository
public interface MailRecordRepository extends JpaRepository<MailRecord, Long>, JpaSpecificationExecutor<MailRecord> {

    /**
     * 根据批次ID查询记录
     * @param batchId 批次ID
     * @return 邮件记录列表
     */
    List<MailRecord> findByBatchId(String batchId);

    /**
     * 根据客户ID查询记录
     * @param customerId 客户ID
     * @return 邮件记录列表
     */
    List<MailRecord> findByCustomerId(Long customerId);

    /**
     * 根据状态查询记录
     * @param status 发送状态
     * @return 邮件记录列表
     */
    List<MailRecord> findByStatus(MailRecord.Status status);

    /**
     * 根据批次ID和状态查询记录
     * @param batchId 批次ID
     * @param status 发送状态
     * @return 邮件记录列表
     */
    List<MailRecord> findByBatchIdAndStatus(String batchId, MailRecord.Status status);

    /**
     * 统计批次中各种状态的邮件数量
     * @param batchId 批次ID
     * @return 统计结果
     */
    @Query("SELECT mr.status, COUNT(mr) FROM MailRecord mr WHERE mr.batchId = :batchId GROUP BY mr.status")
    List<Object[]> countByBatchIdAndStatus(@Param("batchId") String batchId);

    /**
     * 更新邮件记录状态
     * @param id 记录ID
     * @param status 新状态
     * @param errorMsg 错误信息
     */
    @Modifying
    @Query("UPDATE MailRecord mr SET mr.status = :status, mr.errorMsg = :errorMsg, mr.sentAt = :sentAt WHERE mr.id = :id")
    void updateStatus(@Param("id") Long id, 
                     @Param("status") MailRecord.Status status, 
                     @Param("errorMsg") String errorMsg,
                     @Param("sentAt") LocalDateTime sentAt);

    /**
     * 更新重试次数
     * @param id 记录ID
     * @param retryCount 重试次数
     */
    @Modifying
    @Query("UPDATE MailRecord mr SET mr.retryCount = :retryCount WHERE mr.id = :id")
    void updateRetryCount(@Param("id") Long id, @Param("retryCount") Integer retryCount);

    /**
     * 删除批次的所有记录
     * @param batchId 批次ID
     */
    @Modifying
    @Query("DELETE FROM MailRecord mr WHERE mr.batchId = :batchId")
    void deleteByBatchId(@Param("batchId") String batchId);
}
