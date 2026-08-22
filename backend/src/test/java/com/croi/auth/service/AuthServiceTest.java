package com.croi.auth.service;

import com.croi.auth.dto.AuthResponse;
import com.croi.auth.dto.SignupRequest;
import com.croi.organization.dto.CreateOrganizationRequest;
import com.croi.organization.service.OrganizationService;
import com.croi.security.JwtProvider;
import com.croi.users.entity.User;
import com.croi.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private OrganizationService organizationService;
    @InjectMocks private AuthService authService;

    @Test
    void signupCreatesAnOwnedWorkspaceForTheNewUser() {
        UUID userId = UUID.randomUUID();
        SignupRequest request = new SignupRequest("ada@example.com", "secure-password", "Ada", "Lovelace");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(jwtProvider.generateToken(request.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.signup(request);

        ArgumentCaptor<CreateOrganizationRequest> organization = ArgumentCaptor.forClass(CreateOrganizationRequest.class);
        verify(organizationService).createOrganization(organization.capture(), eq(userId));
        assertEquals("Ada's Workspace", organization.getValue().getName());
        assertEquals("jwt-token", response.getToken());
        assertEquals(userId, response.getUser().getId());
    }
}
