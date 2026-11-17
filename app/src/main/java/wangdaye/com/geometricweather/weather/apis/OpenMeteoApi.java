package wangdaye.com.geometricweather.weather.apis;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoAirQualityResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoForecastResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResult;

/**
 * Open-Meteo API.
 *
 * Free, keyless and worldwide weather API (the same default source used by
 * Breezy Weather). All endpoints are passed with @Url because the forecast,
 * air-quality and geocoding services live on three different hosts.
 */
public interface OpenMeteoApi {

    @GET
    Observable<OpenMeteoForecastResult> getForecast(
            @Url String url,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current,
            @Query("hourly") String hourly,
            @Query("daily") String daily,
            @Query("timezone") String timezone,
            @Query("timeformat") String timeformat,
            @Query("forecast_days") int forecastDays,
            @Query("wind_speed_unit") String windSpeedUnit
    );

    @GET
    Observable<OpenMeteoAirQualityResult> getAirQuality(
            @Url String url,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("hourly") String hourly,
            @Query("timezone") String timezone,
            @Query("timeformat") String timeformat,
            @Query("forecast_days") int forecastDays
    );

    @GET
    Observable<OpenMeteoLocationResult> getLocations(
            @Url String url,
            @Query("name") String name,
            @Query("count") int count,
            @Query("language") String language,
            @Query("format") String format
    );

    @GET
    Call<OpenMeteoLocationResult> callLocations(
            @Url String url,
            @Query("name") String name,
            @Query("count") int count,
            @Query("language") String language,
            @Query("format") String format
    );
}
