package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Metadata  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String key;
    private String value;
    private String language;
}