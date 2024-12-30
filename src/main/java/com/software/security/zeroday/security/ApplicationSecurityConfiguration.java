package com.software.security.zeroday.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.security.zeroday.dto.ErrorEntity;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.InetAddress;
import java.net.UnknownHostException;

@EnableMethodSecurity
@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfiguration {
    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin/logs", "/actuator/**").access((auth, context) -> {
                    String remoteAddr = context.getRequest().getRemoteAddr();
                    boolean isLocal = remoteAddr.equals(getLocalIpAddress()) || remoteAddr.equals("127.0.0.1");
                    return new AuthorizationDecision(true);
                })
                .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/activate").permitAll()
                .requestMatchers(HttpMethod.POST, "/activate/new").permitAll()
                .requestMatchers(HttpMethod.POST, "/signin").permitAll()
                .requestMatchers(HttpMethod.POST, "/password/reset").permitAll()
                .requestMatchers(HttpMethod.POST, "/password/new").permitAll()
                .requestMatchers(HttpMethod.GET, "/options").permitAll()
                .anyRequest().authenticated()
            ).exceptionHandling(exceptionHandling -> exceptionHandling
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setContentType("application/json;charset=UTF-8");
                        String authorization = request.getHeader("Authorization");
                        ErrorEntity errorEntity;

                        if (authorization != null && authorization.startsWith("Bearer ")) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            errorEntity = ErrorEntity.builder()
                                .status(HttpServletResponse.SC_FORBIDDEN)
                                .message("Access is denied")
                                .build();
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            errorEntity = ErrorEntity.builder()
                                .status(HttpServletResponse.SC_UNAUTHORIZED)
                                .message("Authentication is required")
                                .build();
                        }

                        response.getWriter().write(this.objectMapper.writeValueAsString(errorEntity));
                    })
            )
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                    httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ).addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to determine local IP address", e);
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
