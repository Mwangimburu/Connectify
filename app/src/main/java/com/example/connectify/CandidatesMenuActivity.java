package com.example.connectify;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.connectify.models.Team;
import com.example.connectify.updates.CategoryActivity;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CandidatesMenuActivity extends AppCompatActivity {
    ImageView mBackArrow;
    CircleImageView mProfilePicture;
    TextView mDisplayName;
    RecyclerView recyclerView;
    Utils utils;
    RelativeLayout mProfileSectionRelativeLayout;
    String[] services = {"Post update", "View your Teams", "Create Team", "Log Out", "Delete Account"};
    int[] icons = {R.drawable.trending, R.drawable.round_people_dark,
            R.drawable.link_number, R.drawable.logout_icon, R.drawable.delete_account};
    ApplicationUser applicationUser;
    FirebaseAuth mAuth;
    Dialog mDeleteAccountDialog;
    Dialog mReAuthenticateDialog;
    String email, password;
    TextInputLayout mEmail, mPassword;
    List<Team> teams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidates_menu);
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
        applicationUser = getIntent().getParcelableExtra("userDetails");
        mDisplayName = findViewById(R.id.display_name);
        mProfilePicture = findViewById(R.id.profile_picture_menu);
        recyclerView = findViewById(R.id.recycler_view);
        mProfileSectionRelativeLayout = findViewById(R.id.profile_section_layout);
        // mBackArrow = findViewById(R.id.back_arrow);
        utils = new Utils();
        teams = new ArrayList<>();
        loadData();

        mAuth = FirebaseAuth.getInstance();

//        Fade fade = new Fade();
//        View decor = getWindow().getDecorView();
//        fade.excludeTarget(decor.findViewById(R.id.display_name), true);
//        fade.excludeTarget(decor.findViewById(R.id.view_profile), true);
//        fade.excludeTarget(decor.findViewById(R.id.recycler_view), true);
//        getWindow().setEnterTransition(fade);
//        getWindow().setExitTransition(fade);
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.package.ACTION_LOGOUT");
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                startActivity(new Intent(CandidatesMenuActivity.this, LoginActivity.class));
//                finish();
//            }
//        }, intentFilter);


        MenuRecyclerViewAdapter adapter = new MenuRecyclerViewAdapter(getApplicationContext(), services, icons);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(position -> {
            //Toast.makeText(CandidatesMenuActivity.this, services[position], Toast.LENGTH_SHORT).show();
            performAction(CandidatesMenuActivity.this, services[position]);
        });

        // mBackArrow.setOnClickListener(v -> onBackPressed());


        mProfileSectionRelativeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(CandidatesMenuActivity.this, ProfileActivity.class);
            intent.putExtra("userDetails", applicationUser);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(CandidatesMenuActivity.this,
                    mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
            startActivity(intent, optionsCompat.toBundle());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void performAction(Context context, String menuItemClicked) {
        switch (menuItemClicked) {
            case "View your Teams":
                openTeamsActivity();
                break;
            case "Create Team":
                Intent intent4;
                intent4 = new Intent(context, GenerateTeamCodeActivity.class);
                intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent4.putExtra("userDetails", applicationUser);
                startActivity(intent4);
                break;
            case "Log Out":
                logOut();
                break;
            case "Post update":
                getTeams();
                break;
            case "Delete Account":
                showDeleteAccountDialog(CandidatesMenuActivity.this, applicationUser.getProfileImageUri());
                break;
        }
    }

    public void getTeams() {
        utils.showProgress(CandidatesMenuActivity.this);
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mTeamsReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Toast.makeText(CandidatesMenuActivity.this, "No Teams", Toast.LENGTH_SHORT).show();
                } else {
                    openPostUpdateActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                utils.hideProgress();
                utils.showErrorDialog(CandidatesMenuActivity.this, error.getMessage());

            }
        });

    }

    public void openPostUpdateActivity() {
        Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("teamData", teams.get(0));
        intent.putExtra("userDetails", applicationUser);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openTeamsActivity() {
        Intent intent = new Intent(getApplicationContext(), TeamsActivity.class);
        intent.putExtra("userDetails", applicationUser);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(CandidatesMenuActivity.this,
                mProfilePicture, ViewCompat.getTransitionName(mProfilePicture));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent, optionsCompat.toBundle());
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CandidatesMenuActivity.this);
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

    private void openMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void loadData() {
        Picasso.get().load(applicationUser.getProfileImageUri())
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