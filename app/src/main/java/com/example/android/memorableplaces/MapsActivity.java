package com.example.android.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /** General Variables */
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private GoogleMap mMap;
    private SharedPreferences mSharedPreferences;
    private ArrayList<String> currentNames = new ArrayList<>();
    private ArrayList<String> currentLatLng = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Check if the user accepts the permission, if he does, then update the location to
            // the last known location
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                // Set the last known location
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                // Clear the map before adding a new marker
                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18));
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get our SharedPreferences
        mSharedPreferences = this.getSharedPreferences("com.example.android.memorableplaces", Context.MODE_PRIVATE);

        // Get extras
        final Bundle bundle = getIntent().getExtras();
        try {
            currentNames = (ArrayList<String>) ObjectSerializer.deserialize(mSharedPreferences
                    .getString("names", ObjectSerializer.serialize(new ArrayList<String>())));
            currentLatLng = (ArrayList<String>) ObjectSerializer.deserialize(mSharedPreferences
                    .getString("latlng", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        }

        // LocationManager and LocationListener
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               /* // Check if the intent from the MainActivity has extras
                if(bundle.getInt("pos") == -1) {
                    // Get the new location when it changes
                    current = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.clear();
                    // Clear the map before adding a new marker
                    mMap.addMarker(new MarkerOptions().position(current).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

                    // Get extras
                    ArrayList<String> list = bundle.getStringArrayList("latLngList");
                    ArrayList<String> namesAddress = bundle.getStringArrayList("namesList");
                    // Send extras again
                    mIntent.putExtra("namesList", namesAddress);
                    mIntent.putExtra("latLngList", list);
                }*/

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // If device is running SDK < 23
        if (Build.VERSION.SDK_INT < 23) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } else {
            // Check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Ask permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                // We have permission!
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

                // Set the last known location
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                // Clear the map before adding a new marker
                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18));

            }
        }
        // Check if we got a -1 from the extra in the intent
        if(bundle.getInt("pos") == -1) {
            // Get the long pressed position that the user wants to save
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {
                        List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if(addressList != null && addressList.size() > 0) {
                           currentNames.add(addressList.get(0).getAddressLine(0));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Add the current Latitude and Longitude to our list
                    currentLatLng.add(latLng.toString());

                    // Edit all the list and add it to the SharedPreferences, show a Toast message
                    // to the user
                    try {
                        mSharedPreferences.edit().putString("latlng", ObjectSerializer.serialize(currentLatLng)).apply();
                        mSharedPreferences.edit().putString("names", ObjectSerializer.serialize(currentNames)).apply();
                        Toast.makeText(MapsActivity.this, "Location saved!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        } else {

            // Create the new LatLng
            String latlngString = currentLatLng.get(bundle.getInt("pos"));
            LatLng current = getLatAndLng(latlngString);

            // Clear, add a marker and move the camera to the new position
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(current).title(currentNames.get(bundle.getInt("pos"))));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 18));

        }
    }

    // Get the Latitude and Longitude from an item of the ArrayList of Strings
    private LatLng getLatAndLng(String lng) {
        // Create a pattern and matcher to get the LatLng from parenthesis
        Pattern p = Pattern.compile("\\((.*?)\\)");
        Matcher m = p.matcher(lng);
        // Create a variable to store the match from the pattern that we created
        String lngRegex = "";
        // Get the match and save it in our variable
        while(m.find()) {
            lngRegex = m.group(1);
        }
        // Create a new variable to split the Latitude and Longitude separated by a comma
        String[] splitLatLng = lngRegex.split(",");
        // Get the Latitude and Longitude parsing the string to double
        Double currentLat = Double.parseDouble(splitLatLng[0]);
        Double currentLng = Double.parseDouble(splitLatLng[1]);
        // Return the new LatLng object
        return new LatLng(currentLat, currentLng);
    }
}
