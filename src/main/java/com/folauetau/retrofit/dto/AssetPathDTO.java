package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssetPathDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assetId;
    private String collectionId;
    private String assetPath;
    private String collectionPath;
    private String collectionLanguage;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }

    public String getCollectionPath() {
        return collectionPath;
    }

    public void setCollectionPath(String collectionPath) {
        this.collectionPath = collectionPath;
    }

    public String getCollectionLanguage() {
        return collectionLanguage;
    }

    public void setCollectionLanguage(String collectionLanguage) {
        this.collectionLanguage = collectionLanguage;
    }
}
