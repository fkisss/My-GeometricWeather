package wangdaye.com.geometricweather.weather.json.openmeteo;

import java.util.List;

/**
 * Open-Meteo /v1/forecast result.
 *
 * Timestamps are ISO-8601 *local* strings (no timezone suffix) because the
 * request uses timezone=auto. utc_offset_seconds is used by the converter to
 * turn them into real epoch seconds.
 */
public class OpenMeteoForecastResult {

    public double latitude;
    public double longitude;
    public String timezone;
    public String timezone_abbreviation;
    public double elevation;
    public int utc_offset_seconds;

    public Current current;
    public Hourly hourly;
    public Daily daily;

    public static class Current {
        public String time;
        public int interval;
        public Double temperature_2m;
        public Double apparent_temperature;
        public Integer relative_humidity_2m;
        public Integer is_day;
        public Double precipitation;
        public Double rain;
        public Double showers;
        public Double snowfall;
        public Integer weather_code;
        public Integer cloud_cover;
        public Double surface_pressure;
        public Double wind_speed_10m;
        public Integer wind_direction_10m;
        public Double wind_gusts_10m;
    }

    public static class Hourly {
        public List<String> time;
        public List<Double> temperature_2m;
        public List<Double> apparent_temperature;
        public List<Integer> relative_humidity_2m;
        public List<Double> dew_point_2m;
        public List<Integer> precipitation_probability;
        public List<Double> precipitation;
        public List<Double> rain;
        public List<Double> showers;
        public List<Double> snowfall;
        public List<Integer> weather_code;
        public List<Double> visibility;
        public List<Double> wind_speed_10m;
        public List<Integer> wind_direction_10m;
        public List<Double> uv_index;
        public List<Integer> is_day;
        public List<Integer> cloud_cover;
        public List<Double> surface_pressure;
    }

    public static class Daily {
        public List<String> time;
        public List<Integer> weather_code;
        public List<Double> temperature_2m_max;
        public List<Double> temperature_2m_min;
        public List<Double> apparent_temperature_max;
        public List<Double> apparent_temperature_min;
        public List<String> sunrise;
        public List<String> sunset;
        public List<Double> uv_index_max;
        public List<Double> precipitation_sum;
        public List<Double> rain_sum;
        public List<Double> showers_sum;
        public List<Double> snowfall_sum;
        public List<Integer> precipitation_probability_max;
        public List<Double> wind_speed_10m_max;
        public List<Integer> wind_direction_10m_dominant;
    }
}
