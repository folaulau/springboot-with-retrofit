package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Child  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String path;
    private String collectionID;
    private String type;
    private String lastUpdatedDate;
    private List<Link> links;

    // json file name
    private String fileName;
}
