package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class BinaryLanguage  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String code639_3;
    private String preferredID;
}
