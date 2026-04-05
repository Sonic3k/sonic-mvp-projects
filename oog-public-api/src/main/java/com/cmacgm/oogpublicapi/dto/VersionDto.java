package com.cmacgm.oogpublicapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class VersionDto {
    private String app;
    private String version;
    private String podName;
    private String containerImage;
    private List<UpstreamVersionDto> upstreams;
}
