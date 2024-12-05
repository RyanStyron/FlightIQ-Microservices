package New_Foreflight.Weather.Service;

import New_Foreflight.Weather.DTO.AirportWeatherResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class WeatherServiceImpl implements Weatherservice {

    @Value("${checkwx.api.url}")
    private String apiUrl;

    @Value("${checkwx.api.key}")
    private String apiKey;

    @Override
    public AirportWeatherResponse getAirportWeather(String icao) {

        String endpoint = apiUrl.replace("{station}", icao) + "?x-api-key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        String apiResponseJSON = restTemplate.getForObject(endpoint, String.class);


        return new AirportWeatherResponse(ParseRawMETARText(apiResponseJSON), SeperateMetarComponents(apiResponseJSON), "vfr");

    }

    @Override
    public String ParseRawMETARText(String apiResponse) {

        JSONObject json = new JSONObject(apiResponse);
        Object result = json.getJSONArray("data").getJSONObject(0).get("raw_text");
        return result.toString();
    }


    @Override
    public HashMap<String, Object> SeperateMetarComponents(String info) {
        JSONObject json = new JSONObject(info);
        JSONObject result = json.getJSONArray("data").getJSONObject(0);

        LinkedHashMap<String, Object> METARcomponents = new LinkedHashMap<>();

        String[] keyList = {
                "observed", "wind", "visibility", "clouds", "temperature", "dewpoint", "barometer", "humidity"
        };

        for (String key : keyList) {

            if (result.has("wind") && !result.isNull("wind")) {
                String windString = parseWinds(result.getJSONObject("wind"));
                METARcomponents.put("winds", windString);
            }


            if (key.equals("clouds") && result.has(key) && !result.isNull(key)) {
                // Extract clouds data with a helper function
                List<HashMap<String, String>> cloudsList = parseClouds(result.getJSONArray(key));
                METARcomponents.put(key, cloudsList);
            } else {
                String value = result.optString(key, "Not Available");
                METARcomponents.put(key, value);
            }
        }

        return METARcomponents;
    }

    /**
     * Helper functions to parse the fields data.
     */
    private List<HashMap<String, String>> parseClouds(JSONArray cloudsArray) {
        List<HashMap<String, String>> cloudsList = new ArrayList<>();

        for (int i = 0; i < cloudsArray.length(); i++) {
            JSONObject cloud = cloudsArray.getJSONObject(i);

            // Using LinkedHashMap to preserve the order of keys (code, then feet)
            LinkedHashMap<String, String> cloudMap = new LinkedHashMap<>();

            String code = cloud.optString("code", "Unknown");
            cloudMap.put("code", code); // Add code first

            // Only add "feet" if code is not CLR
            if (!"CLR".equalsIgnoreCase(code)) {
                String feet = cloud.optString("feet", "Unknown");
                cloudMap.put("feet", feet); // Add feet second if code is not CLR
            }

            cloudsList.add(cloudMap);
        }

        return cloudsList;
    }
    private String parseWinds(JSONObject windData) {
        // Extracting the necessary values from the JSON
        int direction = windData.optInt("degrees", 0);  // Default to 0 if not available
        int speedKts = windData.optInt("speed_kts", 0); // Default to 0 if not available
        int gustKts = windData.optInt("gust_kts", 0);   // Default to 0 if not available

        // Base wind string format
        StringBuilder windString = new StringBuilder();

        // Add the direction
        windString.append(direction);

        // Add the speed with gusts if present
        if (gustKts > 0) {
            windString.append(" at ").append(speedKts).append("-").append(gustKts).append(" kts");
        } else {
            windString.append(" at ").append(speedKts).append(" kts");
        }

        return windString.toString();
    }






}
