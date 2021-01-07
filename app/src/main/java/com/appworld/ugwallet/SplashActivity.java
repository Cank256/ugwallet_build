package com.appworld.ugwallet;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appworld.ugwallet.utils.DataConnection;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity implements OnClickListener {

    private static String TAG = SplashActivity.class.getSimpleName();

    private PrefManager prefManager;
    private DataConnection connection;
    private String authHeader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefManager = new PrefManager(getApplicationContext());
        connection = new DataConnection(getApplicationContext());

        /*get the auth header*/
        getAuthHeader();

        /*initialize the buttons on the screen*/
        Button buyButton = (Button) findViewById(R.id.buyTokenButton);
        Button recentTransactionsBtn = (Button) findViewById(R.id.recentTransactionsButton);
        Button viewHistoryBtn = (Button) findViewById(R.id.viewHistoryButton);
        Button contactUsBtn = (Button) findViewById(R.id.contactButton);
        Button shareAppBtn = (Button) findViewById(R.id.share_app_btn);
        Button rateAppBtn = (Button) findViewById(R.id.rate_app_btn);
        Button registerBtn = (Button) findViewById(R.id.registerButton);
        Button signInBtn = (Button) findViewById(R.id.signinButton);

        //disable the automatic all-caps for the button text
        buyButton.setTransformationMethod(null);
        recentTransactionsBtn.setTransformationMethod(null);
        viewHistoryBtn.setTransformationMethod(null);
        contactUsBtn.setTransformationMethod(null);
        registerBtn.setTransformationMethod(null);
        signInBtn.setTransformationMethod(null);

        /*set click listeners*/
        buyButton.setOnClickListener(this);
        recentTransactionsBtn.setOnClickListener(this);
        viewHistoryBtn.setOnClickListener(this);
        contactUsBtn.setOnClickListener(this);
        shareAppBtn.setOnClickListener(this);
        rateAppBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        signInBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        /*determine which button was clicked*/
        switch (view.getId())
        {
            case R.id.buyTokenButton:
                if (connection.isConnectingToInternet()) {
                    /*only proceed if the auth header has a value*/
                    if (authHeader == null || prefManager.getTokenExpiryTime() <= System.currentTimeMillis()) {
                        getAuthHeader();
                    }
                    else {
                        Intent purchaseIntent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(purchaseIntent);
                    }
                }
                else {
                    Utils.showToastMessage(getApplicationContext(), getString(R.string.no_internet));
                }
                break;

            case R.id.recentTransactionsButton:
                /*only proceed if the auth header has a value*/
                if (authHeader == null || prefManager.getTokenExpiryTime() <= System.currentTimeMillis()) {
                    getAuthHeader();
                }
                else {
                    Intent transactionsIntent = new Intent(SplashActivity.this, TransactionsActivity.class);
                    startActivity(transactionsIntent);
                }
                break;

            case R.id.viewHistoryButton:
                Intent historyIntent = new Intent(SplashActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
                break;

            case R.id.contactButton:
                Intent contactIntent = new Intent(SplashActivity.this, ContactUsActivity.class);
                startActivity(contactIntent);
                break;

            case R.id.share_app_btn:
                shareApp();
                break;

            case R.id.rate_app_btn:
                rateApp();
                break;
            case R.id.registerButton:
                startActivity(
                        new Intent(SplashActivity.this,RegistrationActivity.class)
                );
                break;
            case R.id.signinButton:
                startActivity(
                        new Intent(SplashActivity.this,LoginActivity.class)
                );
                break;
        }
    }

    /**
     * method used to get the auth header from shared prefs or request a new one
     * **/
    private void getAuthHeader() {
        int expiryInMinutes = Utils.getMinutesFromMillis(prefManager.getTokenExpiryTime());
        int nowInMinutes = Utils.getMinutesFromMillis(System.currentTimeMillis());
        int diff = expiryInMinutes - nowInMinutes;
        
        if ( !TextUtils.isEmpty(prefManager.getAuthHeader()) && prefManager.getTokenExpiryTime() > System.currentTimeMillis() && diff > 10 ) {
            authHeader = prefManager.getAuthHeader();
        }
        else {
            final ProgressDialog progressDialog = Utils.createProgressDialog(this, "A moment please ...", false);
            progressDialog.show();

                /*formulate the login json parameters and start the volley request*/
            JSONObject requestParams = new JSONObject();
            try {
                requestParams.put("email", Utils.USERNAME);
                requestParams.put("password", Utils.PASSWORD);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, Utils.LOGIN_URL, requestParams, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            if( Utils.DEBUG ) {
                                Log.d(TAG, response.toString());
                            }

                            int expiresIn;
                            String token;
                            try {
                                token = response.getString("token");
                                authHeader = "Bearer "+ token;
                                expiresIn = response.getInt("expires_in");
                                prefManager.storeSessionDetails(token, expiresIn);
                            }
                            catch (JSONException e)
                            {
                                if( Utils.DEBUG ) {
                                    Log.e("Exception", e.toString());
                                }
                            }

                            progressDialog.hide();

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Utils.processVolleyError(getApplicationContext(), error, TAG);
                            //hide the dialog
                            progressDialog.hide();
                        }
                    });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjectRequest);
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

    /**
     * method used to initiate the app rating procedure
     * **/
    private void rateApp()
    {
        Uri uri = Uri.parse("market://details?id=" + Utils.APP_P_NAME);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Utils.getAppShareLink()));
        }
    }
}
