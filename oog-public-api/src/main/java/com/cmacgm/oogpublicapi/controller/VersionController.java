package com.cmacgm.oogpublicapi.controller;

import com.cmacgm.oogpublicapi.config.GatewayProperties;
import com.cmacgm.oogpublicapi.dto.UpstreamVersionDto;
import com.cmacgm.oogpublicapi.dto.VersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionDto versionDto;
    private final GatewayProperties gatewayProperties;
    private final WebClient.Builder webClientBuilder;

    @GetMapping
    public Mono<VersionDto> getVersion() {
        return fetchAllUpstreams()
            .collectList()
            .map(upstreams -> {
                versionDto.setUpstreams(upstreams);
                return versionDto;
            });
    }

    private Flux<UpstreamVersionDto> fetchAllUpstreams() {
        return Flux.fromIterable(gatewayProperties.getBackends())
            .flatMap(backend -> webClientBuilder.build()
                .get()
                .uri(backend.getBaseUrl() + "/api/version")
                .retrieve()
                .bodyToMono(UpstreamVersionDto.class)
                .map(dto -> {
                    dto.setBackendId(backend.getId());
                    return dto;
                })
                .onErrorResume(ex -> {
                    log.warn("Upstream '{}' version unavailable: {}", backend.getId(), ex.getMessage());
                    UpstreamVersionDto dto = new UpstreamVersionDto();
                    dto.setBackendId(backend.getId());
                    return Mono.just(dto);
                })
            );
    }
}
