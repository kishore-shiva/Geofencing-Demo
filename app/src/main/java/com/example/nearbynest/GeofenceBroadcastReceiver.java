package com.example.nearbynest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    // ...
    public void onReceive(Context context, Intent intent) {
        System.out.println("Broadcast receiver called!!!!!!!!");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            System.out.println("ERR: " + errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for(Geofence geofence : triggeringGeofences) {
                System.out.println("Geofence Triggered: " + geofence.getRequestId());

                String topic = geofence.getRequestId();

                //send notification:
                showNotification(context, topic, "Your closer to your Nearby "+topic+". Have a Visit!!!");


            }

//            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//            );
//
//            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails);
//            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
//            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
//                    geofenceTransition));
        }
    }

    public static void showNotification(Context context, String title, String message) {
        String CHANNEL_ID = "Geofence";
        String CHANNEL_NAME = "Geofence channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Set the small icon for the notification
                .setContentTitle(title) // Set the title of the notification
                .setContentText(message) // Set the content text of the notification
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set the priority to high for heads-up notification
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Include sound, vibration, and LED lights

        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if the Android version is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("My Notification Channel Description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            // Register the notification channel with the system
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification
        notificationManager.notify(1, builder.build());
    }
}