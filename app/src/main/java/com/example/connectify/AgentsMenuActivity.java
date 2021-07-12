package com.example.connectify;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.connectify.updates.CategoryActivity;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AgentsMenuActivity extends AppCompatActivity {
    private static final String TAG = "AgentsMenuActivity";

    CircleImageView mProfilePicture;
    TextView mDisplayName;
    ImageView mBackArrow;
    RecyclerView recyclerView;
    RelativeLayout mProfileSectionRelativeLayout;

    String[] services = {"Post update", "View Your Team", "Join Team", "Log Out", "Delete Account"};
    int[] icons = {R.drawable.trending, R.drawable.round_people_dark, R.drawable.join_icon, R.drawable.logout_icon, R.drawable.delete_account};

    ApplicationUser applicationUser;
    DatabaseReference mTeamsReference;
    DatabaseReference mAgentTeamReference;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseAuth mAuth;
    Dialog mDeleteAccountDialog;
    Dialog mReAuthenticateDialog;

    String teamCode;
    Team team;
    Utils utils;
    String email, password;
    TextInputLayout mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agents_menu);
        Log.d(TAG, "onCreate: called");
        applicationUser = getIntent().getParcelableExtra("userDetails");
//        Fade fade = new Fade();
//        View decor = getWindow().getDecorView();
//        fade.excludeTarget(decor.findViewById(R.id.display_name), true);
//        fade.excludeTarget(decor.findViewById(R.id.view_profile), true);
//        fade.excludeTarget(decor.findViewById(R.id.recycler_view), true);
//        getWindow().setEnterTransition(fade);
//        getWindow().setExitTransition(fade);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mAgentTeamReference = mFirebaseDatabase.getReference("Team Members");
        mAuth = FirebaseAuth.getInstance();
        utils = new Utils();

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Menu");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.package.ACTION_LOGOUT");
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                startActivity(new Intent(AgentsMenuActivity.this, LoginActivity.class));
//                finish();
//            }
//        }, intentFilter);

        recyclerView = findViewById(R.id.recycler_view);
        mProfileSectionRelativeLayout = findViewById(R.id.profile_section_layout);
        // mBackArrow = findViewById(R.id.back_arrow);
        mProfilePicture = findViewById(R.id.profile_picture_agent);
        mDisplayName = findViewById(R.id.display_name);
        loadData();


        MenuRecyclerViewAdapter adapter = new MenuRecyclerViewAdapter(getApplicationContext(), services, icons);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(new MenuRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                performAction(AgentsMenuActivity.this, services[position]);
            }
        });


        mProfileSectionRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AgentsMenuActivity.this, ProfileActivity.class);
                intent.putExtra("userDetails", applicationUser);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(AgentsMenuActivity.this,
                        mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
                startActivity(intent, optionsCompat.toBundle());
            }
        });
