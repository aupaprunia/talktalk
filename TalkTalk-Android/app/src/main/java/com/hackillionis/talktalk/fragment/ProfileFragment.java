package com.hackillionis.talktalk.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.activity.LoginActivity;
import com.hackillionis.talktalk.util.Constants;
import com.squareup.picasso.Picasso;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    ImageView imgProfile;
    TextView txtName, txtEmail, txtMobile, txtLogout, txtToken;
    SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        txtName = view.findViewById(R.id.txtProfileName);
        txtEmail = view.findViewById(R.id.txtProfileEmail);
        txtMobile = view.findViewById(R.id.txtProfileContact);
        txtLogout = view.findViewById(R.id.txtLogout);
        txtToken = view.findViewById(R.id.txtProfileToken);
        sharedPreferences = getActivity().getSharedPreferences(Constants.MY_PREF, MODE_PRIVATE);

        txtName.setText(sharedPreferences.getString(Constants.NAME,"--"));
        txtMobile.setText("Contact : "+sharedPreferences.getString(Constants.MOBILE,"--"));
        txtEmail.setText("Email : "+sharedPreferences.getString(Constants.EMAIL,"--"));
        txtToken.setText(String.valueOf(sharedPreferences.getLong(Constants.TOKEN,-1)));

        Picasso.get().load(sharedPreferences.getString(Constants.IMAGE_LINK,"no_image")).error(R.drawable.ic_user_150)
                .into(imgProfile);

        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(sharedPreferences.getString(Constants.UID,"guest"))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}