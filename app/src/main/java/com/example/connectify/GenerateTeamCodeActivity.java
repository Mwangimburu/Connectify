package com.example.connectify;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Team;
import com.example.connectify.utils.BottomSheet;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;


public class GenerateTeamCodeActivity extends AppCompatActivity implements BottomSheet.BottomSheetListener {
    private static final int IMAGE_REQUEST_CODE = 1;
    TextInputLayout mTeamName,mTeamDesc, mTeamLocation;
    Button mGeneratedCode;
    ImageView mImageChooser,mBackArrow;
    CircleImageView  mTeamProfile;



    String teamName,teamDesc,teamLocation ;
    ApplicationUser applicationUser;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mTeamsReference;
    StorageReference mStorageReference;
    Utils utils;
    Uri imageUri, croppedImageUri;
    private int CAMERA_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_team_code);

        Intent intent = getIntent();
        applicationUser = intent.getParcelableExtra("userDetails");
        utils = new Utils();
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Create team code");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTeamName = findViewById(R.id.team_name);
        mGeneratedCode = findViewById(R.id.generate_code);
//        mBackArrow = findViewById(R.id.back_icon);
        mImageChooser = findViewById(R.id.image_chooser_icon);
        mTeamProfile = findViewById(R.id.team_profile);
        mTeamDesc = findViewById(R.id.team_desc);
        mTeamLocation = findViewById(R.id.team_area);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mTeamsReference = mFirebaseDatabase.getReference("Teams");
        mStorageReference = FirebaseStorage.getInstance().getReference("/uploads");

        mGeneratedCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamName = mTeamName.getEditText().getText().toString().trim();
                teamDesc = mTeamDesc.getEditText().getText().toString().trim();
                teamLocation = mTeamLocation.getEditText().getText().toString().trim();
                if (!validateTeamLocation() | !validateTeamDesc() | !validateTeamName()) {
                    return;
                }
                String teamUniqueCode = utils.generateCode(teamName);
                if (croppedImageUri != null && !croppedImageUri.equals(Uri.EMPTY)) {
                    //Image picked.
                    registerTeamWithImage(GenerateTeamCodeActivity.this, teamUniqueCode);

                } else {
                    //No image
                    showDialog(teamUniqueCode);
                }
            }
        });
//
//        mBackArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openCandidateMenu();
//            }
//        });

        mImageChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showImageProfileOptions();
            }
        });
    }

    private void showDialog(String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GenerateTeamCodeActivity.this);
        builder.setMessage("Continue with no team profile ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        registerTeamWithNoImage(GenerateTeamCodeActivity.this, code);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            startCroppingActivity(imageUri);
            return;
        }
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageUri = getImageUri(getApplicationContext(), photo);
            startCroppingActivity(imageUri);
            return;
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                croppedImageUri = result.getUri();
                Picasso.get().load(croppedImageUri).into(mTeamProfile);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private void startCroppingActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1080, 1080)
                .setMaxCropResultSize(1080, 1080)
                .setMinCropResultSize(540, 540)
                .setFixAspectRatio(true)
                .start(this);
    }

    public void showImageProfileOptions() {
        BottomSheet bottomSheet = new BottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private String getFileExt(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void registerTeamWithImage(Context context, String code) {
        utils.showProgress(context);
        StorageReference profileRef = mStorageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
        profileRef.putFile(croppedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String profileUri = uri.toString();
                        Team newTeam = new Team(code, teamName, applicationUser.getUserId(),teamDesc,teamLocation, profileUri);
                        mTeamsReference.child(code).setValue(newTeam).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                utils.hideProgress();
                                showCreatedCodeDialog(GenerateTeamCodeActivity.this, code);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                utils.hideProgress();
                                utils.showErrorDialog(context, e.getMessage());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.hideProgress();
                        utils.showErrorDialog(GenerateTeamCodeActivity.this, e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.hideProgress();
                utils.showErrorDialog(GenerateTeamCodeActivity.this, e.getMessage());
            }
        });

    }

    public void registerTeamWithNoImage(Context context, String teamCode) {
        utils.showProgress(context);
        Team newTeam = new Team(teamCode, teamName, applicationUser.getUserId(),teamDesc,teamLocation, null);
        mTeamsReference.child(teamCode).setValue(newTeam).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                utils.hideProgress();
                showCreatedCodeDialog(GenerateTeamCodeActivity.this, teamCode);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.hideProgress();
                utils.showErrorDialog(context, e.getMessage());
            }
        });
    }

    public void showCreatedCodeDialog(Context context, String code) {
        Dialog codeDialog = new Dialog(context);
        codeDialog.setContentView(R.layout.team_code_layout);
        codeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        codeDialog.getWindow().setGravity(Gravity.CENTER);
        codeDialog.setCancelable(false);
        TextView mCodeTextView = codeDialog.findViewById(R.id.code);
        mCodeTextView.setText(code);

        RelativeLayout mCopyLayout = codeDialog.findViewById(R.id.copy);
        mCopyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text = mCodeTextView.getText();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Code copied ", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Code copied ", Toast.LENGTH_LONG).show();
            }
        });

        Button mCancel = codeDialog.findViewById(R.id.cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeDialog.cancel();
                Intent candidateIntent = new Intent(GenerateTeamCodeActivity.this, CandidatesMenuActivity.class);
                candidateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                candidateIntent.putExtra("userDetails", applicationUser);
                startActivity(candidateIntent);

                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        codeDialog.show();
    }


    public boolean validateTeamName() {
        if (teamName.isEmpty()) {
            mTeamName.setError("Team name required");
            mTeamName.setErrorIconDrawable(null);
            mTeamName.requestFocus();
            return false;
        } else if (teamName.length() < 5) {
            mTeamName.setError("Team name should not have less than 5 characters");
            mTeamName.setErrorIconDrawable(null);
            mTeamName.requestFocus();
            return false;
        }
        mTeamName.setError("");
        return true;
    }
    private boolean validateTeamDesc(){
        if (teamDesc.isEmpty()){
            mTeamDesc.setErrorIconDrawable(null);
            mTeamDesc.setError("Team description required");
            mTeamDesc.requestFocus();
            return false;
        }
        mTeamDesc.setError(null);
        return true;
    }

    private boolean validateTeamLocation(){
         if (teamLocation.isEmpty()){
            mTeamLocation.setErrorIconDrawable(null);
            mTeamLocation.setError("Team location required");
            mTeamLocation.requestFocus();
            return false;
        }
        mTeamLocation.setError("");
         return  true;
    }

    public void openCandidateMenu() {
        Intent intent = new Intent(GenerateTeamCodeActivity.this, CandidatesMenuActivity.class);
        intent.putExtra("userDetails", applicationUser);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onCardListener(String cardName) {
        switch (cardName) {
            case "removeProfile":
                croppedImageUri = null;
                mTeamProfile.setImageDrawable(getDrawable(R.drawable.profile_holder));
                break;
            case "gallery":
                openImageChooser();
                break;
            case "camera":
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_CODE);
                break;

        }
    }
}