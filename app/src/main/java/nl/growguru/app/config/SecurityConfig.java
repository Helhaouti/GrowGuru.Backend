package nl.growguru.app.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.services.GrowGuruService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static nl.growguru.app.api.html.StripeLandingController.SUCCESSFUL_STRIPE_PAYMENT_PAGE;
import static nl.growguru.app.api.rest.AuthenticateController.AUTH_API_BASE;
import static nl.growguru.app.api.rest.WebhookController.HOOK_API_BASE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * The security configuration of the application.
 */
@Configuration
@EnableWebSecurity

@RequiredArgsConstructor

@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer"
)
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final GrowGuruService growGuruService;
    private final JWTFilter tokenFilter;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // No need for CSRF protection, given use of JWT.
        http.csrf(AbstractHttpConfigurer::disable);

        http.headers(headers -> {
            // Disable frame options to allow same-origin frames
            if (activeProfile.equals("dev"))
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
        });

        http.authorizeHttpRequests(request ->
                        request.requestMatchers(
                                        AntPathRequestMatcher.antMatcher(POST, HOOK_API_BASE + "/**"),
                                        AntPathRequestMatcher.antMatcher(AUTH_API_BASE + "/**"),
                                        AntPathRequestMatcher.antMatcher(SUCCESSFUL_STRIPE_PAYMENT_PAGE),
                                        AntPathRequestMatcher.antMatcher("/favicon.ico/"),
                                        AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                                        AntPathRequestMatcher.antMatcher("/v3/api-docs/**")
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                // Set session management to stateless (for JWT)
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS));

        // Add JWT token filter
        http.addFilterAt(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(growGuruService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

}