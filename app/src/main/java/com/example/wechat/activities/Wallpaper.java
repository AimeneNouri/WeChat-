package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.wechat.R;

public class Wallpaper extends AppCompatActivity {

    ImageView item1, item3, item2, item4;
    private String msgReceiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        msgReceiverId = getIntent().getExtras().get("visit_user_id").toString();

        item1 = findViewById(R.id.btn_item1);
        item2 = findViewById(R.id.btn_item2);

        item1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Wallpaper.this, Theme.class);
                String pathPic = "bg_item1";
                i.putExtra("path picture", pathPic);
                i.putExtra("visit_user_id", msgReceiverId);
                startActivity(i);
            }
        });

        item2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Wallpaper.this, Theme.class);
                String pathPic = "bg_item2";
                i.putExtra("path picture", pathPic);
                i.putExtra("visit_user_id", msgReceiverId);
                startActivity(i);
            }
        });
    }
}
