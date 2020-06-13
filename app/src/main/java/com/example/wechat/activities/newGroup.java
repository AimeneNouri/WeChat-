package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.wechat.Groups;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class newGroup extends AppCompatActivity {

    private CircleImageView groupPhoto;
    private String GroupPhotoCreated;
    private ImageView GroupImage = null;
    private EditText name, groupSubject;
    private StorageTask uploadTask;

    private FloatingActionButton create;

    private static final int galleryPick = 1;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        loadingBar = new ProgressDialog(this);

        RootRef = FirebaseDatabase.getInstance().getReference();

        groupPhoto = findViewById(R.id.groupPhoto);
        name = findViewById(R.id.groupName);
        groupSubject = findViewById(R.id.groupStatus);
        create = findViewById(R.id.createGroup);

        GroupImage = groupPhoto;

        groupPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(newGroup.this);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupNom = name.getText().toString();
                String groupStatus = groupSubject.getText().toString();
                if(TextUtils.isEmpty(groupNom) && TextUtils.isEmpty(groupStatus))
                {
                    Toast.makeText(newGroup.this, "Please fill in the blank", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupNom, groupStatus, GroupPhotoCreated);
                    onBackPressed();
                }
            }
        });
    }

    private void CreateNewGroup(final String groupName, final String groupStatus, final String photo) {
        Groups group = new Groups();
        group.setName(groupName);
        group.setPhoto(photo);
        group.setGroupStatus(groupStatus);
        group.setAdminId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        group.setAdminName(FirebaseAuth.getInstance().getCurrentUser().toString());
        DatabaseReference newRef = RootRef.child("Groups").push();
        newRef.setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(newGroup.this, groupName+ " created successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Room Profile");
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
                        Toast.makeText(newGroup.this, "Failed", Toast.LENGTH_SHORT).show();
                        String message = e.toString();
                        Toast.makeText(newGroup.this, "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(newGroup.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        groupImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                GroupPhotoCreated = uri.toString();
                                Picasso.get().load(GroupPhotoCreated).into(GroupImage);
                                loadingBar.dismiss();
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
