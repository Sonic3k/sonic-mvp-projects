package com.sonic.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private Security security = new Security();
    private List<BackendConfig> backends = List.of();

    @Data
    public static class Security {
        private boolean enabled = false;
        private String issuerUri;
        private String jwkSetUri;
    }

    @Data
    public static class BackendConfig {
        /** Used as URL prefix: /{id}/... */
        private String id;
        /** Upstream server URL */
        private String baseUrl;
        /** Whitelisted routes for this backend */
        private List<RouteRule> routes = List.of();
    }

    @Data
    public static class RouteRule {
        /** Ant-style path pattern, e.g. /api/products/** */
        private String path;
        /** Allowed HTTP methods, e.g. [GET, POST] */
        private List<String> methods = List.of();
    }
}
