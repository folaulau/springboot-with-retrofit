package com.folauetau.retrofit.service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Slf4j
public class UserClient extends AbstractRetrofitApiClient{

    private final Set<Class<?>> supportedServices;

    private Duration readTimeout = null;

    public UserClient(Set<Class<?>> supportedServices) {
        this.supportedServices = supportedServices;
    }

    @Override
    public Set<Class<?>> getSupportedServices() {
        return supportedServices;
    }

    @Nullable
    @Override
    public OkHttpClient createOkHttpClient() {
        log.info("Creating custom OkHttpClient");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        Optional.ofNullable(readTimeout).ifPresentOrElse(
            timeout -> {
                log.info("Setting custom read timeout to {} seconds", timeout.getSeconds());
                builder.readTimeout(timeout);
            },
            () -> {
                log.info("Setting default read timeout");
                builder.readTimeout(Duration.ofSeconds(30));
            }
        );

        return builder.build();
    }

    @Override
    public <T> T getService(@NotNull Class<T> serviceClass) {
        log.info("getService");
        return null;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
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
