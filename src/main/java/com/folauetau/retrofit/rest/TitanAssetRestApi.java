package com.folauetau.retrofit.rest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.folauetau.retrofit.dto.titanasset.TitanAssetApiResponse;
import com.folauetau.retrofit.service.TitanAssetService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TitanAssetRestApi {

    private String domain = "https://contentapi.churchofjesuschrist.org/api/v2/asset/details/id/";

    private Retrofit retrofit = null;

    public TitanAssetRestApi() {
        // Create a custom OkHttpClient with a custom read timeout
        OkHttpClient customClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS) // Custom read timeout
            .build();

        // Create a Gson instance
        Gson gson = new GsonBuilder()
            .setLenient()
            .create();

        retrofit = new Retrofit.Builder()
            .client(customClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(domain)
            .build();

    }

    public TitanAssetApiResponse getTitanAsset(String titanId) {
//        System.out.println("url: " + retrofit.baseUrl().toString()+collectionID+"\n");
        TitanAssetService titanService = retrofit.create(TitanAssetService.class);
        TitanAssetApiResponse response = null;
        try {
            response = titanService.getTitanAsset(titanId).execute().body();
        } catch (IOException e) {
            System.out.println("getTitanAsset API call error: " + e.getMessage());
        }
        return response;
    }
}
