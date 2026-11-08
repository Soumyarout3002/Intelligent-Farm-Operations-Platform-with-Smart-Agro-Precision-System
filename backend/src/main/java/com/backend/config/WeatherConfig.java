package com.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WeatherConfig {

    @Bean
    public RestClient openMeteoRestClient() {

        return RestClient.create(
                "https://api.open-meteo.com"
        );
    }
}
