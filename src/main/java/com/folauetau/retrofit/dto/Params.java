package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Params implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("securityConstraints")
    private List<SecurityConstraint> securityConstraints;
    private String collectionID;

}
