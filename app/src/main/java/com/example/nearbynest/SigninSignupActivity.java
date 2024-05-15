package com.example.nearbynest;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SigninSignupActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_signup);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
                .requestEmail()
                .build();

        //sharedpref testing:
//        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//        SharedPreferences.Editor myEdit = sharedPreferences.edit();
//        myEdit.putString("testing", "testing for NearbyNest");
//        myEdit.apply();

//        //adding Starbucks locations to the database:
//        HashMap<String, String> sb = new HashMap<>();
//        String sbname = "3506 S State St, Chicago";
//        sb.put("lat", "41.83184");
//        sb.put("lon", "-87.62622");
//        database.getReference().child("Starbucks").child(sbname).setValue(sb);
//
//        //adding Mcdonalds locations to the database:
//        HashMap<String, String> mcd = new HashMap<>();
//        //2525S
//        String name = "E 35th St";
//        mcd.put("lat", "41.83084");
//        mcd.put("lon", "-87.62115");
//        database.getReference().child("Mcdonalds").child(name).setValue(mcd);

        LinearLayout signInSignUpButton = findViewById(R.id.signin_signup_button);
        signInSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(auth.getCurrentUser() != null) {
                    //Toast.makeText(getApplicationContext(), "signed in as: "+auth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SigninSignupActivity.this, MainActivity.class);
                    intent.putExtra("username", auth.getCurrentUser().getDisplayName());
                    intent.putExtra("photo_url", auth.getCurrentUser().getPhotoUrl().toString());
                    intent.putExtra("user_id", auth.getCurrentUser().getUid());

                    //adding Mcdonalds locations to the database:
//                    HashMap<String, String> mcd = new HashMap<>();
                    //2525S
//                    String name = "2525 S King Dr, Chicago";
//                    mcd.put("lat", "41.847004");
//                    mcd.put("lon", "-87.616908");
//                    database.getReference().child("Mcdonalds").child(name).setValue(mcd);

                    //check permissions to add:
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        ActivityCompat.requestPermissions(SigninSignupActivity.this , new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 123);
                    }
//                    geofencing.addGeofence(getApplicationContext(), SigninSignupActivity.this, name, "41.8413746", "-87.616777");

                    startActivity(intent);
                }
                else{
                    // Display a toast message
                    //Toast.makeText(getApplicationContext(), "Sign in/Sign up with Google clicked", Toast.LENGTH_SHORT).show();

                    googleSignIn();
                }

            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void googleSignIn(){
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            }
            catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            map.put("profile", user.getPhotoUrl().toString());
                            map.put("mcdonalds", "yes");
                            map.put("starbucks", "yes");

                            database.getReference().child("users").child(user.getUid()).setValue(map);

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                ActivityCompat.requestPermissions(SigninSignupActivity.this , new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 123);
                            }

                            //add Gefences default:
                            Geofencing geofencing = new Geofencing();
                            geofencing.addGeofence(getApplicationContext(), SigninSignupActivity.this, "Mcdonalds-2525 S King Dr, Chicago", "41.847004", "-87.616908");
                            geofencing.addGeofence(getApplicationContext(), SigninSignupActivity.this, "Mcdonalds-E 35th St", "41.83084", "-87.62115");
                            geofencing.addGeofence(getApplicationContext(), SigninSignupActivity.this, "Starbucks-3506 S State St, Chicago", "41.83184", "-87.62622");

                            Intent intent = new Intent(SigninSignupActivity.this, MainActivity.class);
                            intent.putExtra("username", auth.getCurrentUser().getDisplayName());
                            intent.putExtra("photo_url", auth.getCurrentUser().getPhotoUrl().toString());
                            intent.putExtra("user_id", auth.getCurrentUser().getUid());
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(SigninSignupActivity.this, "OOPS! Something Went Wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
