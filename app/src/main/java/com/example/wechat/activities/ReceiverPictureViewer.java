package com.example.wechat.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechat.R;
import com.squareup.picasso.Picasso;

public class ReceiverPictureViewer extends AppCompatActivity {

    private ImageView ReceiverImage;
    private ImageButton back_btn;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_picture_viewer);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ReceiverImage = findViewById(R.id.image_viewer);
        back_btn = findViewById(R.id.back_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(ReceiverImage);

    }
}
