package odm_finance.finance.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import odm_finance.finance.model.EmailTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.company-name}")
    private String companyName;

    /**
     * Envoie un email HTML avec une pièce jointe optionnelle
     *
     * @param emailTemplate Les données de l'email à envoyer
     * @throws MessagingException Si une erreur survient lors de l'envoi
     */
    public void sendHtmlEmail(EmailTemplate emailTemplate) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuration de base
            helper.setFrom(fromEmail, companyName);
            helper.setTo(emailTemplate.getTo());
            helper.setSubject(emailTemplate.getSubject());

            // Traitement du template HTML
            Context context = new Context();
            if (emailTemplate.getVariables() != null) {
                emailTemplate.getVariables().forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(emailTemplate.getTemplateName(), context);
            helper.setText(htmlContent, true);

            // Ajout de la pièce jointe si présente
            if (emailTemplate.getAttachment() != null && emailTemplate.getAttachmentName() != null) {
                ByteArrayResource attachment = new ByteArrayResource(emailTemplate.getAttachment());
                helper.addAttachment(emailTemplate.getAttachmentName(), attachment, emailTemplate.getAttachmentType());
            }

            // Envoi de l'email
            mailSender.send(message);
            log.info("Email envoyé avec succès à : {}", emailTemplate.getTo());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à : {}", emailTemplate.getTo(), e);
            throw new MessagingException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email simple sans pièce jointe
     *
     * @param to Destinataire
     * @param subject Sujet
     * @param templateName Nom du template
     * @param variables Variables pour le template
     * @throws MessagingException Si une erreur survient lors de l'envoi
     */
    public void sendSimpleEmail(String to, String subject, String templateName,
                                java.util.Map<String, Object> variables) throws MessagingException {
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTo(to);
        emailTemplate.setSubject(subject);
        emailTemplate.setTemplateName(templateName);
        emailTemplate.setVariables(variables);

        sendHtmlEmail(emailTemplate);
    }
}