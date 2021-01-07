package com.appworld.ugwallet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.appworld.ugwallet.utils.DataConnection;
import com.appworld.ugwallet.utils.Utils;

/**
 * Created by ugmart on 2/27/17.
 * base activity to hold the global app menu
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, ContactListActivity.class));
                return true;

            case R.id.action_transactions:
                startActivity(new Intent(this, TransactionsActivity.class));
                return true;

            case R.id.action_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;

            case R.id.action_share:
                shareApp();
                return true;

            case R.id.action_refresh:
                //check internet connection and get form data
                DataConnection connection = new DataConnection(getApplicationContext());
                if( connection.isConnectingToInternet() )
                {
                    getFormData();
                }
                else
                {
                    Utils.showToastMessage(getApplicationContext(), getString(R.string.no_internet));
                }
                return true;

            case R.id.action_contact_us:
                callCustomerCare();
                return true;

            case R.id.home:
                onBackPressed();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * method used to initiate the app sharing procedure
     * **/
    private void shareApp()
    {
        String shareText = getString(R.string.recommendation) + ": " + Utils.getAppShareLink();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(share, getString(R.string.app_share)));
    }

    private void getFormData()
    {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * method used to initiate the phone call to customer care
     * **/
    private void callCustomerCare()
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Utils.getContactLink());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission to make phone call
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest
                        .permission.CALL_PHONE}, 1);
            }
            return;
        }
        startActivity(callIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Utils.getContactLink());
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        //request permission to make phone call
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                        }
                        return;
                    }
                    startActivity(callIntent);
                } else {

                    // permission denied
                    Utils.showToastMessage(getApplicationContext(), getString(R.string.call_permission_denied));

                }

            }
            break;

        }
    }

}
