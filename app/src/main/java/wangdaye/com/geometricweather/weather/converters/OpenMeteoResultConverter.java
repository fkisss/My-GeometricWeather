package wangdaye.com.geometricweather.weather.converters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoAirQualityResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoForecastResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;

/**
 * Adapts the keyless Open-Meteo responses into the OpenWeather OneCall DTOs.
 *
 * Reusing {@link OwmResultConverter} keeps the whole mapping into the app's
 * Weather model (Base/Current/Daily/Hourly/Alerts/AirQuality) untouched, so
 * this source behaves exactly like a working OWM source without an API key.
 */
public class OpenMeteoResultConverter {

    private static final String PATTERN_MINUTE = "yyyy-MM-dd'T'HH:mm";
    private static final String PATTERN_DAY = "yyyy-MM-dd";

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public static OwmOneCallResult convertForecast(OpenMeteoForecastResult src, boolean chinese) {
        if (src == null || src.current == null || src.hourly == null || src.daily == null) {
            return null;
        }

        final int offset = src.utc_offset_seconds;
        final List<String> hourTimes = src.hourly.time;
        final List<String> dayTimes = src.daily.time;
        final int hourSize = hourTimes == null ? 0 : hourTimes.size();
        final int daySize = dayTimes == null ? 0 : dayTimes.size();

        OwmOneCallResult result = new OwmOneCallResult();
        result.lat = src.latitude;
        result.lon = src.longitude;
        result.timezone = src.timezone;
        result.timezoneOffset = offset;

        // --- current ---
        long sunrise = epoch(get(src.daily.sunrise, 0), offset, PATTERN_MINUTE);
        long sunset = epoch(get(src.daily.sunset, 0), offset, PATTERN_MINUTE);
        int hourIndex = currentHourIndex(hourTimes, src.current.time);

        OwmOneCallResult.Current current = new OwmOneCallResult.Current();
        current.dt = epoch(src.current.time, offset, PATTERN_MINUTE);
        current.sunrise = sunrise;
        current.sunset = sunset;
        current.temp = nz(src.current.temperature_2m);
        current.feelsLike = nz(src.current.apparent_temperature);
        current.pressure = (int) nz(src.current.surface_pressure);
        current.humidity = integer(src.current.relative_humidity_2m);
        current.dewPoint = dbl(src.hourly.dew_point_2m, hourIndex);
        current.uvi = dbl(src.hourly.uv_index, hourIndex);
        current.clouds = integer(src.current.cloud_cover);
        current.visibility = (int) dbl(src.hourly.visibility, hourIndex);
        current.windSpeed = (float) nz(src.current.wind_speed_10m);
        current.windDeg = integer(src.current.wind_direction_10m);
        current.weather = Collections.singletonList(
                buildWeather(integer(src.current.weather_code), chinese));
        current.rain = precipitation(nz(src.current.rain));
        // Open-Meteo reports snowfall in centimetres, OWM expects millimetres.
        current.snow = precipitation(nz(src.current.snowfall) * 10d);
        result.current = current;

        // --- hourly ---
        result.hourly = new ArrayList<>(hourSize);
        for (int i = 0; i < hourSize; i++) {
            OwmOneCallResult.Hourly hourly = new OwmOneCallResult.Hourly();
            hourly.dt = epoch(get(hourTimes, i), offset, PATTERN_MINUTE);
            hourly.temp = dbl(src.hourly.temperature_2m, i);
            hourly.feelsLike = dbl(src.hourly.apparent_temperature, i);
            hourly.pressure = (int) dbl(src.hourly.surface_pressure, i);
            hourly.humidity = integer(src.hourly.relative_humidity_2m, i);
            hourly.dewPoint = dbl(src.hourly.dew_point_2m, i);
            hourly.uvi = dbl(src.hourly.uv_index, i);
            hourly.clouds = integer(src.hourly.cloud_cover, i);
            hourly.visibility = (int) dbl(src.hourly.visibility, i);
            hourly.windSpeed = (float) dbl(src.hourly.wind_speed_10m, i);
            hourly.windDeg = integer(src.hourly.wind_direction_10m, i);
            hourly.weather = Collections.singletonList(
                    buildWeather(integer(src.hourly.weather_code, i), chinese));
            hourly.pop = integer(src.hourly.precipitation_probability, i) / 100f;
            hourly.rain = precipitation(dbl(src.hourly.rain, i));
            hourly.snow = precipitation(dbl(src.hourly.snowfall, i) * 10d);
            result.hourly.add(hourly);
        }

        // --- daily ---
        result.daily = new ArrayList<>(daySize);
        for (int i = 0; i < daySize; i++) {
            OwmOneCallResult.Daily daily = new OwmOneCallResult.Daily();
            daily.dt = epoch(get(dayTimes, i), offset, PATTERN_DAY);
            daily.sunrise = epoch(get(src.daily.sunrise, i), offset, PATTERN_MINUTE);
            daily.sunset = epoch(get(src.daily.sunset, i), offset, PATTERN_MINUTE);

            OwmOneCallResult.Daily.Temp temp = new OwmOneCallResult.Daily.Temp();
            temp.day = dbl(src.daily.temperature_2m_max, i);
            temp.night = dbl(src.daily.temperature_2m_min, i);
            temp.min = temp.night;
            temp.max = temp.day;
            temp.morn = temp.night;
            temp.eve = temp.day;
            daily.temp = temp;

            OwmOneCallResult.Daily.FeelsLike feelsLike = new OwmOneCallResult.Daily.FeelsLike();
            feelsLike.day = dbl(src.daily.apparent_temperature_max, i);
            feelsLike.night = dbl(src.daily.apparent_temperature_min, i);
            feelsLike.morn = feelsLike.night;
            feelsLike.eve = feelsLike.day;
            daily.feelsLike = feelsLike;

            daily.pressure = 0;
            daily.humidity = 0;
            daily.dewPoint = 0;
            daily.clouds = 0;
            daily.windSpeed = (float) dbl(src.daily.wind_speed_10m_max, i);
            daily.windDeg = integer(src.daily.wind_direction_10m_dominant, i);
            daily.weather = Collections.singletonList(
                    buildWeather(integer(src.daily.weather_code, i), chinese));
            daily.pop = integer(src.daily.precipitation_probability_max, i) / 100f;
            daily.rain = (float) dbl(src.daily.rain_sum, i);
            daily.snow = (float) (dbl(src.daily.snowfall_sum, i) * 10d);
            daily.uvi = dbl(src.daily.uv_index_max, i);
            result.daily.add(daily);
        }

        // Minutely precipitation and official alerts are not provided by Open-Meteo.
        result.minutely = null;
        result.alerts = null;
        return result;
    }

