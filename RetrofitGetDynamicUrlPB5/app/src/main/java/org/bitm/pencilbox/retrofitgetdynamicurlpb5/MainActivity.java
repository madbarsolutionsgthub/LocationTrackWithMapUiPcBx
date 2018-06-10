package org.bitm.pencilbox.retrofitgetdynamicurlpb5;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.bitm.pencilbox.retrofitgetdynamicurlpb5.currentweather.CurrentWeatherResponse;
import org.bitm.pencilbox.retrofitgetdynamicurlpb5.currentweather.Main;
import org.bitm.pencilbox.retrofitgetdynamicurlpb5.currentweather.WeatherService;
import org.bitm.pencilbox.retrofitgetdynamicurlpb5.geocode.GeocodeResponse;
import org.bitm.pencilbox.retrofitgetdynamicurlpb5.nearby.NearbyPlaceResponses;
import org.bitm.pencilbox.retrofitgetdynamicurlpb5.nearby.NearbyService;
import org.bitm.pencilbox.retrofitgetdynamicurlpb5.nearby.Result;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static final String GEO_BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String TAG = MainActivity.class.getSimpleName();
    private WeatherService service;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String units = "metric";
    private FusedLocationProviderClient client;
    private TextView tv, latlngtv;
    private LocationCallback callback;
    private GoogleMapOptions options;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        geoDataClient = Places.getGeoDataClient(this);
        placeDetectionClient = Places.getPlaceDetectionClient(this);
        options = new GoogleMapOptions();
        options.zoomControlsEnabled(true);
        mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapContainer,mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this);
        if(checkLocationPermission()){
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        mapFragment.getMapAsync(MainActivity.this);
                        getAddressData(latitude,longitude);
                        //getData();
                    }
                }
            });
            callback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for(Location location : locationResult.getLocations()){
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        //getData();//weather data
                        //getAddressData();
                    }
                }
            };
            client.requestLocationUpdates(getLocationRequest(),callback,null);
        }
    }

    private void getAddressData(double latitude, double longitude) {
        GeocodeService service = RetrofitClient.getClient(GEO_BASE_URL).create(GeocodeService.class);

        String endUrl = String.format("geocode/json?latlng=%f,%f&key=%s",latitude,longitude,getString(R.string.geocode_api));
        service.getAddress(endUrl).enqueue(new Callback<GeocodeResponse>() {
            @Override
            public void onResponse(Call<GeocodeResponse> call, Response<GeocodeResponse> response) {
                if(response.code() == 200){
                    GeocodeResponse geocodeResponse = response.body();
                    String address = geocodeResponse.getResults().get(0).getFormattedAddress();
                    ((TextView)findViewById(R.id.addressTV)).setText(address);
                }
            }

            @Override
            public void onFailure(Call<GeocodeResponse> call, Throwable t) {

            }
        });
    }

    private LocationRequest getLocationRequest(){
        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(2000);
        return request;
    }

    private void getData() {

        service = RetrofitClient.getClient(BASE_URL).create(WeatherService.class);
        String endUrl = String.format("weather?lat=%f&lon=%f&units=%s&appid=%s",latitude,longitude,units,
                getString(R.string.weather_api));

        Call<CurrentWeatherResponse> call = service.getCurrentWeatherData(endUrl);
        call.enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if(response.code() == 200){
                    CurrentWeatherResponse currentWeatherResponse = response.body();
                    String city = currentWeatherResponse.getName();
                    double temp = currentWeatherResponse.getMain().getTemp();
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {

            }
        });
    }

    private boolean checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},111);
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        LatLng latLng = new LatLng(latitude,longitude);
        map.addMarker(new MarkerOptions().title("BDBL").position(latLng));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                map.clear();
                getAddressData(latLng.latitude,latLng.longitude);
                map.addMarker(new MarkerOptions().position(latLng));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.current_places:
                getCurrentPlaces();
                break;
            case R.id.nearby_places:
                getNearbyPlaces();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getNearbyPlaces() {
        NearbyService nearbyService = RetrofitClient.getClient(GEO_BASE_URL).create(NearbyService.class);
        String endUrl = String.format("place/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",latitude,longitude,500,"atm",getString(R.string.nearby_places_api));
        nearbyService.getNearbyPlaces(endUrl).enqueue(new Callback<NearbyPlaceResponses>() {
            @Override
            public void onResponse(Call<NearbyPlaceResponses> call, Response<NearbyPlaceResponses> response) {
                if(response.code() == 200){
                    map.clear();
                    NearbyPlaceResponses responses = response.body();
                    List<Result>results = responses.getResults();
                    for(Result r : results){
                        double lat = r.getGeometry().getLocation().getLat();
                        double lng = r.getGeometry().getLocation().getLng();
                        LatLng latLng = new LatLng(lat,lng);
                        String name = r.getName();
                        String address = r.getVicinity();

                        map.addMarker(new MarkerOptions().position(latLng).title(name).snippet(address));
                    }
                }
            }

            @Override
            public void onFailure(Call<NearbyPlaceResponses> call, Throwable t) {

            }
        });
    }

    private void getCurrentPlaces() {
        if(checkLocationPermission())
        placeDetectionClient.getCurrentPlace(null)
                .addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            PlaceLikelihoodBufferResponse response = task.getResult();
                            int size = response.getCount();
                            //Toast.makeText(MainActivity.this, "size: "+size, Toast.LENGTH_SHORT).show();
                            for(int i = 0; i < size; i++){
                                PlaceLikelihood likelihood = response.get(i);
                                LatLng placeLatlng = likelihood.getPlace().getLatLng();
                                String name = likelihood.getPlace().getName().toString();
                                String address = likelihood.getPlace().getAddress().toString();
                                map.addMarker(new MarkerOptions().position(placeLatlng)
                                .title(name).snippet(address));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: "+e.getMessage());
            }
        });
    }
}
