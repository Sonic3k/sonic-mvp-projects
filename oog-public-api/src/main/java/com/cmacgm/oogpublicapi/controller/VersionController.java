package com.cmacgm.oogpublicapi.controller;

import com.cmacgm.oogpublicapi.config.GatewayProperties;
import com.cmacgm.oogpublicapi.dto.UpstreamVersionDto;
import com.cmacgm.oogpublicapi.dto.VersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionDto versionDto;
    private final GatewayProperties gatewayProperties;
    private final WebClient.Builder webClientBuilder;

    @GetMapping
    public VersionDto getVersion() {
        return versionDto;
    }

    @GetMapping("/upstream/{backendId}")
    public Mono<ResponseEntity<UpstreamVersionDto>> getUpstreamVersion(@PathVariable String backendId) {

        var backend = gatewayProperties.getBackends().stream()
            .filter(b -> b.getId().equals(backendId))
            .findFirst()
            .orElse(null);

        if (backend == null) {
            UpstreamVersionDto notFound = new UpstreamVersionDto();
            notFound.setBackendId(backendId);
            notFound.setError("Unknown backend: " + backendId);
            return Mono.just(ResponseEntity.badRequest().body(notFound));
        }

        return webClientBuilder.build()
            .get()
            .uri(backend.getBaseUrl() + "/api/version")
            .retrieve()
            .bodyToMono(VersionDto.class)
            .map(version -> {
                UpstreamVersionDto dto = new UpstreamVersionDto();
                dto.setBackendId(backendId);
                dto.setBaseUrl(backend.getBaseUrl());
                dto.setVersion(version);
                return ResponseEntity.ok(dto);
            })
            .onErrorResume(ex -> {
                log.warn("Failed to fetch version from upstream '{}': {}", backendId, ex.getMessage());
                UpstreamVersionDto dto = new UpstreamVersionDto();
                dto.setBackendId(backendId);
                dto.setBaseUrl(backend.getBaseUrl());
                dto.setError(ex.getMessage());
                return Mono.just(ResponseEntity.ok(dto));
            });
    }
}
