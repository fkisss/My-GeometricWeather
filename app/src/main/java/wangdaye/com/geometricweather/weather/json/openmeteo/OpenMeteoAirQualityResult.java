package wangdaye.com.geometricweather.weather.json.openmeteo;

import java.util.List;

/**
 * Open-Meteo /v1/air-quality result (CAMS model, timeformat=unixtime).
 * All pollutant values are in micrograms per cubic metre, the same unit used by
 * the original OpenWeather air-pollution DTO.
 */
public class OpenMeteoAirQualityResult {

    public double latitude;
    public double longitude;
    public String timezone;
    public int utc_offset_seconds;

    public Hourly hourly;

    public static class Hourly {
        public List<Long> time;
        public List<Double> pm10;
        public List<Double> pm2_5;
        public List<Double> carbon_monoxide;
        public List<Double> nitrogen_dioxide;
        public List<Double> sulphur_dioxide;
        public List<Double> ozone;
        public List<Double> ammonia;
        public List<Double> us_aqi;
    }
}
