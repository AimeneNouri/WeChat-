package com.example.wechat.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int galleryPick = 1;
    private String photoUrl = " ", calledBy = "";
    private StorageTask uploadTask;

    private CircleImageView UserImage, updateImage;
    private TextView UserEmail, phone_number;
    private EditText UserName, UserStatus;
    private Button UpdateAccount;
    private ProgressDialog loadingBar;
    private ImageButton BackToMain, LogOut;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private FirebaseUser user;
    private StorageReference UserImageRef;

    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        BackToMain = findViewById(R.id.backToMainActivity);
        UserImage = findViewById(R.id.profile_image);
        UserName = findViewById(R.id.Profile_username);
        UserEmail = findViewById(R.id.User_mail_profile);
        phone_number = findViewById(R.id.User_phoneNumber_profile);
        UserStatus = findViewById(R.id.profile_status);
        UpdateAccount = findViewById(R.id.updateSetting_button);
        updateImage = findViewById(R.id.update_picture);
        LogOut = findViewById(R.id.LogOut);
        loadingBar = new ProgressDialog(this);

        relativeLayout = findViewById(R.id.layoutSettings);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = SettingsActivity.this.getCurrentFocus();
                if (view != null)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    UserName.clearFocus();
                    UserStatus.clearFocus();
                }
            }
        });

        //check for email existing
        if (TextUtils.isEmpty(user.getEmail()))
        {
            UserEmail.setVisibility(View.GONE);
        }
        else
        {
            UserEmail.setText(user.getEmail());
        }

        //check for phone number existing
        if (TextUtils.isEmpty(user.getPhoneNumber()))
        {
            phone_number.setVisibility(View.GONE);
        }
        else
        {
            phone_number.setText(user.getPhoneNumber());
        }


        UpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               UpdateSettings();
            }
        });

        RetrieveUserInfo();

        BackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        UserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
            }
        });

        checkForReceivingCall();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(SettingsActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStatusMap = new HashMap<>();
        onlineStatusMap.put("time", saveCurrentTime);
        onlineStatusMap.put("date", saveCurrentDate);
        onlineStatusMap.put("state", state);

        RootRef.child("Users").child(currentUserId).child("UsersState")
                .updateChildren(onlineStatusMap);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage= dataSnapshot.child("image").getValue().toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(UserImage);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                            String retrieveStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                            UserName.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait until we update your profile image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                //StorageReference filePath = UserImageRef.child(currentUserId + ".jpg");

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                final StorageReference ImagesRef = storageRef.child("WECHAT" + "/PROFILES/" + resultUri.toString().split("/")[resultUri.toString().split("/").length - 1]);

                InputStream stream = null;
                try {
                    stream = new FileInputStream(new File(resultUri.getPath()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                uploadTask = ImagesRef.putStream(stream);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        String message = e.toString();
                        Toast.makeText(SettingsActivity.this, "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        ImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String link = uri.toString();
                                Picasso.get().load(link).into(UserImage);

                                RootRef.child("Users").child(currentUserId).child("image")
                                        .setValue(uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    loadingBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Photo saved Successfully", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    String message = task.getException().toString();
                                                    loadingBar.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainIntent);
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

  private void UpdateSettings() {
        String setUserName = UserName.getText().toString();
        String setUserstatus = UserStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please Write your Username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserstatus))
        {
            Toast.makeText(this, "Please Write your status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserstatus);
            //profileMap.put("image",photoUrl);

            RootRef.child("Users")
                    .child(currentUserId)
                    .updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile was updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void checkForReceivingCall()
    {
        RootRef.child("Users").child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("ringing"))
                        {
                            calledBy = dataSnapshot.child("ringing").getValue(String.class);

                            Intent CallIntent = new Intent(SettingsActivity.this, CallingActivity.class);
                            CallIntent.putExtra("visit_user_id", calledBy);
                            startActivity(CallIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
