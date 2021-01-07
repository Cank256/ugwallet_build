package com.appworld.ugwallet;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.appworld.ugwallet.adapters.PurchaseListAdapter;
import com.appworld.ugwallet.models.Purchase;
import com.appworld.ugwallet.utils.NotificationUtils;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends BaseActivity {

    private PrefManager prefManager;
    private ArrayList<Purchase> purchases;
    private PurchaseListAdapter adapter;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        String title = getResources().getString(R.string.purchase_history);
        Utils.setActionBarTitle(HistoryActivity.this, getSupportActionBar(), title);

        //initialize the utility classes
        prefManager = new PrefManager(getApplicationContext());

        //setup the broad cast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Utils.processNotification(HistoryActivity.this, intent);
            }
        };

        purchases = prefManager.getPurchases();
        ListView historyList = (ListView) findViewById(R.id.historyList);
        TextView emptyHistory = (TextView) findViewById(R.id.emptyHistory);
        historyList.setEmptyView(emptyHistory);

        //initialize the adapter
        if( purchases != null )
        {
            Collections.reverse(purchases);
            adapter = new PurchaseListAdapter(this, purchases);

            historyList.setAdapter(adapter);
        }

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //get selected purchase item
                final Purchase purchase = purchases.get(position);
                StringBuilder purchaseInfo = Utils.getPurchaseDetails(purchase);

                final AlertDialog ad = new AlertDialog.Builder(HistoryActivity.this).create();
                ad.setCancelable(false);
                ad.setTitle("Purchase Information");
                ad.setMessage(purchaseInfo);

                ad.setButton(DialogInterface.BUTTON_NEUTRAL, "Share Token",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("text/plain");
                                share.putExtra(Intent.EXTRA_TEXT, purchase.getToken());

                                startActivity(Intent.createChooser(share, getString(R.string.token_share)));

                            }
                        });

                ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });

                ad.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Utils.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Utils.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
