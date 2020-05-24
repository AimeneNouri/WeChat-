package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.wechat.R;

public class videoView extends AppCompatActivity {

    private VideoView mVideoView;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        mVideoView = findViewById(R.id.video_view);
        videoUrl = getIntent().getStringExtra("url");

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);

        Uri uri = Uri.parse(videoUrl);
        mVideoView.setMediaController(mediaController);
        mVideoView.setVideoURI(uri);
        mVideoView.start();
    }
}
