package com.appworld.ugwallet.service;

import android.app.Notification;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.appworld.ugwallet.R;

import java.util.Iterator;
import java.util.Map;

import br.com.goncalves.pugnotification.notification.PugNotification;

/**
 * Created by osalia on 5/9/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
//        Log.d(Urls.TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String,String> data = remoteMessage.getData();
//            Log.d(Urls.TAG, "Message data payload: " + remoteMessage.getData())

            Iterator<Map.Entry<String, String>> i = data.entrySet().iterator();
            Map.Entry<String, String> entry = null;
            String title=null;
            String message = null;
            while (i.hasNext()) {
                entry = i.next();

                String key = entry.getKey();
                String value = entry.getValue();
                switch (key){
                    case "title":
                        title = value;
                        break;
                    case "message":
                        message = value+"1";
                        break;
                }

            }

            PugNotification.with(this)
                    .load()
                    .title(title)
                    .message(message)
                    .bigTextStyle(message)
                    .smallIcon(R.mipmap.ic_launcher)
                    .largeIcon(R.mipmap.ic_launcher)
                    .flags(Notification.DEFAULT_ALL)
//                    .button(icon, title, pendingIntent)
//                    .click(cctivity, bundle)
//                    .dismiss(activity, bundle)
                    .color(R.color.colorPrimary)
                    .autoCancel(true)
                    .simple()
                    .build();



            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
               // handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
