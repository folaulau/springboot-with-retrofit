package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Collection implements Serializable {

    private static final long serialVersionUID = 1L;

    private String collection;
    private String collectionID;
    private String addedToCollectionDate;
}
