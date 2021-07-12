package com.example.connectify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Team;
import com.example.connectify.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TeamsActivity extends AppCompatActivity {

    ImageView mProfilePicture;
    ApplicationUser applicationUser;
    List<Team> teams;
    RecyclerView mTeams;
    TeamsAdapter teamsAdapter;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);
        Intent intent = getIntent();
        applicationUser = intent.getParcelableExtra("userDetails");

        Toolbar mToolbar = findViewById(R.id.collapsing_toolbar_teams);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Your teams");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        teams = new ArrayList<>();
        mTeams = findViewById(R.id.teams_recyclerView);
        utils = new Utils();

        mProfilePicture = findViewById(R.id.creator_image);
        Picasso.get().load(applicationUser.getProfileImageUri()).placeholder(R.drawable.profile_holder).into(mProfilePicture);


        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfilePictureActivity();
            }
        });

        getTeams();
        initRecyclerView();


    }

    private void openProfilePictureActivity() {
        if (applicationUser.getProfileImageUri() != null) {
            Intent intent = new Intent(TeamsActivity.this, ProfilePictureActivity.class);
            intent.putExtra("userDetails", applicationUser);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(TeamsActivity.this,
                    mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
            startActivity(intent, optionsCompat.toBundle());
        }else {
            Toast.makeText(this, "No profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    public void getTeams() {
        utils.showProgress(TeamsActivity.this);
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mTeamsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teams.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Team currentTeam = dataSnapshot.getValue(Team.class);
                    if (currentTeam.getTeamCreatorId().equals(applicationUser.getUserId())) {
                        teams.add(currentTeam);
                    }
                }
                utils.hideProgress();
                if (teams.size() == 0) {
                    Toast.makeText(TeamsActivity.this, "No Teams", Toast.LENGTH_SHORT).show();
                }
                teamsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                utils.hideProgress();
                utils.showErrorDialog(TeamsActivity.this, error.getMessage());

            }
        });

    }



    public void initRecyclerView() {
        teamsAdapter = new TeamsAdapter(teams);
        mTeams.setLayoutManager(new LinearLayoutManager(this));
        mTeams.setAdapter(teamsAdapter);

        teamsAdapter.setItemClickListener(new TeamsAdapter.OnItemClickListener() {
            @Override
            public void itemClickListener(int position) {
                Intent intent = new Intent(getApplicationContext(), TeamActivity.class);
                intent.putExtra("teamData", teams.get(position));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }


}