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

}
