package com.folauetau.retrofit;

import com.folauetau.retrofit.service.RapidApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class SpringbootWithRetrofitApplicationTests {

	@Test
	void contextLoads() {

		// Create a custom OkHttpClient with a custom read timeout
		OkHttpClient customClient = new OkHttpClient.Builder()
				.readTimeout(30, TimeUnit.SECONDS) // Custom read timeout
				.build();

		// Create a Gson instance
		Gson gson = new GsonBuilder()
				.setLenient()
				.create();

		Retrofit retrofit = new Retrofit.Builder()
				.client(customClient)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.baseUrl("https://api.blip-delivery.com/v1/")
				.build();

		RapidApiService service = retrofit.create(RapidApiService.class);

		Call<Map<String, Object>> call = service.ping();

        try {
            Map<String, Object> response = call.execute().body();

			System.out.println(response);
        } catch (Exception e) {
            log.warn("Error: {}", e.getMessage(), e);
        }

//		Call<Map<String, Object>> call = service.listRepos("country", "json");
//
//		try {
//			Map<String, Object> response = call.execute().body();
//
//			System.out.println(response);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}

    }

}
