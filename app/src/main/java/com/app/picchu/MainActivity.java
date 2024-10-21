package com.app.picchu;

import android.os.Bundle;
import com.mapbox.maps.MapView;

public class MainActivity extends BaseActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets up the main activity layout, inflates the content view, and initializes the MapView
        // with a custom Mapbox style
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.activity_content), true);

        mapView = findViewById(R.id.mapView);
        mapView.getMapboxMap().loadStyleUri("mapbox://styles/nuoxis/cm1y0jezl00v101r7hlzcekd7");
    }

    @Override
    protected void onDestroy() {
        // Handles any necessary cleanup when the activity is destroyed
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        // Called when the activity becomes visible to the user
        super.onStart();

    }

    @Override
    protected void onResume() {
        // Called when the activity will start interacting with the user
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Called when the system is about to start resuming another activity
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Called when the activity is no longer visible to the user
        super.onStop();

    }

    @Override
    public void onLowMemory() {
        // Called when the system is running low on memory, notifies the MapView to handle it
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
