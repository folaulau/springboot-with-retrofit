package com.folauetau.retrofit.dto.titanasset;

import java.io.Serializable;
import java.util.List;

import com.folauetau.retrofit.dto.CollectionDetails;
import com.folauetau.retrofit.dto.LanguageDetails;
import com.folauetau.retrofit.dto.Link;
import com.folauetau.retrofit.dto.Metadata;
import lombok.Data;

@Data
public class TitanAssetApiResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private TitanAsset result;
    private String status;// SUCCESS or FAILURE
}