    public static OwmAirPollutionResult convertAirQuality(OpenMeteoAirQualityResult src) {
        if (src == null || src.hourly == null
                || src.hourly.time == null || src.hourly.time.isEmpty()) {
            return null;
        }

        OwmAirPollutionResult result = new OwmAirPollutionResult();
        result.list = new ArrayList<>();
        final int size = src.hourly.time.size();

        for (int i = 0; i < size; i++) {
            OwmAirPollutionResult.AirPollution pollution =
                    new OwmAirPollutionResult.AirPollution();
            pollution.dt = epoch(get(src.hourly.time, i), src.utc_offset_seconds, PATTERN_MINUTE);

            OwmAirPollutionResult.AirPollution.Main main =
                    new OwmAirPollutionResult.AirPollution.Main();
            Double aqi = get(src.hourly.us_aqi, i);
            main.aqi = aqi == null ? 0 : aqi.intValue();
            pollution.main = main;

            OwmAirPollutionResult.AirPollution.Components components =
                    new OwmAirPollutionResult.AirPollution.Components();
            components.pm2_5 = dbl(src.hourly.pm2_5, i);
            components.pm10 = dbl(src.hourly.pm10, i);
            components.so2 = dbl(src.hourly.sulphur_dioxide, i);
            components.no2 = dbl(src.hourly.nitrogen_dioxide, i);
            components.o3 = dbl(src.hourly.ozone, i);
            components.co = dbl(src.hourly.carbon_monoxide, i);
            components.nh3 = dbl(src.hourly.ammonia, i);
            components.no = 0;
            pollution.components = components;

            result.list.add(pollution);
        }

        // OwmResultConverter reads list.get(0) as the "current" AQI, so put the
        // sample closest to now first while keeping the rest for daily matching.
        long now = System.currentTimeMillis() / 1000L;
        int closest = 0;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 0; i < result.list.size(); i++) {
            long distance = Math.abs(result.list.get(i).dt - now);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = i;
            }
        }
        if (closest > 0) {
            Collections.swap(result.list, 0, closest);
        }
        return result;
    }

    public static List<OwmLocationResult> convertLocations(OpenMeteoLocationResult src) {
        List<OwmLocationResult> list = new ArrayList<>();
        if (src == null || src.results == null) {
            return list;
        }
        for (OpenMeteoLocationResult.Result r : src.results) {
            if (r == null || r.name == null) {
                continue;
            }
            OwmLocationResult location = new OwmLocationResult();
            location.name = r.admin1 == null || r.admin1.isEmpty()
                    ? r.name
                    : r.admin1 + " · " + r.name;
            location.lat = r.latitude;
            location.lon = r.longitude;
            location.country = r.country_code == null ? "" : r.country_code;
            location.timezone = r.timezone;
            list.add(location);
        }
        return list;
    }

    public static OwmLocationResult convertLocation(double latitude, double longitude) {
        OwmLocationResult location = new OwmLocationResult();
        location.name = String.format(Locale.US, "%.3f, %.3f", latitude, longitude);
        location.lat = latitude;
        location.lon = longitude;
        location.country = "";
        location.timezone = TimeZone.getDefault().getID();
        return location;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static OwmOneCallResult.Weather buildWeather(int wmoCode, boolean chinese) {
        OwmOneCallResult.Weather weather = new OwmOneCallResult.Weather();
        weather.id = wmoToOwmId(wmoCode);
        weather.main = weatherText(wmoCode, chinese);
        weather.description = weather.main;
        weather.icon = "";
        return weather;
    }

    private static OwmOneCallResult.Precipitation precipitation(double millimetres) {
        OwmOneCallResult.Precipitation p = new OwmOneCallResult.Precipitation();
        p.cumul1h = (float) millimetres;
        return p;
    }

    /** ISO-8601 local time (timezone=auto) -> real epoch seconds. */
    private static long epoch(String localIso, int utcOffsetSeconds, String pattern) {
        if (localIso == null || localIso.length() == 0) {
            return 0L;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = format.parse(localIso);
            return date == null ? 0L : date.getTime() / 1000L - utcOffsetSeconds;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static int currentHourIndex(List<String> times, String currentTime) {
        if (times == null || times.isEmpty() || currentTime == null || currentTime.length() < 13) {
            return 0;
        }
        String prefix = currentTime.substring(0, 13);
        for (int i = times.size() - 1; i >= 0; i--) {
            String time = times.get(i);
            if (time != null && time.startsWith(prefix)) {
                return i;
            }
        }
        return 0;
    }

    private static <T> T get(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    private static double dbl(List<Double> list, int index) {
        Double value = get(list, index);
        return value == null ? 0d : value;
    }

    private static int integer(List<Integer> list, int index) {
        Integer value = get(list, index);
        return value == null ? 0 : value;
    }

    private static int integer(Integer value) {
        return value == null ? 0 : value;
    }

    private static double nz(Double value) {
        return value == null ? 0d : value;
    }

    /** WMO weather interpretation code -> OpenWeather condition id. */
    private static int wmoToOwmId(int code) {
        switch (code) {
            case 0: return 800;
            case 1: return 801;
            case 2: return 802;
            case 3: return 804;
            case 45: case 48: return 741;
            case 51: case 53: case 55: return 300;
            case 56: case 57: return 511;
            case 61: return 500;
            case 63: return 501;
            case 65: return 502;
            case 66: case 67: return 511;
            case 71: return 600;
            case 73: return 601;
            case 75: return 602;
            case 77: return 600;
            case 80: return 500;
            case 81: return 501;
            case 82: return 502;
            case 85: return 600;
            case 86: return 602;
            case 95: return 200;
            case 96: case 99: return 202;
            default: return 803;
        }
    }

    private static String weatherText(int code, boolean chinese) {
        switch (code) {
            case 0:  return chinese ? "晴" : "Clear sky";
            case 1:  return chinese ? "大部晴朗" : "Mainly clear";
            case 2:  return chinese ? "局部多云" : "Partly cloudy";
            case 3:  return chinese ? "阴" : "Overcast";
            case 45: return chinese ? "雾" : "Fog";
            case 48: return chinese ? "雾凇" : "Depositing rime fog";
            case 51: return chinese ? "小毛毛雨" : "Light drizzle";
            case 53: return chinese ? "毛毛雨" : "Drizzle";
            case 55: return chinese ? "强毛毛雨" : "Dense drizzle";
            case 56: return chinese ? "小冻雨" : "Light freezing drizzle";
            case 57: return chinese ? "强冻雨" : "Dense freezing drizzle";
            case 61: return chinese ? "小雨" : "Slight rain";
            case 63: return chinese ? "中雨" : "Moderate rain";
            case 65: return chinese ? "大雨" : "Heavy rain";
            case 66: return chinese ? "冻雨" : "Light freezing rain";
            case 67: return chinese ? "强冻雨" : "Heavy freezing rain";
            case 71: return chinese ? "小雪" : "Slight snowfall";
            case 73: return chinese ? "中雪" : "Moderate snowfall";
            case 75: return chinese ? "大雪" : "Heavy snowfall";
            case 77: return chinese ? "米雪" : "Snow grains";
            case 80: return chinese ? "小阵雨" : "Slight rain showers";
            case 81: return chinese ? "阵雨" : "Moderate rain showers";
            case 82: return chinese ? "强阵雨" : "Violent rain showers";
            case 85: return chinese ? "小阵雪" : "Slight snow showers";
            case 86: return chinese ? "强阵雪" : "Heavy snow showers";
            case 95: return chinese ? "雷阵雨" : "Thunderstorm";
            case 96: return chinese ? "雷阵雨伴冰雹" : "Thunderstorm with slight hail";
            case 99: return chinese ? "雷阵雨伴大冰雹" : "Thunderstorm with heavy hail";
            default: return chinese ? "多云" : "Cloudy";
        }
    }
}
