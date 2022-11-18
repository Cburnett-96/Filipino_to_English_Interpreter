package com.orbotz.filipino_english_interpreter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

public class LoadingData {
    private final Activity activity;
    private AlertDialog alertDialog;

    LoadingData(Activity myActivity)
    {
        activity= myActivity;
    }
    @SuppressLint("InflateParams")
    void StartLoadingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.CustomAlertDialog);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_data,null));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }


    void DismissDialog()
    {
        alertDialog.dismiss();
    }

}
