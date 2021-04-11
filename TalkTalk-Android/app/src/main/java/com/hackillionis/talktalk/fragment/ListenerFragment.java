package com.hackillionis.talktalk.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.data.ListenerData;
import com.hackillionis.talktalk.data.UserData;
import com.hackillionis.talktalk.dialog.DialogMood;
import com.hackillionis.talktalk.dialog.DialogProgress;
import com.hackillionis.talktalk.live.activities.LiveActivity;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.ToastHelper;

import java.util.ArrayList;

public class ListenerFragment extends Fragment implements DialogMood.OnMoodSelected {
    TextView txtToken, txtStart;
    SharedPreferences sharedPreferences;
    ArrayList<ListenerData> userList = new ArrayList<>();
    boolean isStop = false;
    boolean isSearching = false;
    String mood = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listener, container, false);

        txtToken = view.findViewById(R.id.txtListenerToken);
        txtStart = view.findViewById(R.id.txtListenerStart);
        sharedPreferences = getActivity().getSharedPreferences(Constants.MY_PREF, Context.MODE_PRIVATE);

        txtToken.setText(String.valueOf(sharedPreferences.getLong(Constants.TOKEN,-1)));

        txtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new DialogMood(ListenerFragment.this);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "Dialog Mood");
            }
        });

        return view;
    }

    @Override
    public void onMoodSelected(String mood) {
        this.mood = mood;
        DialogProgress dialogProgress = new DialogProgress("Searching speakers...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getActivity().getSupportFragmentManager(),"Dialog Progress");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Online/Listener/"+mood+"/"+sharedPreferences.getString(Constants.UID,"guest"));

        ListenerData listenerData = new ListenerData(sharedPreferences.getString(Constants.UID,"guest"),"0");

        databaseReference.setValue(listenerData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful() && !isStop){
                    isSearching = true;
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!isStop){
                                ListenerData listenerUpdate = snapshot.getValue(ListenerData.class);
                                if(!listenerUpdate.getStatus().equals("0")){
                                    isStop = true;
                                    dialogProgress.dismiss();
                                    sharedPreferences.edit().putString(Constants.CONNECT_TO,listenerUpdate.getUid()).apply();
                                    sharedPreferences.edit().putString(Constants.CONNECTED_TO, listenerUpdate.getStatus()).apply();

                                    Intent intent = new Intent(getActivity(), LiveActivity.class);
                                    intent.putExtra("allot_time","5");
                                    startActivity(intent);

                                    databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                isStop = false;
                                            }
                                        }
                                    });

                                    DatabaseReference databaseUpdateToken = firebaseDatabase.getReference("Users/"+sharedPreferences.getString(Constants.UID,"guest"));
                                    databaseUpdateToken.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            UserData listenerToken = snapshot.getValue(UserData.class);
                                            long token = listenerToken.getToken();
                                            token++;
                                            databaseUpdateToken.child("token").setValue(token);
                                            sharedPreferences.edit().putLong(Constants.TOKEN,token).apply();
                                            txtToken.setText(String.valueOf(token));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            isStop = true;
                            databaseReference.removeValue();
                            dialogProgress.dismiss();
                            new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                        }
                    });
                }else{
                    dialogProgress.dismiss();
                    new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isSearching){
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Online/Listener/"+mood+"/"+sharedPreferences.getString(Constants.UID,"guest"));
            databaseReference.removeValue();

            onDestroy();
        }
    }
}