package wangdaye.com.geometricweather.weather.json.owm;

/**
 * OpenWeather location result.
 * */

public class OwmLocationResult {
    public String name;
    // local_names
    public double lat;
    public double lon;
    public String country;
    // Filled by the Open-Meteo geocoding adapter (IANA id, e.g. Asia/Shanghai).
    public String timezone;
}
