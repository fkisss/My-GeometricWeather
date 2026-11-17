package wangdaye.com.geometricweather.weather.json.openmeteo;

import java.util.List;

/**
 * Open-Meteo /v1/forecast result (timeformat=unixtime, timezone=auto).
 *
 * All time fields are real unix timestamps in seconds (UTC), so no ISO parsing
 * or manual utc-offset arithmetic is needed anywhere downstream.
 *
 * Every numeric field is deliberately declared as Double/List<Double>: Open-Meteo
 * serializes some nominally integral series (e.g. visibility) with a decimal
 * point, and Gson would throw a JsonSyntaxException when feeding a fractional
 * number into an Integer field - which killed the whole request.
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
        public Long time;
        public Double temperature_2m;
        public Double apparent_temperature;
        public Double relative_humidity_2m;
        public Double is_day;
        public Double precipitation;
        public Double rain;
        public Double showers;
        public Double snowfall;
        public Double weather_code;
        public Double cloud_cover;
        public Double pressure_msl;
        public Double wind_speed_10m;
        public Double wind_direction_10m;
        public Double wind_gusts_10m;
    }

    public static class Hourly {
        public List<Long> time;
        public List<Double> temperature_2m;
        public List<Double> apparent_temperature;
        public List<Double> relative_humidity_2m;
        public List<Double> dew_point_2m;
        public List<Double> precipitation_probability;
        public List<Double> precipitation;
        public List<Double> rain;
        public List<Double> showers;
        public List<Double> snowfall;
        public List<Double> weather_code;
        public List<Double> visibility;
        public List<Double> wind_speed_10m;
        public List<Double> wind_direction_10m;
        public List<Double> uv_index;
        public List<Double> is_day;
        public List<Double> cloud_cover;
        public List<Double> pressure_msl;
    }

    public static class Daily {
        public List<Long> time;
        public List<Double> weather_code;
        public List<Double> temperature_2m_max;
        public List<Double> temperature_2m_min;
        public List<Double> apparent_temperature_max;
        public List<Double> apparent_temperature_min;
        public List<Long> sunrise;
        public List<Long> sunset;
        public List<Double> uv_index_max;
        public List<Double> precipitation_sum;
        public List<Double> rain_sum;
        public List<Double> showers_sum;
        public List<Double> snowfall_sum;
        public List<Double> precipitation_probability_max;
        public List<Double> wind_speed_10m_max;
        public List<Double> wind_direction_10m_dominant;
        // daily aggregates used to fill fields the OWM OneCall provides.
        public List<Double> pressure_msl_mean;
        public List<Double> relative_humidity_2m_mean;
        public List<Double> dew_point_2m_mean;
        public List<Double> cloud_cover_mean;
    }
}
