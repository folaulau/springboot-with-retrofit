package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Related  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String language;
    private String mimeType;
    private String sourceType;
    private Long size;
    private Long width;
    private Long height;
    private String assetType;
    private List<String> binaryLanguages;
    private List<Link> links;
    private String relationshipType;
}
