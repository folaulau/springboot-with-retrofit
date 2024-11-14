package com.folauetau.retrofit.service;

import java.util.Map;

import com.folauetau.retrofit.dto.TitanApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface TitanService {

    @Headers({
        "Accept: */*",
        "User-Agent: Retrofit-Sample-App"
    })
    @GET("{collectionId}")
    Call<TitanApiResponse> getCollection(@Path("collectionId") String collectionId);
}
