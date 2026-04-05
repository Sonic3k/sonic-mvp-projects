package com.cmacgm.oogpublicapi.controller;

import com.cmacgm.oogpublicapi.dto.VersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionDto versionDto;

    @GetMapping
    public VersionDto getVersion() {
        return versionDto;
    }
}
