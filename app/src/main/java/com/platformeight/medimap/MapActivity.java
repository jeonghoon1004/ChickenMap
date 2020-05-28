// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.platformeight.medimap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.naver.maps.geometry.Tm128;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int CALL_PERMISSION_REQUEST_CODE = 2;
    private static final String TAG = "basicmap";
    float DEFAULT_ZOOM = 15;
    Location mLastKnownLocation;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private boolean pPermissionDenied = false;
    private boolean mLocationPermissionGranted = false;
    private boolean pCallPermissionGranted = false;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mDefaultLocation = new LatLng(37.5666102, 126.9783881); //서울시청

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ad();
    }
    public void ad(){
        //MobileAds.initialize(this, getString(R.string.admob_app_id));
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // 광고가 제대로 로드 되는지 테스트 하기 위한 코드입니다.
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                // 광고가 문제 없이 로드시 출력됩니다.
                //Log.d("@@@", "onAdLoaded");
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                // 광고 로드에 문제가 있을시 출력됩니다.
                Log.d("@@@", "onAdFailedToLoad " + errorCode);
            }
            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }
            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }
            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        //전면광고
        mInterstitialAd = new InterstitialAd(this);
        //mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id_for_test));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

    }
    public void adShow(){
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(final GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        // Set a listener for info window events.


        //setLocation(); //서울시청
        enableMyLocation();
        //getDeviceLocation();
        //enablePhoneCall();
        //mMap.setOnInfoWindowClickListener(this);
    }
    public void setLocation(){
        LocationSource locationSource = new LocationSource() {
            OnLocationChangedListener mlistner;
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                mlistner = onLocationChangedListener;
                Location location = new Location("LongPressLocationProvider");
                location.setLatitude(mDefaultLocation.latitude);
                location.setLongitude(mDefaultLocation.longitude);
                location.setAccuracy(100);
                mlistner.onLocationChanged(location);
            }

            @Override
            public void deactivate() {

            }
        };
        mMap.setLocationSource(locationSource);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //Toast.makeText(this, "Info window clicked" + marker.getSnippet(), Toast.LENGTH_SHORT).show();
        try {
            if (pCallPermissionGranted) {
                Intent tt = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + marker.getTag()));
                startActivity(tt);
                adShow();
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }

    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            Log.d("mylocation", "Current location task:\n" + task.getResult());

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            search(new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()));
                            /*
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    mDefaultLocation, DEFAULT_ZOOM));
                            search(mDefaultLocation);
                             */
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    public void search(LatLng latLng){
        NetworkTask nt = new NetworkTask(latLng);
        //NetworkTask nt = new NetworkTask(mDefaultLocation);
        try {
            //Toast.makeText(this, nt.execute().get(), Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObj = new JSONObject(nt.execute().get());
                JSONArray array = new JSONArray(String.valueOf(jsonObj.get("items")));
                //for or iter
                for (int i = 0; i<array.length();i++){// 1~30개 순차
                    JSONObject obj = array.getJSONObject(i);
                    com.naver.maps.geometry.LatLng tm = new Tm128(obj.getDouble("mapx"), obj.getDouble("mapy")).toLatLng();
                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(tm.latitude, tm.longitude))
                            .title(obj.getString("title"))
                            .snippet("전화연결: "+obj.getString("telephone"));
                    mMap.addMarker(options).setTag(obj.getString("telephone"));
                    Log.d("mylocation", i+" location marker:\n"+obj.getString("title")+ " tel :"+obj.getString("telephone") +" "+tm.latitude+", "+ tm.longitude);
                }
                /*
                JSONObject obj = array1.getJSONObject(0); // 1~30개 순차
                com.naver.maps.geometry.LatLng tm = new Tm128(obj.getDouble("mapx"), obj.getDouble("mapy")).toLatLng();
                mMap.addMarker(new MarkerOptions().position(new LatLng(tm.latitude, tm.longitude)).title("Marker"));
                Log.d("mylocation", "Current location marker:\n" +tm.latitude+", "+ tm.longitude);

                obj.getString("title");
                obj.getString("telephone");
                obj.getString("address");
                obj.getString("mapx");
                obj.getString("mapy");
                 */
                //Toast.makeText(this, obj.getString("title")+obj.getString("telephone")+obj.getString("address")+obj.getString("mapx")+obj.getString("mapy"), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        Log.d("mylocation", "Current location:\n" + mMap.getMyLocation());
        getDeviceLocation();
        return false;
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mLocationPermissionGranted = true;
                getDeviceLocation();
                enablePhoneCall();
                mMap.setOnInfoWindowClickListener(this);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }
    private void enablePhoneCall() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            pCallPermissionGranted = true;
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, CALL_PERMISSION_REQUEST_CODE,
                    Manifest.permission.CALL_PHONE, true);
        }
        // [END maps_check_location_permission]
    }
    boolean flag =false;
    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                enableMyLocation();
                flag=true;
            } else {
                // Permission was denied. Display an error message
                // [START_EXCLUDE]
                // Display the missing permission error dialog when the fragments resume.
                mPermissionDenied = true;
                // [END_EXCLUDE]
            }
        } else if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.CALL_PHONE)) {
                // Enable the my location layer if the permission has been granted.
                enablePhoneCall();
                flag=true;
            } else {
                // Permission was denied. Display an error message
                // [START_EXCLUDE]
                // Display the missing permission error dialog when the fragments resume.
                pPermissionDenied = true;
                // [END_EXCLUDE]
            }
        }
    }
    // [END maps_check_location_permission_result]

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }else if (pPermissionDenied) {
            // Permission was not granted, display error dialog.
            Toast.makeText(this, "전화걸기 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            pPermissionDenied = false;
        }else if (flag==true){
            flag=false;
        } else {
            adShow();
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
