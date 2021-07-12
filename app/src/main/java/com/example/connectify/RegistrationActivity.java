package com.example.connectify;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.connectify.models.ApplicationUser;
import com.example.connectify.utils.BottomSheet;
import com.example.connectify.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity implements BottomSheet.BottomSheetListener {
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    //"(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,10}" +               //at least 6 characters
                    "$");
    private static final int CAMERA_CODE = 1;

    TextView mRegistration, mPersonalDetails, mAccount, mLocation, mSignInLink;
    ImageView mBackArrow;

    CircleImageView mProfile;
    ImageView mProfileImageChooser;
    TextInputLayout mFirstName, mLastName, mEmail, mPassword,
            mConfirmPassword, mAccountTypeLayout, mCandidatePositionLayout,
            mCountyLayout, mConstituencyLayout, mWardLayout;

    AutoCompleteTextView mAccountType, mCandidatePosition, mCounty, mConstituency, mWard;

    Button mSignUp;
    String[] accountTypes = {"Candidate", "Campaign Agent"};
    String firstName, lastName, email, password, confirmPassword, accountType, county, constituency, ward;
    Uri imageUri, croppedImageUri;
    JSONArray subCounties;
    ArrayList<String> countiesLists;
    ArrayList<String> su_countiesLists;
    ArrayList<String> wards_list;

    DatabaseReference mLocationDatabaseReference;
    DatabaseReference mUsersDatabaseReference;
    FirebaseDatabase mFirebaseDatabase;
    FirebaseAuth mAuth;
    StorageReference mStorageReference;
    Utils utils;

    private final String successMessage = "Registration Successfully Processed. \n \n" + "Please wait as we redirect you";

    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //mRegistration = findViewById(R.id.reg_text_view);
