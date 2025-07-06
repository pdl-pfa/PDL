package odm_auth.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import odm_auth.auth.entity.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     *
     * @param user
     * @param verificationLink
     */
    public void sendVerificationEmail(User user, String verificationLink) {
        try {
            String htmlContent = loadTemplate("templates/verification-email.html", Map.of(
                    "name", user.getName(),
                    "lastName", user.getLastName(),
                    "link", verificationLink
            ));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setFrom("adminApp@app.com");
            helper.setSubject("Verify your ODM Auth Account");
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     *
     * @param classpathFile
     * @param placeholders
     * @return template
     */
    public String loadTemplate(String classpathFile, Map<String, String> placeholders) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathFile);
            byte[] bytes = resource.getInputStream().readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                content = content.replace("[[" + entry.getKey() + "]]", entry.getValue());
            }
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }
}
