package com.folauetau.retrofit.rest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.folauetau.retrofit.dto.TitanApiResponse;
import com.folauetau.retrofit.service.TitanService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TitanRestApi {

    private String domain = "https://titanapi.churchofjesuschrist.org/assetsearch/api/v2/collection/details/collectionID/";

    private Retrofit retrofit = null;

    public TitanRestApi() {
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

    public TitanApiResponse getCollection(String collectionID) {
//        System.out.println("url: " + retrofit.baseUrl().toString()+collectionID+"\n");
        TitanService titanService = retrofit.create(TitanService.class);
        TitanApiResponse response = null;
        try {
            response = titanService.getCollection(collectionID).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }



}
