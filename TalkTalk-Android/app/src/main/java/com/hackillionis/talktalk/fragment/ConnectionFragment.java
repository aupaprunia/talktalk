package com.hackillionis.talktalk.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.hackillionis.talktalk.activity.HomeActivity;
import com.hackillionis.talktalk.adapter.ConnectionsAdapter;
import com.hackillionis.talktalk.data.ConnectionData;
import com.hackillionis.talktalk.dialog.DialogConnection;
import com.hackillionis.talktalk.dialog.DialogProgress;
import com.hackillionis.talktalk.network.APIClient;
import com.hackillionis.talktalk.network.Api;
import com.hackillionis.talktalk.util.Constants;
import com.hackillionis.talktalk.util.ToastHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectionFragment extends Fragment implements ConnectionsAdapter.OnConnectionRequest {

    TextView txtToken, txtNoCr;
    SharedPreferences sharedPreferences;

    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;
    ConnectionsAdapter connectionsAdapter;
    ArrayList<ConnectionData> data = new ArrayList<>();
    TextView txtCrDes;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection, container, false);

        txtToken = view.findViewById(R.id.txtConnectionToken);
        txtNoCr = view.findViewById(R.id.txtNoCr);
        recyclerView = view.findViewById(R.id.recyclerCR);
        txtCrDes = view.findViewById(R.id.txtCrDes);
        layoutManager = new LinearLayoutManager(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(Constants.MY_PREF, Context.MODE_PRIVATE);

        txtCrDes.setVisibility(View.GONE);
        txtNoCr.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        getData();

        txtToken.setText(String.valueOf(sharedPreferences.getLong(Constants.TOKEN,0)));

        return view;
    }

    public void getData(){
        DialogProgress dialogProgress = new DialogProgress("Loading connections...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getActivity().getSupportFragmentManager(),"Dialog Progress");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Connections/"+sharedPreferences.getString(Constants.UID,"guest"));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getChildrenCount() == 0){
                    txtNoCr.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    txtCrDes.setVisibility(View.GONE);
                }else{
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ConnectionData connectionData = dataSnapshot.getValue(ConnectionData.class);
                        data.add(connectionData);
                    }

                    connectionsAdapter = new ConnectionsAdapter(getActivity(), data, ConnectionFragment.this);
                    recyclerView.setAdapter(connectionsAdapter);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setVisibility(View.VISIBLE);
                    txtNoCr.setVisibility(View.GONE);
                    txtCrDes.setVisibility(View.VISIBLE);
                }
                dialogProgress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogProgress.dismiss();
                new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    public void onConnectionAccept(ConnectionData connectionData) {
        DialogProgress dialogProgress = new DialogProgress("Accepting request...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getActivity().getSupportFragmentManager(), "Dailog Progress");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Connections/"+sharedPreferences.getString(Constants.UID,"guest")
                +"/"+connectionData.getUid());
        databaseReference.child("status").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    dialogProgress.dismiss();
                    new ToastHelper().makeToast(getActivity(),"Request Accepted", Toast.LENGTH_LONG);
                    sendNotification(connectionData.getUid());

                    ConnectionData connectionData1 = new ConnectionData(sharedPreferences.getString(Constants.UID,"guest"), sharedPreferences.getString(Constants.NAME,"guest"),
                            sharedPreferences.getString(Constants.EMAIL,"guest"), sharedPreferences.getString(Constants.MOBILE,"guest"), sharedPreferences.getString(Constants.IMAGE_LINK,"guest"), "1");
                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("Connections/"+connectionData.getUid());
                    databaseReference1.child(sharedPreferences.getString(Constants.UID,"guest")).setValue(connectionData1);

                    ((HomeActivity)getActivity()).bottomNavigationView.setSelectedItemId(R.id.nav_connection);

                }else{
                    dialogProgress.dismiss();
                    new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                }
            }
        });
    }

    @Override
    public void onConnectionReject(ConnectionData connectionData) {
        DialogProgress dialogProgress = new DialogProgress("Rejecting request...");
        dialogProgress.setCancelable(false);
        dialogProgress.show(getActivity().getSupportFragmentManager(), "Dialog Progress");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Connections/"+sharedPreferences.getString(Constants.UID,"guest")
                +"/"+connectionData.getUid());
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    dialogProgress.dismiss();
                    new ToastHelper().makeToast(getActivity(),"Request Rejected", Toast.LENGTH_LONG);
                    ((HomeActivity)getActivity()).bottomNavigationView.setSelectedItemId(R.id.nav_connection);

                }else{
                    dialogProgress.dismiss();
                    new ToastHelper().makeToast(getActivity(),"Something went wrong! Please try again later.", Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void sendNotification(String uid) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/"+uid);
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", "Connection Update!");
            notificationBody.put("body", "Hola, "+sharedPreferences.getString(Constants.NAME,"Guest")+" accepted your connection request.");

            jsonObject.put("data", notificationBody);

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

            Call<ResponseBody> call = APIClient.getClient().create(Api.class).sendNotification("key=" + getString(R.string.fcm_key), body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("hello", "Res : " + response.code() + " " + response.message());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("hello", "Res Failed : " + t.getMessage());
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}