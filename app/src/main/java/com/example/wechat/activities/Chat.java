package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.wechat.Images;
import com.example.wechat.Messages;
import com.example.wechat.MessagesAdapter;
import com.example.wechat.R;
import com.example.wechat.Report;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    private Toolbar mToolbar;
    private String msgReceiverId, msgReceiverName, msgReceiverImage, msgSenderId, senderToken;
    private TextView userName, userLastSeen, cancel_audio_btn;
    private Chronometer record_timer;
    private CircleImageView userImage;
    Animation slideFromRight, slideToRight;
    Animation topAnim, bottomAnim, bottomAnimation;

    private boolean isRecording = true;

    private ImageButton backSpace;
    private ImageButton send_msg, record_btn;
    private ImageView uploadFilesBtn;
    private EditText msgInput;

    private String PushMsg;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String deviceToken;

    public static boolean isDiscussionActivityRunning;
    String senderName = "";
    private MediaPlayer mPlayer = null;

    private final List<com.example.wechat.Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView UsersMessagesList, MediaList;

    private String saveCurrentTime;
    private String checker = "", myUrl = "", calledBy = "";
    private StorageTask UploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;
    private FloatingActionButton floatingActionButton;
    private DatabaseReference ImageMsgKeyRef, AudioMsgKeyRef;
    RelativeLayout relativeLayout;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    public static int bgResource;

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

        deviceToken = getIntent().getExtras().get("device_token").toString();

        relativeLayout = findViewById(R.id.layout);
        Initialisation();
        floatingActionButton = findViewById(R.id.BackToLastMessage);

        userName.setText(msgReceiverName);
        Picasso.get().load(msgReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim_chat);
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_anim_chat);
        /*
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Chat.this, chat_receiver_profile.class);
                profileIntent.putExtra("name_receiver", msgReceiverName);
                profileIntent.putExtra("receiver_image", msgReceiverImage);
                startActivity(profileIntent);
            }
        });

        userLastSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(Chat.this, chat_receiver_profile.class);
                profileIntent.putExtra("name_receiver", msgReceiverName);
                profileIntent.putExtra("receiver_image", msgReceiverImage);
                startActivity(profileIntent);
            }
        });
         */

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
                onBackPressed();
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

        UsersMessagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                UsersMessagesList.smoothScrollToPosition(UsersMessagesList.getAdapter().getItemCount());
                floatingActionButton.hide();
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
                msgInput.clearFocus();
            }
        });


        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording){

                    msgInput.setVisibility(View.GONE);
                    uploadFilesBtn.setVisibility(View.GONE);
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

                    msgInput.setAnimation(bottomAnim);
                    uploadFilesBtn.setAnimation(bottomAnim);
                    msgInput.setVisibility(View.VISIBLE);
                    uploadFilesBtn.setVisibility(View.VISIBLE);
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

                msgInput.setAnimation(bottomAnim);
                uploadFilesBtn.setAnimation(bottomAnim);
                msgInput.setVisibility(View.VISIBLE);
                uploadFilesBtn.setVisibility(View.VISIBLE);
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

        final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
        final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

        AudioMsgKeyRef = RootRef.child("Messages").child(msgSenderId)
                .child(msgReceiverId).push();

        PushMsg = AudioMsgKeyRef.getKey();

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
                    msgTextBody.put("to", msgReceiverId);
                    msgTextBody.put("messageID", PushMsg);
                    msgTextBody.put("time", saveCurrentTime);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + PushMsg, msgTextBody);
                    messageBodyDetails.put(messageReceiverRef + "/" + PushMsg, msgTextBody);

                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(Chat.this, "Audio has been sent", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(Chat.this, "Error", Toast.LENGTH_SHORT).show();
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

        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar, null);
        actionBar.setCustomView(actionBarView);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = Chat.this
                        .getCurrentFocus();
                if (view != null)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    msgInput.clearFocus();
                }
            }
        });


        userName = findViewById(R.id.receiverName);
        userImage = findViewById(R.id.profile_image_receiver);
        userLastSeen = findViewById(R.id.receiver_last_seen);
        backSpace = findViewById(R.id.backToMainActivity);
        slideFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slideToRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);

        send_msg =  findViewById(R.id.send_msg_button);
        record_btn =  findViewById(R.id.record_audio);
        msgInput = findViewById(R.id.input_chat_message);
        uploadFilesBtn = findViewById(R.id.send_files_btn);
        record_timer = findViewById(R.id.record_timer);
        cancel_audio_btn = findViewById(R.id.cancel_audio);

        messagesAdapter = new MessagesAdapter(messagesList);
        UsersMessagesList = findViewById(R.id.private_msg_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        UsersMessagesList.setLayoutManager(linearLayoutManager);
        UsersMessagesList.setAdapter(messagesAdapter);

        loadingBar = new ProgressDialog(this);

        /*final LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(570, LinearLayout.LayoutParams.WRAP_CONTENT);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(635, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParam.setMargins(0,4,0, 4);
        layoutParams.setMargins(0,4,0, 4);*/

        msgInput.setHint("Message "+ msgReceiverName +"'s chat");
        msgInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

                if (s.length() != 0)
                {
                    send_msg.setVisibility(View.VISIBLE);
                    record_btn.setVisibility(View.GONE);
                    RootRef.child("Users").child(msgSenderId).child("UsersState").child("state").setValue("Typing");
                }
                if (s.length() == 0)
                {
                    send_msg.setVisibility(View.GONE);
                    record_btn.setVisibility(View.VISIBLE);
                }
            }

            private Timer timer = new Timer();
            private final long DELAY = 500;

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        RootRef.child("Users").child(msgSenderId).child("UsersState").child("state").setValue("online");
                    }
                },DELAY);
            }
        });

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
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

            if (!checker.equals("image") && !checker.equals("video"))
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

                                Map msgTextBody = new HashMap();
                                msgTextBody.put("message", downloadUrl);
                                msgTextBody.put("name", fileUri.getLastPathSegment());
                                msgTextBody.put("type", checker);
                                msgTextBody.put("from", msgSenderId);
                                msgTextBody.put("to", msgReceiverId);
                                msgTextBody.put("messageID", msgPushID);
                                msgTextBody.put("time", saveCurrentTime);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + msgPushID, msgTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + msgPushID, msgTextBody);

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

                ImageMsgKeyRef = RootRef.child("Messages").child(msgSenderId)
                        .child(msgReceiverId).push();

                PushMsg = ImageMsgKeyRef.getKey();

                final StorageReference filePath = storageReference.child(PushMsg + "." + "jpg");

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
                            msgTextBody.put("to", msgReceiverId);
                            msgTextBody.put("messageID", PushMsg);
                            msgTextBody.put("time", saveCurrentTime);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + PushMsg, msgTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + PushMsg, msgTextBody);

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
                                        Toast.makeText(Chat.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    msgInput.setText("");
                                }
                            });
                        }
                    }
                });
            }

            else if (checker.equals("video"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Videos Files");

                final String messageSenderRef = "Messages/" + msgSenderId + "/" + msgReceiverId;
                final String messageReceiverRef = "Messages/" + msgReceiverId + "/" + msgSenderId;

                DatabaseReference userMsgKeyRef = RootRef.child("Messages").child(msgSenderId)
                        .child(msgReceiverId).push();

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
                                        loadingBar.dismiss();
                                        Toast.makeText(Chat.this, "video has been sent", Toast.LENGTH_SHORT).show();
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
                    String state = dataSnapshot.child("UsersState").child("state").getValue().toString();
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
                            date = "today";
                        } else if (yesterday_Date.equals(date)) {
                            date = "yesterday";
                        }

                        userLastSeen.setText("Last Seen " + date + " at " + time);
                    }
                    else if (state.equals("Typing")) {
                        userLastSeen.setText("Typing...");
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
        checkForReceivingCall();
        //relativeLayout.setBackgroundResource(bgResource);

        //userName.setAnimation(bottomAnimation);
        //userLastSeen.setAnimation(bottomAnimation);

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
        //relativeLayout.setBackgroundResource(bgResource);

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
        //relativeLayout.setBackgroundResource(bgResource);

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

                            Intent CallIntent = new Intent(Chat.this, CallingActivity.class);
                            CallIntent.putExtra("visit_user_id", calledBy);
                            startActivity(CallIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
        getMenuInflater().inflate(R.menu.chat_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.voice_call_icon_option){

            Intent VoiceIntent = new Intent(Chat.this, VoiceCalling.class);
            VoiceIntent.putExtra("visit_user_id", msgReceiverId);
            VoiceIntent.putExtra("recipientToken", deviceToken);
            VoiceIntent.putExtra("visit_user_name", msgReceiverName);
            VoiceIntent.putExtra("visit_user_image", msgReceiverImage);
            startActivity(VoiceIntent);
        }

        if (item.getItemId() == R.id.video_call_icon_option){
            Intent Calling = new Intent(Chat.this, CallingActivity.class);
            Calling.putExtra("visit_user_id", msgReceiverId);
            startActivity(Calling);
        }


        /*if (item.getItemId() == R.id.wallpaper_option)
        {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Chat.this, R.style.BottomSheet);

            View bottomSheet = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.wallpaper_bottom_sheet, (RelativeLayout) findViewById(R.id.wallpaper_bottom_sheet));

            bottomSheet.findViewById(R.id.wallpaper_bottom_sheet).setVerticalScrollBarEnabled(true);

            bottomSheet.findViewById(R.id.item1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bgResource = R.drawable.background_discussion5;
                    relativeLayout.setBackgroundResource(bgResource);
                    Toast.makeText(Chat.this, "Background Changed", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheet.findViewById(R.id.item2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!msgReceiverId.equals(msgSenderId))
                    {
                        bgResource = R.drawable.background_discussion6;
                        relativeLayout.setBackgroundResource(bgResource);
                        Toast.makeText(Chat.this, "Background Changed", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                }
            });

            bottomSheet.findViewById(R.id.item3).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!msgReceiverId.equals(msgSenderId))
                    {
                       bgResource = R.drawable.background_discussion;
                        relativeLayout.setBackgroundResource(bgResource);
                        Toast.makeText(Chat.this, "Background Changed", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                }
            });

            bottomSheet.findViewById(R.id.item4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!msgReceiverId.equals(msgSenderId))
                    {
                        bgResource = R.drawable.bg_item2;
                        relativeLayout.setBackgroundResource(bgResource);
                        Toast.makeText(Chat.this, "Background Changed", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                }
            });

            bottomSheetDialog.setContentView(bottomSheet);
            bottomSheetDialog.show();
        }*/

        if (item.getItemId() == R.id.remove_option)
        {
            DatabaseReference ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
            if (!msgSenderId.equals(msgReceiverId))
            {
                ContactsRef.child(msgSenderId).child(msgReceiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(msgReceiverId).child(msgSenderId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(Chat.this, "Contact removed", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Chat.this, MainActivity.class));
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }

        if (item.getItemId() == R.id.ClearChat)
        {
            DatabaseReference MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
            if (!msgSenderId.equals(msgReceiverId))
            {
                MessagesRef.child(msgSenderId).child(msgReceiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(Chat.this, "Chat has been cleared", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Chat.this, MainActivity.class));
                        }
                    }
                });
            }
        }

        if (item.getItemId() == R.id.Report_option)
        {
            Report report = new Report();
            report.setFrom(msgSenderId);
            report.setTo(msgReceiverId);
            DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("Report").push();
            reportRef.setValue(report).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(Chat.this, "Your report has been saved", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
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

            Map<String, String> msgTextBody = new HashMap();
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

                        RootRef.child("Users").child(msgSenderId).child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                senderName = dataSnapshot.getValue(String.class);

                                /*try {
                                    Jsoup.connect("https://fcm.googleapis.com/fcm/send")
                                            .header("Content-type", "application/json")
                                            .header("Authorization", "key=AAAAqKZPPWs:APA91bHvxLeeuN_WaIHOxGpV1j3_VGuKJW1Zzsdbch6BY5bRM-Nf6xYRxAAtfuu3JuVOSpS3HyRJEpiovEC3WlcK0XQtInNd92EIcXit52HYzgbqw-Tq9zn4f1z0iHlE7G0udY1aE3w4")
                                            .requestBody("{\"notification\":{\"title\":\"" + senderName + "\",\"body\":\"" + messageText + "\"},\"to\" : \"" + deviceToken + "\"}")
                                            .post();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

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

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(Chat.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}
