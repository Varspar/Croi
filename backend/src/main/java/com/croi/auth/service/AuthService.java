package com.croi.auth.service;

import com.croi.auth.dto.AuthResponse;
import com.croi.auth.dto.LoginRequest;
import com.croi.auth.dto.SignupRequest;
import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ConflictException;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.UnauthorizedException;
import com.croi.security.JwtProvider;
import com.croi.security.UserPrincipal;
import com.croi.organization.dto.CreateOrganizationRequest;
import com.croi.organization.service.OrganizationService;
import com.croi.users.dto.UserDto;
import com.croi.users.entity.User;
import com.croi.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final OrganizationService organizationService;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS, ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        user = userRepository.save(user);

        // A new account is immediately a member (and owner) of a usable workspace.
        // This makes the first visit to the dashboard a complete, ready-to-chat flow.
        organizationService.createOrganization(
                new CreateOrganizationRequest(defaultOrganizationName(user)), user.getId());

        String token = jwtProvider.generateToken(user.getEmail());
        return AuthResponse.builder().token(token).user(toDto(user)).build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, ErrorMessages.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, ErrorMessages.INVALID_CREDENTIALS);
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtProvider.generateToken(user.getEmail());
        return AuthResponse.builder().token(token).user(toDto(user)).build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_TOKEN, ErrorMessages.INVALID_TOKEN));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String defaultOrganizationName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        return firstName.isEmpty() ? "My Workspace" : firstName + "'s Workspace";
    }
}
