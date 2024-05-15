package com.example.nearbynest;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class Geofencing {
    List<Geofence> geofenceList = new ArrayList<>();
    PendingIntent geofencePendingIntent;
    GeofencingClient geofencingClient;
    Context context;

    protected void addGeofence(Context context, Activity activity, String id, String latitude, String longitude) {

        this.context = context;
        geofencingClient = LocationServices.getGeofencingClient(context);

        System.out.println("creating geofence on: "+Double.parseDouble(latitude)+" and "+Double.parseDouble(longitude));

        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(id)
                .setCircularRegion(
                        Double.parseDouble(latitude),// lat
                        Double.parseDouble(longitude),// lng
                        (float) 500)// add the radius in float.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(300000) //Geofence.NEVER_EXPIRE
                .setNotificationResponsiveness(1000)
                .build());


        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        System.out.println("-----LOG1---------Geofences SuccessFully Added");

                        //store the geofence ID on sharefPreferences:
                        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                        myEdit.putString("Geofences", id);
                        myEdit.apply();
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        System.out.println("-----LOG2 ERR---------Error adding geofences: " + e);
                    }
                });

    }

    protected void removeAllGeofences(List<String> geofenceIdsToDelete, Context context){
        geofencingClient = LocationServices.getGeofencingClient(context);
        geofencingClient.removeGeofences(geofenceIdsToDelete).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    // Geofences removed successfully
                    // Handle success
                    System.out.println("---------- LOG: GEOFENCE DELETED SUCCESSFULLY");
                } else {
                    // Geofences removal failed
                    // Handle failure
                    System.out.println("---------- LOG: GEOFENCE DELETE ERROR");
                }
            }
        });
    }

    protected GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    protected PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.
                FLAG_MUTABLE);
        return geofencePendingIntent;
    }
}
