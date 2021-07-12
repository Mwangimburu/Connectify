package com.example.connectify.updates;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.R;
import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Post;
import com.example.connectify.models.Team;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PostImageActivity extends AppCompatActivity {
    private static final String TAG = "PostImageActivity";
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int MULTIPLE_IMAGE_REQUEST_CODE = 1;
    ImageView mPostImage, mImageChooser;
    EditText mPostText;
    String postText;
    Uri imageUri, croppedImageUri;
    Button mPostImageBtn;
    Team team;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    FirebaseDatabase mFirebaseDatabase;
    ApplicationUser applicationUser;
    Utils utils;
    RecyclerView mImagesRecyclerView;
    List<Uri> imagesUrls;
    List<String> imagesDownLoadUrls;
    ImagesAdapter imagesAdapter;
    StorageTask uploadTask;
    int j = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);
        Intent intent = getIntent();
        team = intent.getParcelableExtra("teamData");
        applicationUser = intent.getParcelableExtra("userDetails");
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Post image");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("Team Data");
        mStorageReference = FirebaseStorage.getInstance().getReference("/uploads");
        mPostImage = findViewById(R.id.image);
        mImagesRecyclerView = findViewById(R.id.images);
        mImageChooser = findViewById(R.id.image_chooser);
        mPostText = findViewById(R.id.text);
        mPostImageBtn = findViewById(R.id.post_image_btn);
        utils = new Utils();
        imagesUrls = new ArrayList<>();
        imagesDownLoadUrls = new ArrayList<>();

        //   LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        imagesAdapter = new ImagesAdapter(imagesUrls);
        mImagesRecyclerView.setLayoutManager(gridLayoutManager);
        mImagesRecyclerView.setAdapter(imagesAdapter);

        mImageChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImages();
            }
        });


        mPostImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean stop = false;
                postText = mPostText.getText().toString().trim();

                if (imagesUrls.size() > 0) {
                    //Image picked.
                    imagesDownLoadUrls.clear();
                    utils.showProgress(PostImageActivity.this);
                    for (int i = 0; i < imagesUrls.size(); i++) {
                        StorageReference profileRef = mStorageReference.child(System.currentTimeMillis() + "." + getFileExt(imagesUrls.get(i)));
                        uploadTask = profileRef.putFile(imagesUrls.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String postUri = uri.toString();
                                        imagesDownLoadUrls.add(postUri);
                                        j++;
                                        if (j == imagesUrls.size()) {
                                            j = 0;
                                            Calendar calendar = Calendar.getInstance();
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
                                            String formattedDate = "" + calendar.getTime();
                                            String postId = mDatabaseReference.push().getKey();
                                            Post newPost = new Post(postText, imagesDownLoadUrls, formattedDate, applicationUser.getUserId());
                                            mDatabaseReference.child(team.getTeamCode()).child("Posts").child(postId).setValue(newPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    utils.hideProgress();
                                                    utils.showSuccessDialog(PostImageActivity.this, "Post created successfully");
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            utils.hideSuccess();
                                                            onBackPressed();
                                                        }
                                                    }, 3000);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                        }
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        utils.hideProgress();
                                        utils.showErrorDialog(PostImageActivity.this, e.getMessage());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                utils.hideProgress();
                                utils.showErrorDialog(PostImageActivity.this, e.getMessage());
                            }
                        });
                    }
                } else {
                    Toast.makeText(PostImageActivity.this, "At least one Image required", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), MULTIPLE_IMAGE_REQUEST_CODE);
    }


    private String getFileExt(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MULTIPLE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                imagesUrls.clear();
                int totalImages = data.getClipData().getItemCount();
                for (int i = 0; i < totalImages; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    imagesUrls.add(uri);
                }
                imagesAdapter.notifyDataSetChanged();
            } else if (data != null && data.getData() != null) {
                imagesUrls.clear();
                Uri imageUri = data.getData();
                imagesUrls.add(imageUri);
                imagesAdapter.notifyDataSetChanged();
            }
        }
//        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            startCroppingActivity(imageUri);
//            return;
//        }
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                croppedImageUri = result.getUri();
//                Picasso.get().load(croppedImageUri).into(mPostImage);
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
//            }
//
//        } else {
//            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
//        }
    }

    private void startCroppingActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1080, 1080)
                .setMaxCropResultSize(1080, 1080)
                .setMinCropResultSize(540, 540)
                .start(this);
    }


    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }
}