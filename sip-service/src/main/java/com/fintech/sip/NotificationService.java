package com.fintech.sip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private EmailService emailService;

    @Value("${fcm.server.key:}")
    private String fcmServerKey;

    @Value("${fcm.enabled:true}")
    private boolean fcmEnabled;

    /**
     * Send SIP push notification via FCM
     * @param sip the SIP to send notification for
     */
    public void sendSipPushNotification(Sip sip) {
        if (!fcmEnabled) {
            logger.debug("FCM notifications disabled, skipping push notification for SIP ID: {}", sip.getId());
            return;
        }

        if (fcmServerKey == null || fcmServerKey.trim().isEmpty()) {
            logger.warn("FCM server key not configured, skipping push notification for SIP ID: {}", sip.getId());
            return;
        }

        try {
            // TODO: Implement actual FCM integration
            // This is a placeholder implementation
            FcmMessage fcmMessage = createSipFcmMessage(sip);
            
            // Simulate FCM API call
            boolean success = sendFcmMessage(fcmMessage);
            
            if (success) {
                logger.info("FCM push notification sent successfully for SIP ID: {}", sip.getId());
            } else {
                logger.error("Failed to send FCM push notification for SIP ID: {}", sip.getId());
            }
            
        } catch (Exception e) {
            logger.error("Error sending FCM push notification for SIP ID {}: {}", sip.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }

    /**
     * Send SIP email notification
     * @param sip the SIP to send notification for
     */
    public void sendSipEmailNotification(Sip sip) {
        try {
            String subject = "SIP Investment Reminder - " + sip.getSymbol();
            String emailBody = createSipEmailBody(sip);
            
            // TODO: Get user email from user service
            String userEmail = "user@example.com"; // Placeholder
            
            emailService.sendEmail(userEmail, subject, emailBody);
            
            logger.info("Email notification sent successfully for SIP ID: {}", sip.getId());
            
        } catch (Exception e) {
            logger.error("Error sending email notification for SIP ID {}: {}", sip.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    /**
     * Create FCM message for SIP
     * @param sip the SIP
     * @return FCM message
     */
    private FcmMessage createSipFcmMessage(Sip sip) {
        Map<String, String> data = new HashMap<>();
        data.put("sipId", sip.getId().toString());
        data.put("symbol", sip.getSymbol());
        data.put("amount", sip.getAmount().toString());
        data.put("frequency", sip.getFrequency().toString());
        data.put("type", "SIP_REMINDER");

        return new FcmMessage(
            sip.getUserId().toString(), // user ID as target
            "SIP Investment Reminder",
            String.format("Your %s SIP of ₹%.2f is due today", sip.getSymbol(), sip.getAmount()),
            data
        );
    }

    /**
     * Create email body for SIP notification
     * @param sip the SIP
     * @return email body
     */
    private String createSipEmailBody(Sip sip) {
        return String.format("""
            Dear Investor,
            
            This is a reminder that your Systematic Investment Plan (SIP) is due today.
            
            SIP Details:
            - Symbol: %s
            - Amount: ₹%.2f
            - Frequency: %s
            - Next Due Date: %s
            
            Please ensure you have sufficient funds in your account for the SIP investment.
            
            Note: This is an automated reminder. Please do not reply to this email.
            
            Best regards,
            Fintech Investment Team
            """, 
            sip.getSymbol(), 
            sip.getAmount(), 
            sip.getFrequency(), 
            sip.getNextDueDate()
        );
    }

    /**
     * Send FCM message (placeholder implementation)
     * @param message the FCM message
     * @return true if successful, false otherwise
     */
    private boolean sendFcmMessage(FcmMessage message) {
        // TODO: Implement actual FCM API integration
        // This would typically involve:
        // 1. Creating HTTP request to FCM API
        // 2. Setting Authorization header with server key
        // 3. Sending JSON payload with message data
        // 4. Handling response and errors
        
        logger.debug("Simulating FCM message send: {}", message);
        
        // Simulate success for now
        return true;
    }

    /**
     * Send general notification
     * @param userId the user ID
     * @param title notification title
     * @param body notification body
     * @param data additional data
     */
    public void sendGeneralNotification(Long userId, String title, String body, Map<String, String> data) {
        try {
            FcmMessage message = new FcmMessage(userId.toString(), title, body, data);
            boolean success = sendFcmMessage(message);
            
            if (success) {
                logger.info("General notification sent successfully to user: {}", userId);
            } else {
                logger.error("Failed to send general notification to user: {}", userId);
            }
            
        } catch (Exception e) {
            logger.error("Error sending general notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Send email notification
     * @param email recipient email
     * @param subject email subject
     * @param body email body
     */
    public void sendEmailNotification(String email, String subject, String body) {
        try {
            emailService.sendEmail(email, subject, body);
            logger.info("Email notification sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Error sending email notification to {}: {}", email, e.getMessage(), e);
        }
    }

    /**
     * FCM message model
     */
    public static class FcmMessage {
        private String target;
        private String title;
        private String body;
        private Map<String, String> data;

        public FcmMessage() {}

        public FcmMessage(String target, String title, String body, Map<String, String> data) {
            this.target = target;
            this.title = title;
            this.body = body;
            this.data = data;
        }

        // Getters and setters
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        public Map<String, String> getData() { return data; }
        public void setData(Map<String, String> data) { this.data = data; }

        @Override
        public String toString() {
            return String.format("FcmMessage{target='%s', title='%s', body='%s', data=%s}", 
                               target, title, body, data);
        }
    }
}
