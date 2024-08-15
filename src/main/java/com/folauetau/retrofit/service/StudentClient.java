package com.folauetau.retrofit.service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

public class StudentClient extends UserClient{

    public StudentClient(Set<Class<?>> supportedServices) {
        super(supportedServices);
    }

    public static void main(String[] args) {
        StudentClient client = new StudentClient(Set.of(RapidApiService.class));
        client.testRun();
    }

    public void testRun(){
        System.out.println("StudentClient is running");

        setReadTimeout(Duration.ofSeconds(30));

        RapidApiService service = createService(RapidApiService.class);

        Call<Map<String, Object>> call = service.ping();

        try {
            Response<Map<String, Object>> response = execute(() -> service.ping());

            System.out.println(response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
