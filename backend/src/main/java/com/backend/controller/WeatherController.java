package com.backend.controller;

import com.backend.dto.weather.CurrentWeatherResponse;
import com.backend.dto.weather.DailyForecastResponse;
import com.backend.dto.weather.WeatherAlertResponse;
import com.backend.entity.User;
import com.backend.service.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    // Current weather

    @GetMapping("/current")
    public CurrentWeatherResponse current(
            @PathVariable Long farmId,
            HttpServletRequest request) {

    User user = authenticatedUser(request);

    CurrentWeatherResponse response =
            weatherService.getCurrentWeather(
                    farmId,
                    user.getId()
            );

    return response;
    }

    // Seven-day forecast

    @GetMapping("/forecast")
    public List<DailyForecastResponse> forecast(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        return weatherService.getForecast(
                farmId,
                authenticatedUser(request).getId()
        );
    }

    // Weather alerts

    @GetMapping("/alerts")
    public List<WeatherAlertResponse> alerts(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        return weatherService.getAlerts(
                farmId,
                authenticatedUser(request).getId()
        );
    }

    // Authenticated user

    private User authenticatedUser(HttpServletRequest request) {

        Object value = request.getAttribute("authenticatedUser");

        if (value instanceof User user) {
            return user;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required"
        );
    }
}