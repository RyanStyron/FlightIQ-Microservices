package New_Foreflight.Weather.controller;

import New_Foreflight.Weather.dto.AirportWeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import New_Foreflight.Weather.service.WeatherService;

@RestController
@RequestMapping("/api/v1")
public class WeatherServiceController {

    @Autowired
    WeatherService weatherService;

    @GetMapping(value = "/getAirportWeather")
    public ResponseEntity<AirportWeatherResponse> getAirportWeather(@RequestParam String airportCode) {
        try {
            return ResponseEntity.ok(weatherService.getAirportWeather(airportCode));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/getWindsAloft")
    public ResponseEntity<String> getWindsAloft(@RequestParam String airportCode, @RequestParam int altitude) {
        try {
            return ResponseEntity.ok(weatherService.getWindsAloft(airportCode, altitude));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
