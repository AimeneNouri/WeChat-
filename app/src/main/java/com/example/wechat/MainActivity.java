package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FrameLayout myFrameLayout;
    private BottomNavigationView myBottomNavigationView;

    private FirebaseUser currentUser;

    private ChatsFragment chatsFragment;
    private GroupsFragment groupsFragment;
    private ContactsFragment contactsFragment;
    private CallFragment callFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WeChat");

        myFrameLayout = (FrameLayout) findViewById(R.id.main_frame);
        myBottomNavigationView = (BottomNavigationView) findViewById(R.id.main_tabs);

        chatsFragment = new ChatsFragment();
        groupsFragment = new GroupsFragment();
        contactsFragment = new ContactsFragment();
        callFragment = new CallFragment();

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

        if(currentUser == null){
            sendUserToLoginActivity();
        }
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
