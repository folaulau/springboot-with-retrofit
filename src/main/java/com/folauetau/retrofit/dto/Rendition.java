package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Rendition  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String mimeType;
    private int size;
    private int width;
    private int height;
    private List<Link> links;
    private String relationshipType;
}
