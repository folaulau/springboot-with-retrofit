package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SecurityConstraint  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private String value;
}
