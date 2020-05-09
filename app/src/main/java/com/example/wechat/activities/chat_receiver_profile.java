package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.wechat.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class chat_receiver_profile extends AppCompatActivity {

    private ImageButton BackToChat;
    private CircleImageView ReceiverImage;
    private TextView ReceiverName;
    private String receiverName, receiverPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_receiver_profile);

        BackToChat = findViewById(R.id.backToChatActivity);
        ReceiverImage = findViewById(R.id.receiver_profile_image);
        ReceiverName = findViewById(R.id.Receiver_name);

        receiverName = getIntent().getExtras().get("name_receiver").toString();
        receiverPicture = getIntent().getExtras().get("receiver_image").toString();

        ReceiverName.setText(receiverName);
        Picasso.get().load(receiverPicture).placeholder(R.drawable.profile_image).into(ReceiverImage);

        BackToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ContactIntent = new Intent(chat_receiver_profile.this, Chat.class);
                ContactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(ContactIntent);
                finish();
            }
        });
    }

}
