package com.hackillionis.talktalk.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.dialog.DialogProgress;
import com.hackillionis.talktalk.util.NetworkConnectivityHelper;
import com.hackillionis.talktalk.util.ToastHelper;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    EditText etName, etEmail, etMobile, etPassword, etCPassword, etOtp;
    TextView txtOtpDes, txtBack;
    Button btnSignUp, btnOtp;
    TextInputLayout txtILOtp;
    ImageView imgSignUp;
    CardView cvSignUp;

    String imageLink = "";
    boolean isImageSelected = false;
    String verificationToken = "";
    final int CAMERA_REQ = 101;

    String name, email, mobile, password, cPassword, user_type;

    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    DialogFragment progressDialogOTP;
    DialogFragment progressDialogSendingOTP;

    private String emailPattern = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,4}$";

    boolean isOTPVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = findViewById(R.id.etSignUpName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etMobile = findViewById(R.id.etSignUpMobile);
        etPassword = findViewById(R.id.etSignUpPassword);
        etCPassword = findViewById(R.id.etSignUpCPassword);
        etOtp = findViewById(R.id.etSignUpOtp);
        txtOtpDes = findViewById(R.id.txtSignUpOtpDes);
        txtBack = findViewById(R.id.txtSingUpBack);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtILOtp = findViewById(R.id.txtILSignUpOtp);
        imgSignUp = findViewById(R.id.imgSignUp);
        cvSignUp = findViewById(R.id.cvSignUp);
        progressDialogOTP = new DialogProgress("Verifying OTP...");
        progressDialogSendingOTP = new DialogProgress("Sending OTP...");
        progressDialogOTP.setCancelable(false);
        progressDialogSendingOTP.setCancelable(false);
        firebaseAuth = FirebaseAuth.getInstance();
        btnOtp = findViewById(R.id.btnSignUpOtp);

        btnOtp.setVisibility(View.GONE);
        txtILOtp.setVisibility(View.GONE);
        txtOtpDes.setVisibility(View.GONE);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                progressDialogSendingOTP.dismiss();
                new ToastHelper().makeToast(SignUpActivity.this,"OTP Verified!", Toast.LENGTH_LONG);
                onOTPVerified();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialogSendingOTP.dismiss();
                new ToastHelper().makeToast(SignUpActivity.this,"OTP Verification Failed!", Toast.LENGTH_LONG);
                Log.d("hello","OTP Failed : "+e.getMessage()+" ");
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                progressDialogSendingOTP.dismiss();
                txtILOtp.setVisibility(View.VISIBLE);
                txtOtpDes.setVisibility(View.VISIBLE);
                btnSignUp.setVisibility(View.GONE);
                btnOtp.setVisibility(View.VISIBLE);
                txtOtpDes.setText("OTP have been sent to : "+mobile+".Please enter OTP to complete verification.");

                new ToastHelper().makeToast(SignUpActivity.this,"OTP sent successfully!", Toast.LENGTH_LONG);
                verificationToken = verificationId;
            }
        };

        btnOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFocus();
                if(!isOTPVerified) {
                    String otp = etOtp.getText().toString().trim();
                    if (otp.isEmpty()) {
                        new ToastHelper().makeErrorToastForEditText(SignUpActivity.this,"Enter OTP!","Enter OTP", Toast.LENGTH_LONG, etOtp);
                    } else if (otp.length() < 6) {
                        new ToastHelper().makeErrorToastForEditText(SignUpActivity.this,"Enter 6 digit OTP!","Enter 6 digit OTP", Toast.LENGTH_LONG, etOtp);
                    } else if(NetworkConnectivityHelper.isConnected) {
                        progressDialogOTP.show(getSupportFragmentManager(),"Dialog Progress");
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationToken, otp);
                        SigninWithPhone(credential);
                    }else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.CustomAlertDialogNew);
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
                }else{
                    onOTPVerified();
                }
            }
        });

        imgSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFocus();
                isImageSelected = false;
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, CAMERA_REQ);
            }
        });

        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFocus();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFocus();
                name = etName.getText().toString().trim();
                email = etEmail.getText().toString().trim();
                mobile = etMobile.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                cPassword = etCPassword.getText().toString().trim();

                Pattern pattern = Pattern.compile(emailPattern);
                Matcher matcher = pattern.matcher(email);

                if(name.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty() || cPassword.isEmpty()){
                    new ToastHelper().makeErrorToast(SignUpActivity.this,"All fields are mandatory!", Toast.LENGTH_LONG, cvSignUp);
                }else if(mobile.length() < 10){
                    new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Invalid Contact Number!","Enter 10 digit contact number", Toast.LENGTH_LONG, etMobile);
                }else if(!matcher.find()){
                    new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Invalid Email!","Invalid email", Toast.LENGTH_LONG, etEmail);
                }else if(!password.equals(cPassword)){
                    new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Password Mismatch!","Password mismatch", Toast.LENGTH_LONG, etPassword);
                    new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Password Mismatch!","Password mismatch", Toast.LENGTH_LONG, etCPassword);
                }else{

                    if(!isOTPVerified) {
                        progressDialogSendingOTP.show(getSupportFragmentManager(),"Dialog Progress");
                        Log.d("hello","Mobile : "+mobile);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+91" + mobile,
                                60,
                                TimeUnit.SECONDS,
                                SignUpActivity.this,
                                mCallbacks);
                    }else{
                        onOTPVerified();
                    }
                }
            }
        });
    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialogOTP.dismiss();
                            new ToastHelper().makeToast(SignUpActivity.this,"OTP Verified!", Toast.LENGTH_LONG);
                            onOTPVerified();
                        } else {
                            progressDialogOTP.dismiss();
                            new ToastHelper().makeErrorToastForEditText(SignUpActivity.this,"Invalid OTP!","Invalid otp", Toast.LENGTH_LONG, etOtp);
                        }
                    }
                });
    }

    public void onOTPVerified() {
        if(NetworkConnectivityHelper.isConnected) {
            isOTPVerified = true;
            DialogFragment progressDialog = new DialogProgress("Creating user...");
            progressDialog.setCancelable(false);
            progressDialog.show(getSupportFragmentManager(), "Dialog Progress");

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = firebaseDatabase.getReference("Users/" + task.getResult().getUser().getUid());

                        databaseReference.child("name").setValue(name);
                        databaseReference.child("email").setValue(email);
                        databaseReference.child("mobile").setValue(mobile);
                        databaseReference.child("uid").setValue(task.getResult().getUser().getUid());
                        databaseReference.child("token").setValue(1);

                        if (isImageSelected) {
                            databaseReference.child("image_link").setValue(imageLink);
                        } else {
                            databaseReference.child("image_link").setValue("no_image");
                        }

                        progressDialog.dismiss();
                        new ToastHelper().makeToast(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_LONG);
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        btnOtp.setVisibility(View.GONE);
                        btnSignUp.setVisibility(View.VISIBLE);
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException weakPassword) {
                            new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Enter a strong password!", "Enter a strong password", Toast.LENGTH_LONG, etPassword);
                            new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Enter a strong password!", "Enter a strong password", Toast.LENGTH_LONG, etCPassword);
                        } catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                            new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Invalid email!", "Invalid email", Toast.LENGTH_LONG, etEmail);
                        } catch (FirebaseAuthUserCollisionException existEmail) {
                            new ToastHelper().makeErrorToastForEditText(SignUpActivity.this, "Email already registered!", "Email already registered", Toast.LENGTH_LONG, etEmail);
                        } catch (Exception e) {
                            new ToastHelper().makeToast(SignUpActivity.this, "Authentication Failed!", Toast.LENGTH_LONG);
                        }
                    }
                }
            });
        }else{
            final AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.CustomAlertDialogNew);
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

    private void clearFocus(){
        etOtp.clearFocus();
        etCPassword.clearFocus();
        etPassword.clearFocus();
        etName.clearFocus();
        etMobile.clearFocus();
        etEmail.clearFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQ && resultCode == RESULT_OK && data != null) {
            Log.d("hello", "Intent Proccess");
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String pictureFilePath = cursor.getString(columnIndex);
            cursor.close();
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                Log.d("hello", "Upload Proccess");
                imgSignUp.setImageURI(Uri.fromFile(imgFile));
                uploadToCloud(selectedImage, System.currentTimeMillis() + "_" + imgFile.getName());
            }
        } else {
            Log.d("hello", "Invalid Data");
        }
    }

    private void uploadToCloud(Uri file, String filename) {
        Log.d("hello", "Upload Initiated");
        DialogFragment dialogProgress = new DialogProgress("Uploading Image...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getSupportFragmentManager(),"Dialog Progress");

        StorageReference storageRef = FirebaseStorage.getInstance().getReference(filename);

        storageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dialogProgress.dismiss();
                                new ToastHelper().makeToast(SignUpActivity.this,"Image Uploaded Successfully!",Toast.LENGTH_LONG);
                                imageLink = uri.toString();
                                isImageSelected = true;
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        dialogProgress.dismiss();
                        new ToastHelper().makeToast(SignUpActivity.this,"Something went wrong! Please try again later OR continue without image.",Toast.LENGTH_LONG);
                        Log.d("hello", "Exception : " + exception.getMessage());
                    }
                });
    }
}