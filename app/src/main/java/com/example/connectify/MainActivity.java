package com.example.connectify;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Post;
import com.example.connectify.models.Team;
import com.example.connectify.updates.SinglePostActivity;
import com.example.connectify.updates.SinglePostWithImageActivity;
import com.example.connectify.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FragmentCommunication {
    public static TabLayout tabLayout;
    private static final String TAG = "MainActivity";
    FirebaseAuth mAuth;
    DatabaseReference mUsersReference;
    FirebaseDatabase mFirebaseDatabase;
    ApplicationUser applicationUser;
    Utils utils;
    int tabPosition = 0;
    List<Team> teams;
    private Team team;
    private DatabaseReference mTeamsReference;
    public static BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // tabLayout = findViewById(R.id.tab_layout);
        teams = new ArrayList<>();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = mFirebaseDatabase.getReference("users");
        mTeamsReference = mFirebaseDatabase.getReference("Teams");
        utils = new Utils();
        getUser();
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.home_icon));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.tallies));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.notifications));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.video));
//        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#4285F4"));
//        tabLayout.setSelectedTabIndicatorHeight((int) (0 * getResources().getDisplayMetrics().density));
//        tabLayout.setTabTextColors(Color.parseColor("#696969"), Color.parseColor("#4285F4"));
//        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        TabLayout.Tab tab = MainActivity.tabLayout.getTabAt(0);
//        tab.select();
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                tabPosition = tab.getPosition();
//                switch (tabPosition) {
//                    case 0:
//                        initHomeFragment();
//                        break;
//                    case 1:
//                        VoteTalliesFragment voteTalliesFragment = new VoteTalliesFragment();
//                        fragmentTransactions(voteTalliesFragment, false);
//                        break;
//                    case 2:
//                        NotificationFragment notificationFragment = new NotificationFragment();
//                        fragmentTransactions(notificationFragment, false);
//                        break;
////                    case 3:
////                        if (applicationUser.getAccountType().equals("Candidate")) {
////                            CandidateTeams candidateTeams = new CandidateTeams();
////                            fragmentTransactions(candidateTeams, false);
////                        } else {
////                            TeamMembersFragment teamMembersFragment = new TeamMembersFragment();
////                            fragmentTransactions(teamMembersFragment, false);
////                        }
////                        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TeamMembersFragment()).commit();
////                        break;
//                }
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//            }
//        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home_nav_item:
                    initHomeFragment();
                    break;
                case R.id.tallies_nav_item:
                    VoteTalliesFragment voteTalliesFragment = new VoteTalliesFragment();
                    fragmentTransactions(voteTalliesFragment, false);
                    break;
                case R.id.notifications_nav_item:
                    NotificationFragment notificationFragment = new NotificationFragment();
                    fragmentTransactions(notificationFragment, false);
                    break;
            }
            return true;
        }

    };

    public void initHomeFragment() {
        HomeFragment homeFragment = new HomeFragment();
        fragmentTransactions(homeFragment, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.right_menu_icon, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_icon) {
            switch (applicationUser.getAccountType()) {
                case "Candidate":
                    Intent candidateIntent = new Intent(this, CandidatesMenuActivity.class);
                    candidateIntent.putExtra("userDetails", applicationUser);
                    startActivity(candidateIntent);
                    break;
                case "Campaign Agent":
                    Intent agentIntent = new Intent(this, AgentsMenuActivity.class);
                    agentIntent.putExtra("userDetails", applicationUser);
                    startActivity(agentIntent);
                    break;
            }

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    mAuth.signOut();
                    finish();
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void fragmentTransactions(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putParcelable("userDetails", applicationUser);
        fragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack("added");
        }
        fragmentTransaction.commit();
    }

    public void getUser() {
        utils.showProgress(MainActivity.this);
        FirebaseUser loggedInUser = mAuth.getCurrentUser();
        if (loggedInUser != null) {
            String uid = loggedInUser.getUid();
            mUsersReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    applicationUser = snapshot.getValue(ApplicationUser.class);
                    initHomeFragment();
                    utils.hideProgress();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    utils.showErrorDialog(MainActivity.this, error.getMessage());
                    Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();

                }
            });

        } else {
            openLoginActivity();
        }
    }


    private void openLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void sendPost(Post post) {
        Log.d(TAG, "sendPost: post " + post);
        if (post.getPostImagesUrl() == null) {
            Intent postWithNoImage = new Intent(MainActivity.this, SinglePostActivity.class);
            postWithNoImage.putExtra("postData", post);
            startActivity(postWithNoImage);
        } else {
            Intent postWithImage = new Intent(MainActivity.this, SinglePostWithImageActivity.class);
            postWithImage.putExtra("postData", post);
            startActivity(postWithImage);
        }

    }

}