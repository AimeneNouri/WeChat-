package com.example.wechat.Notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {

                            Log.d("getInstanceId failed ", task.getException().toString());
                            return;
                        }
                        String token = task.getResult().getToken();
                        final SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                        preferences.edit().putString("FcmToken", token).apply();
                        String phone = preferences.getString("PhoneNumber", "Undefined");
                        if (!phone.equals("Undefined")) {
                        }
                        Log.d("MY-FCM", token);
                    }
                });
    }
}