package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.Messages;
import com.example.wechat.MessagesAdapter;
import com.example.wechat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.markushi.ui.CircleButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    private Toolbar mToolbar;
    private String msgReceiverId, msgReceiverName, msgReceiverImage, msgSenderId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private ImageButton uploadFilesBtn, backSpace;
    private CircleButton send_msg;
    private EditText msgInput;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID, deviceToken;

    public static boolean isDiscussionActivityRunning;
    String senderName = "";

    private final List<com.example.wechat.Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView UsersMessagesList;

    private String saveCurrentTime;
    private String checker = "", myUrl = "";
    private StorageTask UploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;

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
        Picasso.get().load(msgReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Chat.this, chat_receiver_profile.class);
                profileIntent.putExtra("name_receiver", msgReceiverName);
                profileIntent.putExtra("receiver_image", msgReceiverImage);
                startActivity(profileIntent);
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Chat.this, ReceiverPictureViewer.class);
                profileIntent.putExtra("url", msgReceiverImage);
                startActivity(profileIntent);
            }
        });

        DisplayLastSeen();

        backSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Chat.this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
                finish();
            }
        });

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

        send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });

        uploadFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Chat.this, R.style.BottomSheet);

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

                bottomSheet.findViewById(R.id.cancel_delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheet);
                bottomSheetDialog.show();
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
        uploadFilesBtn = findViewById(R.id.send_files_btn);

        messagesAdapter = new MessagesAdapter(messagesList);
        UsersMessagesList = findViewById(R.id.private_msg_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        UsersMessagesList.setLayoutManager(linearLayoutManager);
        UsersMessagesList.setAdapter(messagesAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, while your file is sending... ");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documents Files");

                final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
                final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgKeyRef = RootRef.child("Messages").child(msgSenderId)
                        .child(msgReceiverId).push();

                final String msgPushID = userMsgKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushID + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map msgFileBody = new HashMap();
                                msgFileBody.put("message", downloadUrl);
                                msgFileBody.put("name", fileUri.getLastPathSegment());
                                msgFileBody.put("type", checker);
                                msgFileBody.put("from", msgSenderId);
                                msgFileBody.put("to", msgReceiverId);
                                msgFileBody.put("messageID", msgPushID);
                                msgFileBody.put("time", saveCurrentTime);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgFileBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgFileBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(Chat.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) progress + " % Uploading...");
                    }
                });
            }
            else if (checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
                final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgKeyRef = RootRef.child("Messages").child(msgSenderId)
                        .child(msgReceiverId).push();

                final String msgPushID = userMsgKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushID + "." + "jpg");

                UploadTask = filePath.putFile(fileUri);
                UploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw  task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map msgPictureBody = new HashMap();
                            msgPictureBody.put("message", myUrl);
                            msgPictureBody.put("name", fileUri.getLastPathSegment());
                            msgPictureBody.put("type", checker);
                            msgPictureBody.put("from", msgSenderId);
                            msgPictureBody.put("to", msgReceiverId);
                            msgPictureBody.put("messageID", msgPushID);
                            msgPictureBody.put("time", saveCurrentTime);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgPictureBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgPictureBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        //Toast.makeText(Chat.this, "message sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(Chat.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    msgInput.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error", Toast.LENGTH_SHORT).show();
            }
        }
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
                        SimpleDateFormat currentDate = new SimpleDateFormat("EEEE");
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
        isDiscussionActivityRunning = true;

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
        isDiscussionActivityRunning = false;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDiscussionActivityRunning = false;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (!MainActivity.isMainActivityRunning){
            if (currentUser != null)
            {
                updateUserStatus("offline");
            }
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

    private void sendUserToSettingsActivity() {
        Intent loginIntent = new Intent(Chat.this, SettingsActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.call, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.voice_call_icon_option){
            voiceCall();
        }

        if (item.getItemId() == R.id.video_call_icon_option){
            videoCall();
        }

        return true;
    }

    private void videoCall()
    {
        Intent VideoCallIntent = new Intent(Chat.this, VideoCalling.class);
        startActivity(VideoCallIntent);
    }

    private void voiceCall()
    {

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
            msgTextBody.put("to", msgReceiverId);
            msgTextBody.put("messageID", msgPushID);
            msgTextBody.put("time", saveCurrentTime);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {

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

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(Chat.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}
