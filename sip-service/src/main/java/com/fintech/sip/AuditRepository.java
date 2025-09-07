package com.fintech.sip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditRecord, Long> {

    /**
     * Find audit records by SIP ID
     * @param sipId the SIP ID
     * @return list of audit records
     */
    List<AuditRecord> findBySipId(Long sipId);

    /**
     * Find audit records by user ID
     * @param userId the user ID
     * @return list of audit records
     */
    List<AuditRecord> findByUserId(Long userId);

    /**
     * Find audit records by audit type
     * @param auditType the audit type
     * @return list of audit records
     */
    List<AuditRecord> findByAuditType(AuditType auditType);

    /**
     * Find audit records by status
     * @param status the status
     * @return list of audit records
     */
    List<AuditRecord> findByStatus(String status);

    /**
     * Find audit records by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of audit records
     */
    @Query("SELECT a FROM AuditRecord a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<AuditRecord> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find error audit records
     * @return list of error audit records
     */
    @Query("SELECT a FROM AuditRecord a WHERE a.status = 'ERROR'")
    List<AuditRecord> findErrorRecords();

    /**
     * Count audit records by audit type
     * @param auditType the audit type
     * @return count of audit records
     */
    long countByAuditType(AuditType auditType);

    /**
     * Count audit records by status
     * @param status the status
     * @return count of audit records
     */
    long countByStatus(String status);
}
