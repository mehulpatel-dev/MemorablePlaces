package com.mehul.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;

    LocationManager locMan;
    LocationListener locList;

    //onRequestPermissionsResult method to respond to the users action when they are requested for permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //if both are true we have permission
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //perform an explicit check for permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //listen for users location, track location, location updates
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);

                Location lastKnownLocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                centerMapOnLocation(lastKnownLocation, "Your location");
            }
        }
    }

    //method to process the location passed to this method to add a marker and center on the location
    public void centerMapOnLocation(Location location, String title) {
        //generate a LatLng from the location passed to this method
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        //clear all the markers on the map
        mMap.clear();

        //if we open the app and it is not at user location, a marker will be placed at current location
        if (!title.equals("Your location")) {
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        }

        //move and zoom the camera by 10 to the user location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        mMap.setOnMapLongClickListener(this);

        //create intent to receive content from MainActivity.java
        Intent intent = getIntent();

        //if the value passed from main activity is 0, zoom in on the user's location
        if (intent.getIntExtra("placenumber", 0) == 0) {
            //zoom in on user's location
            //set up location manager
            locMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            //set up location listener
            locList = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //call centerMapOnLocation we created to pass the location to that method when
                    //location is changed with title Your location since location passed will be where the user is located
                    centerMapOnLocation(location, "Your location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            //check if version earlier than 23(marshmallow) and for permission use gps to find location
            if (Build.VERSION.SDK_INT < 23) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    //request the location updates
                    locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);
                }
            } else {
                //check if we have permission
                //if we have permission start getting location updates
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                    locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);

                    //retrieve last known location from location manager
                    Location lastKnownLocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    //center the map location using the last known location
                    centerMapOnLocation(lastKnownLocation, "Your location");

                //ask for permission
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }

         //center or take us to the location selected in the listview by the user instead of the
         //current location of the user
        }else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            //set latitude from the locations array in MainActivity
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).latitude);
            //set longitude from the locations array in MainActivity
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);

            //call centerMapOnLocation passing the coordinates/lat/lng from placeLocation
            centerMapOnLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("PlaceNumber", 0)));
        }
    }

    //method for when the user holds or long clicks on the map
    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        //get a more precise address of the location the long click on the map
        try {
            //get the geo details of the locations
            List<Address> listAddresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
            //if listaddress is not empty and thoroughfare and subthoroughfare is not empty, append
            //the thoroughfare and subthoroughfare to the address string
            if(listAddresses != null && listAddresses.size() > 0){
                if(listAddresses.get(0).getThoroughfare() != null){
                    if(listAddresses.get(0).getSubThoroughfare() != null){
                        address += listAddresses.get(0).getSubThoroughfare() + " ";
                    }
                        address += listAddresses.get(0).getThoroughfare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if address string is empty, append the date and time to it
        if(address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address = sdf.format(new Date());
        }



        //a marker will be placed
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        //update the locations and places array list from the MainActivity
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        //update/save table/listview and show the new location when the place is long pressed
        MainActivity.arrayAdapter.notifyDataSetChanged();

        //create shared Preferences object for this package so we can use to save data
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.mehul.memorableplaces", Context.MODE_PRIVATE);

        try {
            //saving locations need to be converted to string tp be saved in sharedpreferences since they are LatLng
            //array lists to hold latitudes and longitudes
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            //search for all coordinates in the locations array in MainActivity
            //convert and save latitudes and longitudes separately
            for(LatLng coordinates : MainActivity.locations){
                latitudes.add(Double.toString(coordinates.latitude));
                longitudes.add(Double.toString(coordinates.longitude));
            }


            //save and serialize the places in storage/shared preferences
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
            //save the latitude in storage/shared preferences
            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            //save the longitude in storage/shared preferences
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
    }
}
