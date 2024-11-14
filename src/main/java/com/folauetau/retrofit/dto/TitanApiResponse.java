package com.folauetau.retrofit.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TitanApiResponse  implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private Params params;
    private int duration;
    private CollectionDetails result;

}
