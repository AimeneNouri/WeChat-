package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
    private String currentUserID;

    private ChatsFragment chatsFragment;
    private GroupsFragment groupsFragment;
    private ContactsFragment contactsFragment;
    private CallFragment callFragment;
    private Requests chatRequestFragment;

    private ProgressDialog loadingBar;
    private ImageView groupPhoto;
    private String GroupPhotoCreated;
    private ImageView GroupImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("WeChat");

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
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            VerifyUserExist();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        getMenuInflater().inflate(R.menu.creategroupbutton, menu);

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
        if (item.getItemId() == R.id.creat_groups){
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

        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
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

       SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
       saveCurrentTime = currentTime.format(calendar.getTime());

       HashMap<String, Object> onlineStatusMap = new HashMap<>();
       onlineStatusMap.put("time", saveCurrentTime);
       onlineStatusMap.put("date", saveCurrentDate);
       onlineStatusMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("UsersState")
                .updateChildren(onlineStatusMap);
   }
}
