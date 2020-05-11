package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoCalling extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String  API_KEY = "46729602";
    private static String SESSION_ID = "1_MX40NjcyOTYwMn5-MTU4OTEzMTQ5OTg0NH52cWIzSWd5djNaSG1rWmRQOGxnclJKK0x-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjcyOTYwMiZzaWc9YmI5MDkyMWNlYjRmZTlkNzc1ZDQ2ZjFkMTBiZmMxMDJmNzY1M2JhYjpzZXNzaW9uX2lkPTFfTVg0ME5qY3lPVFl3TW41LU1UVTRPVEV6TVRRNU9UZzBOSDUyY1dJelNXZDVkak5hU0cxcldtUlFPR3huY2xKS0sweC1mZyZjcmVhdGVfdGltZT0xNTg5MTMxNTY2Jm5vbmNlPTAuNzQ2ODU5NDEzMzIzNDY3NCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTg5MTUzMTYzJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoCalling.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView closeVideoChat;

    private DatabaseReference UsersRef;
    private String UserId = "";

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_calling);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        closeVideoChat = findViewById(R.id.end_video_chat_btn);

        closeVideoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child(UserId).hasChild("Ringing"))
                        {
                            UsersRef.child(UserId).child("Ringing").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCalling.this, MainActivity.class));
                            finish();
                        }
                        if (dataSnapshot.child(UserId).hasChild("Calling"))
                        {
                            UsersRef.child(UserId).child("Calling").removeValue();

                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCalling.this, MainActivity.class));
                            finish();
                        }
                        else
                        {
                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCalling.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCalling.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this, perms))
        {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoCalling.this);
            mSession.connect(TOKEN);
        }
        else {
            EasyPermissions.requestPermissions(this, "Our app needs the Mic and Camera permissions, Please allow", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //Publishing Stream to the session
    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCalling.this);

        mPublisherViewController.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session)
    {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    //Subscribing Stream to the session
    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Session Received");

        if (mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Session Dropped");

        if (mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG, "Session Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
