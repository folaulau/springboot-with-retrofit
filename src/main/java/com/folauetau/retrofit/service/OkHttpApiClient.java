package com.folauetau.retrofit.service;

import jakarta.annotation.Nullable;
import okhttp3.OkHttpClient;

public interface OkHttpApiClient {

    @Nullable
    OkHttpClient getOkHttpClient();

    /**
     * {@link java.util.function.Supplier} for the creation of an {@link OkHttpClient}.
     *
     * @return an {@link OkHttpClient} (nullable).
     */
    @Nullable
    OkHttpClient createOkHttpClient();
}