//        mPersonalDetails = findViewById(R.id.personal_details_section);
        mAccount = findViewById(R.id.account_section);
        mLocation = findViewById(R.id.location_details_section);
        mSignInLink = findViewById(R.id.sign_in_link);
        //mBackArrow = findViewById(R.id.back_img);
        mAccountTypeLayout = findViewById(R.id.actyp);
        mCountyLayout = findViewById(R.id.cnty);
        mConstituencyLayout = findViewById(R.id.consist);
        mWardLayout = findViewById(R.id.wrd);
        mProfile = findViewById(R.id.profile);
        mProfileImageChooser = findViewById(R.id.image_chooser);
        mFirstName = findViewById(R.id.first_name);
        mLastName = findViewById(R.id.last_name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.c_password);
        mAccountType = findViewById(R.id.account_type);
        mCounty = findViewById(R.id.county);
        mConstituency = findViewById(R.id.constituency);
        mWard = findViewById(R.id.ward);
        mSignUp = findViewById(R.id.sign_in_btn);
        utils = new Utils();

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Create account");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference("/uploads");
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference("users");
        mLocationDatabaseReference = mFirebaseDatabase.getReference("location");

        ArrayAdapter<String> accountTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountTypes);
        mAccountType.setAdapter(accountTypeAdapter);
        mAccountType.setText("Select type", false);

        mProfileImageChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageProfileOptions();
            }
        });

        mAccountType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyBoard();
            }
        });

        mAccountType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAccountTypeLayout.setError(null);
                accountType = parent.getItemAtPosition(position).toString();
            }
        });


        mSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        mBackArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });


        countiesLists = new ArrayList<>();
        su_countiesLists = new ArrayList<>();
        wards_list = new ArrayList<>();


        ArrayAdapter<String> countyAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countiesLists);
        ArrayAdapter<String> subCountyAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, su_countiesLists);
        ArrayAdapter<String> wards =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wards_list);
        mWard.setAdapter(wards);
        mCounty.setAdapter(countyAdapter);
        mConstituency.setAdapter(subCountyAdapter);

        initCounties();

        mCounty.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCountyLayout.setError(null);
                county = parent.getItemAtPosition(position).toString();
                Toast.makeText(RegistrationActivity.this, county, Toast.LENGTH_SHORT).show();
                su_countiesLists.clear();
                mConstituency.setText("Select sub county", false);
                wards_list.clear();
                mWard.setText("Select ward", false);
                constituency = null;
                ward = null;

                initSubCounties(county);
            }
        });

        mConstituency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mConstituencyLayout.setError(null);
                constituency = parent.getItemAtPosition(position).toString();
                wards_list.clear();
                mWard.setText("Select ward", false);
                initWards(constituency);
            }
        });

        mWard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWardLayout.setError(null);
                ward = parent.getItemAtPosition(position).toString();
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = mFirstName.getEditText().getText().toString().trim();
                lastName = mLastName.getEditText().getText().toString().trim();
                email = mEmail.getEditText().getText().toString().trim();
                password = mPassword.getEditText().getText().toString().trim();
                confirmPassword = mConfirmPassword.getEditText().getText().toString().trim();

                if (!validateAccountType()
                        | !validateConfirmationPassword()
                        | !validatePassword()
                        | !validateEmail()
                        | !validateLastName()
                        | !validateFirstName()
                        | !validateCounty()
                        | !validateConstituency()
                        | !validateWard()
                ) {

                    return;
                }

                if (croppedImageUri != null && !croppedImageUri.equals(Uri.EMPTY)) {
                    //Image picked.
                    registerWithImage();

                } else {
                    //No image
                    showDialog();
                }

            }
        });

    }

    public void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setMessage("Continue with no profile picture ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        registerWithNoImage();
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

    private String getFileExt(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }


    public boolean validateCounty() {
        if (county == null) {
            mCountyLayout.setError("County required");
            return false;
        }
        mCountyLayout.setError(null);
        return true;
    }

    public boolean validateConstituency() {
        if (constituency == null) {
            mConstituencyLayout.setError("Constituency required");
            return false;
        }
        mConstituencyLayout.setError(null);
        return true;
    }

    public boolean validateWard() {
        if (ward == null) {
            mWardLayout.setError("Ward required");
            return false;
        }
        mWardLayout.setError(null);
        return true;
    }


    public void registerWithImage() {
        utils.showProgress(RegistrationActivity.this);
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                StorageReference profileRef = mStorageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
                profileRef.putFile(croppedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String image_uri = uri.toString();
                                String uid = mAuth.getCurrentUser().getUid();
                                String agentId = mAuth.getCurrentUser().getUid();
                                ApplicationUser applicationUser = new ApplicationUser(uid, firstName, lastName, accountType, county, constituency, ward, image_uri);
                                mUsersDatabaseReference.child(agentId).setValue(applicationUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            utils.hideProgress();
                                            redirect();
                                            utils.showSuccessDialog(RegistrationActivity.this, successMessage);

                                        } else {
                                            utils.hideProgress();
                                            utils.showErrorDialog(RegistrationActivity.this, task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                utils.hideProgress();
                                utils.showErrorDialog(RegistrationActivity.this, e.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.hideProgress();
                        utils.showErrorDialog(RegistrationActivity.this, e.getMessage());
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.hideProgress();
                utils.showErrorDialog(RegistrationActivity.this, e.getMessage());
            }
        });


    }

    public void registerWithNoImage() {
        utils.showProgress(RegistrationActivity.this);
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String agentId = mAuth.getCurrentUser().getUid();
                ApplicationUser agent = new ApplicationUser(agentId, firstName, lastName, accountType, county, constituency, ward, null);
                mUsersDatabaseReference.child(agentId).setValue(agent).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            utils.hideProgress();
                            redirect();
                            utils.showSuccessDialog(RegistrationActivity.this, successMessage);

                        } else {
                            utils.hideProgress();
                            utils.showErrorDialog(RegistrationActivity.this, task.getException().toString());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.hideProgress();
                utils.showErrorDialog(RegistrationActivity.this, e.getMessage());

            }
        });
    }

    private boolean validateAccountType() {
        if (accountType == null) {
            mAccountTypeLayout.setError("Required");
            return false;
        }
        return true;
    }

    private boolean validateFirstName() {
        if (firstName.isEmpty()) {
            mFirstName.setError("Please provide first name");
            mFirstName.setErrorIconDrawable(null);
            mFirstName.requestFocus();
            return false;
        }
        mFirstName.setError(null);
        return true;
    }

    private boolean validateLastName() {
        if (lastName.isEmpty()) {
            mLastName.setError("Please provide last name");
            mLastName.setErrorIconDrawable(null);
            mLastName.requestFocus();
            return false;
        }
        mLastName.setError(null);
        return true;
    }

    public void redirect() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        }, 5000);
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
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            mPassword.setError(
                    "Password should contain at least 1 uppercase \n" +
                            "Password should contain at least 1 lowercase \n" +
                            "Password should contain at least 1 numeric \n" +
                            "Password should have at least 6 and max 10 characters\n");
            mPassword.requestFocus();
            mPassword.setErrorIconDrawable(null);
            return false;
        } else {
            mPassword.setError(null);
            mPassword.setErrorEnabled(false);
            return true;
        }
    }

    public boolean validateConfirmationPassword() {
        if (confirmPassword.isEmpty()) {
            mConfirmPassword.setError("Please fill this field");
            mConfirmPassword.setErrorIconDrawable(null);
            mConfirmPassword.requestFocus();
            return false;
        } else if (!confirmPassword.equals(password)) {
            mConfirmPassword.setError("Password don't match");
            mConfirmPassword.setErrorIconDrawable(null);
            mConfirmPassword.requestFocus();
            return false;
        }
        mConfirmPassword.setError(null);
        return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            imageUri = data.getData();
            startCroppingActivity(imageUri);
            Log.d(TAG, "onActivityResult: picker results");
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
            Log.d(TAG, "onActivityResult: cropped results");
            if (resultCode == RESULT_OK) {
                croppedImageUri = result.getUri();
                Picasso.get().load(croppedImageUri).into(mProfile);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    private void openLoginWithOutDelayActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void initCounties() {
        try {
            JSONObject object = new JSONObject(readJSON());
            JSONArray array = object.getJSONArray("counties");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String name = jsonObject.getString("name");
                countiesLists.add(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initSubCounties(String selectedCountyName) {
        try {
            JSONObject object = new JSONObject(readJSON());
            JSONArray allCounties = object.getJSONArray("counties");
            for (int i = 0; i < allCounties.length(); i++) {
                JSONObject selectedCountyObject = allCounties.getJSONObject(i);
                String countyName = selectedCountyObject.getString("name");
                if (countyName.equals(selectedCountyName)) {
                    subCounties = selectedCountyObject.getJSONArray("sub_counties");
                    for (int j = 0; j < subCounties.length(); j++) {
                        JSONObject subCountyObject = subCounties.getJSONObject(j);
                        String subCountyName = subCountyObject.getString("sub_county_name");
                        su_countiesLists.add(subCountyName);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void initWards(String sub_count) {
        try {
            for (int i = 0; i < subCounties.length(); i++) {

                JSONObject ob = subCounties.getJSONObject(i);
                String sub_name = ob.getString("sub_county_name");

                if (sub_name.equals(sub_count)) {
                    JSONArray arr = ob.getJSONArray("wards");
                    for (int k = 0; k < arr.length(); k++) {
                        JSONObject wardObject = arr.getJSONObject(k);
                        String wardName = wardObject.getString("ward_name");
                        wards_list.add(wardName);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String readJSON() {
        String json = null;
        try {
            // Opening data.json file
            InputStream inputStream = getAssets().open("counties.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            inputStream.read(buffer);
            inputStream.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return json;
        }
        return json;
    }

    @Override
    public void onCardListener(String cardName) {
        switch (cardName) {
            case "removeProfile":
                croppedImageUri = null;
                mProfile.setImageDrawable(getDrawable(R.drawable.profile_holder));
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