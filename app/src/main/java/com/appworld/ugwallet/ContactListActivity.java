package com.appworld.ugwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.appworld.ugwallet.adapters.ContactListAdapter;
import com.appworld.ugwallet.models.PhoneNumber;
import com.appworld.ugwallet.utils.NotificationUtils;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;

import java.util.ArrayList;

public class ContactListActivity extends BaseActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        String title = getResources().getString(R.string.contact_list);
        Utils.setActionBarTitle(ContactListActivity.this, getSupportActionBar(), title);

        //setup the broad cast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Utils.processNotification(ContactListActivity.this, intent);
            }
        };

        PrefManager prefManager = new PrefManager(getApplicationContext());
        ArrayList<PhoneNumber> phoneNumbers = prefManager.getPhoneNumbers();

        ListView contactList = (ListView)findViewById(R.id.contactList);
        TextView emptyResult = (TextView) findViewById(R.id.emptyResult);
        contactList.setEmptyView(emptyResult);

        //initialize the adapter
        if( phoneNumbers != null )
        {
            ContactListAdapter adapter = new ContactListAdapter(this, phoneNumbers);
            contactList.setAdapter(adapter);
        }
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
