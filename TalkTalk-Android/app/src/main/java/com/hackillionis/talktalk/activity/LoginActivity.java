package com.hackillionis.talktalk.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.data.UserData;
import com.hackillionis.talktalk.dialog.DialogProgress;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.NetworkConnectivityHelper;
import com.hackillionis.talktalk.util.ToastHelper;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    TextView txtSignUp;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtLoginSignUp);

        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    new ToastHelper().makeToast(LoginActivity.this, "All fields are mandatory!", Toast.LENGTH_LONG);
                } else if(NetworkConnectivityHelper.isConnected) {
                    DialogFragment progressDialog = new DialogProgress("Verifying user...");
                    progressDialog.setCancelable(false);
                    progressDialog.show(getSupportFragmentManager(), "Dialog Progress");

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                SharedPreferences sharedPreferences = getSharedPreferences(Constants.MY_PREF, MODE_PRIVATE);

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + task.getResult().getUser().getUid());
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        com.hackillionis.talktalk.data.UserData userData = snapshot.getValue(UserData.class);
                                        sharedPreferences.edit().putString(Constants.NAME, userData.getName()).apply();
                                        sharedPreferences.edit().putString(Constants.EMAIL, userData.getEmail()).apply();
                                        sharedPreferences.edit().putString(Constants.MOBILE, userData.getMobile()).apply();
                                        sharedPreferences.edit().putString(Constants.UID, userData.getUid()).apply();
                                        sharedPreferences.edit().putString(Constants.IMAGE_LINK, userData.getImage_link()).apply();
                                        sharedPreferences.edit().putLong(Constants.TOKEN, userData.getToken()).apply();
                                        sharedPreferences.edit().putBoolean(Constants.IS_LOGGED_IN, true).apply();

                                        progressDialog.dismiss();

                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                        new ToastHelper().makeToast(LoginActivity.this, "Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                                    }
                                });

                            } else {
                                progressDialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    new ToastHelper().makeErrorToastForEditText(LoginActivity.this, "User not registered!", "Email", Toast.LENGTH_LONG, etEmail);
                                    new ToastHelper().makeErrorToastForEditText(LoginActivity.this, "User not registered!", "Password", Toast.LENGTH_LONG, etPassword);

                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    new ToastHelper().makeErrorToastForEditText(LoginActivity.this, "Invalid Password!", "Invalid password", Toast.LENGTH_LONG, etPassword);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    new ToastHelper().makeToast(LoginActivity.this, "Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                                }
                            }
                        }
                    });
                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.CustomAlertDialogNew);
                    final View customLayout
                            = getLayoutInflater()
                            .inflate(R.layout.custom_alert_dialog, null);
                    Button btnDialog = customLayout.findViewById(R.id.btnDialog);
                    ImageView imgDialog = customLayout.findViewById(R.id.imgDialog);
                    imgDialog.setImageDrawable(getResources().getDrawable(R.drawable.emo_sad));
                    builder.setView(customLayout);
                    builder.setTitle("Can't connect to Internet!");
                    builder.setMessage("Please check your internet connection and try again later.");
                    final AlertDialog alertDialog = builder.create();
                    //alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                    alertDialog.show();
                    btnDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });

    }
}