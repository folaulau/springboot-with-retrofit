package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Asset implements Serializable {

    private static final long serialVersionUID = 1L;

//    private String createdDate;
    private String distributionUri;
//    private int height;
//    private boolean isCurrentVersion;
//    private boolean isDeleted;
//    private boolean isOriginal;
    private String language;
//    private LanguageDetails languageDetails;
//    private String lastUpdatedDate;
    private String mimeType;
//    private String versionID;
//    private int size;
    private String type;
//    private int width;
    private String assetID;
//    private String sourceType;
//    private String binaryModifyDate;
//    private String cdnModifiedDate;
    private List<Metadata> metadata;
//    private List<Collection> collections;
//    private List<Link> links;
//c2199b453b7e41a4aeff18aa3446722b,
}
