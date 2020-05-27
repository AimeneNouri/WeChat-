package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;

public class Theme extends AppCompatActivity {

    RelativeLayout theme_preview;
    ImageButton back_btn, save_btn ;
    String newString;
    private String currentUserId, msgReceiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        msgReceiverId = getIntent().getExtras().get("visit_user_id").toString();

        theme_preview = findViewById(R.id.theme_preview);
        back_btn = findViewById(R.id.back_btn);
        save_btn = findViewById(R.id.save_btn);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                newString = null;
            }
            else
            {
                newString = extras.getString("path picture");

                if (newString.equals("bg_item1"))
                {
                    theme_preview.setBackgroundResource(R.drawable.bg_item1);
                }
                else if (newString.equals("bg_item2"))
                {
                    theme_preview.setBackgroundResource(R.drawable.bg_item2);
                }
            }
        }

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!msgReceiverId.equals(currentUserId))
                {
                    if (newString.equals("bg_item1"))
                    {
                        Toast.makeText(Theme.this, "Wallpaper changed successfully", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Theme.this, MainActivity.class);
                        i.putExtra("url", R.drawable.bg_item1);
                        startActivity(i);
                    }
                    else if (newString.equals("bg_item2"))
                    {
                        Toast.makeText(Theme.this, "Wallpaper changed successfully", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Theme.this, MainActivity.class);
                        i.putExtra("url", R.drawable.bg_item2);
                        startActivity(i);
                    }
                }
            }
        });
    }
}
