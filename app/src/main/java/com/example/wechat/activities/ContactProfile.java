package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactProfile extends AppCompatActivity {

    private String senderUserId, receiverUserId, current_state;
    private CircleImageView userProfileImage;
    private TextView Backspace, userProfileName, userProfileStatus;
    private Button RemoveContact;

    private DatabaseReference UserRef, chatRequestRef, ContactsRef, notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        Backspace = findViewById(R.id.backToContactFragment);
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        RemoveContact = findViewById(R.id.remove_friend);
        current_state = "new";

        RetrieveUserInfo();

        Backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ContactIntent = new Intent(ContactProfile.this, MainActivity.class);
                ContactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(ContactIntent);
                finish();
            }
        });
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
                else
                {
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {

        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId))
                {
                    String typeof_Request = dataSnapshot.child(receiverUserId).child("typeof_Request").getValue().toString();

                }
                else
                {
                    ContactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {

                            if(dataSnapshot.hasChild(receiverUserId))
                            {
                                current_state = "friends";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(receiverUserId))
        {
            RemoveContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (current_state.equals("friends")) {
                        RemoveFriends();
                    }

                }
            });
        }
        else{
            RemoveContact.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveFriends() {
        ContactsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    ContactsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                current_state = "new";
                                RemoveContact.setVisibility(View.GONE);

                                Intent FriendsIntent = new Intent(ContactProfile.this, MainActivity.class);
                                startActivity(FriendsIntent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

}
