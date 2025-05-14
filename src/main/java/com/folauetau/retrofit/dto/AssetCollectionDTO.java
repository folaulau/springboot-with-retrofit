package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AssetCollectionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String assetId;
    private String assetPath;
    private String assetLanguage;
    private String collectionId;
    private String collectionPath;
}
