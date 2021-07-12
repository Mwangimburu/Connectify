package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.example.connectify.models.ApplicationUser;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    ApplicationUser applicationUser;
    CircleImageView mProfilePicture;
    TextInputLayout mFirstName, mLastName;
    TextView mAccountDetails, mLocationDetails;
    RelativeLayout mProfileLayout;
    ImageView mBackArrow;
    RelativeLayout mProfileSectionRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent = getIntent();
        applicationUser = intent.getParcelableExtra("userDetails");


        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Profile");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mProfilePicture = findViewById(R.id.profile_picture_profile);
        mFirstName = findViewById(R.id.first_name);
        mLastName = findViewById(R.id.last_name);
        mAccountDetails = findViewById(R.id.account_details);
        mLocationDetails = findViewById(R.id.location_details);
        mProfileLayout = findViewById(R.id.profile_layout);
        mProfileSectionRelativeLayout = findViewById(R.id.profile_picture_layout);

        // mBackArrow = findViewById(R.id.back);


        String location = applicationUser.getCounty() + "\n"
                + "  " + applicationUser.getConstituency() + "\n"
                + "    " + applicationUser.getWard() + "\n";


        Picasso.get().load(applicationUser.getProfileImageUri()).placeholder(R.drawable.profile_holder).into(mProfilePicture);
        mFirstName.getEditText().setText(applicationUser.getFirstName());
        mLastName.getEditText().setText(applicationUser.getLastName());
        mAccountDetails.setText(applicationUser.getAccountType());
        mLocationDetails.setText(location);


        mProfileLayout.requestFocus();

//        mBackArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
////                switch (applicationUser.getAccountType()) {
////                    case "Campaign Agent":
////                        openAgentsMenuActivity();
////                        break;
////                    case "Candidate":
////                        openCandidatesMenuActivity();
////                        break;
////                }
////            }
//        });
        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfilePictureActivity();
            }
        });


    }

    private void openProfilePictureActivity() {
        if (applicationUser.getProfileImageUri() != null) {
            Intent intent = new Intent(ProfileActivity.this, ProfilePictureActivity.class);
            intent.putExtra("userDetails", applicationUser);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this,
                    mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
            startActivity(intent, optionsCompat.toBundle());
        } else {
            Toast.makeText(this, "No profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void openCandidatesMenuActivity() {
        Intent intent = new Intent(ProfileActivity.this, CandidatesMenuActivity.class);
        intent.putExtra("userDetails", applicationUser);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this,
                mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
        startActivity(intent, optionsCompat.toBundle());
        finish();
    }

    public void openAgentsMenuActivity() {
        Intent intent = new Intent(ProfileActivity.this, AgentsMenuActivity.class);
        intent.putExtra("userDetails", applicationUser);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this,
                mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
        startActivity(intent, optionsCompat.toBundle());
        finish();
    }

}