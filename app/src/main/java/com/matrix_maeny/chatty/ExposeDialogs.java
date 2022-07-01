package com.matrix_maeny.chatty;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

public class ExposeDialogs {

    private final Context context;

    private ProgressDialog progressDialog;

    public ExposeDialogs(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
        setAllCancellable(false);

    }

    public void setAllCancellable(boolean yes) {
        progressDialog.setCancelable(yes);
        progressDialog.setCanceledOnTouchOutside(yes);
    }


    public void showProgressDialog(String title, String message) {

        progressDialog.setTitle(title);
        progressDialog.setMessage(message);

        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dismissProgressDialog() {

        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showToast(String msg, int time) {
        if (time == 0) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

    }
}
