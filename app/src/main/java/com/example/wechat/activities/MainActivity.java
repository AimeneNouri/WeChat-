package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wechat.Fragments.CallFragment;
import com.example.wechat.Fragments.ChatsFragment;
import com.example.wechat.Fragments.ContactsFragment;
import com.example.wechat.Fragments.GroupsFragment;
import com.example.wechat.Fragments.Requests;
import com.example.wechat.Groups;
import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FrameLayout myFrameLayout;
    private BottomNavigationView myBottomNavigationView;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserId, calledBy = "";;

    public static boolean isMainActivityRunning;

    private ChatsFragment chatsFragment;
    private GroupsFragment groupsFragment;
    private ContactsFragment contactsFragment;
    private CallFragment callFragment;
    private Requests chatRequestFragment;

    private static final int galleryPick = 1;
    private ProgressDialog loadingBar;
    private ImageView groupPhoto;
    private String GroupPhotoCreated;
    private ImageView GroupImage = null;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("WeChat");
        getSupportActionBar().setIcon(R.drawable.logo_wecht_main);

        myFrameLayout = (FrameLayout) findViewById(R.id.main_frame);
        myBottomNavigationView = (BottomNavigationView) findViewById(R.id.main_tabs);

        chatsFragment = new ChatsFragment();
        groupsFragment = new GroupsFragment();
        contactsFragment = new ContactsFragment();
        callFragment = new CallFragment();
        chatRequestFragment = new Requests();

        setFragment(chatsFragment);

        myBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.nav_chats:
                        setFragment(chatsFragment);
                        return true;

                    case R.id.nav_groups:
                        setFragment(groupsFragment);
                        return true;

                    case R.id.nav_contacts:
                        setFragment(contactsFragment);
                        return true;

                    case R.id.nav_calls:
                        setFragment(callFragment);
                        return true;

                    case R.id.requests:
                        setFragment(chatRequestFragment);
                        return true;

                    default:
                        return false;
                }

            }

        });
    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        isMainActivityRunning = true;

        checkForReceivingCall();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Log.d("WeChat", "null");
            sendUserToLoginActivity();
        }
        else{
            Log.d("WeChat", "not null");
            updateUserStatus("online");
            VerifyUserExist();
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

                            Intent CallIntent = new Intent(MainActivity.this, CallingActivity.class);
                            CallIntent.putExtra("visit_user_id", calledBy);
                            startActivity(CallIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        isMainActivityRunning = false;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isMainActivityRunning = false;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExist() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists()))
                {}
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_settings_option){
            sendUserToSettingsActivity();
        }

        if (item.getItemId() == R.id.main_logout_option){
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }

        if (item.getItemId() == R.id.main_find_friends_option){
            Intent findFriendIntent = new Intent(MainActivity.this, FindFriends.class);
            startActivity(findFriendIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        if (item.getItemId() == R.id.new_room){
           NewGroupRequest();
        }
        return true;
    }

    private void NewGroupRequest() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.newgroup);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final EditText name = dialog.findViewById(R.id.groupName);
        groupPhoto = dialog.findViewById(R.id.groupPhoto);
        GroupImage = groupPhoto;
        Button Create = dialog.findViewById(R.id.createGroup);
        Button cancel = dialog.findViewById(R.id.cancel);

        groupPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(MainActivity.this);


            }
        });

        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 String groupName = name.getText().toString();
                 if(TextUtils.isEmpty(groupName))
                 {
                     Toast.makeText(MainActivity.this, "Please write a name for your group", Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                    CreateNewGroup(groupName, GroupPhotoCreated);
                 }

                 dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void CreateNewGroup(final String groupName, final String photo) {

        Groups group = new Groups();
        group.setName(groupName);
        group.setPhoto(photo);
        group.setAdminId(mAuth.getCurrentUser().getUid());
        group.setAdminName(mAuth.getCurrentUser().toString());
        DatabaseReference newRef = RootRef.child("Groups").push();
        newRef.setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, groupName+ " created successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if(requestCode == galleryPick  &&  resultCode == RESULT_OK  &&  data != null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }*/

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Room Profile Image");
                loadingBar.setMessage("Please wait, while The picture is updating... ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference groupImageRef = FirebaseStorage.getInstance().getReference().child("WECHAT" + "/PROFILES/" + resultUri.toString().split("/")[resultUri.toString().split("/").length - 1]);

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
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        String message = e.toString();
                        Toast.makeText(MainActivity.this, "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

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
}
