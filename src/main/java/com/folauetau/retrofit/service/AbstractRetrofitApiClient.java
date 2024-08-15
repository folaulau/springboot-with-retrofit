package com.folauetau.retrofit.service;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;

@Slf4j
public abstract class AbstractRetrofitApiClient implements RetrofitApiClient{

    @Nullable
    @Override
    public OkHttpClient getOkHttpClient() {
        log.info("getOkHttpClient()");
        return this.createOkHttpClient();
    }
}
