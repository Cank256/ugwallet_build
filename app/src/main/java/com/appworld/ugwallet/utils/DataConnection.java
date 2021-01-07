package com.appworld.ugwallet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by bernard on 12/31/16.
 * class that manages the internet connection
 */
public class DataConnection {

    private Context _context;

    public DataConnection(Context context){
        this._context = context;
    }

    public boolean isConnectingToInternet(){

        ConnectivityManager connectivityMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityMgr.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnected();
    }

}
