package com.cmacgm.oogpublicapi.dto;

import lombok.Data;

@Data
public class VersionDto {
    private String app;
    private String version;
    private String podName;
    private String containerImage;
}
