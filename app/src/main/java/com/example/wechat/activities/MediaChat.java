package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.wechat.R;

public class MediaChat extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView MediaRecyclerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_chat);

        MediaRecyclerList = findViewById(R.id.Media_list);
        MediaRecyclerList.setHasFixedSize(true);
        MediaRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.chat_toolbar);
    }
}
