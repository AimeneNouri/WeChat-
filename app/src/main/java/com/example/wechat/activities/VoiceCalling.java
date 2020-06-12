package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VoiceCalling extends AppCompatActivity {

    private static final String APP_KEY = "56313b0d-a469-45d5-a8b1-8a97f2f0d613";
    private static final String APP_SECRET = "HJr/Bus8VUukF3DnRnZ5lg==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    private String SenderCallId, receiverCallId, msgReceiverName, msgReceiverImage, DeviceTokenReceiver, currentUserId;
    private SinchClient sinchClient;

    private ImageButton endCall, acceptCall, acceptCallReceiver;
    private TextView callState, ReceiverName;
    private Call call;
    private CircleImageView receiverImage;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_calling);

        msgReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        msgReceiverImage = getIntent().getExtras().get("visit_user_image").toString();
        receiverCallId = getIntent().getStringExtra("visit_user_id");
        DeviceTokenReceiver = getIntent().getExtras().get("recipientToken").toString();

        SenderCallId = FirebaseInstanceId.getInstance().getToken();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        endCall = findViewById(R.id.buttonEndCall);
        callState = findViewById(R.id.callState);
        receiverImage = findViewById(R.id.receiver_profile_image);
        ReceiverName = findViewById(R.id.receiver_Name);
        acceptCall = findViewById(R.id.btn_accept_cal);
        acceptCallReceiver = findViewById(R.id.btn_accept_call_receiver);

        ReceiverName.setText(msgReceiverName);
        Picasso.get().load(msgReceiverImage).placeholder(R.drawable.profile_image).into(receiverImage);

        mediaPlayer = MediaPlayer.create(this, R.raw.whatsapp_ring);

        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(SenderCallId)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (call == null){

                    call = sinchClient.getCallClient().callUser(receiverCallId);
                    call.addCallListener(new SinchCallListener());
                    callState.setText("Calling...");
                }
            }
        });

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (call != null)
                {
                    call.hangup();
                    onBackPressed();
                }
                else {
                    onBackPressed();
                }
            }
        });


        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
        sinchClient.setSupportActiveConnectionInBackground(true);
    }

    private class SinchCallListener implements CallListener{

        @Override
        public void onCallProgressing(Call call) {
            callState.setText("Ringing...");
        }

        @Override
        public void onCallEstablished(Call call) {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            Toast.makeText(VoiceCalling.this, "connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            //SinchError a = endedCall.getDetails().getError();
            callState.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            Toast.makeText(VoiceCalling.this, "call end", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private class SinchCallClientListener implements CallClientListener
    {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            //mediaPlayer.start();
            acceptCallReceiver.setVisibility(View.VISIBLE);
            acceptCall.setVisibility(View.GONE);

            acceptCallReceiver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mediaPlayer.stop();
                    call = incomingCall;
                    call.answer();
                    call.addCallListener(new SinchCallListener());
                    Toast.makeText(VoiceCalling.this, "Call started", Toast.LENGTH_SHORT).show();
                }
            });

            endCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    call.hangup();
                    onBackPressed();
                }
            });
        }
    }
}
