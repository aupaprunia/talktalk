package com.hackillionis.talktalk.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.NetworkConnectivityHelper;
import com.hackillionis.talktalk.util.ToastHelper;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    final int REQUEST_PERMISSION = 101;

    SpinKitView spinKitSplash;
    TextView txtSplashLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new NetworkConnectivityHelper(this).startNetworkCallBack();

        spinKitSplash = findViewById(R.id.spinKitSplash);
        txtSplashLoading = findViewById(R.id.txtSplashLoading);

        spinKitSplash.setVisibility(View.GONE);
        txtSplashLoading.setVisibility(View.GONE);

        /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Log.d("hello","fcm"+instanceIdResult.getToken());
            }
        });*/

        checkPermissionStatus();

    }

    private void performIntent() {
        spinKitSplash.setVisibility(View.VISIBLE);
        txtSplashLoading.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.MY_PREF, MODE_PRIVATE);

        if (sharedPreferences.getBoolean(Constants.IS_LOGGED_IN, false)) {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Users/"+sharedPreferences.getString(Constants.UID,"guest"));
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long token = (long)snapshot.child(Constants.TOKEN).getValue();
                    sharedPreferences.edit().putLong(Constants.TOKEN,token).apply();
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
    }

    private void checkPermissionStatus() {
        List<String> permissionsNeeded = new ArrayList<>();
        for (String isPermissionRequired : permissions) {
            int result = ActivityCompat.checkSelfPermission(SplashActivity.this, isPermissionRequired);
            if (result == PackageManager.PERMISSION_DENIED) {
                permissionsNeeded.add(isPermissionRequired);
            }
        }
        if (permissionsNeeded.isEmpty()) {
            performIntent();
        } else {
            ActivityCompat.requestPermissions(SplashActivity.this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean isAllGranted = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                performIntent();
            } else {
                new ToastHelper().makeToast(SplashActivity.this, "Permission Required.", Toast.LENGTH_LONG);
                finishAffinity();
            }
        }
    }
}