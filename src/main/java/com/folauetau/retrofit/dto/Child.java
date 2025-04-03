package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Child  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String path;
    private String rootChildLanguage;
    private String collectionID;
    private String type;
    private String lastUpdatedDate;
    private List<Link> links;

    // json file name
    private String fileName;

    private Integer orderIndex;

    public int getOrderIndex() {
        // return -1 if orderIndex is not a number
        return (orderIndex==null || orderIndex<0) ? -1 : orderIndex;
    }
}
