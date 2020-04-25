package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersProfile extends AppCompatActivity {

    private String senderUserId, receiverUserId, current_state;
    private CircleImageView userProfileImage;
    private TextView Backspace, userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef, chatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        Backspace = findViewById(R.id.backToFriendsActivity);
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = findViewById(R.id.decline_message_request_button);
        current_state = "new";

        RetrieveUserInfo();

        Backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent FriendsIntent = new Intent(UsersProfile.this, FindFriends.class);
                startActivity(FriendsIntent);
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
                    String userIName = dataSnapshot.child("name").getValue().toString();

                    userProfileName.setText(userIName);
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

                    if(typeof_Request.equals("sent"))
                    {
                        current_state = "request_sent";
                        SendMessageRequestButton.setText("Cancel Friend Request");
                    }
                    else if (typeof_Request.equals("received"))
                    {
                        current_state = "request_received";
                        SendMessageRequestButton.setText("Accept Friend Request");

                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);

                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
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
                                SendMessageRequestButton.setText("Remove this Contact");
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
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);

                    if(current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (current_state.equals("request_received")) {
                        AcceptFriendRequest();
                    }
                    if (current_state.equals("friends")) {
                        RemoveFriends();
                    }

                }
            });
        }
        else{
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).child("typeof_Request").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    chatRequestRef.child(receiverUserId).child(senderUserId).child("typeof_Request").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendMessageRequestButton.setEnabled(true);
                                current_state = "request_sent";
                                SendMessageRequestButton.setText("Cancel Friend Request");
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendMessageRequestButton.setEnabled(true);
                                current_state = "new";
                                SendMessageRequestButton.setText("Send Friend Request");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptFriendRequest() {
        ContactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    ContactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                        SendMessageRequestButton.setEnabled(true);
                                                        current_state ="friends";
                                                        SendMessageRequestButton.setText("Remove this Contact");
                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                        DeclineMessageRequestButton.setEnabled(false);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
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
                                SendMessageRequestButton.setEnabled(true);
                                current_state = "new";
                                SendMessageRequestButton.setText("Send Friend Request");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

}
