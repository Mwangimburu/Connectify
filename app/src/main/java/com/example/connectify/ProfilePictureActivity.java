package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.connectify.models.ApplicationUser;
import com.squareup.picasso.Picasso;

public class ProfilePictureActivity extends AppCompatActivity {
    ImageView mProfilePicture, mBack;
    ApplicationUser applicationUser;
    private static final String TAG = "ProfilePictureActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        applicationUser = intent.getParcelableExtra("userDetails");
        setContentView(R.layout.activity_profile_picture);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Profile photo");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mProfilePicture = findViewById(R.id.profile_image);
        //mBack = findViewById(R.id.back_arrow);

        Picasso.get().load(applicationUser.getProfileImageUri())
                .placeholder(R.drawable.profile_holder)
                .into(mProfilePicture);

//        mBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}