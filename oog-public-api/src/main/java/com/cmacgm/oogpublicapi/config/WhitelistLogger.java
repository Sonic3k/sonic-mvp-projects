package com.cmacgm.oogpublicapi.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistLogger {

    private final GatewayProperties gatewayProperties;

    @PostConstruct
    public void logLoadedWhitelist() {
        log.info("========== LOADED WHITELIST ==========");
        for (var backend : gatewayProperties.getBackends()) {
            log.info("Backend [{}] → {}", backend.getId(), backend.getBaseUrl());
            for (var route : backend.getRoutes()) {
                log.info("  {} {}", route.getMethods(), route.getPath());
            }
        }
        log.info("======================================");
    }
}
