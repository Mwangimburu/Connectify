package com.example.connectify.updates;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.R;
import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SinglePostWithImageActivity extends AppCompatActivity {
    Post post;
    CircleImageView creatorProfile;
    TextView mName, mTime, mPostText;
    private ApplicationUser applicationUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mUsersReference;
    RecyclerView mImagesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post_with_image);
        Intent intent = getIntent();
        post = intent.getParcelableExtra("postData");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = mFirebaseDatabase.getReference("users");
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Post");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mImagesRecyclerView = findViewById(R.id.post_images);
        PostImagesAdapter postImagesAdapter = new PostImagesAdapter(post.getPostImagesUrl());
        mImagesRecyclerView.setAdapter(postImagesAdapter);
        mImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        creatorProfile = findViewById(R.id.post_creator_profile);
        mTime = findViewById(R.id.time);
        mName = findViewById(R.id.name);
        mPostText = findViewById(R.id.text);
        mPostText.setText(post.getPostText());
        mTime.setText(post.getPostedOn());

        mUsersReference.child(post.getPostedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationUser = snapshot.getValue(ApplicationUser.class);
                Picasso.get().load(applicationUser.getProfileImageUri()).placeholder(R.drawable.profile_holder).into(creatorProfile);
                String names = applicationUser.getFirstName() + " " + applicationUser.getLastName();
                String[] strArray = names.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String s : strArray) {
                    String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                    builder.append(cap + " ");
                }
                mName.setText(builder.toString());
                getSupportActionBar().setTitle(builder.toString() + " Post");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}