package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.Messages;
import com.example.wechat.MessagesAdapter;
import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.markushi.ui.CircleButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    private Toolbar mToolbar;
    private String msgReceiverId, msgReceiverName, msgReceiverImage, msgSenderId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private ImageButton uploadFiles, backSpace;
    private CircleButton send_msg;
    private EditText msgInput;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView UsersMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        msgSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        msgReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        msgReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        msgReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        Initialisation();

        userName.setText(msgReceiverName);
        Picasso.get().load(msgReceiverImage).into(userImage);

        backSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Chat.this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
                finish();
            }
        });

        send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });
    }

    private void Initialisation() {

        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.receiverName);
        userImage = findViewById(R.id.profile_image_receiver);
        userLastSeen = findViewById(R.id.receiver_last_seen);
        backSpace = findViewById(R.id.backToMainActivity);

        send_msg =  findViewById(R.id.send_msg_button);
        msgInput = findViewById(R.id.input_chat_message);
        uploadFiles = findViewById(R.id.send_files_btn);

        messagesAdapter = new MessagesAdapter(messagesList);
        UsersMessagesList = findViewById(R.id.private_msg_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        UsersMessagesList.setLayoutManager(linearLayoutManager);
        UsersMessagesList.setAdapter(messagesAdapter);

        DisplayLastSeen();
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(msgReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child("UsersState").hasChild("state"))
                {
                    String state = "" + dataSnapshot.child("UsersState").child("state").getValue();
                    String date = dataSnapshot.child("UsersState").child("date").getValue().toString();
                    String time = dataSnapshot.child("UsersState").child("time").getValue().toString();

                    if (state.equals("online"))
                    {
                       userLastSeen.setText("online");
                    }
                    else if (state.equals("offline"))
                    {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                        String current_Date = currentDate.format(calendar.getTime());

                        calendar.add(Calendar.DATE, -1);
                        SimpleDateFormat yesterdayDate = new SimpleDateFormat("dd/MM/yyyy");
                        String yesterday_Date = yesterdayDate.format(calendar.getTime());

                        if (current_Date.equals(date)) {
                            date = "Today";
                        } else if (yesterday_Date.equals(date)) {
                            date = "Yesterday";
                        }

                        userLastSeen.setText("Last Seen " + date + " at " + time);
                    }
                    /*else if (state.equals("Typing"))
                    {
                        userLastSeen.setText("Typing...");
                    }*/
                }
                else
                {
                    userLastSeen.setText("offline");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(msgSenderId).child(msgReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();

                        UsersMessagesList.smoothScrollToPosition(UsersMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void sendMessage()
    {
        final String messageText = msgInput.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please write The Text Message first !!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
            String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

            DatabaseReference userMsgKeyRef = RootRef.child("Messages").child(msgSenderId)
                    .child(msgReceiverId).push();

            String msgPushID = userMsgKeyRef.getKey();

            Map msgTextBody = new HashMap();
            msgTextBody.put("message", messageText);
            msgTextBody.put("type", "text");
            msgTextBody.put("from", msgSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        //Toast.makeText(Chat.this, "message sent", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(Chat.this, "message doesn't sent", Toast.LENGTH_SHORT).show();
                    }
                    msgInput.setText("");
                }
            });
        }
    }
}
