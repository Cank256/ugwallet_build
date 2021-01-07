package com.appworld.ugwallet.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by osalia on 5/9/18.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(Urls.TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(refreshedToken);
    }

    /*private void sendRegistrationToServer(String refreshedToken) {
        try {
            Bridge.post(Urls.fcm_device_reg)
                    .body(
                            new JSONObject()
                            .put("device_id",null)
                            .put("registration_id", refreshedToken)
                    ).asString(new ResponseConvertCallback<String>() {
                @Override
                public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                    Log.d(Urls.TAG, "onResponse: "+s);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/
}
