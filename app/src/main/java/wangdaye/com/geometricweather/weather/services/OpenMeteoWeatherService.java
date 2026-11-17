package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.apis.OpenMeteoApi;
import wangdaye.com.geometricweather.weather.converters.OpenMeteoResultConverter;
import wangdaye.com.geometricweather.weather.converters.OwmResultConverter;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoAirQualityResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoForecastResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;

/**
 * Open-Meteo weather service.
 *
 * Drop-in replacement for the dead OpenWeatherMap / AccuWeather sources.
 * No API key, no quota, worldwide coverage, and it also provides air quality.
 * The response is normalised into the existing OpenWeather DTOs so the well
 * tested OwmResultConverter can build the app's Weather model unchanged.
 */
public class OpenMeteoWeatherService extends WeatherService {

    private static final int FORECAST_DAYS = 7;
    private static final String TIMEZONE = "auto";
    // Same trick as Breezy Weather: let the API return real unix timestamps
    // instead of timezone-aware ISO strings, so no offset arithmetic is needed.
    private static final String TIMEFORMAT = "unixtime";
    private static final String WIND_UNIT = "ms"; // m/s, same as OWM metric

    private static final String CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,"
                    + "precipitation,rain,showers,snowfall,weather_code,cloud_cover,"
                    + "pressure_msl,wind_speed_10m,wind_direction_10m,wind_gusts_10m";

    private static final String HOURLY_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,"
                    + "precipitation_probability,precipitation,rain,showers,snowfall,"
                    + "weather_code,visibility,wind_speed_10m,wind_direction_10m,"
                    + "uv_index,is_day,dew_point_2m,cloud_cover,pressure_msl";

    private static final String DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,"
                    + "apparent_temperature_max,apparent_temperature_min,sunrise,sunset,"
                    + "uv_index_max,precipitation_sum,rain_sum,showers_sum,snowfall_sum,"
                    + "precipitation_probability_max,wind_speed_10m_max,"
                    + "wind_direction_10m_dominant,"
                    + "pressure_msl_mean,relative_humidity_2m_mean,"
                    + "dew_point_2m_mean,cloud_cover_mean";

    private static final String AIR_QUALITY_FIELDS =
            "pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,"
                    + "ammonia,us_aqi";

    private static class EmptyAqiResult extends OpenMeteoAirQualityResult {
    }

    private final OpenMeteoApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public OpenMeteoWeatherService(OpenMeteoApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location,
                               @NonNull RequestWeatherCallback callback) {
        final boolean chinese = "zh".equals(Locale.getDefault().getLanguage());
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        Observable<OpenMeteoForecastResult> forecast = mApi.getForecast(
                BuildConfig.OPEN_METEO_FORECAST_URL,
                latitude, longitude,
                CURRENT_FIELDS, HOURLY_FIELDS, DAILY_FIELDS,
                TIMEZONE, TIMEFORMAT, FORECAST_DAYS, WIND_UNIT
        );

        // Air quality is optional: a failure must never break the weather request.
        Observable<OpenMeteoAirQualityResult> airQuality = mApi.getAirQuality(
                BuildConfig.OPEN_METEO_AIR_QUALITY_URL,
                latitude, longitude,
                AIR_QUALITY_FIELDS, TIMEZONE, TIMEFORMAT, FORECAST_DAYS
        ).onExceptionResumeNext(
                Observable.<OpenMeteoAirQualityResult>just(new EmptyAqiResult())
        );

        Observable.zip(forecast, airQuality,
                (forecastResult, airQualityResult) -> {
                    OwmOneCallResult oneCall = OpenMeteoResultConverter.convertForecast(
                            forecastResult, chinese);
                    OwmAirPollutionResult pollution =
                            OpenMeteoResultConverter.convertAirQuality(airQualityResult);
                    if (oneCall == null) {
                        return new WeatherResultWrapper(null);
                    }
                    return OwmResultConverter.convert(
                            context, location, oneCall, pollution, pollution);
                })
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable,
                        new BaseObserver<WeatherResultWrapper>() {
                            @Override
                            public void onSucceed(WeatherResultWrapper wrapper) {
                                if (wrapper.result != null) {
                                    callback.requestWeatherSuccess(
                                            Location.copy(location, wrapper.result));
                                } else {
                                    onFailed();
                                }
                            }

                            @Override
                            public void onFailed() {
                                callback.requestWeatherFailed(location);
                            }
                        }));
    }

    @Override
    @NonNull
    public List<Location> requestLocation(Context context, String query) {
        OpenMeteoLocationResult result = null;
        try {
            result = mApi.callLocations(
                    BuildConfig.OPEN_METEO_GEOCODING_URL,
                    query, 20, languageCode(), "json"
            ).execute().body();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }

        List<Location> locationList = new ArrayList<>();
        for (OwmLocationResult r : OpenMeteoResultConverter.convertLocations(result)) {
            locationList.add(OwmResultConverter.convert(null, r, null));
        }
        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {
        // Open-Meteo has no reverse geocoding endpoint. Keep the GPS position
        // and let the forecast API resolve the correct timezone for us.
        String query = location.getLatitude() + "," + location.getLongitude();
        List<Location> locationList = new ArrayList<>();
        try {
            locationList.add(OwmResultConverter.convert(
                    location,
                    OpenMeteoResultConverter.convertLocation(
                            location.getLatitude(), location.getLongitude()),
                    null
            ));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if (locationList.isEmpty()) {
            callback.requestLocationFailed(query);
        } else {
            callback.requestLocationSuccess(query, locationList);
        }
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        mApi.getLocations(
                BuildConfig.OPEN_METEO_GEOCODING_URL,
                query, 20, languageCode(), "json"
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable,
                        new BaseObserver<OpenMeteoLocationResult>() {
                            @Override
                            public void onSucceed(OpenMeteoLocationResult result) {
                                List<Location> locationList = new ArrayList<>();
                                for (OwmLocationResult r :
                                        OpenMeteoResultConverter.convertLocations(result)) {
                                    locationList.add(
                                            OwmResultConverter.convert(null, r, null));
                                }
                                if (locationList.isEmpty()) {
                                    callback.requestLocationFailed(query);
                                } else {
                                    callback.requestLocationSuccess(query, locationList);
                                }
                            }

                            @Override
                            public void onFailed() {
                                callback.requestLocationFailed(query);
                            }
                        }));
    }

    @Override
    public void cancel() {
        mCompositeDisposable.clear();
    }

    private static String languageCode() {
        String language = Locale.getDefault().getLanguage();
        return language == null || language.isEmpty() ? "en" : language;
    }
}
