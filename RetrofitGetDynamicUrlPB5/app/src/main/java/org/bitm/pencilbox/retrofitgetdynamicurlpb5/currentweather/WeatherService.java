package org.bitm.pencilbox.retrofitgetdynamicurlpb5.currentweather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Mobile App on 6/5/2018.
 */

public interface WeatherService {
    @GET()
    Call<CurrentWeatherResponse>getCurrentWeatherData(@Url String endUrl);
}
