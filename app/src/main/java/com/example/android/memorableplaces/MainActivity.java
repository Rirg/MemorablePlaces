package com.example.android.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // Create Views for the Layout
    ListView mListView;
    TextView mAddTextView;

    // General variables
    private ArrayAdapter<String> mAdapter;
    public static ArrayList<String> mNames;
    public static SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the TextView and ListView from the layout
        mAddTextView = (TextView) findViewById(R.id.addTextView);
        mListView = (ListView) findViewById(R.id.listView);

        // Get tbe shared preferences from the package
        mSharedPreferences = this.getSharedPreferences("com.example.android.memorableplaces", Context.MODE_PRIVATE);

        // Create the ArrayList
        mNames = new ArrayList<>();

        // Get the ArrayList from the SharedPreferences
        try {
            mNames = (ArrayList<String>) ObjectSerializer.deserialize(mSharedPreferences
                    .getString("names", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.i("MainActivity state ", "ObjectSerializer");

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create the adapter with the ArrayList from the SharedPreferences
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mNames);

        // Set the adapter to the ListView
        mListView.setAdapter(mAdapter);

        // Set a listener for the TextView
        mAddTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("pos", -1);
                startActivity(intent);
            }
        });

        // Set a listener for every item on the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("pos", position);
                startActivity(intent);
            }
        });


        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO set a method to edit the name of the place
                return true;
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("MainActivity state ", "onRestart");
        // Get the new list when the activity is in this state
        try {
            mNames = (ArrayList<String>) ObjectSerializer.deserialize(mSharedPreferences
                    .getString("names", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.i("MainActivity state ", "onStop");

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create an Adapter again to set the new list
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mNames);

        // Set the adapter to the ListView
        mListView.setAdapter(mAdapter);
    }
}
