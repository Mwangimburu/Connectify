package com.example.connectify;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextView mSignUpLink, mErrorMsg, mForgotPassword;
    RelativeLayout mLoginLayout;
    Button mLoginBtn;
    TextInputLayout mEmail, mPassword;
    String email, password;
    FirebaseAuth mAuth;
    Utils utils;
    ImageView mImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSignUpLink = findViewById(R.id.sign_up_link);
        mLoginLayout = findViewById(R.id.login_layout);
        mLoginBtn = findViewById(R.id.sign_in_btn);
        mImageView = findViewById(R.id.img_logo);
        //mForgotPassword = findViewById(R.id.forgot_password);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.lg_password);
        mErrorMsg = findViewById(R.id.email_password_error);

        mAuth = FirebaseAuth.getInstance();
        utils = new Utils();
//        mForgotPassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(LoginActivity.this, "This feature is under development", Toast.LENGTH_LONG).show();
//            }
//        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = mEmail.getEditText().getText().toString().trim();
                password = mPassword.getEditText().getText().toString().trim();

                if (!validatePassword() | !validateEmail()) {
                    return;
                }
                signInUser();

            }
        });


        mSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReg();
            }
        });

        mLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginLayout.requestFocus();
            }
        });

    }

    private void signInUser() {
        utils.showProgress(LoginActivity.this);
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                redirectMainActivity();
                utils.hideProgress();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.hideProgress();
                mErrorMsg.setText(e.getMessage().toString());
                mPassword.setError(" ");
                mPassword.setErrorIconDrawable(null);
                mPassword.requestFocus();
                mPassword.setErrorIconDrawable(null);
                mEmail.setError(" ");
                mEmail.setErrorIconDrawable(null);
                mEmail.requestFocus();

            }
        });
    }

    private boolean validateEmail() {
        if (email.isEmpty()) {
            mEmail.setError("Please provide email");
            mEmail.setErrorIconDrawable(null);
            mEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Invalid email");
            mEmail.setErrorIconDrawable(null);
            mEmail.requestFocus();
            return false;
        } else {
            mEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {

        if (password.isEmpty()) {
            mPassword.setError("Please fill this field");
            mPassword.setErrorIconDrawable(null);
            mPassword.requestFocus();
            return false;
        } else {
            mPassword.setError(null);
            mPassword.setErrorEnabled(false);
            return true;
        }
    }

    private void redirectMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openReg() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

}