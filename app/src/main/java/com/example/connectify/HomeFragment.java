package com.example.connectify;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Member;
import com.example.connectify.models.Post;
import com.example.connectify.models.Team;
import com.example.connectify.updates.PostsAdapter;
import com.example.connectify.utils.Utils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    ApplicationUser applicationUser;
    Utils utils;
    FragmentCommunication fragmentCommunication;
    private static final String TAG = "HomeFragment";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTeamsReference;
    private List<Team> teams;
    private Team team;
    List<Post> posts;
    public static RecyclerView mPostRecyclerView;
    PostsAdapter adapter;
    SwipeRefreshLayout refreshLayout;
    RelativeLayout mPostsLayout;
    DatabaseReference mTeamsData;
    Button updates;
    BadgeDrawable badge;
    private int i = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        refreshLayout = view.findViewById(R.id.post_swipe);
        mPostsLayout = view.findViewById(R.id.posts);
        refreshLayout.setColorSchemeColors(Color.parseColor("#4285F4"), Color.BLACK, Color.RED);
        refreshLayout.setRefreshing(true);
        mPostRecyclerView = view.findViewById(R.id.post_recycler_view);
        adapter = new PostsAdapter(posts);
        mPostRecyclerView.setAdapter(adapter);
        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                teams.clear();
                posts.clear();
                adapter.notifyDataSetChanged();
                if (applicationUser.getAccountType().equals("Candidate")) {
                    getTeams(applicationUser.getUserId());
                } else {
                    getAgentTeam();
                }

            }
        });
        adapter.setOnCardClickListener(new PostsAdapter.onCardLickListener() {
            @Override
            public void onCardClick(int position) {
                Post postClicked = posts.get(position);
                fragmentCommunication.sendPost(postClicked);
            }
        });
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        utils = new Utils();
        teams = new ArrayList<>();
        posts = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mTeamsData = mFirebaseDatabase.getReference("Team Data");
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            applicationUser = bundle.getParcelable("userDetails");
            if (applicationUser.getAccountType().equals("Candidate")) {
                getTeams(applicationUser.getUserId());
            } else {
                getAgentTeam();
            }
        }
    }

    public void getTeams(String teamOwner) {
        //utils.showProgress(getContext());

        DatabaseReference mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mTeamsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teams.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Team currentTeam = dataSnapshot.getValue(Team.class);
                    if (currentTeam.getTeamCreatorId().equals(teamOwner)) {
                        teams.add(currentTeam);
                        mTeamsData.child(currentTeam.getTeamCode()).child("Posts").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                BottomNavigationView bottomNavigationView = MainActivity.bottomNavigationView;
                                if (bottomNavigationView.getBadge(R.id.home_nav_item) == null) {
                                    badge = bottomNavigationView.getOrCreateBadge(R.id.home_nav_item);
                                    badge.setBackgroundColor(Color.parseColor("#4285F4"));
                                }
//                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                                layoutParams.setMargins(10, 0, 0, 30);
//                                updates = new Button(mPostsLayout.getContext());
//                                updates.setText("Refresh for updates");
//                                updates.setId(i);
//                                updates.setLayoutParams(layoutParams);
//                                mPostsLayout.addView(updates,100,20);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                if (teams.size() == 0) {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "No Teams", Toast.LENGTH_LONG).show();
                } else {
                    for (Team team : teams) {
                        mTeamsData.child(team.getTeamCode()).child("Posts").orderByChild("postedOn").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Post post = dataSnapshot.getValue(Post.class);
                                    posts.add(post);
                                }
                                if (refreshLayout.isRefreshing()) {
                                    refreshLayout.setRefreshing(false);
                                }
                                adapter.notifyDataSetChanged();
                                if (posts.size() == 0) {
                                    Toast.makeText(getContext(), "No Data To Display", Toast.LENGTH_LONG).show();
                                }
                                BottomNavigationView bottomNavigationView = MainActivity.bottomNavigationView;
                                bottomNavigationView.removeBadge(R.id.home_nav_item);
                                Log.d(TAG, "onDataChange: Teams Data " + posts);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //utils.hideProgress();
                                utils.showErrorDialog(getContext(), error.getMessage());

                            }
                        });

                    }
                    Log.d(TAG, "onDataChange: teams " + teams);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //utils.hideProgress();
                utils.showErrorDialog(getContext(), error.getMessage());

            }
        });

    }

    public void getAgentTeam() {
        //utils.showProgress(getContext());
        DatabaseReference mAgentTeamReference = mFirebaseDatabase.getReference("Team Members");
        mAgentTeamReference.child(applicationUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Member member = snapshot.getValue(Member.class);
                if (member == null) {
                    //utils.hideProgress();
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), "You have no team", Toast.LENGTH_LONG).show();
                } else {
                    mTeamsReference.child(member.getMemberTo()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            team = snapshot.getValue(Team.class);
                            getTeams(team.getTeamCreatorId());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //utils.hideProgress();
                            utils.showErrorDialog(getContext(), error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //utils.hideProgress();
                utils.showErrorDialog(getContext(), error.getMessage());
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentCommunication = (FragmentCommunication) getContext();
    }

}
