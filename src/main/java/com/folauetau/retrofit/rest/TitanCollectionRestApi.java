package com.folauetau.retrofit.rest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.folauetau.retrofit.dto.TitanCollectionApiResponse;
import com.folauetau.retrofit.dto.TitanCollectionApiResponse;
import com.folauetau.retrofit.service.TitanCollectionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TitanCollectionRestApi {

    private String domain = "https://titanapi.churchofjesuschrist.org/assetsearch/api/v2/collection/details/collectionID/";

    private Retrofit retrofit = null;

    public TitanCollectionRestApi() {
        // Create a custom OkHttpClient with a custom read timeout
        OkHttpClient customClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS) // Custom read timeout
            .build();

        retrofit = new Retrofit.Builder()
            .client(customClient)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                .setLenient()
                .create()))
            .baseUrl(domain)
            .build();

    }

    public TitanCollectionApiResponse getCollection(String collectionID) {
        TitanCollectionService titanService = retrofit.create(TitanCollectionService.class);
        TitanCollectionApiResponse response = null;
        try {
            response = titanService.getCollection(collectionID).execute().body();
        } catch (IOException e) {
            System.out.println("API call error: " + e.getMessage());
        }
        return response;
    }



}
