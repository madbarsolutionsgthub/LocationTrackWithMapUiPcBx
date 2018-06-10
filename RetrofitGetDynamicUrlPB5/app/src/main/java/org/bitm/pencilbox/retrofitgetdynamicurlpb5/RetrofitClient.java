package org.bitm.pencilbox.retrofitgetdynamicurlpb5;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mobile App on 6/5/2018.
 */

public final class RetrofitClient {
    public static Retrofit getClient(String base_url){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
