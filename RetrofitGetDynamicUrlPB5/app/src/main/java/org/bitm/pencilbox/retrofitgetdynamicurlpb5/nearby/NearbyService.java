package org.bitm.pencilbox.retrofitgetdynamicurlpb5.nearby;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Mobile App on 6/9/2018.
 */

public interface NearbyService {
    @GET
    Call<NearbyPlaceResponses>getNearbyPlaces(@Url String endUrl);
}
