package com.dushy.tenantmanage.security;

import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.enums.AuthProvider;
import com.dushy.tenantmanage.enums.UserType;
import com.dushy.tenantmanage.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler for successful OAuth2 authentication.
 * Generates a JWT token and redirects to the frontend with the token.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirectUri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }

        // Find or create user - ensures user exists before generating token
        userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email, name));

        // Generate JWT token using the verified email
        String token = jwtUtils.generateToken(email);

        // Build redirect URL with token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User createNewUser(String email, String name) {
        User newUser = User.builder()
                .email(email)
                .fullName(name != null ? name : email)
                .authProvider(AuthProvider.GOOGLE)
                .userType(UserType.OWNER)
                .isActive(true)
                .build();
        return userRepository.save(newUser);
    }
}
