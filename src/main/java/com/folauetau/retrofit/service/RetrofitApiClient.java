package com.folauetau.retrofit.service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public interface RetrofitApiClient extends OkHttpApiClient{

    @Nonnull
    default <T> Response<T> execute(
        @Nonnull Supplier<Call<T>> serviceCall
    )
        throws Exception {

        Objects.requireNonNull(serviceCall, "Service call supplier must not be null!");

        Call<T> call = serviceCall.get();
        Objects.requireNonNull(call, "Cannot execute null service call!");

        // Execute the call.
        Response<T> response = call.execute();

        return response;
    }

    @Nullable
    <T> T getService(@Nonnull Class<T> serviceClass);

    @Nullable
    default <T> T createService(@Nonnull Class<T> serviceClass) {

        if (!this.isSupported(serviceClass)) {

            return null;
        }

        return Optional.ofNullable(getRetrofit(serviceClass))
            .map(retrofit -> retrofit.create(serviceClass))
            .orElse(null);
    }

    @Nullable
    Retrofit getRetrofit(@Nonnull Class<?> serviceClass);

    default boolean isSupported(@Nonnull Class<?> serviceClass) {
        return this.getSupportedServices().contains(serviceClass);
    }

    @Nonnull
    Set<Class<?>> getSupportedServices();
}
