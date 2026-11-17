package wangdaye.com.geometricweather.common.basic.models.options.provider

import android.content.Context
import androidx.annotation.ColorInt
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils
import wangdaye.com.geometricweather.common.basic.models.options._basic.VoiceEnum

enum class WeatherSource(
    override val id: String,
    @ColorInt val sourceColor: Int,
    val sourceUrl: String
): VoiceEnum {

    ACCU("accu", -0x10a7dd, "accuweather.com"),
    // "owm" is kept as the persisted id for backwards compatibility, but it now
    // resolves to the keyless Open-Meteo service (see WeatherServiceSet).
    OWM("owm", -0x1491b5, "open-meteo.com"),
    MF("mf", -0xffa76e, "meteofrance.com"),
    CAIYUN("caiyun", -0xa14472, " caiyunapp.com"),
    // Xiaomi weather gateway served China Weather Network + CaiYun data; same
    // service as CAIYUN but exposed as its own entry for discoverability.
    CHINA("china", -0xcc4a02, "weather.com.cn");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ): WeatherSource {
            if (value.lowercase() == "china") {
                return CHINA
            }
            if (value.lowercase().contains("owm")) {
                return OWM
            }
            if (value.lowercase().contains("mf")) {
                return MF
            }
            if (value.lowercase().contains("caiyun")
                || value.lowercase().contains("cn")) {
                return CAIYUN
            }
            return ACCU
        }
    }

    override val valueArrayId = R.array.weather_source_values
    override val nameArrayId = R.array.weather_sources
    override val voiceArrayId = R.array.weather_source_voices

    override fun getName(context: Context) = Utils.getName(context, this)
    override fun getVoice(context: Context) = Utils.getVoice(context, this)
}