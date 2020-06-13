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
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.GroupMessageAdapter;
import com.example.wechat.Messages;
import com.example.wechat.MessagesAdapter;
import com.example.wechat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsChat extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ImageView Send_File_Btn;
    private ImageButton backspace;

    private TextView groupName, memberNumber;
    private CircleImageView GroupImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, GroupNameRef, GroupMessageKeyRef, RootRef, AudioMsgKeyRef;

    private String currentGroupName, currentGroupId, msgSenderId, groupAdminId,currentUserName, currentDate, currentTime, checker = "", groupImage, msgReceiverId, groupStatus;
    String senderName = "",calledBy = "";
    public static boolean isDiscussionActivityRunning;

    private CircleImageView group_image;

    private ProgressDialog loadingBar;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter GroupMessageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime;
    private String myUrl = "";
    private StorageTask UploadTask;
    private Uri fileUri;

    private FloatingActionButton floatingActionButton;
    private RelativeLayout GroupInfo;

    private MediaRecorder mediaRecorder;
    private String recordFile;
    Animation topAnim, bottomAnim;
    private TextView cancel_audio_btn;
    private ImageButton record_btn;
    private Chronometer record_timer;

    private boolean isRecording = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupId = getIntent().getExtras().get("groupId").toString();
        groupImage = getIntent().getExtras().get("visit_group_image").toString();
        groupAdminId = getIntent().getExtras().get("groupAdminId").toString();
        groupStatus = getIntent().getExtras().get("groupStatus").toString();

        mAuth = FirebaseAuth.getInstance();
        msgSenderId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        RootRef = FirebaseDatabase.getInstance().getReference();
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Messages");

        Initialisation();
        floatingActionButton = findViewById(R.id.BackToLastMessage);

        Toast.makeText(GroupsChat.this, currentGroupName, Toast.LENGTH_SHORT).show();

        groupName.setText(currentGroupName);
        Picasso.get().load(groupImage).placeholder(R.drawable.group_image3).into(GroupImage);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        DisplayGroupInfos();

        GroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(GroupsChat.this, ImageViewer.class);
                profileIntent.putExtra("url", groupImage);
                startActivity(profileIntent);
            }
        });

        checkForReceivingCall();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
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

                //Videos
                bottomSheet.findViewById(R.id.videos).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        checker = "video";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                        startActivityForResult(intent.createChooser(intent, "Select Video"), 438);
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

                userMessageInput.clearFocus();
            }
        });

        RootRef.child("Groups").child(currentGroupId).child("Messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                //MediaPlayer mp = MediaPlayer.create(GroupsChat.this, R.raw.ringing);
                messagesList.add(messages);
                GroupMessageAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

        userMessagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0)
                {
                    floatingActionButton.show();
                }
                else if (!recyclerView.canScrollVertically(1))
                {
                    floatingActionButton.hide();
                }
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                floatingActionButton.hide();
            }
        });

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording){

                    userMessageInput.setVisibility(View.GONE);
                    Send_File_Btn.setVisibility(View.GONE);
                    record_timer.setVisibility(View.VISIBLE);
                    cancel_audio_btn.setVisibility(View.VISIBLE);

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss", Locale.getDefault());
                    String currentDate = formatter.format(new Date());

                    recordFile = Environment.getExternalStorageDirectory().getAbsolutePath();
                    recordFile += "/Recording_" + currentDate + ".3gp";

                    startRecording();
                    record_btn.setBackgroundResource(R.drawable.stop_record);
                    isRecording = false;
                }
                else{
                    isRecording = true;
                    stopRecording();

                    userMessageInput.setAnimation(bottomAnim);
                    Send_File_Btn.setAnimation(bottomAnim);
                    userMessageInput.setVisibility(View.VISIBLE);
                    Send_File_Btn.setVisibility(View.VISIBLE);
                    record_timer.setVisibility(View.GONE);
                    cancel_audio_btn.setVisibility(View.GONE);
                    record_btn.setBackgroundResource(R.drawable.micro_btn);

                    uploadAudio(recordFile);
                }
            }
        });

        cancel_audio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = true;
                stopRecording();

                File file = new File(recordFile);
                file.delete();

                userMessageInput.setAnimation(bottomAnim);
                Send_File_Btn.setAnimation(bottomAnim);
                userMessageInput.setVisibility(View.VISIBLE);
                Send_File_Btn.setVisibility(View.VISIBLE);
                record_timer.setVisibility(View.GONE);
                cancel_audio_btn.setVisibility(View.GONE);
                record_btn.setBackgroundResource(R.drawable.micro_btn);
            }
        });
    }

    private void uploadAudio(String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference AudioRef = storageRef.child("Audio Files");
        Uri uri = Uri.fromFile(new File(recordFile));

        final String messageSenderRef = "Groups/" + currentGroupId + "/" + "Messages";
        //final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
        //final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

        AudioMsgKeyRef = RootRef.child("Groups").child(currentGroupId)
                .child("Messages").push();

        final String PushMsg = AudioMsgKeyRef.getKey();

        final StorageReference filePath = AudioRef.child(PushMsg + "." + "3gp");

        UploadTask = filePath.putFile(uri);
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

                    Map msgTextBody  = new HashMap();
                    msgTextBody.put("message", myUrl);
                    msgTextBody.put("type", "audio");
                    msgTextBody.put("from", msgSenderId);
                    msgTextBody.put("to", "ALL");
                    msgTextBody.put("messageID", PushMsg);
                    msgTextBody.put("time", saveCurrentTime);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + PushMsg, msgTextBody);
                    //messageBodyDetails.put(messageReceiverRef + "/" + PushMsg, msgTextBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(GroupsChat.this, "Audio has been sent", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(GroupsChat.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void startRecording() {
        record_timer.setBase(SystemClock.elapsedRealtime());
        record_timer.start();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        record_timer.stop();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void Initialisation() {

        mToolbar = findViewById(R.id.group_chat_bar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.group__chatbar, null);
        actionBar.setCustomView(actionBarView);

        groupName = findViewById(R.id.groupName);
        memberNumber = findViewById(R.id.user_last_seen);
        GroupImage = findViewById(R.id.groupPicture);
        backspace = findViewById(R.id.backToMainActivity);

        sendMessageButton = findViewById(R.id.send_msg_button);
        userMessageInput = findViewById(R.id.input_groups_message);
        Send_File_Btn = findViewById(R.id.send_files_btn);
        record_timer = findViewById(R.id.record_timer);
        cancel_audio_btn = findViewById(R.id.cancel_audio);
        record_btn =  findViewById(R.id.record_audio);

        GroupMessageAdapter = new GroupMessageAdapter(this, messagesList);
        userMessagesList = findViewById(R.id.private_msg_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(GroupMessageAdapter);
        GroupInfo = findViewById(R.id.groupInfo);

        /*GroupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //groupName.setAlpha((float) 0.6);
                //memberNumber.setAlpha((float) 0.6);
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GroupsChat.this, R.style.BottomSheet);

                View bottomSheet = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.group_info_bottom_sheet, findViewById(R.id.group_bottom_sheet));

                Button remove_room = bottomSheet.findViewById(R.id.remove_room);
                group_image = bottomSheet.findViewById(R.id.group_profile_image);
                TextView group_name = bottomSheet.findViewById(R.id.group_name);
                TextView group_admin_name = bottomSheet.findViewById(R.id.group_admin);
                CircleImageView update_image = bottomSheet.findViewById(R.id.update_image);

                if (msgSenderId.equals(groupAdminId))
                {
                    remove_room.setVisibility(View.VISIBLE);
                    update_image.setVisibility(View.VISIBLE);

                    group_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setAspectRatio(1, 1)
                                    .start(GroupsChat.this);
                        }
                    });

                    update_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setAspectRatio(1, 1)
                                    .start(GroupsChat.this);
                        }
                    });
                }

                RootRef.child("Users").child(groupAdminId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.child("name").exists()))
                        {
                            String admin_name = dataSnapshot.child("name").getValue().toString();
                            group_admin_name.setText(admin_name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Picasso.get().load(groupImage).placeholder(R.drawable.group_image3).into(group_image);
                group_name.setText(currentGroupName);
                remove_room.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (msgSenderId.equals(groupAdminId))
                        {
                            RootRef.child("Groups").child(currentGroupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(GroupsChat.this, currentGroupName + " deleted", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GroupsChat.this, MainActivity.class));
                                    }
                                }
                            });
                        }
                    }
                });

                bottomSheetDialog.setContentView(bottomSheet);
                bottomSheetDialog.show();

            }
        });*/


        loadingBar = new ProgressDialog(this);

        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /*final LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(570, LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(635, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParam.setMargins(0,4,0, 4);
        layoutParams.setMargins(0,4,0, 4);*/

        userMessageInput.setHint("Message "+ currentGroupName +"'s chat");
        userMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() != 0)
                {
                    //sendMessageButton.setVisibility(View.VISIBLE);
                    //userMessageInput.setLayoutParams(layoutParam);
                    //RootRef.child("Users").child(msgSenderId).child("UsersState").child("state").setValue("Typing");
                    Send_File_Btn.setVisibility(View.GONE);
                }
                if (s.length() == 0)
                {
                    //sendMessageButton.setVisibility(View.GONE);
                    //userMessageInput.setLayoutParams(layoutParams);
                    Send_File_Btn.setVisibility(View.VISIBLE);
                }
            }

            private Timer timer = new Timer();
            private final long DELAY = 500; // milliseconds

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        RootRef.child("Users").child(msgSenderId).child("UsersState").child("state").setValue("Online");
                    }
                },DELAY);
            }
        });

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

                final String messageSenderRef = "Groups/" + currentGroupId + "/" + "Messages";
                //final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
                //final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgKeyRef = RootRef.child("Groups").child(currentGroupId)
                        .child("Messages").push();

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
                                msgFileBody.put("to", "ALL");
                                msgFileBody.put("messageID", msgPushID);
                                msgFileBody.put("time", saveCurrentTime);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgFileBody);
                                //messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgFileBody);

                                RootRef.updateChildren(messageBodyDetails);
                                loadingBar.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupsChat.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

                final String messageSenderRef = "Groups/" + currentGroupId + "/" + "Messages";
                //final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
                //final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgKeyRef = RootRef.child("Groups").child(currentGroupId)
                        .child("Messages").push();

                final String msgPushID = userMsgKeyRef.getKey();

                final StorageReference filePath = storageReference.child(msgPushID + "." + "jpg");

                UploadTask = filePath.putFile(fileUri);
                UploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
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
                            msgPictureBody.put("to", "ALL");
                            msgPictureBody.put("messageID", msgPushID);
                            msgPictureBody.put("time", saveCurrentTime);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgPictureBody);
                            //messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgPictureBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(GroupsChat.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    userMessageInput.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else if (checker.equals("video"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Videos Files");

                final String messageSenderRef = "Groups/" + currentGroupId + "/" + "Messages";

                DatabaseReference userMsgKeyRef = RootRef.child("Groups").child(currentGroupId)
                        .child("Messages").push();

                final String msgPushID = userMsgKeyRef.getKey();
                final StorageReference filePath = storageReference.child(msgPushID + "." + "mp4");

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

                            Map msgTextBody  = new HashMap();
                            msgTextBody.put("message", myUrl);
                            msgTextBody.put("name", fileUri.getLastPathSegment());
                            msgTextBody.put("type", checker);
                            msgTextBody.put("from", msgSenderId);
                            msgTextBody.put("to", "ALL");
                            msgTextBody.put("messageID", msgPushID);
                            msgTextBody.put("time", saveCurrentTime);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgTextBody);
                            //messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        //Toast.makeText(GroupsChat.this, "video has been sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(GroupsChat.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    userMessageInput.setText("");
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait until we update your profile image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                final StorageReference ImagesRef = storageRef.child("WECHAT" + "/PROFILES/" + resultUri.toString().split("/")[resultUri.toString().split("/").length - 1]);

                InputStream stream = null;
                try {
                    stream = new FileInputStream(new File(resultUri.getPath()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                UploadTask = ImagesRef.putStream(stream);
                UploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupsChat.this, "Failed", Toast.LENGTH_SHORT).show();
                        String message = e.toString();
                        Toast.makeText(GroupsChat.this, "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(GroupsChat.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        ImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String link = uri.toString();
                                Picasso.get().load(link).into(group_image);

                                RootRef.child("Groups").child(currentGroupId).child("photo")
                                        .setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            loadingBar.dismiss();
                                            Toast.makeText(GroupsChat.this, "Photo Updated", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            String message = task.getException().toString();
                                            loadingBar.dismiss();
                                            Toast.makeText(GroupsChat.this, "Error: " + message,Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    private void DisplayGroupInfos() {
        RootRef.child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        memberNumber.setText(dataSnapshot.getChildrenCount() + " members");
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
        checkForReceivingCall();

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
        Intent loginIntent = new Intent(GroupsChat.this, SettingsActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void sendMessage() {
        final String messageText = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please write The Text Message first !!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Groups/" + currentGroupId + "/" + "Messages";

            DatabaseReference userMsgKeyRef = RootRef.child("Groups").child(currentGroupId)
                    .child("Messages").push();

            String msgPushID = userMsgKeyRef.getKey();

            Map msgTextBody = new HashMap();
            msgTextBody.put("message", messageText);
            msgTextBody.put("type", "text");
            msgTextBody.put("from", msgSenderId);
            msgTextBody.put("to", "ALL");
            msgTextBody.put("messageID", msgPushID);
            msgTextBody.put("time", saveCurrentTime);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {

                    }
                    else
                    {
                        Toast.makeText(GroupsChat.this, "message doesn't sent", Toast.LENGTH_SHORT).show();
                    }
                    userMessageInput.setText("");
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

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStatusMap = new HashMap<>();
        onlineStatusMap.put("time", saveCurrentTime);
        onlineStatusMap.put("date", saveCurrentDate);
        onlineStatusMap.put("state", state);

        msgSenderId = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(msgSenderId).child("UsersState")
                .updateChildren(onlineStatusMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.group_info){
           Intent groupChatIntent = new Intent(GroupsChat.this, groupInfo.class);
            groupChatIntent.putExtra("group_Name", currentGroupName);
            groupChatIntent.putExtra("group_Id", currentGroupId);
            groupChatIntent.putExtra("visit_group_Picture", groupImage);
            groupChatIntent.putExtra("groupAdminId", groupAdminId);
            groupChatIntent.putExtra("groupStatus", groupStatus);
            startActivity(groupChatIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(GroupsChat.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void checkForReceivingCall()
    {
        RootRef.child("Users").child(msgSenderId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("ringing"))
                        {
                            calledBy = dataSnapshot.child("ringing").getValue(String.class);

                            Intent CallIntent = new Intent(GroupsChat.this, CallingActivity.class);
                            CallIntent.putExtra("visit_user_id", calledBy);
                            startActivity(CallIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
