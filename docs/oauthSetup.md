# Google OAuth2 Setup Guide for Tenant Management System

This guide provides step-by-step instructions to implement Google OAuth2 authentication in this Spring Boot application.

---

## Phase 1: Google Cloud Console Configuration

1.  **Create a New Project**:
    *   Go to the [Google Cloud Console](https://console.cloud.google.com/).
    *   Click the project dropdown and select "New Project".
    *   Name it (e.g., `tenantmanage-oauth`) and click "Create".

2.  **Configure OAuth Consent Screen**:
    *   Navigate to **APIs & Services > OAuth consent screen**.
    *   Select **External** user type and click "Create".
    *   Fill in required app information:
        *   **App name**: Tenant Management
        *   **User support email**: Your email
        *   **Developer contact info**: Your email
    *   Click "Save and Continue" through Scopes and Test Users (add your own email as a test user if in testing mode).

3.  **Create Credentials**:
    *   Go to **APIs & Services > Credentials**.
    *   Click **+ Create Credentials** and select **OAuth client ID**.
    *   **Application type**: Web application.
    *   **Name**: Spring Boot Client.
    *   **Authorized JavaScript origins**: `http://localhost:8080` (for local development).
    *   **Authorized redirect URIs**: `http://localhost:8080/login/oauth2/code/google`.
        *   *Note: This path is a standard Spring Security convention.*
    *   Click "Create".
    *   **Copy the Client ID and Client Secret.** You will need these in the next phase.

---

## Phase 2: Application Properties Configuration

Update your `src/main/resources/application.yml` with the following configuration:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope:
              - profile
              - email
```

> [!IMPORTANT]
> Never commit your real Client Secret to version control. Use environment variables or a secure vault in production.

---

## Phase 3: Update Spring Security Configuration

Modify `src/main/java/com/dushy/tenantmanage/config/SecurityConfig.java` to enable OAuth2 login.

### 1. Add OAuth2 Login to Security Filter Chain

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2Login(oauth2 -> oauth2
            .successHandler(oAuth2AuthenticationSuccessHandler()) // Optional: for JWT integration
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

## Phase 4: Integration with JWT (Success Handler)

Since this app uses stateless JWT-based security, you need a success handler to generate a JWT after a successful OAuth2 login and return it to the client (usually via a redirect with a query param or a cookie).

### 1. Create `OAuth2AuthenticationSuccessHandler.java`

```java
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    // Autowire jwtUtils
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        String targetUrl = "http://localhost:3000/oauth2/redirect"; // Frontend URL
        
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        // Generate JWT
        String token = jwtUtils.generateToken(email);
        
        // Use UriComponentsBuilder to add token to redirect URL
        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
```

---

## Phase 5: Verification & Testing

1.  Start the application.
2.  Navigate to `http://localhost:8080/oauth2/authorization/google`.
3.  You should be redirected to Google's login page.
4.  After logging in, Google redirects back to your app.
5.  Your Success Handler will generate a JWT and redirect to your frontend with the token.
