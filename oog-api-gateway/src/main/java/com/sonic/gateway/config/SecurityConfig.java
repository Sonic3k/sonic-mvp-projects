package com.sonic.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayProperties gatewayProperties;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        // Security disabled → open for testing (controlled by SECURITY_ENABLED env var)
        if (!gatewayProperties.getSecurity().isEnabled()) {
            log.warn("⚠️  Security is DISABLED — set SECURITY_ENABLED=true in production!");
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
        }

        // Security enabled → validate OAuth2 OIDC JWT on every request
        log.info("Security ENABLED — OAuth2 JWT validation active");
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex
                .pathMatchers("/actuator/health").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                // issuer-uri / jwk-set-uri configured via spring.security.oauth2
                // Set OIDC_ISSUER_URI env var and add to application.yml when enabling
            }))
            .build();
    }
}
