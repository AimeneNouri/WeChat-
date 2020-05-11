package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.example.wechat.R;
import android.os.Bundle;

public class VoiceCalling extends AppCompatActivity {

    private static final String APP_KEY = "";
    private static final String APP_SECRET = "";
    private static final String ENVIRONMENT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_calling);
    }
}
