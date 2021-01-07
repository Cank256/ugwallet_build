package com.appworld.ugwallet.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;

/**
 * Created by ugmart on 2/22/17.
 * This class receives the firebase registration id which will be unique to each app
 */

public class CustomInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = CustomInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        PrefManager prefManager = new PrefManager(getApplicationContext());
        prefManager.storePushNotificationId(refreshedToken);

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Utils.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        if ( Utils.DEBUG )
        {
            Log.e(TAG, "sendRegistrationToServer: " + token);
        }
    }


}
