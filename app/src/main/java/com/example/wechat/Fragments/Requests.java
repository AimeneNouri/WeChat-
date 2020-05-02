package com.example.wechat.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.Contacts;
import com.example.wechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class Requests extends Fragment {

    private View RequestsView;
    private RecyclerView mRequestsLists;

    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public Requests() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mRequestsLists = RequestsView.findViewById(R.id.chat_request_lists);
        mRequestsLists.setLayoutManager(new LinearLayoutManager(getContext()));

        return  RequestsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.acceptButton).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.CancelBtn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("typeof_Request").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    String type =dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                if (dataSnapshot.hasChild("image"))
                                                {
                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue(String.class);
                                                final String requestUserStatus = dataSnapshot.child("status").getValue(String.class);

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Let's be friend");

                                                Button accept = holder.acceptBtn;
                                                accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                         ContactsRef.child(currentUserId).child(list_user_id).child("Contact")
                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task)
                                                                       {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id).child(currentUserId).child("Contact")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            ContactsRef.child(currentUserId).child(list_user_id).child("Contact")
                                                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        ChatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    ChatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                Toast.makeText(getContext(), "Contact Saved Successfully", Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                       }
                                                                   });
                                                        }
                                                    });

                                                Button cancel  = holder.cancelBtn;
                                                cancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                     public void onClick(View v) {
                                                         ChatRequestsRef.child(list_user_id).child(currentUserId)
                                                              .removeValue()
                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                       if (task.isSuccessful())
                                                                       {
                                                                            ChatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                 .removeValue()
                                                                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                     @Override
                                                                                     public void onComplete(@NonNull Task<Void> task)
                                                                                     {
                                                                                         if (task.isSuccessful())
                                                                                         {
                                                                                             Toast.makeText(getContext(), "Contact Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                                                         }
                                                                                     }
                                                                                 });
                                                                       }
                                                                    }
                                                              });
                                                    }
                                                });

                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                CharSequence options[] = new CharSequence[]
                                                                        {
                                                                                "Accept",
                                                                                "Cancel"
                                                                        };

                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                                builder.setTitle(requestUserName + " Chat Request");
                                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which)
                                                                    {

                                                                    }
                                                                });

                                                                builder.show();
                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    else if (type.equals("sent"))
                                    {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.CancelBtn);
                                        request_sent_btn.setText("Cancel");
                                        request_sent_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ChatRequestsRef.child(list_user_id).child(currentUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            ChatRequestsRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        Toast.makeText(getContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        holder.itemView.findViewById(R.id.acceptButton).setVisibility(View.GONE);

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image"))
                                                {
                                                    final String requestProfileImage= dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you've sent new request to " + requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel friends request"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already Sent Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if(which == 0) {}
                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_request_display_layout, parent, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        mRequestsLists.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptBtn, cancelBtn;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.profile_user_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.profile_user_image);
            acceptBtn = itemView.findViewById(R.id.acceptButton);
            cancelBtn = itemView.findViewById(R.id.CancelBtn);
        }
    }
}
