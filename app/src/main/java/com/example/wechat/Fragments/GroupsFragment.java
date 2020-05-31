package com.example.wechat.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.wechat.Groups;
import com.example.wechat.activities.GroupsChat;
import com.example.wechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupsView;
    private RecyclerView mGroupsList;
    //private ArrayAdapter<String> arrayAdapter;
    //private ArrayList<String> listOfGroups = new ArrayList<>();

    private DatabaseReference GroupsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupsView = inflater.inflate(R.layout.fragment_groups, container, false);
        mGroupsList = groupsView.findViewById(R.id.group_list);
        mGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        currentUserId = mAuth.getCurrentUser().getUid();

        return groupsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Groups>().setQuery(GroupsRef, Groups.class).build();
        FirebaseRecyclerAdapter<Groups, GroupsViewHolder> adapter = new FirebaseRecyclerAdapter<Groups, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupsViewHolder holder, int position, @NonNull Groups model)
            {
                String userIDs = getRef(position).getKey();
                final String[] group_image = {"default_image"};

                GroupsRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("photo"))
                            {
                                group_image[0] = dataSnapshot.child("photo").getValue(String.class);
                                Picasso.get().load(group_image[0] ).placeholder(R.drawable.group_image3).into(holder.groupImage);
                            }

                            final String groupId = dataSnapshot.getKey();
                            final String groupName = dataSnapshot.child("name").getValue(String.class);
                            final String groupAdminId = dataSnapshot.child("adminId").getValue(String.class);
                            holder.group_Name.setText(groupName);

                            GroupsRef.child(groupId).child("Messages").limitToLast(1)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds: dataSnapshot.getChildren())
                                            {
                                                String message =  ""+ds.child("message").getValue();
                                                String messageTime =  ""+ds.child("time").getValue();
                                                String from =  ""+ds.child("from").getValue();
                                                String type =  ""+ds.child("type").getValue();

                                                if (message.length() > 34) {
                                                    message = message.substring(0, 33) + "...";
                                                }

                                                if (type.equals("text"))
                                                {
                                                    holder.groupMember.setText(message);
                                                    holder.messageTime.setText(messageTime);
                                                }
                                                else if (type.equals("image"))
                                                {
                                                    holder.groupMember.setText(" Photo");
                                                    holder.imageMessage.setVisibility(View.VISIBLE);
                                                    holder.messageTime.setText(messageTime);
                                                }
                                                else if (type.equals("video"))
                                                {
                                                    holder.groupMember.setText("Video");
                                                    holder.videoMessage.setVisibility(View.VISIBLE);
                                                    holder.messageTime.setText(messageTime);
                                                }
                                                else if (type.equals("docx"))
                                                {
                                                    holder.groupMember.setText("Word File");
                                                    holder.fileMessage.setVisibility(View.VISIBLE);
                                                    holder.messageTime.setText(messageTime);
                                                }
                                                else if (type.equals("pdf"))
                                                {
                                                    holder.groupMember.setText("PDF File");
                                                    holder.fileMessage.setVisibility(View.VISIBLE);
                                                    holder.messageTime.setText(messageTime);
                                                }

                                                DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
                                                UserRef.orderByChild("uid").equalTo(from)
                                                        .addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot ds: dataSnapshot.getChildren())
                                                                {
                                                                    String name = ""+ds.child("name").getValue();
                                                                    if (from.equals(currentUserId))
                                                                    {
                                                                        holder.senderName.setText("You:");
                                                                    }
                                                                    else
                                                                    {
                                                                        holder.senderName.setText(name+": ");
                                                                    }
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

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String currentGroupName = groupName;
                                    Intent groupChatIntent = new Intent(getContext(), GroupsChat.class);
                                    groupChatIntent.putExtra("groupName", currentGroupName);
                                    groupChatIntent.putExtra("groupId", groupId);
                                    groupChatIntent.putExtra("groupAdminId", groupAdminId);
                                    groupChatIntent.putExtra("visit_group_image", group_image[0] );
                                    startActivity(groupChatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_display_layout, parent, false);
                GroupsViewHolder viewHolder = new GroupsViewHolder(view);
                return viewHolder;
            }
        };
        mGroupsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder
    {
        TextView group_Name, groupMember, messageTime, senderName;
        CircleImageView groupImage;
        ImageView videoMessage, imageMessage, fileMessage;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);

            group_Name = itemView.findViewById(R.id.group_Name);
            groupMember = itemView.findViewById(R.id.group_members);
            groupImage = itemView.findViewById(R.id.group_image);
            messageTime = itemView.findViewById(R.id.message_time);
            senderName = itemView.findViewById(R.id.sender_name);
            videoMessage = itemView.findViewById(R.id.videoMessage);
            imageMessage = itemView.findViewById(R.id.imagePhoto);
            fileMessage = itemView.findViewById(R.id.documentFile);
        }
    }
}
