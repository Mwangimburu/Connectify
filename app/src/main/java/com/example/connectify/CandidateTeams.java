package com.example.connectify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.models.Team;
import com.example.connectify.models.ApplicationUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CandidateTeams extends Fragment {
    ApplicationUser applicationUser;
    List<Team> teams;
    RecyclerView mTeams;
    private static final String TAG = "CandidateTeams";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            applicationUser = bundle.getParcelable("userDetails");
            Log.d(TAG, "onCreateView: userId " + applicationUser);
        }
        super.onCreate(savedInstanceState);
        teams = new ArrayList<>();
        getTeams();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_candidate_teams, container, false);
        mTeams = view.findViewById(R.id.teams);
        return view;
    }

    public void getTeams(){
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mTeamsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Team currentTeam = dataSnapshot.getValue(Team.class);

                    if (currentTeam.getTeamCreatorId().equals(applicationUser.getUserId())){
                        teams.add(currentTeam);
                    }
                }
                initRecyclerView();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void initRecyclerView(){
        TeamsAdapter teamsAdapter = new TeamsAdapter(teams);
        mTeams.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTeams.setAdapter(teamsAdapter);

        teamsAdapter.setItemClickListener(new TeamsAdapter.OnItemClickListener() {
            @Override
            public void itemClickListener(int position) {
                Toast.makeText(getActivity(), teams.get(position).getTeamCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}
