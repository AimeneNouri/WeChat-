package com.example.wechat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class ChatsFragment extends Fragment {

    private View ChatsView;
    private RecyclerView ChatsList;

    FirebaseRecyclerOptions<Contacts> options;
    FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter;

    private DatabaseReference chatRef,usersRef;
    private FirebaseAuth mAuth;
    private String  currentUserId;


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
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        ChatsList = ChatsView.findViewById(R.id.chats_list);
        ChatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return ChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatRef, Contacts.class).build();

        adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
            {
                final String userIds = getRef(position).getKey();

                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("image"))
                        {
                           final String userImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }

                        final String profileName = dataSnapshot.child("name").getValue().toString();
                        final String profileStatus = dataSnapshot.child("status").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText("Last Seen: " + "\n" + "Date " + " Time");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new ChatsViewHolder(v);
            }
        };

        ChatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
