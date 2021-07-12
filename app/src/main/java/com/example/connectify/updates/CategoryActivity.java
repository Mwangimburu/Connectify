package com.example.connectify.updates;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.connectify.R;
import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Team;
import com.google.android.material.card.MaterialCardView;

public class CategoryActivity extends AppCompatActivity {
    MaterialCardView mPostText, mPostImage, mPostVideo, mPostMultiImages, mPostNotification, mPostTally;
    Team team;
    ApplicationUser applicationUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Intent intent = getIntent();
        team = intent.getParcelableExtra("teamData");
        applicationUser = intent.getParcelableExtra("userDetails");
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Post update");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mPostText = findViewById(R.id.post_text);
        mPostImage = findViewById(R.id.post_image);
        mPostVideo = findViewById(R.id.post_video);
        mPostMultiImages = findViewById(R.id.post_multiple_images);
        mPostNotification = findViewById(R.id.post_notifiction);
        mPostTally = findViewById(R.id.post_tally);

        mPostText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PostTextActivity.class);
                intent.putExtra("teamData", team);
                intent.putExtra("userDetails",applicationUser);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PostImageActivity.class);
                intent.putExtra("teamData", team);
                intent.putExtra("userDetails",applicationUser);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        mPostVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CategoryActivity.this, "Post video", Toast.LENGTH_SHORT).show();
            }
        });
        mPostMultiImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CategoryActivity.this, "Post images", Toast.LENGTH_SHORT).show();
            }
        });
        mPostNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CategoryActivity.this, "Post notice", Toast.LENGTH_SHORT).show();
            }
        });
        mPostTally.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CategoryActivity.this, "Post tally", Toast.LENGTH_SHORT).show();
            }
        });


    }
}