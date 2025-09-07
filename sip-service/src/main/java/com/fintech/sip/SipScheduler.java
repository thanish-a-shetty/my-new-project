package com.fintech.sip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SipScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SipScheduler.class);

    @Autowired
    private SipRepository sipRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    /**
     * Process due SIPs - runs daily at 9:00 AM
     * 
     * Cron expression breakdown:
     * - "0 0 9 * * ?" = Every day at 9:00 AM
     * 
     * To change the schedule:
     * - "0 0 9 * * MON-FRI" = Weekdays at 9:00 AM
     * - "0 0 */6 * * ?" = Every 6 hours
     * - "0 30 8,12,16 * * ?" = At 8:30, 12:30, and 4:30 PM daily
     * - "0 0 0 1 * ?" = First day of every month at midnight
     * 
     * Cron format: second minute hour day month day-of-week
     * - second: 0-59
     * - minute: 0-59  
     * - hour: 0-23
     * - day: 1-31
     * - month: 1-12 or JAN-DEC
     * - day-of-week: 0-7 or SUN-SAT (0 and 7 both represent Sunday)
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void processDueSips() {
        logger.info("Starting SIP processing job at {}", LocalDateTime.now());
        
        try {
            // Find all due SIPs
            List<Sip> dueSips = sipRepository.findDueSips();
            logger.info("Found {} due SIPs to process", dueSips.size());
            
            if (dueSips.isEmpty()) {
                logger.info("No due SIPs found, skipping processing");
                return;
            }
            
            int processedCount = 0;
            int errorCount = 0;
            
            for (Sip sip : dueSips) {
                try {
                    processSip(sip);
                    processedCount++;
                    logger.debug("Successfully processed SIP ID: {}", sip.getId());
                } catch (Exception e) {
                    errorCount++;
                    logger.error("Error processing SIP ID {}: {}", sip.getId(), e.getMessage(), e);
                    
                    // Log error in audit
                    auditService.logSipProcessingError(sip.getId(), e.getMessage());
                }
            }
            
            logger.info("SIP processing completed. Processed: {}, Errors: {}", processedCount, errorCount);
            
            // Log overall job completion
            auditService.logSipJobCompletion(processedCount, errorCount, dueSips.size());
            
        } catch (Exception e) {
            logger.error("Critical error in SIP processing job: {}", e.getMessage(), e);
            auditService.logSipJobError(e.getMessage());
        }
    }

    /**
     * Process individual SIP
     * @param sip the SIP to process
     */
    private void processSip(Sip sip) {
        logger.debug("Processing SIP ID: {}, User: {}, Amount: {}", 
                    sip.getId(), sip.getUserId(), sip.getAmount());
        
        // Create audit record
        AuditRecord auditRecord = auditService.createSipAuditRecord(sip);
        
        // Send notifications (do not execute trades)
        sendSipNotifications(sip);
        
        // Update SIP status
        sip.setLastProcessedAt(LocalDateTime.now());
        sip.setNextDueDate(calculateNextDueDate(sip));
        sipRepository.save(sip);
        
        logger.info("SIP ID {} processed successfully. Next due: {}", 
                   sip.getId(), sip.getNextDueDate());
    }

    /**
     * Send notifications for SIP
     * @param sip the SIP to send notifications for
     */
    private void sendSipNotifications(Sip sip) {
        try {
            // Send push notification via FCM
            notificationService.sendSipPushNotification(sip);
            
            // Send email notification
            notificationService.sendSipEmailNotification(sip);
            
            logger.debug("Notifications sent for SIP ID: {}", sip.getId());
            
        } catch (Exception e) {
            logger.error("Error sending notifications for SIP ID {}: {}", sip.getId(), e.getMessage());
            throw e; // Re-throw to be caught by processSip
        }
    }

    /**
     * Calculate next due date for SIP
     * @param sip the SIP
     * @return next due date
     */
    private LocalDateTime calculateNextDueDate(Sip sip) {
        LocalDateTime currentDue = sip.getNextDueDate();
        
        switch (sip.getFrequency()) {
            case DAILY:
                return currentDue.plusDays(1);
            case WEEKLY:
                return currentDue.plusWeeks(1);
            case MONTHLY:
                return currentDue.plusMonths(1);
            case QUARTERLY:
                return currentDue.plusMonths(3);
            case YEARLY:
                return currentDue.plusYears(1);
            default:
                logger.warn("Unknown SIP frequency: {}, defaulting to monthly", sip.getFrequency());
                return currentDue.plusMonths(1);
        }
    }

    /**
     * Manual trigger for SIP processing (for testing/admin purposes)
     */
    public void triggerSipProcessing() {
        logger.info("Manual SIP processing triggered");
        processDueSips();
    }

    /**
     * Get SIP processing statistics
     * @return processing statistics
     */
    public SipProcessingStats getProcessingStats() {
        List<Sip> dueSips = sipRepository.findDueSips();
        List<Sip> activeSips = sipRepository.findActiveSips();
        
        return new SipProcessingStats(
            dueSips.size(),
            activeSips.size(),
            LocalDateTime.now()
        );
    }

    /**
     * SIP processing statistics model
     */
    public static class SipProcessingStats {
        private int dueSipsCount;
        private int activeSipsCount;
        private LocalDateTime lastChecked;

        public SipProcessingStats(int dueSipsCount, int activeSipsCount, LocalDateTime lastChecked) {
            this.dueSipsCount = dueSipsCount;
            this.activeSipsCount = activeSipsCount;
            this.lastChecked = lastChecked;
        }

        // Getters
        public int getDueSipsCount() { return dueSipsCount; }
        public int getActiveSipsCount() { return activeSipsCount; }
        public LocalDateTime getLastChecked() { return lastChecked; }
    }
}
