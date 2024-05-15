package com.example.nearbynest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    private ListAdapter adapter;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        //Sign out button:
        Button signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                Intent intent = new Intent(MainActivity.this, SigninSignupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Get references to the TextView and ImageView
        TextView usernameTextView = findViewById(R.id.username);
        ImageView userPhotoImageView = findViewById(R.id.user_photo);

        // Get the values passed from the previous intent
        String username = mAuth.getCurrentUser().getDisplayName();
        String photoUrl = mAuth.getCurrentUser().getPhotoUrl().toString();
        String userid = mAuth.getCurrentUser().getUid();

        // Set the username text
        usernameTextView.setText(username);

        // Load the image from the URL using Picasso library and set it to the ImageView
        Picasso.get().load(photoUrl).into(userPhotoImageView);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        List<Integer> iconResIds = new ArrayList<>();
        iconResIds.add(R.drawable.mcdonalds_logo);
        iconResIds.add(R.drawable.starbucks_logo);
        // Add more icon resource IDs as needed

        List<String> texts = new ArrayList<>();
        texts.add("McDonald's");
        texts.add("StarBucks");
        // Add more text as needed

        List<Boolean> switchstates = new ArrayList<>();
        switchstates.add(false);
        switchstates.add(false);

        //read from firebase and update the toggle switches:
        database = FirebaseDatabase.getInstance();
        //mcdonalds toggle:
        database.getReference().child("users").child(userid).child("mcdonalds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the value of the child node
                String mcdonaldsValue = dataSnapshot.getValue(String.class);
                // Check if the value is not null
                if (mcdonaldsValue != null) {
                    // Display the value in a toast message
                    if (mcdonaldsValue.equals("yes")) {
                        //Toast.makeText(getApplicationContext(), "mcd value: "+mcdonaldsValue, Toast.LENGTH_SHORT).show();
                        //switchstates.add(true);
                        adapter.updateSwitchState(0, true);
                    }
                    System.out.println("----------LOG:   Adding the switch states value: " + switchstates);
                } else {
                    Toast.makeText(getApplicationContext(), "Value is null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(getApplicationContext(), "Failed to read value", Toast.LENGTH_SHORT).show();
            }
        });

        //starbucks toggle:
        database.getReference().child("users").child(userid).child("starbucks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the value of the child node
                String starbucksValue = dataSnapshot.getValue(String.class);
                // Check if the value is not null
                if (starbucksValue != null) {
                    // Display the value in a toast message
                    if (starbucksValue.equals("yes")) {
//                        switchstates.add(true);
                        adapter.updateSwitchState(1, true);
                    }
                    System.out.println("----------LOG:   Adding the switch states value: " + switchstates);
                } else {
                    Toast.makeText(getApplicationContext(), "Value is null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(getApplicationContext(), "Failed to read value", Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new ListAdapter(iconResIds, texts, switchstates, getApplicationContext());

        recyclerView.setAdapter(adapter);

        database.getReference().child("users").child(userid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String geofenceName = snapshot.getKey();
                if(!(snapshot.getKey().equals("id") || snapshot.getKey().equals("mcdonalds")
                        || snapshot.getKey().equals("name") || snapshot.getKey().equals("profile")
                        || snapshot.getKey().equals("starbucks"))){
                    System.out.println("----------LOG: TRIGGER NEW CUSTOM FENCE: " + geofenceName);
                    adapter.updateList(R.drawable.logo, geofenceName,true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        //custom geofence adapeter toggle:
//        database.getReference().child("users").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println("---------LOG MainActivity: GETTING IN");
//                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    System.out.println("---------LOG MainActivity: " + snapshot.getKey());
//
//                    if(!(snapshot.getKey().equals("id") || snapshot.getKey().equals("mcdonalds")
//                            || snapshot.getKey().equals("name") || snapshot.getKey().equals("profile")
//                    || snapshot.getKey().equals("starbucks"))){
//
//                        // Get the value of the child node
//                        String geofenceName = snapshot.getKey();
//                        // Check if the value is not null
//                        if (geofenceName != null) {
//                            // Display the value in a toast message
//                            System.out.println("----------LOG NEW CUSTOM FENCE: " + geofenceName);
//
//                            //update the adapter:
//                            adapter.updateList(R.drawable.logo, geofenceName,true);
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Value is null", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Handle errors
//                Toast.makeText(getApplicationContext(), "Failed to read value", Toast.LENGTH_SHORT).show();
//            }
//        });

//        adapter.updateSwitchState(0, true);

        //Add Geofence Button
        Button addGeofence = findViewById(R.id.add_geofence_button);
        addGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CustomGeofenceCreationActivity.class);
                intent.putExtra("user_id", userid);
                startActivity(intent);
            }
        });

    }
}