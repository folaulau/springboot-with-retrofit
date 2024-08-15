package com.folauetau.retrofit.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

import java.util.Map;

public interface RapidApiService {

    @Headers({
            "Accept: */*",
            "User-Agent: Retrofit-Sample-App"
    })
    @GET("ping")
    Call<Map<String, Object>> ping();

    @GET("{param}/{format}")
    Call<Map<String, Object>> listRepos(@Path("param") String user, @Path("format") String format);
}
