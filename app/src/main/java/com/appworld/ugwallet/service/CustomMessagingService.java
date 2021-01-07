package com.appworld.ugwallet.service;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
//import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.appworld.ugwallet.MainActivity;
import com.appworld.ugwallet.utils.NotificationUtils;

import java.util.Map;

/**
 * Created by ugmart on 2/22/17.
 * This class receives the firebase messages into onMessageReceived() method.
 */

public class CustomMessagingService extends FirebaseMessagingService {

    private static final String TAG = CustomMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils=new NotificationUtils(this);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;


        if (remoteMessage.getNotification()!=null){
            Log.d(TAG, "onMessageReceived: ");
            if (NotificationUtils.isAppIsInBackground(getApplicationContext())) {


                /*NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.pugnotification_ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Much longer text that cannot fit one line...")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Much longer text that cannot fit one line..."))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);*/
            }else {
                Intent intent = new Intent("speedExceeded");
                intent.putExtra("title",remoteMessage.getNotification().getTitle());
                intent.putExtra("message",remoteMessage.getNotification().getBody());
                sendLocationBroadcast(intent);
            }
        }



    }
    private void sendLocationBroadcast(Intent intent){

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



    private void handleNotification(Intent intent) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        Log.d(TAG, "handleDataMessage: ");
        String title = data.get("title");
        String message =data.get("message");
        boolean update = data.containsKey("update") ? Boolean.valueOf(data.get("update")) : false;
        String imageUrl = data.containsKey("image") ? data.get("image") : "";
        String timestamp = data.containsKey("timestamp") ? data.get("timestamp") : "";

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            Log.d(TAG, "handleDataMessage: "+title);

            AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();

        } else {
            // app is in background, show the notification in notification tray
            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.putExtra("message", message);
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("update", update);

            // check for image attachment
            if (TextUtils.isEmpty(imageUrl)) {
                showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
            } else {
                // image is present, show notification with image
                showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
            }
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

}
