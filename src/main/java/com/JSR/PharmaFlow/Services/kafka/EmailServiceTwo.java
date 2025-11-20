package com.JSR.PharmaFlow.Services.kafka;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceTwo {


    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    // ACTUAL EMAIL SENDING HAPPENS HERE
    // Creates and sends "Welcome to PharmFlow!" email
    public void sendWelcomeEmail(String toEmail , String username) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Welcome to pharmFlow!");

        String emailContent = buildWelcomeEmail(username);
        helper.setText(emailContent , true); //  // true indicates HTML

        mailSender.send(message);
    }

    private String buildWelcomeEmail(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to PharmFlow!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your account has been successfully created and you're now part of the PharmFlow community!</p>
                        <p>We're excited to have you on board and look forward to helping you manage your pharmacy operations efficiently.</p>
                        <p>If you have any questions, feel free to reach out to our support team.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>The PharmFlow Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username);
    }

    public void sendOrderConfirmationEmail(String toEmail, String userName, Long orderId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Order Confirmed - Order #" + orderId);

        String emailContent = buildOrderConfirmationEmail(userName, orderId);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }


    public void sendOrderStatusUpdateEmail(String toEmail, String userName, Long orderId,
                                           String oldStatus, String newStatus) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Order Status Updated - Order #" + orderId);

        String emailContent = buildOrderStatusUpdateEmail(userName, orderId, oldStatus, newStatus);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }

    public void sendOrderDeliveredEmail(String toEmail, String userName, Long orderId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Order Delivered - Order #" + orderId);

        String emailContent = buildOrderDeliveredEmail(userName, orderId);
        helper.setText(emailContent, true);

        mailSender.send(message);
    }


    private String buildOrderConfirmationEmail(String userName, Long orderId) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #666; }
                    .order-id { background: #e7f3ff; padding: 10px; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Order Confirmed! 🎉</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for your order! We're excited to let you know that your order has been confirmed and is being processed.</p>
                        
                        <div class="order-id">
                            <strong>Order ID:</strong> #%d
                        </div>
                        
                        <p>We'll notify you when your order ships. You can check your order status anytime in your account.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>The PharmFlow Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, orderId);
    }



    private String buildOrderStatusUpdateEmail(String userName, Long orderId, String oldStatus, String newStatus) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #10B981; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #666; }
                    .status-update { background: #d1fae5; padding: 10px; border-radius: 5px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Order Status Updated</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your order status has been updated:</p>
                        
                        <div class="status-update">
                            <strong>Order ID:</strong> #%d<br>
                            <strong>Status Changed:</strong> %s → %s
                        </div>
                        
                        <p>Your order is now one step closer to delivery!</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>The PharmFlow Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, orderId, oldStatus, newStatus);
    }


    private String buildOrderDeliveredEmail(String userName, Long orderId) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { padding: 20px; text-align: center; color: #666; }
                    .delivery-message { background: #d1fae5; padding: 15px; border-radius: 5px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Order Delivered! ✅</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        
                        <div class="delivery-message">
                            <h3>🎉 Your order has been successfully delivered!</h3>
                            <p><strong>Order ID:</strong> #%d</p>
                        </div>
                        
                        <p>We hope you're satisfied with your purchase. If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
                        
                        <p>Thank you for choosing PharmFlow!</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>The PharmFlow Team</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, orderId);
    }

}
