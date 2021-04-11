package com.hackillionis.talktalk.dialog;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.util.ToastHelper;


public class DialogMood extends DialogFragment {

    String mood = "";
    CardView cvHappy, cvSad, cvAngry, cvConfused;
    OnMoodSelected onMoodSelected;
    Button btnMood;

    TextView txtHappy, txtSad, txtAngry, txtConfused;

    public DialogMood(OnMoodSelected onMoodSelected){
        this.onMoodSelected = onMoodSelected;
    }

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
        View view = inflater.inflate(R.layout.fragment_dialog_mood, container, false);

        cvHappy = view.findViewById(R.id.cvHappy);
        cvSad = view.findViewById(R.id.cvSad);
        cvConfused = view.findViewById(R.id.cvConfused);
        cvAngry = view.findViewById(R.id.cvAngry);
        txtHappy = view.findViewById(R.id.txtHappy);
        txtSad = view.findViewById(R.id.txtSad);
        txtConfused = view.findViewById(R.id.txtConfused);
        txtAngry = view.findViewById(R.id.txtAngry);
        btnMood = view.findViewById(R.id.btnSelectMood);

        unselectCard(cvHappy, txtHappy);
        unselectCard(cvSad, txtSad);
        unselectCard(cvConfused, txtConfused);
        unselectCard(cvAngry, txtAngry);

        cvHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCard(cvHappy, txtHappy);
                unselectCard(cvSad, txtSad);
                unselectCard(cvConfused, txtConfused);
                unselectCard(cvAngry, txtAngry);
            }
        });

        cvSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unselectCard(cvHappy, txtHappy);
                selectCard(cvSad, txtSad);
                unselectCard(cvConfused, txtConfused);
                unselectCard(cvAngry, txtAngry);
            }
        });

        cvConfused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unselectCard(cvHappy, txtHappy);
                unselectCard(cvSad, txtSad);
                selectCard(cvConfused, txtConfused);
                unselectCard(cvAngry, txtAngry);
            }
        });

        cvAngry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unselectCard(cvHappy, txtHappy);
                unselectCard(cvSad, txtSad);
                unselectCard(cvConfused, txtConfused);
                selectCard(cvAngry, txtAngry);
            }
        });

        btnMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mood.isEmpty()){
                    new ToastHelper().makeToast(getActivity(),"Select mood to proceed!", Toast.LENGTH_LONG);
                }else{
                    dismiss();
                    onMoodSelected.onMoodSelected(mood);
                }
            }
        });

        return view;
    }

    private void unselectCard(CardView cardView, TextView textView){
        cardView.setBackgroundColor(getActivity().getResources().getColor(R.color.white, getActivity().getTheme()));
        textView.setTextColor(getActivity().getResources().getColor(R.color.purpleLight, getActivity().getTheme()));
    }

    private void selectCard(CardView cardView, TextView textView){
        mood = cardView.getTag().toString();
        cardView.setBackgroundColor(getActivity().getResources().getColor(R.color.purpleLight, getActivity().getTheme()));
        textView.setTextColor(getActivity().getResources().getColor(R.color.white, getActivity().getTheme()));
    }

    public interface OnMoodSelected{
        void onMoodSelected(String mood);
    }
}