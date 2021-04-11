package com.hackillionis.talktalk.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.activity.SignUpActivity;
import com.hackillionis.talktalk.data.ListenerData;
import com.hackillionis.talktalk.data.UserData;
import com.hackillionis.talktalk.dialog.DialogMood;
import com.hackillionis.talktalk.dialog.DialogProgress;
import com.hackillionis.talktalk.live.activities.LiveActivity;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.ToastHelper;

import java.util.ArrayList;
import java.util.Random;

public class SpeakerFragment extends Fragment implements DialogMood.OnMoodSelected {

    TextView txtToken,txtStart;
    SharedPreferences sharedPreferences;
    ArrayList<ListenerData> userList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speaker, container, false);

        txtToken = view.findViewById(R.id.txtSpeakerToken);
        txtStart = view.findViewById(R.id.txtSpeakerStart);
        sharedPreferences = getActivity().getSharedPreferences(Constants.MY_PREF, Context.MODE_PRIVATE);

        txtToken.setText(String.valueOf(sharedPreferences.getLong(Constants.TOKEN,-1)));

        txtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharedPreferences.getLong(Constants.TOKEN,0) == 0){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogNew);
                    final View customLayout
                            = getLayoutInflater()
                            .inflate(R.layout.custom_alert_dialog, null);
                    Button btnDialog = customLayout.findViewById(R.id.btnDialog);
                    ImageView imgDialog = customLayout.findViewById(R.id.imgDialog);
                    imgDialog.setImageDrawable(getResources().getDrawable(R.drawable.emo_sad));
                    builder.setView(customLayout);
                    builder.setTitle("Oppss!");
                    builder.setMessage("You don't have enough tokens to speak! You can earn tokens by listening to someone.");
                    final AlertDialog alertDialog = builder.create();
                    //alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                    alertDialog.show();
                    btnDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                }else {
                    DialogFragment dialogFragment = new DialogMood(SpeakerFragment.this);
                    dialogFragment.show(getActivity().getSupportFragmentManager(), "Dialog Mood");
                }
            }
        });

        return view;
    }

    @Override
    public void onMoodSelected(String mood) {
        DialogProgress dialogProgress = new DialogProgress("Searching listeners...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getActivity().getSupportFragmentManager(),"Dialog Progress");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Online/Listener/"+mood);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getChildrenCount() == 0){
                    dialogProgress.dismiss();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogNew);
                    final View customLayout
                            = getLayoutInflater()
                            .inflate(R.layout.custom_alert_dialog, null);
                    Button btnDialog = customLayout.findViewById(R.id.btnDialog);
                    ImageView imgDialog = customLayout.findViewById(R.id.imgDialog);
                    imgDialog.setImageDrawable(getResources().getDrawable(R.drawable.emo_sad));
                    builder.setView(customLayout);
                    builder.setTitle("Sorry!");
                    builder.setMessage("No listener found at the moment. Please try again later.");
                    final AlertDialog alertDialog = builder.create();
                    //alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                    alertDialog.show();
                    btnDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                }else{

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ListenerData uid = dataSnapshot.getValue(ListenerData.class);
                        userList.add(uid);
                    }

                    Random random = new Random();
                    ListenerData uid = userList.get(random.nextInt(userList.size()));

                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("Online/Listener/"+mood+"/"+uid.getUid());
                    databaseReference1.child("status").setValue(sharedPreferences.getString(Constants.UID,"guest")).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("hello","Connecting to : "+uid);
                                dialogProgress.dismiss();
                                sharedPreferences.edit().putString(Constants.CONNECT_TO,uid.getUid()).apply();
                                sharedPreferences.edit().putString(Constants.CONNECTED_TO,sharedPreferences.getString(Constants.UID,"guest")).apply();
                                Intent intent = new Intent(getActivity(), LiveActivity.class);
                                intent.putExtra("allot_time","5");
                                startActivity(intent);

                                DatabaseReference databaseUpdateToken = firebaseDatabase.getReference("Users/"+sharedPreferences.getString(Constants.UID,"guest"));
                                databaseUpdateToken.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserData listenerToken = snapshot.getValue(UserData.class);
                                        long token = listenerToken.getToken();
                                        token--;
                                        databaseUpdateToken.child("token").setValue(token);
                                        sharedPreferences.edit().putLong(Constants.TOKEN,token).apply();
                                        txtToken.setText(String.valueOf(token));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else{
                                dialogProgress.dismiss();
                                new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                            }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogProgress.dismiss();
                new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
            }
        });
    }
}