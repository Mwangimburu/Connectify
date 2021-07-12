package com.example.connectify;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Member;
import com.example.connectify.models.Team;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TeamActivity extends AppCompatActivity {
    ImageView mTeamIcon;
    Team teamData;
    ApplicationUser applicationUser;
    List<ApplicationUser> teamMembers;
    RecyclerView mTeamMembersRecyclerView;
    TeamMembersAdapter teamMembersAdapter;
    TextView mAgentsLabel, mCreatorName, mTeamDescription, mNumbers;
    FirebaseAuth mAuth;
    DatabaseReference mUsersReference;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseStorage mStorage;
    RelativeLayout mExit, mDelete, mCopy;
    DatabaseReference mAgentTeamReference, mTeamsReference;
    String loggedInUserId;
    Utils utils;
    ValueEventListener mListener;
    private static final String TAG = "TeamActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);
        Intent intent = getIntent();
        teamData = intent.getParcelableExtra("teamData");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = mFirebaseDatabase.getReference("users");
        mAgentTeamReference = mFirebaseDatabase.getReference("Team Members");
        mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mStorage = FirebaseStorage.getInstance();
        FirebaseUser loggedInUser = mAuth.getCurrentUser();
        loggedInUserId = loggedInUser.getUid();
        getCandidateData();
        mTeamIcon = findViewById(R.id.team_image);
        mCreatorName = findViewById(R.id.creator_name);
        mTeamDescription = findViewById(R.id.description_text);
        Picasso.get().load(teamData.getTeamProfilePicture()).placeholder(R.drawable.round_people).into(mTeamIcon);
        String teamName = teamData.getTeamName();
        mTeamDescription.setText(teamData.getTeamDesc());
        if (mListener != null) {
            mAgentTeamReference.removeEventListener(mListener);
        }
        Toolbar mToolbar = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(teamName);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.collapsing_toolbar), true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        teamMembers = new ArrayList<>();
        mTeamMembersRecyclerView = findViewById(R.id.team_members);
        mAgentsLabel = findViewById(R.id.agents_label);
        mExit = findViewById(R.id.exit_layout);
        mDelete = findViewById(R.id.delete_layout);
        mCopy = findViewById(R.id.copy_code_layout);
        mNumbers = findViewById(R.id.numbers);
        utils = new Utils();
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTeam();

            }
        });
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitTeam();

            }
        });
        mCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTeamCode();
            }
        });

        mTeamIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTeamIconActivity();
            }
        });

        getTeamMembers();
        initRecyclerView();
    }

    private void copyTeamCode() {
        String text = "Connectify Team Code.\n\nUse this code to join my campaign team on Connectify \n\n" +
                "Team name : " + teamData.getTeamName() + "\n\n" +
                "Team code : " + teamData.getTeamCode() + "\n\n" +
                "Connectify assist you to conduct campaigns effectively from comfort of your phone";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Connectify");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Share with"));

