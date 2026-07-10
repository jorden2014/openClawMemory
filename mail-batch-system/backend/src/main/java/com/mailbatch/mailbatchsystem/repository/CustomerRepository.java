package com.mailbatch.mailbatchsystem.repository;

import com.mailbatch.mailbatchsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户数据访问接口
 * 支持复杂查询和分页
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * 根据邮箱查询客户
     * @param email 邮箱地址
     * @return 客户实体
     */
    Customer findByEmail(String email);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据名称模糊搜索客户
     * @param name 客户名称
     * @return 客户列表
     */
    List<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * 根据邮箱模糊搜索客户
     * @param email 邮箱地址
     * @return 客户列表
     */
    List<Customer> findByEmailContainingIgnoreCase(String email);

    /**
     * 根据称呼模糊搜索客户
     * @param salutation 称呼
     * @return 客户列表
     */
    List<Customer> findBySalutationContainingIgnoreCase(String salutation);

    /**
     * 根据标签筛选客户（tags字段包含指定标签）
     * @param tag 标签
     * @return 客户列表
     */
    @Query("SELECT c FROM Customer c WHERE c.tags LIKE %:tag%")
    List<Customer> findByTag(@Param("tag") String tag);

    /**
     * 批量删除客户
     * @param ids 客户ID列表
     */
    @Modifying
    @Query("DELETE FROM Customer c WHERE c.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.tags FROM Customer c WHERE c.tags IS NOT NULL AND c.tags <> ''")
    List<String> findAllTags();
}
