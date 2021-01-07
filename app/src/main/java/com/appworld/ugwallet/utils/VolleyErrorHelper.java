package com.appworld.ugwallet.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.appworld.ugwallet.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bernard on 11/21/16.
 * class that handles volley errors
 */
public class VolleyErrorHelper {

    /**
     * Returns appropriate message which is to be displayed to the user
     * against the specified error object.
     */
    public static String getMessage(Object error, Context context) {
        if (error instanceof TimeoutError) {
            return context.getResources().getString(R.string.generic_server_down);
        }
        else if (isServerProblem(error)) {
            return handleServerError(error, context);
        }
        else if (isNetworkProblem(error)) {
            return context.getResources().getString(R.string.unstable_internet);
        }
        return context.getResources().getString(R.string.generic_error);
    }

    /**
     * Determines whether the error is related to network
     */
    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }

    /**
     * Determines whether the error is related to server
     */
    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }
    /**
     * Handles the server error, tries to determine whether to show a stock message or to
     * show a message retrieved from the server.
     */
    private static String handleServerError(Object err, Context context) {
        VolleyError error = (VolleyError) err;
        String json = null;

        NetworkResponse response = error.networkResponse;
        String errorMsg = "Server Error: "+context.getResources().getString(R.string.generic_error);

        if (response != null) {
            String message = error.getMessage();
            switch (response.statusCode) {
                case 400:
                case 401:
                case 403:
                case 404:
                case 422:
                case 500:
                case 503:
                case 504:
                    // invalid request
                    try {
                        json = new String(response.data);
                        String trimmed = trimMessage(json, "message");
                        if(trimmed != null) message = trimmed;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
            }

            return message;
        }
        //return context.getResources().getString(R.string.generic_error);
        return errorMsg;
    }

    private static String trimMessage(String json, String key){
        String trimmedString = null;

        try{
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }

}