//        CharSequence text = teamData.getTeamCode();
//        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData clip = ClipData.newPlainText("Code copied ", text);
//        clipboard.setPrimaryClip(clip);
//        Toast.makeText(getApplicationContext(), "Code copied ", Toast.LENGTH_LONG).show();
    }

    private void exitTeam() {
        if (teamData.getTeamCreatorId().equals(loggedInUserId)) {
            Toast.makeText(this, "You can only delete this team", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(TeamActivity.this);
            builder.setMessage("Are you sure you want to delete this team?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mAgentTeamReference.child(loggedInUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        onBackPressed();
                                        Toast.makeText(TeamActivity.this, "You have left the team", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(TeamActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

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

    private void deleteTeam() {
        if (loggedInUserId.equals(teamData.getTeamCreatorId())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TeamActivity.this);
            builder.setMessage("Are you sure you want to delete this team?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            utils.showProgress(TeamActivity.this);
                            mListener = mAgentTeamReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (loggedInUserId.equals(teamData.getTeamCreatorId())) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            Member existingMember = dataSnapshot.getValue(Member.class);
                                            if (existingMember.getMemberTo().equals(teamData.getTeamCode())) {
                                                mAgentTeamReference.child(existingMember.getAgentId()).removeValue();
                                            }
                                        }

                                        if (teamData.getTeamProfilePicture() != null) {
                                            StorageReference mProfileRef = mStorage.getReferenceFromUrl(teamData.getTeamProfilePicture());
                                            mProfileRef.delete();
                                        }

                                        mTeamsReference.child(teamData.getTeamCode()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    utils.hideProgress();
                                                    mAgentTeamReference.removeEventListener(mListener);
                                                    Toast.makeText(TeamActivity.this, "Team deleted", Toast.LENGTH_LONG).show();
                                                    onBackPressed();
                                                    if (applicationUser.getAccountType().equals("Campaign Agent")) {
                                                        childListener();
                                                    }

                                                } else {
                                                    utils.hideProgress();
                                                    mAgentTeamReference.removeEventListener(mListener);
                                                }
                                            }
                                        });
                                    } else {
                                        mAgentTeamReference.removeEventListener(mListener);
                                        utils.hideProgress();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    mAgentTeamReference.removeEventListener(mListener);
                                    utils.hideProgress();
                                    utils.showErrorDialog(TeamActivity.this, error.getMessage());
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();


        } else {

            Toast.makeText(this, "You can only exit this team", Toast.LENGTH_LONG).show();
        }

    }

    public void childListener() {
        mTeamsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Team removedTeam = snapshot.getValue(Team.class);
                Log.d(TAG, "onChildRemoved: deleted team " + removedTeam);

                ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> forGroundActivity = activityManager.getRunningTasks(1);
                ActivityManager.RunningTaskInfo currentActivity;
                currentActivity = forGroundActivity.get(0);
                String activityName = currentActivity.topActivity.getClassName();
                Toast.makeText(TeamActivity.this, activityName, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void openTeamIconActivity() {
        if (teamData.getTeamProfilePicture() != null) {
            Intent intent = new Intent(TeamActivity.this, TeamIconActivity.class);
            intent.putExtra("teamIconUrl", teamData.getTeamProfilePicture());
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(TeamActivity.this,
                    mTeamIcon, ViewCompat.getTransitionName(mTeamIcon));
            startActivity(intent, optionsCompat.toBundle());
        } else {
            Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show();
        }
    }

    public void getTeamMembers() {
        mAgentTeamReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> membersId = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Member memberAgent = dataSnapshot.getValue(Member.class);
                    if (memberAgent.getMemberTo().equals(teamData.getTeamCode())) {
                        membersId.add(memberAgent.getAgentId());
                    }
                }

                DatabaseReference mUsersReference = mFirebaseDatabase.getReference("users");
                mUsersReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        teamMembers.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ApplicationUser members = dataSnapshot.getValue(ApplicationUser.class);
                            for (String id : membersId) {
                                if (members.getUserId().equals(id)) {
                                    teamMembers.add(members);
                                }
                            }
                        }
                        teamMembersAdapter.notifyDataSetChanged();
                        int agentsCount = teamMembers.size();
                        if (agentsCount > 0) {
                            if (agentsCount == 1) {
                                mAgentsLabel.setText(agentsCount + "");
                                mNumbers.setText("Agent");
                            } else {
                                mAgentsLabel.setText(agentsCount + "");
                                mNumbers.setText("Agents");
                            }
                        } else {
                            mAgentsLabel.setText(agentsCount + "");
                            mNumbers.setText("No Agents");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void initRecyclerView() {
        teamMembersAdapter = new TeamMembersAdapter(teamMembers);
        mTeamMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mTeamMembersRecyclerView.setAdapter(teamMembersAdapter);


        teamMembersAdapter.setOnItemClickListener(new TeamMembersAdapter.OnItemClickListener() {
            @Override
            public void itemClickListener(int position) {
                Toast.makeText(getApplicationContext(), teamMembers.get(position).getFirstName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getCandidateData() {
        mUsersReference.child(teamData.getTeamCreatorId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationUser = snapshot.getValue(ApplicationUser.class);
                String names = applicationUser.getFirstName() + " " + applicationUser.getLastName();
                String[] strArray = names.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String s : strArray) {
                    String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                    builder.append(cap + " ");
                }
                mCreatorName.setText(builder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeamActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();

            }
        });
    }

}