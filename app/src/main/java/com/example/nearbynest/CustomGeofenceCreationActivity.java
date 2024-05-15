package com.example.nearbynest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CustomGeofenceCreationActivity extends AppCompatActivity {

    private EditText geofenceName;
    private TextView latlngInfo;
    private Button createFence;
    private String userid;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;

    private double current_lat;
    private double current_lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_geofence_creation);

        latlngInfo = findViewById(R.id.latlng_info);
        createFence = findViewById(R.id.create_geofence_button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try{
            getLocation();
        }
        catch (Exception e){
            System.out.println("LOG ERR: CustomGeofenceCreationActivity: Location fetch error"+e);
        }
        latlngInfo.setText("Current Co-ordinates: \nLat: "+this.current_lat+" \nLon: "+this.current_lon);

        createFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 geofenceName = findViewById(R.id.geofence_name);
                 String geofenceNameValue = geofenceName.getText().toString();
                 userid = getIntent().getStringExtra("user_id");

                FirebaseDatabase database;
                database = FirebaseDatabase.getInstance();
                Map<String, String> map = new HashMap<>();
                map.put("lat", String.valueOf(current_lat));
                map.put("lon", String.valueOf(current_lon));
                System.out.println("---------LOG CustomGeofenceCreationAvtivity: userid: "+userid+" geofencenamevalue: "+geofenceNameValue);

                //create geofence:
                Geofencing geofencing = new Geofencing();
                geofencing.addGeofence(getApplicationContext(), CustomGeofenceCreationActivity.this,
                        geofenceNameValue, String.valueOf(current_lat), String.valueOf(current_lon));

                database.getReference().child("users").child(userid).child(geofenceNameValue).setValue(map);
                Toast.makeText(CustomGeofenceCreationActivity.this, "Geofence Added Successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            return;
        }

        Location location = getLastKnownLocation();
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            this.current_lat = latitude;
            this.current_lon = longitude;
            Toast.makeText(this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("LOG ERR: CustomGeofenceCreationActivity: PERMISSIONS FOR geofence NOT GIVEN");
            }
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
