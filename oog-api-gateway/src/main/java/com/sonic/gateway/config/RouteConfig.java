package com.sonic.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final GatewayProperties props;

    /**
     * Dynamically creates one route per backend entry in application.yml.
     *
     * Routing rule:
     *   Incoming: /{backend-id}/any/path/here
     *   Strip:    /{backend-id}
     *   Forward:  backend.baseUrl + /any/path/here
     *
     * Whitelist enforcement is handled separately by WhitelistGlobalFilter.
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        var routes = builder.routes();

        for (var backend : props.getBackends()) {
            String backendId = backend.getId();
            String targetUrl = backend.getBaseUrl();
            String pathPattern = "/" + backendId + "/**";

            log.info("Registering route [{}] → {} (pattern: {})", backendId, targetUrl, pathPattern);

            routes.route(backendId, r -> r
                .path(pathPattern)
                .filters(f -> f.stripPrefix(1))
                .uri(targetUrl)
            );
        }

        return routes.build();
    }
}
