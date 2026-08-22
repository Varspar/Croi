package com.croi.contact.service;

import com.croi.contact.dto.ContactSubmissionDto;
import com.croi.contact.entity.ContactSubmission;
import com.croi.contact.repository.ContactRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private static final String TEMPLATE_PATH = "templates/contact-notification.html";

    private final ContactRepository contactRepository;
    private final JavaMailSender mailSender;

    @Value("${contact.admin-email}")
    private String adminEmail;

    @Value("${contact.from-email}")
    private String fromEmail;

    @Value("${contact.admin-panel-url}")
    private String adminPanelUrl;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Transactional
    public ContactSubmissionDto submit(ContactSubmissionDto request) {
        ContactSubmission submission = ContactSubmission.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .message(request.getMessage())
                .build();

        contactRepository.save(submission);

        // Best-effort only: the submission above is already durably saved, so a
        // missing/misconfigured mail setup (or an SMTP outage) must never surface
        // as an error to the visitor or roll back what's already been recorded.
        notifyAdmin(submission);

        return toDto(submission);
    }

    private void notifyAdmin(ContactSubmission submission) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("CONTACT_ADMIN_EMAIL is not set; skipping admin notification for submission {}",
                    submission.getId());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setTo(adminEmail);

            // Prefer the explicit from-email; fall back to the authenticated SMTP
            // account, since most providers reject/override a From that doesn't
            // match (or isn't a verified alias of) the authenticated sender.
            String resolvedFrom = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : mailUsername;
            if (resolvedFrom != null && !resolvedFrom.isBlank()) {
                helper.setFrom(resolvedFrom);
            }

            helper.setSubject("New Croi contact form submission: " + submission.getCompanyName());
            helper.setText(renderTemplate(submission), true);

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Failed to send contact form admin notification for submission {}",
                    submission.getId(), ex);
        }
    }

    private String renderTemplate(ContactSubmission submission) throws java.io.IOException {
        String template = new String(
                new ClassPathResource(TEMPLATE_PATH).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);

        // No admin panel exists in this app yet, so the "View in Admin Panel"
        // link is only included once a real URL is actually configured —
        // otherwise the email would ship a permanently dead link.
        String adminPanelSection = "";
        if (adminPanelUrl != null && !adminPanelUrl.isBlank()) {
            adminPanelSection = """
                    <hr>
                    <p><a href="%s/submissions/%s">View in Admin Panel</a></p>
                    """.formatted(adminPanelUrl, submission.getId());
        }

        return template
                .replace("{firstName}", HtmlUtils.htmlEscape(submission.getFirstName()))
                .replace("{lastName}", HtmlUtils.htmlEscape(submission.getLastName()))
                .replace("{companyName}", HtmlUtils.htmlEscape(submission.getCompanyName()))
                .replace("{email}", HtmlUtils.htmlEscape(submission.getEmail()))
                .replace("{phone}", submission.getPhone() == null
                        ? "(not provided)" : HtmlUtils.htmlEscape(submission.getPhone()))
                .replace("{message}", HtmlUtils.htmlEscape(submission.getMessage()).replace("\n", "<br>"))
                .replace("{adminPanelSection}", adminPanelSection);
    }

    private ContactSubmissionDto toDto(ContactSubmission submission) {
        ContactSubmissionDto dto = new ContactSubmissionDto();
        dto.setFirstName(submission.getFirstName());
        dto.setLastName(submission.getLastName());
        dto.setCompanyName(submission.getCompanyName());
        dto.setEmail(submission.getEmail());
        dto.setPhone(submission.getPhone());
        dto.setMessage(submission.getMessage());
        // recaptchaToken intentionally left null: it's the visitor's own
        // single-use token, there's no reason to echo it back to them.
        return dto;
    }
}
