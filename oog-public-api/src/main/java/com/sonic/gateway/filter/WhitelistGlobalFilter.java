package com.sonic.gateway.filter;

import com.sonic.gateway.config.GatewayProperties;
import com.sonic.gateway.config.GatewayProperties.BackendConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayProperties gatewayProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String fullPath = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // Always allow actuator endpoints
        if (fullPath.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        // Parse: /mock-legacy/legacy/api/products/1
        //  → backendId = "mock-legacy"
        //  → downstreamPath = "/legacy/api/products/1"
        String[] segments = fullPath.split("/", 3);
        if (segments.length < 2 || segments[1].isBlank()) {
            return forbidden(exchange, "Missing backend identifier in path");
        }

        String backendId = segments[1];
        String downstreamPath = segments.length > 2 ? "/" + segments[2] : "/";

        // Find backend config
        BackendConfig backend = gatewayProperties.getBackends().stream()
            .filter(b -> b.getId().equals(backendId))
            .findFirst()
            .orElse(null);

        if (backend == null) {
            log.warn("Unknown backend '{}' requested from {}", backendId, getClientIp(exchange));
            return forbidden(exchange, "Unknown backend: " + backendId);
        }

        // Check if downstreamPath + method is in the whitelist
        boolean allowed = backend.getRoutes().stream().anyMatch(route -> {
            boolean pathMatch = pathMatcher.match(route.getPath(), downstreamPath);
            boolean methodMatch = route.getMethods().stream()
                .anyMatch(m -> m.equalsIgnoreCase(method));
            return pathMatch && methodMatch;
        });

        if (!allowed) {
            log.warn("Blocked [{}] {} {} — not in whitelist for backend '{}'",
                getClientIp(exchange), method, downstreamPath, backendId);
            return forbidden(exchange, method + " " + downstreamPath + " is not exposed");
        }

        log.debug("Allowed [{}] {} {} → {}", getClientIp(exchange), method, downstreamPath, backend.getBaseUrl());
        return chain.filter(exchange);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String reason) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
            {"status":403,"error":"Forbidden","message":"%s"}
            """.formatted(reason);

        DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    private String getClientIp(ServerWebExchange exchange) {
        var forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null) return forwardedFor.split(",")[0].trim();
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        // Run before routing so we block early
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
