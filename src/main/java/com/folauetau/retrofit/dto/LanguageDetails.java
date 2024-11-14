package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class LanguageDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private int languageUid;
    private String code639_3;
    private String preferredID;
    private String scriptCode;
    private String legacyWeb;
    private MetadataLanguage metadataLanguage;
    private List<BinaryLanguage> binaryLanguages;


}
