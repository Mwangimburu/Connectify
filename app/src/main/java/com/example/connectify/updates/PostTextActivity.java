package com.example.connectify.updates;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.connectify.R;
import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Post;
import com.example.connectify.models.Team;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostTextActivity extends AppCompatActivity {
    EditText mPostText;
    Button mPostTextBtn;
    String text;
    Team team;
    ApplicationUser applicationUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_text);
        Intent intent = getIntent();
        team = intent.getParcelableExtra("teamData");
        applicationUser = intent.getParcelableExtra("userDetails");
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Post text");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("Team Data");
        utils = new Utils();

        mPostText = findViewById(R.id.text);
        mPostTextBtn = findViewById(R.id.post_text_btn);
        mPostTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = mPostText.getText().toString().trim();
                if (!validateText()) {
                    return;
                }
                uploadPost();

            }
        });
    }

    public void uploadPost() {
        utils.showProgress(PostTextActivity.this);
        String postId = mDatabaseReference.push().getKey();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
        String formattedDate = dateFormat.format(calendar.getTime());
        Post newPost = new Post(text, null, formattedDate, applicationUser.getUserId());
        mDatabaseReference.child(team.getTeamCode()).child("Posts").child(postId).setValue(newPost).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                utils.hideProgress();
                utils.showSuccessDialog(PostTextActivity.this, "Post created successfully");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        utils.hideSuccess();
                        onBackPressed();
                    }
                }, 3000);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.hideProgress();
                utils.showErrorDialog(PostTextActivity.this, e.getMessage());
            }
        });
    }

    private boolean validateText() {
        if (text.isEmpty()) {
            mPostText.setError("Text can not be empty");
            mPostText.requestFocus();
            return false;
        } else {
            mPostText.setError(null);
            return true;
        }
    }
}