package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class groupInfo extends AppCompatActivity {

    private CircleImageView groupProfileImage;
    private TextView groupProfileName, groupAdmin;
    private Button RemoveRoom;

    private String currentGroupName, currentGroupId, groupImage, groupAdminId, currentUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        currentGroupName = getIntent().getExtras().get("group_Name").toString();
        currentGroupId = getIntent().getExtras().get("group_Id").toString();
        groupImage = getIntent().getExtras().get("visit_group_Picture").toString();
        groupAdminId = getIntent().getExtras().get("groupAdminId").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        groupProfileImage = findViewById(R.id.group_profile_image);
        groupProfileName = findViewById(R.id.group_name);
        groupAdmin = findViewById(R.id.group_admin);
        RemoveRoom = findViewById(R.id.remove_room);

        //groupAdmin.setText(groupAdminId);
        groupProfileName.setText(currentGroupName);
        Picasso.get().load(groupImage).placeholder(R.drawable.group_image3).into(groupProfileImage);

        if (currentUserID.equals(groupAdminId))
        {
            RootRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        String UserName = dataSnapshot.child("name").getValue(String.class);
                        groupAdmin.setText(UserName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            RemoveRoom.setVisibility(View.VISIBLE);
        }

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

}
