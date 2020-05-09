package com.example.wechat.activities;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupsChat extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private ImageView Send_File_Btn;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime, checker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupsChat.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_msg_button);
        userMessageInput = (EditText) findViewById(R.id.input_groups_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text);
        mScrollView =(ScrollView) findViewById(R.id.scroll_view);
        Send_File_Btn = findViewById(R.id.send_files_btn);

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDB();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        Send_File_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupsChat.this, R.style.BottomSheet);

                View bottomSheet = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.bottom_sheet_layout_chat, (RelativeLayout) findViewById(R.id.bottomSheet_Cont));

                //Images
                bottomSheet.findViewById(R.id.images).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        checker = "image";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        bottomSheetDialog.dismiss();
                    }
                });

                //PDF Files
                bottomSheet.findViewById(R.id.pdf).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        checker = "pdf";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        bottomSheetDialog.dismiss();
                    }
                });

                //Ms Word Files
                bottomSheet.findViewById(R.id.word).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checker = "docx";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/docx");
                        startActivityForResult(intent.createChooser(intent, "Select Ms Word File"), 438);
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheet);
                bottomSheetDialog.show();

            }
        });

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void GetUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageToDB() {
        String msg = userMessageInput.getText().toString();
        String msgKey = GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(msg))
        {
            Toast.makeText(this, "Please write The Text Message first !!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat DateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = DateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat TimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = TimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey =  new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(msgKey);

            HashMap<String, Object> msgInfoMap =  new HashMap<>();
            msgInfoMap.put("name", currentUserName);
            msgInfoMap.put("message", msg);
            msgInfoMap.put("date", currentDate);
            msgInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(msgInfoMap);
        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessages.append(chatName + "\n" + chatMessage + "\n\n" + chatTime);

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
