package com.example.connectify;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Member;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class TeamMembersFragment extends Fragment {


    List<Team> team;
    RecyclerView mTeamsRecyclerView;
    ApplicationUser applicationUser;
    CircleImageView mTeamProfileImage;
    TextView mViewDetails;
    Utils utils;

    TextView mTitle;

    List<ApplicationUser> users;
    private static final String TAG = "TeamMembersFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        utils = new Utils();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            applicationUser = bundle.getParcelable("userDetails");
            Log.d(TAG, "onCreateView: userId " + applicationUser);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_members, container, false);
        mTeamsRecyclerView = view.findViewById(R.id.teams_recycler_view);
        mTitle = view.findViewById(R.id.title);
        mTeamProfileImage = view.findViewById(R.id.team_profile_picture);
        mViewDetails = view.findViewById(R.id.details);
        getTeams();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public void getTeams() {
        utils.showProgress(getActivity());
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mTeamsReference = mFirebaseDatabase.getReference("Teams");
        DatabaseReference mAgentTeamReference = mFirebaseDatabase.getReference("Team Members");
        team = new ArrayList<>();

        mAgentTeamReference.child(applicationUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Member member = snapshot.getValue(Member.class);
                if (member == null) {
                    mTitle.setText("Join a team to see members");
                    mTeamProfileImage.setVisibility(View.GONE);
                    utils.hideProgress();
                } else {
                    if (mTeamProfileImage.getVisibility() == View.GONE) {
                        mTeamProfileImage.setVisibility(View.VISIBLE);
                    }
                    mTeamsReference.child(member.getMemberTo()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Team team = snapshot.getValue(Team.class);
                            Picasso.get().load(team.getTeamProfilePicture()).placeholder(R.drawable.profile_holder).into(mTeamProfileImage);
                            mViewDetails.setText("view team details");
                            mTitle.setText(team.getTeamName());
                            String teamName = team.getTeamName();
                            String[] strArray = teamName.split(" ");
                            StringBuilder builder = new StringBuilder();
                            for (String s : strArray) {
                                String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                                builder.append(cap + " ");
                            }
                            mTitle.setText(builder.toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Log.d(TAG, "onDataChange: member " + member);
                    mAgentTeamReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<String> membersId = new ArrayList<>();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Member members = dataSnapshot.getValue(Member.class);
                                if (members.getMemberTo().equals(member.getMemberTo())) {
                                    membersId.add(members.getAgentId());
                                    Log.d(TAG, "onDataChange: members " + membersId);
                                }
                            }
                            users = new ArrayList<>();

                            DatabaseReference mUsersReference = mFirebaseDatabase.getReference("users");
                            mUsersReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        ApplicationUser members = dataSnapshot.getValue(ApplicationUser.class);
                                        for (String id : membersId) {
                                            if (members.getUserId().equals(id)) {
                                                users.add(members);
                                            }
                                        }
                                    }

                                    Log.d(TAG, "onDataChange: users " + users);
                                    initRecyclerView();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    utils.hideProgress();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            utils.hideProgress();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                utils.hideProgress();
                Log.d(TAG, "onCancelled: error " + error.getMessage());
            }
        });
    }

    public void initRecyclerView() {
        TeamMembersAdapter teamMembersAdapter = new TeamMembersAdapter(users);
        mTeamsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTeamsRecyclerView.setAdapter(teamMembersAdapter);
        utils.hideProgress();

        teamMembersAdapter.setOnItemClickListener(new TeamMembersAdapter.OnItemClickListener() {
            @Override
            public void itemClickListener(int position) {
                Toast.makeText(getActivity(), users.get(position).getFirstName(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
