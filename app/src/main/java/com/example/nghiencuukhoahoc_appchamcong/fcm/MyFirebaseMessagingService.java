package com.example.nghiencuukhoahoc_appchamcong.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.nghiencuukhoahoc_appchamcong.HomeAdminActivity;
import com.example.nghiencuukhoahoc_appchamcong.HomeUserActivity;
import com.example.nghiencuukhoahoc_appchamcong.MyApplication;
import com.example.nghiencuukhoahoc_appchamcong.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = MyFirebaseMessagingService.class.getName();
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        if (data != null && !data.isEmpty()) {
            String title = data.get("title");
            String body = data.get("body");
            sendNotification(title, body);
        }
    }


    private void sendNotification(String title, String body)
    {
        Intent intent = new Intent(this, HomeAdminActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Đặt auto-cancel để thông báo tự động biến mất khi người dùng chạm vào

        Notification notification = notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager != null)
        {
            notificationManager.notify(1, notification);
        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e(TAG,token);
    }
}
