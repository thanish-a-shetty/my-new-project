package com.fintech.sip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    /**
     * Create SIP audit record
     * @param sip the SIP being processed
     * @return created audit record
     */
    public AuditRecord createSipAuditRecord(Sip sip) {
        AuditRecord auditRecord = new AuditRecord(
            sip.getId(),
            sip.getUserId(),
            AuditType.SIP_PROCESSING,
            "PROCESS_SIP",
            String.format("SIP processing initiated for %s - Amount: ₹%.2f", sip.getSymbol(), sip.getAmount()),
            "IN_PROGRESS"
        );
        
        return auditRepository.save(auditRecord);
    }

    /**
     * Log SIP processing error
     * @param sipId the SIP ID
     * @param errorMessage the error message
     */
    public void logSipProcessingError(Long sipId, String errorMessage) {
        AuditRecord auditRecord = new AuditRecord(
            sipId,
            null, // userId will be set if available
            AuditType.ERROR_OCCURRED,
            "SIP_PROCESSING_ERROR",
            "Error occurred during SIP processing",
            "ERROR"
        );
        auditRecord.setErrorMessage(errorMessage);
        
        auditRepository.save(auditRecord);
    }

    /**
     * Log SIP job completion
     * @param processedCount number of SIPs processed
     * @param errorCount number of errors
     * @param totalCount total SIPs found
     */
    public void logSipJobCompletion(int processedCount, int errorCount, int totalCount) {
        AuditRecord auditRecord = new AuditRecord(
            null,
            null,
            AuditType.JOB_COMPLETION,
            "SIP_JOB_COMPLETION",
            String.format("SIP processing job completed. Processed: %d, Errors: %d, Total: %d", 
                         processedCount, errorCount, totalCount),
            "COMPLETED"
        );
        
        auditRepository.save(auditRecord);
    }

    /**
     * Log SIP job error
     * @param errorMessage the error message
     */
    public void logSipJobError(String errorMessage) {
        AuditRecord auditRecord = new AuditRecord(
            null,
            null,
            AuditType.ERROR_OCCURRED,
            "SIP_JOB_ERROR",
            "Critical error in SIP processing job",
            "ERROR"
        );
        auditRecord.setErrorMessage(errorMessage);
        
        auditRepository.save(auditRecord);
    }

    /**
     * Log notification sent
     * @param sipId the SIP ID
     * @param userId the user ID
     * @param notificationType type of notification (FCM, EMAIL)
     * @param status notification status
     */
    public void logNotificationSent(Long sipId, Long userId, String notificationType, String status) {
        AuditRecord auditRecord = new AuditRecord(
            sipId,
            userId,
            AuditType.NOTIFICATION_SENT,
            "NOTIFICATION_SENT",
            String.format("%s notification sent for SIP", notificationType),
            status
        );
        
        auditRepository.save(auditRecord);
    }
}
