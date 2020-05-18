package com.example.wechat.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechat.Contacts;
import com.example.wechat.R;
import com.example.wechat.activities.CallingActivity;
import com.example.wechat.activities.Chat;
import com.example.wechat.activities.ContactProfile;
import com.example.wechat.activities.VideoCalling;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallFragment extends Fragment {

    private View CallView;
    private RecyclerView mContactsList;

    private DatabaseReference ContactsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public CallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        CallView = inflater.inflate(R.layout.fragment_call, container, false);

        mContactsList = CallView.findViewById(R.id.contact_call_list);
        mContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return CallView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ContactsRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactsCallViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsCallViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsCallViewHolder holder, final int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();
                final String[] userImage = {"default_image"};

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.child("UsersState").hasChild("state"))
                            {
                                String state = dataSnapshot.child("UsersState").child("state").getValue().toString();
                                String date = dataSnapshot.child("UsersState").child("date").getValue().toString();
                                String time = dataSnapshot.child("UsersState").child("time").getValue().toString();

                                if (state.equals("online"))
                                {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if(dataSnapshot.hasChild("image"))
                            {
                                userImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                            final String profileName = dataSnapshot.child("name").getValue().toString();
                            String profileStatus = dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);

                        }

                        holder.videoCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent VideoCallIntent = new Intent(getContext(), CallingActivity.class);
                                VideoCallIntent.putExtra("visit_user_id", userIDs);
                                startActivity(VideoCallIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_call_fragment, parent, false);
                ContactsCallViewHolder viewHolder = new ContactsCallViewHolder(view);
                return viewHolder;
            }
        };
        mContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsCallViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        ImageButton videoCall, voiceCall;

        public ContactsCallViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
            voiceCall = itemView.findViewById(R.id.CallVoiceBtn);
            videoCall = itemView.findViewById(R.id.CallVideoBtn);
        }
    }
}
