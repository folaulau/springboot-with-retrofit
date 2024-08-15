package com.folauetau.retrofit.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Slf4j
public class UserClient extends AbstractRetrofitApiClient{

    private final Set<Class<?>> supportedServices;

    public UserClient(Set<Class<?>> supportedServices) {
        this.supportedServices = supportedServices;
    }

    @Override
    public Set<Class<?>> getSupportedServices() {
        return supportedServices;
    }

    @Nullable
    @Override
    public OkHttpClient getOkHttpClient() {
        log.info("getOkHttpClient()");
        return createOkHttpClient();
    }

    @Nullable
    @Override
    public OkHttpClient createOkHttpClient() {
        log.info("Creating custom OkHttpClient");

        OkHttpClient customClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS) // Custom read timeout
            .build();

        return customClient;
    }

    @Override
    public <T> T getService(@NotNull Class<T> serviceClass) {
        log.info("getService");
        return null;
    }

    @Override
    public Retrofit getRetrofit(@NotNull Class<?> serviceClass) {
        log.info("getRetrofit");

        // Create a Gson instance
        Gson gson = new GsonBuilder()
            .setLenient()
            .create();

        Retrofit retrofit = new Retrofit.Builder()
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("https://api.blip-delivery.com/v1/")
            .build();

        return retrofit;
    }
}
