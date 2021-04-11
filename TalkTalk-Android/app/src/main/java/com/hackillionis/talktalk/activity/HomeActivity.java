package com.hackillionis.talktalk.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.fragment.ConnectionFragment;
import com.hackillionis.talktalk.fragment.ListenerFragment;
import com.hackillionis.talktalk.fragment.ProfileFragment;
import com.hackillionis.talktalk.fragment.SpeakerFragment;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.NetworkConnectivityHelper;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    public static BottomNavigationView bottomNavigationView;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPreferences = getSharedPreferences(Constants.MY_PREF,MODE_PRIVATE);

        new NetworkConnectivityHelper(this).startNetworkCallBack();

        FirebaseMessaging.getInstance().subscribeToTopic(sharedPreferences.getString(Constants.UID,"guest"))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        openProfile();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.nav_profile){
                    openProfile();
                }else if(item.getItemId() == R.id.nav_speaker){
                    openSpeaker();
                }else if(item.getItemId() == R.id.nav_listener){
                    openListener();
                }else{
                    openConnections();
                }

                return true;
            }
        });
    }

    private void openProfile(){
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .replace(R.id.frameLayout, new ProfileFragment()).commit();
    }

    private void openSpeaker(){
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .replace(R.id.frameLayout, new SpeakerFragment()).commit();
    }

    private void openListener(){
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .replace(R.id.frameLayout, new ListenerFragment()).commit();
    }

    private void openConnections(){
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                .replace(R.id.frameLayout, new ConnectionFragment()).commit();
    }

}