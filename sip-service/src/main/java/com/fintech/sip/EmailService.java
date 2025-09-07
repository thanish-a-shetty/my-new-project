package com.fintech.sip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send email
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     */
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        
        // REVIEW: production hardening required - configure proper from address
        message.setFrom("noreply@fintech.com");
        
        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + to);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
