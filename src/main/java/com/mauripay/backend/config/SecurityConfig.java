package com.mauripay.backend.config;

import com.mauripay.backend.merchant.ApiKeyAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter)
            throws Exception {
        http
                // Stateful cookie-session auth for users; CSRF disabled because the API is consumed
                // by non-browser clients (mobile app) with no cross-site form posts.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/merchants/register").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Merchant (API key) endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/*/status").hasRole("MERCHANT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/merchants/me").hasRole("MERCHANT")
                        // User (session) endpoints
                        .requestMatchers("/api/v1/payments/**").hasRole("USER")
                        .requestMatchers("/api/v1/transfers/**", "/api/v1/transactions/**").hasRole("USER")
                        .anyRequest().authenticated())
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
