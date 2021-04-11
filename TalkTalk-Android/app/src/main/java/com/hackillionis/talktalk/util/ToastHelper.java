package com.hackillionis.talktalk.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hackillionis.talktalk.R;

public class ToastHelper {

    public void makeToast(Context context, String message, int length){
        View toastView = LayoutInflater.from(context).inflate(R.layout.toast, null);
        Toast toast = new Toast(context);
        TextView textView = toastView.findViewById(R.id.toast_text);
        textView.setText(message);
        toast.setView(toastView);
        toast.setDuration(length);
        toast.show();
    }

    public void makeErrorToast(Context context, String message, int length, View view){
        if(context != null) {
            View toastView = LayoutInflater.from(context).inflate(R.layout.toast, null);
            Toast toast = new Toast(context);
            TextView textView = toastView.findViewById(R.id.toast_text);
            textView.setText(message);
            toast.setView(toastView);
            toast.setDuration(length);
            toast.show();

            if (view != null) {
                view.clearAnimation();
                view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake));
            }
        }
    }

    public void makeErrorToastForEditText(Context context, String message,String hint, int length, EditText editText){
        if(context != null) {
            View toastView = LayoutInflater.from(context).inflate(R.layout.toast, null);
            Toast toast = new Toast(context);
            TextView textView = toastView.findViewById(R.id.toast_text);
            textView.setText(message);
            toast.setView(toastView);
            toast.setDuration(length);
            toast.show();

            if (editText != null) {
                editText.setText(null);
                editText.setHint(hint);
                editText.setHintTextColor(context.getColor(R.color.error));
                editText.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake));
                editText.clearFocus();
            }
        }
    }

}
