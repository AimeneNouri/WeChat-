package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.example.wechat.ReportRoom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class groupInfo extends AppCompatActivity {

    private CircleImageView groupProfileImage;
    private TextView groupProfileName, groupAdmin;
    private Button RemoveRoom, ReportGroup;

    private String currentGroupName, currentGroupId, groupImage, groupAdminId, currentUserID, groupStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton backToChat;
    private FloatingActionButton updateGroupInfo;
    private TextView group_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        currentGroupName = getIntent().getExtras().get("group_Name").toString();
        currentGroupId = getIntent().getExtras().get("group_Id").toString();
        groupImage = getIntent().getExtras().get("visit_group_Picture").toString();
        groupAdminId = getIntent().getExtras().get("groupAdminId").toString();
        groupStatus = getIntent().getExtras().get("groupStatus").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        groupProfileImage = findViewById(R.id.group_profile_image);
        groupProfileName = findViewById(R.id.group_name);
        groupAdmin = findViewById(R.id.group_admin);
        RemoveRoom = findViewById(R.id.remove_room);
        backToChat = findViewById(R.id.backToGroupActivity);
        group_status = findViewById(R.id.groupStatus);
        ReportGroup = findViewById(R.id.Report_room);
        updateGroupInfo = findViewById(R.id.UpdateGroupInfo);

        if (groupStatus == null)
        {
            groupAdmin.setTextColor(Color.parseColor("#ffffff"));
        }
        else
        {
            group_status.setText(groupStatus);
            groupAdmin.setTextColor(Color.parseColor("#E052F3"));
        }
        groupProfileName.setText(currentGroupName);
        Picasso.get().load(groupImage).placeholder(R.drawable.group_image3).into(groupProfileImage);

        if (currentUserID.equals(groupAdminId))
        {
            RemoveRoom.setVisibility(View.VISIBLE);

            RemoveRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUserID.equals(groupAdminId))
                    {
                        RootRef.child("Groups").child(currentGroupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(groupInfo.this, currentGroupName + " deleted", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(groupInfo.this, MainActivity.class));
                                }
                            }
                        });
                    }
                }
            });
        }

        RootRef.child("Users").child(groupAdminId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists()))
                {
                    String admin_name = dataSnapshot.child("name").getValue().toString();

                    if (groupAdminId.equals(currentUserID))
                    {
                        groupAdmin.setText("You");
                    }
                    else {
                        groupAdmin.setText(admin_name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        backToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ReportGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportRoom reportRoom = new ReportRoom();
                reportRoom.setReporter(currentUserID);
                reportRoom.setGroup(currentGroupId);
                DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Report").push();
                reportRef.setValue(reportRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(groupInfo.this, "Your report has been saved", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        updateGroupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupChatIntent = new Intent(groupInfo.this, updateGroup_info.class);
                groupChatIntent.putExtra("group_Name", currentGroupName);
                groupChatIntent.putExtra("group_Id", currentGroupId);
                groupChatIntent.putExtra("visit_group_Picture", groupImage);
                groupChatIntent.putExtra("groupStatus", groupStatus);
                startActivity(groupChatIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
