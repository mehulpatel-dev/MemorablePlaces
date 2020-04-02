package com.mehul.memorableplaces;
/*
    Title: Memorable Places
    Author: Mehul Patel
    Date: 03/30/2020
    Description: This apps will allow the user to select locations and save them to their memorable
                    places list and selecting these places from their list will take them to that
                    location on the map
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //declaring as static allows it to be accessed/used in a different class
    //declare array list for places to hold the name of the places
    //declare array list for location to  hold address of the location
    //declare array adapter
    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //listview variable
        ListView mylistView = (ListView) findViewById(R.id.listView);

        //sharedpreferences object so we can restore saved data from the permanent storage
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.mehul.memorableplaces", Context.MODE_PRIVATE);

        //create array list objects for latitudes and longitudes that will contain the restored from permanent storage
        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();

        //clear any saved items that may already be in the array lists so we don't overlap
        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {
            //deserialize and restore places from permanent storage
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>())));

            //for restoring locations, restore and deserialize latitude and longitudes
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //check to see if we actually have data restored from permanent storage
        if(places.size() > 0 && latitudes.size() > 0 & longitudes.size() > 0){
            //check to see if we have the same number of data restored between places, longitude, and latitudes from the permanent storage
            if(places.size() == latitudes.size() && latitudes.size() == longitudes.size()){
                //loop through both latitudes and longitudes
                for(int i = 0; i < latitudes.size(); i++){
                    //add/append latitudes and longitudes into the locations latlng array list
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));
                }

            }
        } else {
            //add first item in the list to say "add a new place"
            places.add("Add a new place...");
            locations.add(new LatLng(0,0));
        }

        //convert the places array to to listview format using array adapter
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);

        //apply/set the array adapter to the listview
        mylistView.setAdapter(arrayAdapter);

        //Make the items in the listview actionable/clickable
        mylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //create intent in order to access the mapsactivity.java
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                //pass variable name placenumber and it's content i to the mapsactivity
                intent.putExtra("placenumber", i);

                startActivity(intent);
            }
        });
    }
}
