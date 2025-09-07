package com.fintech.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send email verification link to user
     * @param toEmail recipient email address
     * @param verificationUrl the verification URL to include in the email
     */
    public void sendVerificationEmail(String toEmail, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify Your Account - Fintech Auth Service");
        
        String emailBody = String.format("""
            Hello,
            
            Thank you for signing up! Please click the link below to verify your account:
            
            %s
            
            This link will expire in 24 hours.
            
            If you did not create an account, please ignore this email.
            
            Best regards,
            Fintech Auth Service Team
            """, verificationUrl);
        
        message.setText(emailBody);
        
        // REVIEW: production hardening required - configure proper from address
        // Set from address from environment variable or configuration
        message.setFrom("noreply@fintech.com"); // This should come from config
        
        try {
            mailSender.send(message);
            System.out.println("Verification email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send verification email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset email
     * @param toEmail recipient email address
     * @param resetUrl the password reset URL to include in the email
     */
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset - Fintech Auth Service");
        
        String emailBody = String.format("""
            Hello,
            
            You requested a password reset. Please click the link below to reset your password:
            
            %s
            
            This link will expire in 1 hour.
            
            If you did not request this password reset, please ignore this email.
            
            Best regards,
            Fintech Auth Service Team
            """, resetUrl);
        
        message.setText(emailBody);
        message.setFrom("noreply@fintech.com"); // REVIEW: production hardening required - configure proper from address
        
        try {
            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send welcome email after successful verification
     * @param toEmail recipient email address
     * @param firstName user's first name
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to Fintech Auth Service!");
        
        String emailBody = String.format("""
            Hello %s,
            
            Welcome to Fintech Auth Service! Your account has been successfully verified.
            
            You can now log in and start using our services.
            
            If you have any questions, please don't hesitate to contact our support team.
            
            Best regards,
            Fintech Auth Service Team
            """, firstName);
        
        message.setText(emailBody);
        message.setFrom("noreply@fintech.com"); // REVIEW: production hardening required - configure proper from address
        
        try {
            mailSender.send(message);
            System.out.println("Welcome email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            // Don't throw exception for welcome email as it's not critical
        }
    }
}
