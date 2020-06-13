package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class updateGroup_info extends AppCompatActivity {

    private CircleImageView groupPhoto, updatePicture;
    private String GroupPhotoCreated;
    private ImageView GroupImage = null;
    private EditText name, groupSubject;
    private StorageTask uploadTask;

    private FloatingActionButton create;
    private RelativeLayout layout;

    private static final int galleryPick = 1;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    private String currentGroupName, groupImage, currentUserID, groupStatus, currentGroupID;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_info);

        currentGroupName = getIntent().getExtras().get("group_Name").toString();
        groupImage = getIntent().getExtras().get("visit_group_Picture").toString();
        groupStatus = getIntent().getExtras().get("groupStatus").toString();
        currentGroupID = getIntent().getExtras().get("group_Id").toString();

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        groupPhoto = findViewById(R.id.groupPhoto);
        name = findViewById(R.id.groupName);
        groupSubject = findViewById(R.id.groupStatus);
        create = findViewById(R.id.createGroup);
        updatePicture = findViewById(R.id.update_picture);
        layout = findViewById(R.id.layoutUpdate);

        mToolbar = findViewById(R.id.update_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("Update Room Info");


        name.setText(currentGroupName);
        Picasso.get().load(groupImage).placeholder(R.drawable.group_icon1).into(groupPhoto);
        groupSubject.setText(groupStatus);

        GroupImage = groupPhoto;

        groupPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(updateGroup_info.this);
            }
        });

        updatePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(updateGroup_info.this);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupNom = name.getText().toString();
                String groupStatus = groupSubject.getText().toString();
                if(TextUtils.isEmpty(groupNom) && TextUtils.isEmpty(groupStatus))
                {
                    Toast.makeText(updateGroup_info.this, "Please fill in the blank", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("name", groupNom);
                    profileMap.put("groupStatus", groupStatus);

                    RootRef.child("Groups")
                            .child(currentGroupID)
                            .updateChildren(profileMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        onBackPressed();
                                        Toast.makeText(updateGroup_info.this, "Profile updated Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(updateGroup_info.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = updateGroup_info.this.getCurrentFocus();
                if (view != null)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    name.clearFocus();
                    groupSubject.clearFocus();
                }
            }
        });
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Updating Room Profile");
                loadingBar.setMessage("Please wait, while The picture is updating... ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                StorageReference groupImageRef = storageRef.child("WECHAT" + "/PROFILES/" + resultUri.toString().split("/")[resultUri.toString().split("/").length - 1]);

                InputStream stream = null;
                try {
                    stream = new FileInputStream(new File(resultUri.getPath()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                uploadTask = groupImageRef.putStream(stream);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(updateGroup_info.this, "Failed", Toast.LENGTH_SHORT).show();
                        String message = e.toString();
                        Toast.makeText(updateGroup_info.this, "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(updateGroup_info.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        groupImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String link = uri.toString();
                                Picasso.get().load(link).into(GroupImage);

                                RootRef.child("Groups").child(currentGroupID).child("photo")
                                        .setValue(link).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            loadingBar.dismiss();
                                            Toast.makeText(updateGroup_info.this, "Photo updated Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            loadingBar.dismiss();
                                            Toast.makeText(updateGroup_info.this, "Error Uploading", Toast.LENGTH_SHORT).show();
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
}
