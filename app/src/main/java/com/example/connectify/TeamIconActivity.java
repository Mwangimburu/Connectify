package com.example.connectify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class TeamIconActivity extends AppCompatActivity {

    ImageView mTeamImage, mBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_icon);
        Intent intent = getIntent();
        String teamIconUrl = intent.getStringExtra("teamIconUrl");

        mTeamImage = findViewById(R.id.team_image);
        Picasso.get().load(teamIconUrl).into(mTeamImage);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Team icon");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        mBack = findViewById(R.id.back_arrow);
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

    private void openTeamActivity() {
        Intent intent = new Intent(TeamIconActivity.this, TeamActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(TeamIconActivity.this,
                mTeamImage, ViewCompat.getTransitionName(mTeamImage));
        startActivity(intent, optionsCompat.toBundle());
    }
}