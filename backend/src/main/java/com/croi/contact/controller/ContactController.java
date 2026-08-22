package com.croi.contact.controller;

import com.croi.common.dto.ApiResponse;
import com.croi.common.exception.ValidationException;
import com.croi.contact.dto.ContactSubmissionDto;
import com.croi.contact.service.ContactService;
import com.croi.contact.service.RecaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unauthenticated endpoint — submissions come from anonymous visitors on the
 * public landing page. Must stay listed in SecurityConfig's public endpoints.
 */
@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private static final String RECAPTCHA_ACTION = "contact_form";

    private final ContactService contactService;
    private final RecaptchaService recaptchaService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactSubmissionDto>> submitContactForm(
            @Valid @RequestBody ContactSubmissionDto request) {
        if (!recaptchaService.verify(request.getRecaptchaToken(), RECAPTCHA_ACTION)) {
            throw new ValidationException("Verification failed. Please try again.");
        }

        ContactSubmissionDto saved = contactService.submit(request);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Thanks for reaching out! We'll be in touch soon."));
    }
}
