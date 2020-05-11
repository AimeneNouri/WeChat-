package com.example.wechat.notification;

import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wechat.R;
import com.example.wechat.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (remoteMessage.getData().size() > 0) {
                    Log.d("FCM", remoteMessage.getData().toString());


                }

                if (remoteMessage.getNotification() != null) {
                    int requestID = (int) System.currentTimeMillis();
                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(FirebaseMessaging.this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationManager notificationManager;
                    NotificationCompat.Builder builder;
                    NotificationChannel channel;
                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    builder = new NotificationCompat.Builder(FirebaseMessaging.this, "1")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody());
                    builder.setContentIntent(contentIntent);
                    long[] pattern = {500,500,500,500,500,500,500,500,500};
                    builder.setVibrate(pattern);
                    builder.setStyle(new NotificationCompat.InboxStyle());
                    builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                    notificationManager.notify(1, builder.build());
                }

            }
        });


    }

    @Override
    public void onNewToken(String registrationToken) {
        Log.d("Token=", registrationToken);
        startService(new Intent(this, FcmTokenRegistration.class));
    }
}