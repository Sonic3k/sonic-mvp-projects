package com.cmacgm.oogpublicapi.config;

import com.cmacgm.oogpublicapi.dto.VersionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class VersionConfig {

    @Value("${oogpublicapi.app.name}")
    private String appName;

    @Value("${oogpublicapi.app.version}")
    private String appVersion;

    @Value("${POD_CONTAINER_IMAGE:unknown}")
    private String containerImage;

    @Bean
    public VersionDto versionDto() {
        VersionDto dto = new VersionDto();
        dto.setApp(appName);
        dto.setVersion(appVersion);
        dto.setContainerImage(containerImage);
        try {
            dto.setPodName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            dto.setPodName("unknown");
        }
        return dto;
    }
}
