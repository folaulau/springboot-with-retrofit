package com.folauetau.retrofit.service;

import java.util.Map;

import com.folauetau.retrofit.dto.TitanCollectionApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface TitanCollectionService {

    @Headers({
        "Accept: */*",
        "User-Agent: Retrofit-Sample-App"
    })
    @GET("{collectionId}")
    Call<TitanCollectionApiResponse> getCollection(@Path("collectionId") String collectionId);
}
