package com.example.wechat.Fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechat.Messages;
import com.example.wechat.activities.Chat;
import com.example.wechat.Contacts;
import com.example.wechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View ChatsView;
    private RecyclerView mChatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String lastMessage, MessageId;

    private ImageView NoChat;
    private TextView textView;

    LinearLayoutManager mLinearLayoutManager;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mChatsList = ChatsView.findViewById(R.id.chats_list);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mChatsList.setLayoutManager(mLinearLayoutManager);

        NoChat = ChatsView.findViewById(R.id.no_chat);
        textView = ChatsView.findViewById(R.id.textView);

        return ChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        ChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    NoChat.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
                else {
                    NoChat.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatsRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();
                final String[] userImage = {"default_image"};

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image"))
                            {
                                userImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                            final String profileName = dataSnapshot.child("name").getValue().toString();
                            final String profileStatus = dataSnapshot.child("status").getValue().toString();
                            final String device_token = dataSnapshot.child("device_token").getValue().toString();
                            holder.userName.setText(profileName);

                            DatabaseReference MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserId).child(userIDs);
                            MessagesRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.exists()){
                                                for (DataSnapshot ds: dataSnapshot.getChildren())
                                                {
                                                    String message =  ""+ds.child("message").getValue();
                                                    String messageTime =  ""+ds.child("time").getValue();
                                                    String type =  ""+ds.child("type").getValue();

                                                    if (message.length() > 18) {
                                                        message = message.substring(0, 18) + "...";
                                                    }

                                                    if (type.equals("text"))
                                                    {
                                                        holder.userStatus.setText(message);
                                                        holder.messageTime.setText(messageTime);
                                                    }
                                                    else if (type.equals("image"))
                                                    {
                                                        holder.userStatus.setText("Photo");
                                                        holder.imageMessage.setVisibility(View.VISIBLE);
                                                        holder.messageTime.setText(messageTime);
                                                    }
                                                    else if (type.equals("video"))
                                                    {
                                                        holder.userStatus.setText("Video");
                                                        holder.videoMessage.setVisibility(View.VISIBLE);
                                                        holder.messageTime.setText(messageTime);
                                                    }
                                                    else if (type.equals("docx"))
                                                    {
                                                        holder.userStatus.setText("Word File");
                                                        holder.fileMessage.setVisibility(View.VISIBLE);
                                                        holder.messageTime.setText(messageTime);
                                                    }
                                                    else if (type.equals("pdf"))
                                                    {
                                                        holder.userStatus.setText("PDF File");
                                                        holder.fileMessage.setVisibility(View.VISIBLE);
                                                        holder.messageTime.setText(messageTime);
                                                    }
                                                    else if (type.equals("audio"))
                                                    {
                                                        holder.userStatus.setText("Audio");
                                                        holder.audioFile.setVisibility(View.VISIBLE);
                                                        holder.messageTime.setText(messageTime);
                                                    }
                                                }
                                            }
                                            else {
                                                holder.userStatus.setText("Say hello first!");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    Intent chatIntent = new Intent(getContext(), Chat.class);
                                    chatIntent.putExtra("visit_user_id", userIDs);
                                    chatIntent.putExtra("visit_user_name", profileName);
                                    chatIntent.putExtra("visit_user_image", userImage[0]);
                                    chatIntent.putExtra("device_token", device_token);
                                    startActivity(chatIntent);
                                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_chat_layout, parent, false);
                ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }

        };
        mChatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus, messageTime;
        CircleImageView profileImage;
        ImageView videoMessage, imageMessage, fileMessage, audioFile;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            messageTime = itemView.findViewById(R.id.message_time);
            videoMessage = itemView.findViewById(R.id.videoMessage);
            imageMessage = itemView.findViewById(R.id.imagePhoto);
            fileMessage = itemView.findViewById(R.id.documentFile);
            audioFile = itemView.findViewById(R.id.audio_file);
        }
    }
}
