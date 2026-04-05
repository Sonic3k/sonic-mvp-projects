package com.cmacgm.oogpublicapi.dto;

import lombok.Data;

@Data
public class UpstreamVersionDto {
    private String backendId;
    private String baseUrl;
    private VersionDto version;
    private String error;
}
