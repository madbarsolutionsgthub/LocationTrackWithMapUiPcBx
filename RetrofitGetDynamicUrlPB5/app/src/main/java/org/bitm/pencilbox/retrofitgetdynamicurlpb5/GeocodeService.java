package org.bitm.pencilbox.retrofitgetdynamicurlpb5;

import org.bitm.pencilbox.retrofitgetdynamicurlpb5.geocode.GeocodeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Mobile App on 6/5/2018.
 */

public interface GeocodeService {
    @GET
    Call<GeocodeResponse>getAddress(@Url String endUrl);
}
