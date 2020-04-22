package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int galleryPick = 1;

    private CircleImageView UserImage;
    private TextView BackToMain, UserEmail, phone_number;
    private EditText UserName, UserStatus;
    private Button UpdateAccount;
    private ProgressDialog loadingBar;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private FirebaseUser user;
    private StorageReference UserImageRef;


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
        UserImage = (CircleImageView) findViewById(R.id.profile_image);
        UserName = (EditText) findViewById(R.id.Profile_username);
        UserEmail = (TextView) findViewById(R.id.User_mail_profile);
        phone_number = (TextView) findViewById(R.id.User_phoneNumber_profile);
        UserStatus = (EditText) findViewById(R.id.profile_status);
        UpdateAccount = (Button) findViewById(R.id.updateSetting_button);
        loadingBar = new ProgressDialog(this);

        UpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        UserEmail.setText(user.getEmail());
        phone_number.setText(user.getPhoneNumber());

        BackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(homeIntent);
            }
        });

        UserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPick  &&  resultCode == RESULT_OK  &&  data != null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait until we updating you profile image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();

                                RootRef.child("Users").child(currentUserId).child("image")
                                        .setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    loadingBar.dismiss();
                                                }
                                                else{
                                                    String message = task.getException().toString();
                                                    Toast.makeText(SettingsActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();

                                                }
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
            HashMap<String, String> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserId);
                profileMap.put("name", setUserName);
                profileMap.put("status", setUserstatus);

            RootRef.child("Users")
                    .child(currentUserId)
                    .setValue(profileMap)
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

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveUserImage = dataSnapshot.child("image").getValue().toString();

                    UserName.setText(retrieveUserName);
                    UserStatus.setText(retrieveUserStatus);
                    Picasso.get().load(retrieveUserImage).into(UserImage);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                    UserName.setText(retrieveUserName);
                    UserStatus.setText(retrieveUserStatus);
                }
                else
                {

                    Toast.makeText(SettingsActivity.this, "Please update your profile information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
