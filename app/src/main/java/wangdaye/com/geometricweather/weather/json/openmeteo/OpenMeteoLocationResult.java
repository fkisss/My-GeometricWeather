package wangdaye.com.geometricweather.weather.json.openmeteo;

import java.util.List;

/**
 * Open-Meteo /v1/search geocoding result.
 */
public class OpenMeteoLocationResult {

    public List<Result> results;
    public double generationtime_ms;

    public static class Result {
        public long id;
        public String name;
        public double latitude;
        public double longitude;
        public double elevation;
        public String feature_code;
        public String country_code;
        public String country;
        public String country_id;
        public String admin1;
        public String admin1_id;
        public String admin2;
        public String admin3;
        public String admin4;
        public String timezone;
        public int population;
        public List<String> postcodes;
    }
}
