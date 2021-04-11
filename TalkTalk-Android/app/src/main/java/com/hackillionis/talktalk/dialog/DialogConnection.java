package com.hackillionis.talktalk.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.activity.LoginActivity;
import com.hackillionis.talktalk.data.ConnectionData;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.ToastHelper;

public class DialogConnection extends DialogFragment {

    Button btnSend, btnCancel;
    SharedPreferences sharedPreferences;
    OnDismissConnection onDismissConnection;

    String requestTo;
    public DialogConnection(String requestTo, OnDismissConnection onDismissConnection){
        this.requestTo = requestTo;
        this.onDismissConnection = onDismissConnection;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_d_iialog_connection, container, false);

        btnSend = view.findViewById(R.id.btnConnectionSend);
        btnCancel = view.findViewById(R.id.btnConnectionCancel);
        sharedPreferences = getActivity().getSharedPreferences(Constants.MY_PREF, Context.MODE_PRIVATE);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                onDismissConnection.onDismissConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendConnectionRequest();
            }
        });

        return view;
    }

    private void sendConnectionRequest(){
        DialogProgress dialogProgress = new DialogProgress("Sending request...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getActivity().getSupportFragmentManager(),"Dialog Connection");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Connections/"+requestTo);

        ConnectionData connectionData = new ConnectionData(sharedPreferences.getString(Constants.UID,"guest"),
                sharedPreferences.getString(Constants.NAME,"guest"),sharedPreferences.getString(Constants.EMAIL,"guest"),
                sharedPreferences.getString(Constants.MOBILE,"guest"),sharedPreferences.getString(Constants.IMAGE_LINK,"guest"),"0");
        databaseReference.child(sharedPreferences.getString(Constants.UID,"guest")).setValue(connectionData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogNew);
                    final View customLayout
                            = getLayoutInflater()
                            .inflate(R.layout.custom_alert_dialog, null);
                    Button btnDialog = customLayout.findViewById(R.id.btnDialog);
                    ImageView imgDialog = customLayout.findViewById(R.id.imgDialog);
                    imgDialog.setImageDrawable(getResources().getDrawable(R.drawable.emo_happy));
                    builder.setView(customLayout);
                    builder.setTitle("Yayyy!");
                    builder.setCancelable(false);
                    builder.setMessage("Connection request sent. You will get a notification once the user approves your request.");
                    final AlertDialog alertDialog = builder.create();
                    //alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                    alertDialog.show();
                    btnDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            dismiss();
                            onDismissConnection.onDismissConnection();
                        }
                    });
                }else{
                    dismiss();
                    new ToastHelper().makeToast(getActivity(),"Something went wrong please try again later!", Toast.LENGTH_LONG);
                    onDismissConnection.onDismissConnection();
                }
            }
        });
    }

    public interface OnDismissConnection{
        void onDismissConnection();
    }
}