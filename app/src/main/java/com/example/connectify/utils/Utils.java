package com.example.connectify.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.connectify.MainActivity;
import com.example.connectify.R;
import com.example.connectify.models.ApplicationUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class Utils {
    Dialog mProgressDialog;
    Dialog mErrorDialog;
    Dialog mSuccessDialog;

    public void showProgress(Context context) {
        mProgressDialog = new Dialog(context);
        mProgressDialog.setContentView(R.layout.progress_bar_layout);
        mProgressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mProgressDialog.getWindow().setGravity(Gravity.CENTER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }
    }

    public void hideSuccess() {
        if (mSuccessDialog != null) {
            mSuccessDialog.cancel();
        }
    }

    public void showSuccessDialog(Context context, String successMessage) {
        mSuccessDialog = new Dialog(context);
        mSuccessDialog.setContentView(R.layout.success_layout);
        mSuccessDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSuccessDialog.getWindow().setGravity(Gravity.CENTER);
        mSuccessDialog.setCancelable(false);
        TextView mSuccessMessage = mSuccessDialog.findViewById(R.id.message);
        mSuccessMessage.setText(successMessage);
        mSuccessDialog.show();

    }

    public void showErrorDialog(Context context, String errorMessage) {
        mErrorDialog = new Dialog(context);
        mErrorDialog.setContentView(R.layout.error_layout);
        mErrorDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mErrorDialog.getWindow().setGravity(Gravity.CENTER);
        mErrorDialog.setCancelable(false);
        ImageView mCancelButton = mErrorDialog.findViewById(R.id.cancel_button);
        TextView mErrorMessage = mErrorDialog.findViewById(R.id.error_message);
        mErrorMessage.setText(errorMessage);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorDialog.cancel();
            }
        });

        mErrorDialog.show();

    }

    public String generateCode(String teamName) {
        Random random = new Random();
        int stringLength = teamName.length();
        Character startChar = teamName.toUpperCase().charAt(0);
        Character endChar = teamName.toUpperCase().charAt(stringLength - 1);
        int randomNumber = random.nextInt(10000);


        return stringLength + "" + endChar + "" + randomNumber + "" + startChar;

    }


}