//        mBackArrow.setOnClickListener(new View.OnClickListener() {
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

    public void performAction(Context context, String menuItemClicked) {
        switch (menuItemClicked) {
            case "Log Out":
                logOut();
                break;
            case "Post update":
            case "View Your Team":
                getAgentTeam(menuItemClicked);
                break;
            case "Join Team":
                showJoinDialog(AgentsMenuActivity.this);
                break;
            case "Delete Account":
                showDeleteAccountDialog(AgentsMenuActivity.this, applicationUser.getProfileImageUri());
                break;
        }
    }

    public void openPostUpdateActivity() {
        Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
        intent.putExtra("teamData", team);
        intent.putExtra("userDetails",applicationUser);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openTeamActivity() {
        Intent intent = new Intent(getApplicationContext(), TeamActivity.class);
        intent.putExtra("teamData", team);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void getAgentTeam(String menu) {
        utils.showProgress(AgentsMenuActivity.this);
        mAgentTeamReference.child(applicationUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Member member = snapshot.getValue(Member.class);
                if (member == null) {
                    utils.hideProgress();
                    Toast.makeText(AgentsMenuActivity.this, "You have no team", Toast.LENGTH_LONG).show();
                } else {
                    mTeamsReference.child(member.getMemberTo()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            team = snapshot.getValue(Team.class);
                            utils.hideProgress();
                            switch (menu) {
                                case "Post update":
                                    openPostUpdateActivity();
                                    break;
                                case "View Your Team":
                                    openTeamActivity();
                                    break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            utils.hideProgress();
                            utils.showErrorDialog(AgentsMenuActivity.this, error.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                utils.hideProgress();
                utils.showErrorDialog(AgentsMenuActivity.this, error.getMessage());
            }
        });
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsMenuActivity.this);
        builder.setMessage("Are you sure to log out? ")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mAuth.signOut();
                        openMainActivity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showJoinDialog(Context context) {
        Dialog joinDialog = new Dialog(context);
        joinDialog.setContentView(R.layout.join_team_layout);
        joinDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        joinDialog.getWindow().setGravity(Gravity.CENTER);
        joinDialog.setCancelable(true);
        TextInputLayout mCode = joinDialog.findViewById(R.id.code);
        Button mJoin = joinDialog.findViewById(R.id.join);
        ImageView mCancel = joinDialog.findViewById(R.id.cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinDialog.cancel();
            }
        });
        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamCode = mCode.getEditText().getText().toString().trim();
                if (!validatedCode()) {
                    return;
                }
                getTeamDetails(teamCode);
                joinDialog.cancel();
            }
        });

        joinDialog.show();
    }

    private boolean validatedCode() {
        if (teamCode.isEmpty()) {
            Toast.makeText(this, "Joining code required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void getTeamDetails(String code) {
        utils.showProgress(AgentsMenuActivity.this);
        String upperCaseCode = code.toUpperCase();
        mTeamsReference.child(upperCaseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Team team = snapshot.getValue(Team.class);
                utils.hideProgress();
                if (team != null) {
                    showConfirmationDialog(team.getTeamName(), upperCaseCode);
                } else {
                    utils.hideProgress();
                    utils.showErrorDialog(AgentsMenuActivity.this, "No team with ID " + upperCaseCode + " found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    public void deleteAccount(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsMenuActivity.this);
//        builder.setMessage("")
//                .setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        joinTeam(code, capsTeamName);
//                        dialog.cancel();
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }

    public void showConfirmationDialog(String teamName, String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AgentsMenuActivity.this);
        String capsTeamName = teamName.toUpperCase();
        builder.setMessage("Please confirm to join " + capsTeamName + " team")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        joinTeam(code, capsTeamName);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void joinTeam(String joiningCode, String name) {
        mAgentTeamReference.child(applicationUser.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Member existingMember = snapshot.getValue(Member.class);
                if (existingMember != null) {
                    utils.showErrorDialog(AgentsMenuActivity.this, "You are already in a team !!");
                } else {
                    Member newMember = new Member(joiningCode, applicationUser.getUserId());
                    mAgentTeamReference.child(applicationUser.getUserId()).setValue(newMember).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            utils.showSuccessDialog(AgentsMenuActivity.this, "You have successfully joined " + name + " team");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    utils.hideSuccess();
                                }
                            }, 2000);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            utils.showErrorDialog(AgentsMenuActivity.this, "Failed to join " + name);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void openMainActivity() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public void loadData() {
        Picasso.get()
                .load(applicationUser.getProfileImageUri())
                .placeholder(R.drawable.profile_holder)
                .into(mProfilePicture);
        String names = applicationUser.getFirstName() + " " + applicationUser.getLastName();
        String[] strArray = names.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        mDisplayName.setText(builder.toString());
    }

    //deleting account
    public void showDeleteAccountDialog(Context context, String profileUrl) {
        mDeleteAccountDialog = new Dialog(context);
        mDeleteAccountDialog.setContentView(R.layout.delete_account_layout);
        mDeleteAccountDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mDeleteAccountDialog.getWindow().setGravity(Gravity.CENTER);
        mDeleteAccountDialog.setCancelable(true);
        mDeleteAccountDialog.show();

        Button mCancel = mDeleteAccountDialog.findViewById(R.id.cancel_delete);
        Button mDeleteAccount = mDeleteAccountDialog.findViewById(R.id.delete);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteAccountDialog.cancel();
            }
        });

        mDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteAccountDialog.cancel();
                //verifying you dialog
                showReAuthenticateDialog(context, profileUrl);
            }
        });


    }

    public void showReAuthenticateDialog(Context context, String profileUrl) {
        mReAuthenticateDialog = new Dialog(context);
        mReAuthenticateDialog.setContentView(R.layout.re_authenticate_layout);
        mReAuthenticateDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mReAuthenticateDialog.getWindow().setGravity(Gravity.CENTER);
        mReAuthenticateDialog.setCancelable(true);
        mReAuthenticateDialog.show();

        Button mCancel = mReAuthenticateDialog.findViewById(R.id.cancel_delete);
        Button mDeleteAccount = mReAuthenticateDialog.findViewById(R.id.delete_btn);
        mEmail = mReAuthenticateDialog.findViewById(R.id.email);
        mPassword = mReAuthenticateDialog.findViewById(R.id.lg_password);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReAuthenticateDialog.cancel();
            }
        });

        mDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getEditText().getText().toString();
                password = mPassword.getEditText().getText().toString();
                if (!validatePassword() | !validateEmail()) {
                    return;
                }
                reAuthenticate(context, profileUrl);
            }
        });
    }

    //re-authenticate and delete all associated data
    public void reAuthenticate(Context context, String profileUrl) {
        utils.showProgress(context);
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mUsersDatabaseReference = mFirebaseDatabase.getReference("users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        currentUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (profileUrl != null) {
                        StorageReference mProfileRef = mStorage.getReferenceFromUrl(profileUrl);
                        mProfileRef.delete();
                    }
                    mAgentTeamReference.child(currentUser.getUid()).removeValue();
                    mUsersDatabaseReference.child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mReAuthenticateDialog.cancel();
                                            utils.hideProgress();
                                            mAuth.signOut();
                                            openMainActivity();
                                            Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mReAuthenticateDialog.cancel();
                                            utils.hideProgress();
                                            utils.showErrorDialog(context, task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                mReAuthenticateDialog.cancel();
                                utils.hideProgress();
                                utils.showErrorDialog(context, task.getException().getMessage());

                            }
                        }
                    });
                } else {
                    utils.hideProgress();
                    utils.showErrorDialog(context, task.getException().getMessage());
                }
            }
        });

    }

    private boolean validateEmail() {
        if (email.isEmpty()) {
            mEmail.setError("Please provide email");
            mEmail.setErrorIconDrawable(null);
            mEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Invalid email");
            mEmail.setErrorIconDrawable(null);
            mEmail.requestFocus();
            return false;
        } else {
            mEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {

        if (password.isEmpty()) {
            mPassword.setError("Please fill this field");
            mPassword.setErrorIconDrawable(null);
            mPassword.requestFocus();
            return false;
        } else {
            mPassword.setError(null);
            mPassword.setErrorEnabled(false);
            return true;
        }
    }


}