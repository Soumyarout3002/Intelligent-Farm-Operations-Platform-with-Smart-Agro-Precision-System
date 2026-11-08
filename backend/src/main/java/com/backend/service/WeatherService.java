package com.backend.service;

import com.backend.dto.weather.CurrentWeatherResponse;
import com.backend.dto.weather.DailyForecastResponse;
import com.backend.dto.weather.WeatherAlertResponse;
import com.backend.entity.Farm;
import com.backend.repository.FarmRepository;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestClient openMeteoRestClient;
    private final FarmRepository farmRepository;

    // 1. Current weather

    public CurrentWeatherResponse getCurrentWeather(
            Long farmId, Long ownerId) {

        Farm farm = getFarmWithCoordinates(farmId, ownerId);

        JsonNode root = fetchWeather(farm);
        JsonNode current = root.path("current");

        return CurrentWeatherResponse.builder()
                .farmId(farm.getId())
                .farmName(farm.getName())
                .latitude(farm.getLatitude())
                .longitude(farm.getLongitude())

                .temperatureC(number(current, "temperature_2m"))
                .relativeHumidityPercent(
                        integer(current, "relative_humidity_2m"))

                .apparentTemperatureC(
                        number(current, "apparent_temperature"))

                .daytime(booleanValue(current, "is_day"))

                .precipitationMm(number(current, "precipitation"))
                .rainMm(number(current, "rain"))

                .weatherCode(integer(current, "weather_code"))
                .windSpeedKmh(number(current, "wind_speed_10m"))

                .observedAt(text(current, "time"))

                .description(
                        description(integer(current, "weather_code")))

                .build();
    }

    // 2. Seven-day forecast

    public List<DailyForecastResponse> getForecast(
            Long farmId, Long ownerId) {

        Farm farm = getFarmWithCoordinates(farmId, ownerId);

        JsonNode daily = fetchWeather(farm).path("daily");

        JsonNode dates = daily.path("time");

        List<DailyForecastResponse> result = new ArrayList<>();

        for (int i = 0; i < dates.size(); i++) {

            Integer code = atInt(
                    daily.path("weather_code"), i);

            DailyForecastResponse response =
                    DailyForecastResponse.builder()

                            .date(atText(dates, i))

                            .maxTemperatureC(
                                    atDouble(
                                            daily.path("temperature_2m_max"),
                                            i))

                            .minTemperatureC(
                                    atDouble(
                                            daily.path("temperature_2m_min"),
                                            i))

                            .precipitationMm(
                                    atDouble(
                                            daily.path("precipitation_sum"),
                                            i))

                            .precipitationProbabilityMax(
                                    atInt(
                                            daily.path(
                                                    "precipitation_probability_max"),
                                            i))

                            .weatherCode(code)

                            .sunrise(
                                    atText(daily.path("sunrise"), i))

                            .sunset(
                                    atText(daily.path("sunset"), i))

                            .description(description(code))

                            .build();

            result.add(response);
        }

        return result;
    }

    // 3. Weather alerts

    public List<WeatherAlertResponse> getAlerts(
            Long farmId, Long ownerId) {

        Farm farm = getFarmWithCoordinates(farmId, ownerId);

        JsonNode daily = fetchWeather(farm).path("daily");

        JsonNode dates = daily.path("time");

        List<WeatherAlertResponse> alerts = new ArrayList<>();

        for (int i = 0; i < dates.size(); i++) {

            String date = atText(dates, i);

            Double rain = atDouble(
                    daily.path("precipitation_sum"), i);

            Double maxTemp = atDouble(
                    daily.path("temperature_2m_max"), i);

            Integer probability = atInt(
                    daily.path("precipitation_probability_max"), i);

            Integer code = atInt(
                    daily.path("weather_code"), i);

            // Heavy rainfall alert

            if (rain != null && rain >= 20.0) {

                alerts.add(alert(
                        farm,
                        "HEAVY_RAIN",
                        "WARNING",
                        "Heavy rainfall expected",
                        "Forecast rainfall is about " + rain
                                + " mm. Check drainage and avoid unnecessary irrigation.",
                        date
                ));
            }

            // High temperature alert

            if (maxTemp != null && maxTemp >= 38.0) {

                alerts.add(alert(
                        farm,
                        "HIGH_TEMPERATURE",
                        "WARNING",
                        "High temperature expected",
                        "Maximum temperature may reach "
                                + maxTemp
                                + " °C. Monitor crops and soil moisture.",
                        date
                ));
            }

            // Rain probability alert

            if (probability != null
                    && probability >= 70
                    && rain != null
                    && rain >= 5.0) {

                alerts.add(alert(
                        farm,
                        "RAIN_EXPECTED",
                        "INFO",
                        "Rain likely",
                        "There is a " + probability
                                + "% chance of precipitation. Review planned irrigation.",
                        date
                ));
            }

            // Thunderstorm alert

            if (code != null
                    && (code == 95 || code == 96 || code == 99)) {

                alerts.add(alert(
                        farm,
                        "THUNDERSTORM",
                        "WARNING",
                        "Thunderstorm forecast",
                        "Thunderstorm conditions are forecast. Avoid field work during lightning.",
                        date
                ));
            }
        }

        return alerts;
    }

    // 4. Build alert response

    private WeatherAlertResponse alert(
            Farm farm,
            String type,
            String severity,
            String title,
            String message,
            String date) {

        return WeatherAlertResponse.builder()
                .farmId(farm.getId())
                .farmName(farm.getName())
                .alertType(type)
                .severity(severity)
                .title(title)
                .message(message)
                .expectedDate(date)
                .build();
    }

    // 5. Call Open-Meteo API

    private JsonNode fetchWeather(Farm farm) {

        try {

            JsonNode response = openMeteoRestClient.get()

                    .uri(uri -> uri
                            .path("/v1/forecast")

                            .queryParam(
                                    "latitude",
                                    farm.getLatitude())

                            .queryParam(
                                    "longitude",
                                    farm.getLongitude())

                            .queryParam(
                                    "current",
                                    "temperature_2m,relative_humidity_2m,"
                                            + "apparent_temperature,is_day,"
                                            + "precipitation,rain,weather_code,"
                                            + "wind_speed_10m")

                            .queryParam(
                                    "daily",
                                    "weather_code,temperature_2m_max,"
                                            + "temperature_2m_min,"
                                            + "precipitation_sum,"
                                            + "precipitation_probability_max,"
                                            + "sunrise,sunset")

                            .queryParam("forecast_days", 7)

                            .queryParam("timezone", "auto")

                            .build())

                    .retrieve()

                    .body(JsonNode.class);

            if (response == null
                    || response.path("current").isMissingNode()) {

                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Weather provider returned an empty response"
                );
            }

            return response;
            
        } catch (RestClientException ex) {
                
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Weather service is temporarily unavailable. Please try again later.",
                        ex
                        );
                }
    }

    // 6. Check farm ownership and coordinates

    private Farm getFarmWithCoordinates(
            Long farmId, Long ownerId) {

        Farm farm = farmRepository
                .findByIdAndOwnerId(farmId, ownerId)

                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found"
                ));

        if (farm.getLatitude() == null
                || farm.getLongitude() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Add latitude and longitude to this farm before requesting weather information"
            );
        }

        return farm;
    }

    // 7. Weather code descriptions

    private String description(Integer code) {

        if (code == null) {
            return "Unknown conditions";
        }

        return switch (code) {

            case 0 -> "Clear sky";

            case 1, 2 -> "Mainly clear or partly cloudy";

            case 3 -> "Overcast";

            case 45, 48 -> "Fog";

            case 51, 53, 55 -> "Drizzle";

            case 56, 57 -> "Freezing drizzle";

            case 61, 63, 65 -> "Rain";

            case 66, 67 -> "Freezing rain";

            case 71, 73, 75, 77 -> "Snow";

            case 80, 81, 82 -> "Rain showers";

            case 85, 86 -> "Snow showers";

            case 95, 96, 99 -> "Thunderstorm";

            default -> "Variable conditions";
        };
    }

    // 8. JSON helper methods

    private Double number(JsonNode node, String key) {

        return node.path(key).isNumber()
                ? node.path(key).asDouble()
                : null;
    }

    private Integer integer(JsonNode node, String key) {

        return node.path(key).isNumber()
                ? node.path(key).asInt()
                : null;
    }

    private Boolean booleanValue(JsonNode node, String key) {

        return node.path(key).isNumber()
                ? node.path(key).asInt() == 1
                : null;
    }

    private String text(JsonNode node, String key) {

        return node.path(key).isMissingNode()
                ? null
                : node.path(key).asText();
    }

    private Double atDouble(JsonNode array, int i) {

        return array.size() > i && array.get(i).isNumber()
                ? array.get(i).asDouble()
                : null;
    }

    private Integer atInt(JsonNode array, int i) {

        return array.size() > i && array.get(i).isNumber()
                ? array.get(i).asInt()
                : null;
    }

    private String atText(JsonNode array, int i) {

        return array.size() > i && !array.get(i).isNull()
                ? array.get(i).asText()
                : null;
    }
}