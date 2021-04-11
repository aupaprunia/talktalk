package com.hackillionis.talktalk.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hackillionis.talktalk.R;


public class DialogProgress extends DialogFragment {

    String message;
    public DialogProgress(String message){
        this.message = message;
    }

    TextView txtMessage;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_progress_dialog));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_progress, container, false);

        txtMessage = view.findViewById(R.id.txtProgressDialog);
        txtMessage.setTextColor(getResources().getColor(R.color.purpleLight, getActivity().getTheme()));
        txtMessage.setText(message);

        return view;
    }

}