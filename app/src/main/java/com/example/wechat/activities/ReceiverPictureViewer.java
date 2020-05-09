package com.example.wechat.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechat.R;
import com.squareup.picasso.Picasso;

public class ReceiverPictureViewer extends AppCompatActivity {

    private ImageView ReceiverImage;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_picture_viewer);

        ReceiverImage = findViewById(R.id.image_viewer);

        imageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(ReceiverImage);

    }
}
