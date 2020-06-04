package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wechat.Contacts;
import com.example.wechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriends extends AppCompatActivity{

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private String currentUserId, calledBy = "";

    FirebaseRecyclerOptions<Contacts> options;
    FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FindFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setHasFixedSize(true);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Users");


        checkForReceivingCall();
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(usersRef, Contacts.class).build();

        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

              holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindFriends.this, UsersProfile.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new FindFriendViewHolder(v);
            }
        };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void checkForReceivingCall()
    {
        usersRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild("ringing"))
                        {
                            calledBy = dataSnapshot.child("ringing").getValue(String.class);

                            Intent CallIntent = new Intent(FindFriends.this, CallingActivity.class);
                            CallIntent.putExtra("visit_user_id", calledBy);
                            startActivity(CallIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

     public void firebaseSearch(String searchText)
     {
         String q = searchText.toLowerCase();
         Query query = usersRef.orderByChild("name")
                 .startAt(searchText)
                 .endAt(searchText + "\uf8ff");
         options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(query, Contacts.class).build();

         adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
             @Override
             protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                 holder.userName.setText(model.getName());
                 holder.userStatus.setText(model.getStatus());
                 Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                 holder.itemView.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         String visit_user_id = getRef(position).getKey();
                         Intent profileIntent = new Intent(FindFriends.this, UsersProfile.class);
                         profileIntent.putExtra("visit_user_id", visit_user_id);
                         startActivity(profileIntent);
                     }
                 });

             }

             @NonNull
             @Override
             public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                 View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                 FindFriendViewHolder viewHolder = new FindFriendViewHolder(v);
                 return viewHolder;
             }
         };

         FindFriendsRecyclerList.setAdapter(adapter);
         adapter.startListening();
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.search_icon_option);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });

        return true;
    }



}

