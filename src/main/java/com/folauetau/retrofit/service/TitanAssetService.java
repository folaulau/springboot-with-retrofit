package com.folauetau.retrofit.service;

import java.util.Map;

import com.folauetau.retrofit.dto.TitanApiResponse;
import com.folauetau.retrofit.dto.titanasset.TitanAssetResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface TitanAssetService {

    @Headers({
        "Accept: */*",
        "User-Agent: Retrofit-Sample-App"
    })
    @GET("{titanId}")
    Call<TitanAssetResponse> getTitanAsset(@Path("titanId") String titanId);
}
