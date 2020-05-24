package com.example.mymap;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleMap.OnMapLoadedCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener,
        SensorEventListener {

    private GoogleMap mMap;
    Marker gpsMarker = null;
    List<Marker> markerList;
    static public SensorManager mSensorManager;
    private Sensor SensorAcceleration;
    private long lastUpdate = -1;

    TextView accelerationText;
    Button clearMemoryButton;
    ImageButton zoomInButton;
    ImageButton zoomOutButton;
    ImageButton showAccelerationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        accelerationText = findViewById(R.id.acceleration_text);
        clearMemoryButton = findViewById(R.id.clear_memory_button);
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);
        showAccelerationButton = findViewById(R.id.show_acceleration_button);

        markerList = new ArrayList<Marker>();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
            SensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setButtonClickListener();
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
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLoaded() {
        Log.i(MapsActivity.class.getSimpleName(), "MapLoaded");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //if(gpsMarker != null) gpsMarker.remove();
        gpsMarker = mMap.addMarker(new MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker())
            .alpha(0.8f)
            .title("Position:("
                    .concat(String.valueOf((float)Math.round(latLng.latitude*100)/100))
                    .concat(", ")
                    .concat(String.valueOf((float)Math.round(latLng.longitude*100)/100))
                    .concat(")")));
        markerList.add(gpsMarker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public void setButtonClickListener() {
        View.OnClickListener myClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearMemoryClick(view);
            }
        };
        clearMemoryButton.setOnClickListener(myClickListener);
    }

    public void zoomInClick(View v) {
        mMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOutClick(View v) {
        mMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    public void clearMemoryClick(View v) {
        mMap.clear();
        markerList.clear();
    }

    public void showAccelerationClick(View v) {
        if(accelerationText.getVisibility() == View.INVISIBLE)
            accelerationText.setVisibility(View.VISIBLE);
        else
            accelerationText.setVisibility(View.INVISIBLE);
    }

    public void hideButtonsClick(View v) {
        if(zoomInButton.getVisibility() == View.VISIBLE) {
            accelerationText.setVisibility(View.INVISIBLE);
            clearMemoryButton.setVisibility(View.INVISIBLE);
            zoomInButton.setVisibility(View.INVISIBLE);
            zoomOutButton.setVisibility(View.INVISIBLE);
            showAccelerationButton.setVisibility(View.INVISIBLE);
        }
        else {
            clearMemoryButton.setVisibility(View.VISIBLE);
            zoomInButton.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.VISIBLE);
            showAccelerationButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (accelerationText != null && accelerationText.getVisibility() == View.VISIBLE) {
            long timeMicro;
            if (lastUpdate == -1) {
                lastUpdate = event.timestamp;
                timeMicro = 0;
            } else {
                timeMicro = (event.timestamp - lastUpdate) / 1000L;
                lastUpdate = event.timestamp;
            }
            accelerationText.setText("Acceleration:\nx: "
                    .concat(String.valueOf((float)Math.round(event.values[0]*10000)/10000))
                    .concat(" y: ")
                    .concat(String.valueOf((float)Math.round(event.values[1]*10000)/10000)));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        if (accelerationText != null)
            mSensorManager.registerListener(this, SensorAcceleration, 1000000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerationText != null)
            mSensorManager.unregisterListener(this,SensorAcceleration);
    }
}
