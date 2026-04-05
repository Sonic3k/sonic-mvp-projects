package com.cmacgm.oogpublicapi.config;

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
        private String id;
        private String baseUrl;
        private List<RouteRule> routes = List.of();
    }

    @Data
    public static class RouteRule {
        private String path;
        private List<String> methods = List.of();
    }
}
