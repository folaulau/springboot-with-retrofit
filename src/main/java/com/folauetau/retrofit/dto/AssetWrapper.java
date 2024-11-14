package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AssetWrapper implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Asset asset;
    private Integer index;
    private Long addedDate;
    private List<Metadata> metadata;

}
