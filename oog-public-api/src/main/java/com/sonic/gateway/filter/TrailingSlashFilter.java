package com.sonic.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class TrailingSlashFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        String path = uri.getRawPath();

        // Strip trailing slash (but keep root "/")
        if (path.length() > 1 && path.endsWith("/")) {
            String newPath = path.substring(0, path.length() - 1);
            URI newUri = UriComponentsBuilder.fromUri(uri)
                .replacePath(newPath)
                .build(true)
                .toUri();

            log.debug("Strip trailing slash: {} → {}", path, newPath);

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .uri(newUri)
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run after WhitelistGlobalFilter (HIGHEST_PRECEDENCE + 1)
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
