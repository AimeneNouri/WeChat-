package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact, TypeOfCall;
    private ImageView ProfileImage;
    private ImageButton CancelCallBtn, acceptCallBtn;

    private String receiverUserId = "", receiverName = "", receiverImage = "";
    private String senderUserId = "", senderName = "", senderImage = "", checker = "";
    private DatabaseReference UsersRef;

    private String callingId = "", ringingId = "";
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        nameContact = findViewById(R.id.name_calling);
        ProfileImage = findViewById(R.id.profile_image_calling);
        CancelCallBtn = findViewById(R.id.btn_end_call);
        acceptCallBtn = findViewById(R.id.btn_make_cal);
        TypeOfCall = findViewById(R.id.txt);

        mp = MediaPlayer.create(this, R.raw.whatsapp_ring);
        mp.start();

        CancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mp.stop();
                checker = "clicked";
                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final  HashMap<String,Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked","picked");
                mp.stop();

                UsersRef.child(senderUserId).child("Ringing")
                        .updateChildren(callingPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete())
                                {
                                    Intent intent = new Intent(CallingActivity.this, VideoCalling.class);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });

        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(receiverUserId).exists())
                {
                    receiverImage = dataSnapshot.child(receiverUserId).child("image").getValue(String.class);
                    receiverName = dataSnapshot.child(receiverUserId).child("name").getValue(String.class);

                    nameContact.setText(receiverName);
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(ProfileImage);
                }
                if (dataSnapshot.child(senderUserId).exists())
                {
                    senderImage = dataSnapshot.child(senderUserId).child("image").getValue(String.class);
                    senderName = dataSnapshot.child(senderUserId).child("name").getValue(String.class);

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

        mp.start();

        UsersRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing"))
                {
                    final HashMap<String,Object> callingInfo = new HashMap<>();
                    callingInfo.put("calling",receiverUserId);

                    UsersRef.child(senderUserId)
                            .child("Calling")
                            .updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        final HashMap<String,Object> ringingInfo = new HashMap<>();
                                        ringingInfo.put("ringing",senderUserId);

                                        UsersRef.child(receiverUserId).child("Ringing")
                                                .updateChildren(ringingInfo);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling"))
                {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                    TypeOfCall.setText("ringing...");
                }
                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked"))
                {
                    mp.stop();
                    Intent VideoIntent = new Intent(CallingActivity.this, VideoCalling.class);
                    startActivity(VideoIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cancelCallingUser() {

        //senderCall
        UsersRef.child(senderUserId)
                .child("Calling")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                        {
                            callingId = dataSnapshot.child("calling").getValue(String.class);

                            UsersRef.child(callingId).child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        UsersRef.child(senderUserId).child("Calling")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                onBackPressed();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            onBackPressed();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //receiverCall
        UsersRef.child(senderUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                        {
                            ringingId = dataSnapshot.child("ringing").getValue(String.class);

                            UsersRef.child(ringingId).child("Calling")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        UsersRef.child(senderUserId)
                                                .child("Ringing")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                onBackPressed();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        else
                        {
                            onBackPressed();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
