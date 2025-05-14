package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CollectionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String language;
    private String collectionId;
    private String path;
}
