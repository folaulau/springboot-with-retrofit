package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;
    private String rel;
    private String href;
}
