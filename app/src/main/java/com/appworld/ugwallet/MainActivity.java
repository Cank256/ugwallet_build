package com.appworld.ugwallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.ResponseConvertCallback;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.appworld.ugwallet.models.PhoneNumber;
import com.appworld.ugwallet.models.Provider;
import com.appworld.ugwallet.models.Purchase;
import com.appworld.ugwallet.models.Transaction;
import com.appworld.ugwallet.utils.DataConnection;
import com.appworld.ugwallet.utils.NotificationUtils;
import com.appworld.ugwallet.utils.NumberWatcherForThousand;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;
import com.appworld.ugwallet.utils.VolleyErrorHelper;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private AutoCompleteTextView accountField;
    private EditText amountField;
    private Spinner providerSpinner;
    private AutoCompleteTextView phoneField;
    private AutoCompleteTextView emailField;
    private TextView phoneNoLabel;

    private PrefManager prefManager;
    private DataConnection connection;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private JSONObject chargeParams, purchaseParams;

    private ArrayList<Provider> providerArrayList;
    private ArrayList<String> acctHistory, searchHistory;
    private ArrayList<String> providerNames = new ArrayList<>();

    private String accountNo, phoneNumber, email, provider_name, provider_code, wait_message, transactionId, product_name, client_name;
    private int amount, charge, balance,revenue = 0;
    private int version = 7;
    private String authHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onNewIntent(getIntent());

        String title = getResources().getString(R.string.buy_yaka_token);
        Utils.setActionBarTitle(MainActivity.this, getSupportActionBar(), title);

        //initialize the helpers
        prefManager = new PrefManager(getApplicationContext());
        connection = new DataConnection(getApplicationContext());
        chargeParams = new JSONObject();
        purchaseParams = new JSONObject();

        /*make sure you have a valid auth header*/
        authHeader = prefManager.getAuthHeader();

        //setup the broad cast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Utils.processNotification(MainActivity.this, intent);
            }
        };

        //initialise the history search list
        acctHistory = prefManager.getSearchHistory(true) == null ? new ArrayList<String>() : prefManager.getSearchHistory(true);
        searchHistory = prefManager.getSearchHistory(false) == null ? new ArrayList<String>() : prefManager.getSearchHistory(false);

        //initialize the form fields
        phoneNoLabel = (TextView) findViewById(R.id.phone_no_text);
        accountField = (AutoCompleteTextView) findViewById(R.id.account_number_field);
        amountField = (EditText) findViewById(R.id.amount_field);
        providerSpinner = (Spinner) findViewById(R.id.provider_spinner);
        phoneField = (AutoCompleteTextView) findViewById(R.id.phone_number_field);
        emailField = (AutoCompleteTextView) findViewById(R.id.email_field);

        amountField.addTextChangedListener(new NumberWatcherForThousand(amountField));

        //get the buttons and set click listeners
        Button pickSavedNo = (Button) findViewById(R.id.pick_saved_no_btn);
        Button saveNo = (Button) findViewById(R.id.save_no_btn);
        Button makePayment = (Button) findViewById(R.id.pay_btn);
        
        pickSavedNo.setOnClickListener(this);
        saveNo.setOnClickListener(this);
        makePayment.setOnClickListener(this);

        //initialize the search adapters
        setAutoCompleteSource();

        //check internet connection and get form data
        if( connection.isConnectingToInternet() )
        {
            initializeForm();
        }
        else
        {
            Utils.showToastMessage(getApplicationContext(), getString(R.string.no_internet));
        }

    }

    @Override
    public void onNewIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("message") && extras.containsKey("title") && extras.containsKey
                    ("update"))
            {
                Utils.processNotification(MainActivity.this, intent);
            }
        }
    }

    private void setAutoCompleteSource()
    {
        /*set the minimum number of characters to be entered before the search starts*/
        accountField.setThreshold(1);
        phoneField.setThreshold(1);
        emailField.setThreshold(1);

        //attach adapters to the auto-complete fields to help with the search
        ArrayAdapter<String> acctAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, acctHistory);
        accountField.setAdapter(acctAdapter);

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchHistory);
        phoneField.setAdapter(searchAdapter);
        emailField.setAdapter(searchAdapter);
    }

    //add search input to the autocomplete history
    private void addSearchInput(String input, Boolean forAcctNo)
    {
        if ( forAcctNo )
        {
            if (!acctHistory.contains(input))
            {
                acctHistory.add(input);
                setAutoCompleteSource();
            }
        }
        else
        {
            if (!searchHistory.contains(input))
            {
                searchHistory.add(input);
                setAutoCompleteSource();
            }
        }
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

    private void initializeForm()
    {
        final ProgressDialog progressDialog = Utils.createProgressDialog(MainActivity.this, "Initializing. Please Wait ...", true);
        progressDialog.show();
        /*start the volley request*/

        JsonObjectRequest providersRequest = new JsonObjectRequest(Request.Method.GET, Utils.FORM_DATA_URL, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        if( Utils.DEBUG ) {
                            Log.d(TAG, response.toString());
                        }

                        try {
                            version = response.getInt("version");
                        }
                        catch (JSONException e)
                        {
                            if( Utils.DEBUG ) {
                                Log.e("Exception", e.toString());
                            }
                        }

                        JSONArray providers;
                        try {
                            providers = response.getJSONArray("data");
                            providerArrayList = new ArrayList<>();

                            for(int i = 0; i < providers.length(); i++)
                            {
                                JSONObject providerData;
                                try{
                                    providerData = providers.getJSONObject(i);
                                    String code = providerData.getString("code");
                                    String name = providerData.getString("name");
                                    boolean available = providerData.getBoolean("available");
                                    String message = getResources().getString(R.string.wait_message, name);

                                    if ( code.contains("visa") )
                                    {
                                        message = "Please wait as we prepare your VISA/MasterCard payment session. You will then complete the payment for UGMART with amount UGX.";
                                    }

                                    //create provider object using the received data
                                    if (available && !code.contains("visa")) {
                                        Provider provider = new Provider(code, name, message);
                                        providerNames.add(name);
                                        providerArrayList.add(provider);
                                    }

                                }
                                catch (JSONException e)
                                {
                                    if( Utils.DEBUG ) {
                                        Log.e("Exception", e.toString());
                                    }
                                }
                            }

                            //save the payment providers to the shared prefs
                            prefManager.saveServiceProviders(providerArrayList);

                        }
                        catch (JSONException e)
                        {
                            if( Utils.DEBUG ) {
                                Log.e("Exception", e.toString());
                            }
                        }

                        progressDialog.hide();
                        //set up the payment provider spinner
                        setUpProviderSpinner();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hide the dialog
                        progressDialog.hide();
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == Utils.CODE_UNAUTHORIZED || statusCode == Utils.CODE_FORBIDDEN) {
                            /*auth error, go back to the splash screen to regenerate the token*/
                            prefManager.resetAuthHeader();
                            Utils.showToastMessage(getApplicationContext(), getString(R.string.please_start_over));
                            finish();
                            startActivity(new Intent(MainActivity.this, SplashActivity.class));
                        }
                        else {
                            Utils.processVolleyError(getApplicationContext(), error, TAG);
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", authHeader);
                return headers;
            }
        };

        providersRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(providersRequest);

    }

    private void setUpProviderSpinner()
    {
        providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                provider_name = providerNames.get(position);
                Provider selectedProvider = providerArrayList.get(position);
                provider_code = selectedProvider.getCode();
                wait_message = selectedProvider.getWaitMessage();

                if ( provider_code.contains("visa") )
                {
                    phoneNoLabel.setText(getString(R.string.enter_contact_no));
                }
                else
                {
                    phoneNoLabel.setText(getString(R.string.enter_phone_no));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, providerNames);
        providerSpinner.setAdapter(providerAdapter);

        //check the versions and show update message in the dialog
        if ( version > Utils.getVersion(getApplicationContext()) )
        {
            AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
            ad.setCancelable(false);

            //show the update message
            ad.setTitle("Update Available");
            ad.setMessage( getString(R.string.update_available) );
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            rateApp();
                        }
                    });

            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            ad.show();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.pick_saved_no_btn:

                Utils.populateContactList(MainActivity.this, accountField);

                break;

            case R.id.save_no_btn:

                //validate the meter number field
                accountField.setError(null);
                String meter_number = accountField.getText().toString().trim();
                if( TextUtils.isEmpty(meter_number) )
                {
                    accountField.setError(getString(R.string.missing_number));
                    accountField.requestFocus();
                }
                else
                {
                    Utils.savePhoneNumber(MainActivity.this, meter_number.replaceAll("\\D+",""));
                }

                break;

            case R.id.pay_btn:

                //do field validation before proceeding
                accountField.setError(null);
                phoneField.setError(null);
                amountField.setError(null);
                emailField.setError(null);

                accountNo = accountField.getText().toString().replaceAll("\\s","");
                phoneNumber = phoneField.getText().toString().replaceAll("\\s","");
                email = emailField.getText().toString().replaceAll("\\s","");

                boolean cancelSend = false;
                View focusView = null;

                if ( TextUtils.isEmpty(accountNo) )
                {
                    accountField.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = accountField;
                }

                if ( TextUtils.isEmpty(phoneNumber) )
                {
                    phoneField.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = phoneField;
                }

                if ( phoneNumber.length() < 10 )
                {
                    phoneField.setError(getString(R.string.invalid_phone));
                    cancelSend = true;
                    focusView = phoneField;
                }

                String amountString = amountField.getText().toString().replaceAll("\\D+","");
                if ( TextUtils.isEmpty(amountString) )
                {
                    amountField.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = amountField;
                }
                else if ( Integer.parseInt(amountString) < Utils.MIN_AMOUNT )
                {
                    amountField.setError(getString(R.string.lower_amount));
                    cancelSend = true;
                    focusView = amountField;
                }
                else
                {
                    amount = Integer.parseInt(amountString);
                }

                if ( !TextUtils.isEmpty(email) )
                {
                    if ( !email.matches(Utils.EMAIL_REGEX) )
                    {
                        emailField.setError(getString(R.string.invalid_email));
                        cancelSend = true;
                        focusView = emailField;
                    }
                }

                if ( cancelSend )
                {
                    //if there are errors in the form, focus on the field with the error
                    focusView.requestFocus();
                }
                else
                {
                    /*all the fields have been filled in, continue with the process.*/
                    addSearchInput(phoneNumber, false);
                    prefManager.addSearchInput(phoneNumber, false);
                    addSearchInput(accountNo, true);
                    prefManager.addSearchInput(accountNo, true);
                    if( !TextUtils.isEmpty(email) )
                    {
                        addSearchInput(email, false);
                        prefManager.addSearchInput(email, false);
                    }

                    product_name = Utils.VENDOR_PRODUCT_NAME;

                    if (connection.isConnectingToInternet()) {
                        getClientDetails();
                    }
                    else
                    {
                        Utils.showToastMessage(getApplicationContext(), getString(R.string.no_internet));
                    }
                }

                break;
        }
    }

    private void getClientDetails()
    {
        final ProgressDialog progressDialog = Utils.createProgressDialog(MainActivity.this, "Loading details. Please wait ...", false);
        progressDialog.show();

        /*formulate the json parameters and start the volley request*/
        JSONObject requestParams = new JSONObject();
        try {
            requestParams.put("amount", amount);
            requestParams.put("msisdn", accountNo);
            requestParams.put("product_code", Utils.VENDOR_PRODUCT_CODE);

            JSONObject detailsObject = new JSONObject();
            detailsObject.put("contact_phone", phoneNumber);

            requestParams.put("details", detailsObject);
        } catch (JSONException e) {
            if (Utils.DEBUG) {
                e.printStackTrace();
            }
        }

        JsonObjectRequest validationRequest = new JsonObjectRequest(Request.Method.POST, Utils.VALIDATE_URL, requestParams,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (Utils.DEBUG) {
                            Log.e(TAG, response.toString());
                        }

                        JSONObject data;
                        try {
                            data = response.getJSONObject("data");

                            try {
                                client_name = data.getString("customer_name");
                                charge = 50 + data.getInt("tariff_charge");
                            }
                            catch (JSONException e)
                            {
                                if (Utils.DEBUG) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }

                            try {
                                balance = data.getInt("balance");
                            }
                            catch (JSONException e)
                            {
                                if (Utils.DEBUG) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }

                        }
                        catch (JSONException e) {
                            if (Utils.DEBUG) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        progressDialog.hide();

                        if ( amount > balance )
                        {
                            //save the account details in shared prefs
                            PhoneNumber accountDetails = new PhoneNumber(client_name, accountNo);
                            prefManager.saveAccountDetails(accountDetails);

                            //proceed to show confirmation screen
                            showConfirmDialog();
                        }
                        else
                        {
                            String errorMessage = "Dear <strong>"+client_name+"</strong>," +
                                    " UGX. "+Utils.formatCurrency(amount)+" is not " +
                                    "sufficient to pay your outstanding balance and buy a" +
                                    " token. At least <strong>UGX. "+Utils.formatCurrency
                                    (balance)+"</strong> is required.";
                            final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                            ad.setCancelable(false);
                            ad.setTitle("Insufficient Amount");
                            ad.setMessage( Utils.formatHtml(errorMessage) );
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            ad.show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hide the dialog
                        progressDialog.hide();
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == Utils.CODE_UNAUTHORIZED || statusCode == Utils.CODE_FORBIDDEN) {
                            /*auth error, go back to the splash screen to regenerate the token*/
                            prefManager.resetAuthHeader();
                            Utils.showToastMessage(getApplicationContext(), getString(R.string.please_start_over));
                            finish();
                            startActivity(new Intent(MainActivity.this, SplashActivity.class));
                        }
                        else {
                            Utils.processVolleyError(getApplicationContext(), error, TAG);
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", authHeader);
                return headers;
            }
        };

        validationRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(validationRequest);

    }

    //method that displays all transaction details for the user to verify before proceeding
    private void showConfirmDialog()
    {

        final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
        ad.setCancelable(false);

        ad.setTitle("Confirm Information");
        StringBuilder alertInfo = Utils.getAlertInformation(client_name, accountNo, product_name, amount, balance, charge, provider_name, phoneNumber);
        ad.setMessage(alertInfo);
        ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        /*set up the required request parameters and initiate the charge request*/
                        transactionId = Utils.generateUniqueId();
                        String purchaseId = "PUR"+transactionId;
                        int totalAmount = amount + charge;

                        JSONObject detailsObject = new JSONObject();
                        try {
                            detailsObject.put("contact_phone", phoneNumber);
                            if( !TextUtils.isEmpty(email) ) {
                                detailsObject.put("email", email);
                            }
                        } catch (JSONException e) {
                            if (Utils.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            chargeParams.put("account_code", Utils.ACCOUNT_CODE);
                            chargeParams.put("transaction_id", transactionId);
                            chargeParams.put("provider_id", provider_code);
                            chargeParams.put("msisdn", phoneNumber);
                            chargeParams.put("currency", "UGX");
                            chargeParams.put("amount", totalAmount);
                            chargeParams.put("application", Utils.APP_NAME);
                            chargeParams.put("description", Utils.VENDOR_PRODUCT_NAME);
                        } catch (JSONException e) {
                            if (Utils.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            purchaseParams.put("account_code", Utils.ACCOUNT_CODE);
                            purchaseParams.put("transaction_id", purchaseId);
                            purchaseParams.put("amount", amount);
                            purchaseParams.put("msisdn", accountNo);
                            purchaseParams.put("product_code", Utils.VENDOR_PRODUCT_CODE);
                            purchaseParams.put("description", Utils.VENDOR_PRODUCT_NAME);
                            purchaseParams.put("details", detailsObject);
                        } catch (JSONException e) {
                            if (Utils.DEBUG) {
                                e.printStackTrace();
                            }
                        }

                        if (connection.isConnectingToInternet()) {
                            //save the transaction details in shared prefs
                            Transaction transaction = new Transaction(transactionId, accountNo, product_name, amount, phoneNumber, provider_name);
                            if( !TextUtils.isEmpty(email) ) {
                                transaction.setRecipientEmail(email);
                            }
                            prefManager.saveTransaction(transaction);
                            /*make sure you have the valid token before processing the payment request*/
                            initiateChargeRequest();
                        }
                        else
                        {
                            Utils.showToastMessage(getApplicationContext(), getString(R.string.no_internet));
                        }

                    }
                });

        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        balance = 0;
                        charge = 0;
                        dialog.dismiss();
                    }
                });

        ad.show();
    }

    //method that requests money from the client's mobile wallet
    private void initiateChargeRequest()
    {
        //get the total amount to be transacted and formulate the dialog wait message
        int totalAmount = amount + charge;
        String dialogBody = wait_message+" "+Utils.formatCurrency(totalAmount);
        if ( !provider_code.contains("visa") )
        {
            dialogBody += " then wait for the transaction to complete";
        }
        final ProgressDialog progressDialog = Utils.createProgressDialog(MainActivity.this, dialogBody, false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                AppController.getInstance().getRequestQueue().cancelAll(TAG);
                dialog.dismiss();
            }
        });
        progressDialog.show();

        JsonObjectRequest paymentRequest = new JsonObjectRequest(Request.Method.POST, Utils.CHARGE_URL, chargeParams,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (Utils.DEBUG) {
                            Log.e(TAG, response.toString());
                        }

                        int code = 0;
                        try {
                            code = response.getInt("code");
                        } catch (JSONException e) {
                            if (Utils.DEBUG) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        //hide the dialog
                        progressDialog.hide();

                        if (code == Utils.CODE_OK) {
                            /*update the transaction status*/
                            prefManager.updateTransaction(transactionId, Utils.STATUS_COMPLETE, false, false);
                            purchaseToken();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hide the dialog
                        progressDialog.hide();

                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == Utils.CODE_UNAUTHORIZED || statusCode == Utils.CODE_FORBIDDEN) {
                            /*remove transaction from shared prefs*/
                            prefManager.removeTransaction(transactionId);
                            /*auth error, go back to the splash screen to regenerate the token*/
                            prefManager.resetAuthHeader();
                            Utils.showToastMessage(getApplicationContext(), getString(R.string.please_start_over));
                            finish();
                            startActivity(new Intent(MainActivity.this, SplashActivity.class));
                        }
                        else if (statusCode == Utils.CODE_GATEWAY_TIMEOUT) {
                            /*purchase is still pending*/
                            String message = "Your Mobile Money transaction is still <strong>IN PROGRESS</strong>. Please check the status after a few minutes. Click OK to proceed";
                            /*show the status message in a dialog*/
                            final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.payment_in_progress));
                            ad.setMessage(Utils.formatHtml(message));
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            finish();
                                            startActivity(new Intent(MainActivity.this, TransactionsActivity.class));
                                        }
                                    });

                            ad.show();
                        }
                        else {
                            /*update the transaction status*/
                            prefManager.updateTransaction(transactionId, Utils.STATUS_FAILED, false, false);

                            String errorMessage = VolleyErrorHelper.getMessage(error, getApplicationContext());
                            AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.payment_failed));
                            errorMessage = errorMessage + ". Click OK to proceed";
                            ad.setMessage(errorMessage);
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            finish();
                                            startActivity(new Intent(MainActivity.this, TransactionsActivity.class));
                                        }
                                    });

                            ad.show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", authHeader);
                return headers;
            }
        };

        // Set the tag on the request
        paymentRequest.setTag(TAG);
        paymentRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(paymentRequest);
    }

    //method that makes the purchase request
    private void purchaseToken() {
        String dialogBody = "Processing ...";
        final ProgressDialog progressDialog = Utils.createProgressDialog(MainActivity.this, dialogBody, false);
        progressDialog.show();

        JsonObjectRequest purchaseRequest = new JsonObjectRequest(Request.Method.POST, Utils.PURCHASE_URL, purchaseParams,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (Utils.DEBUG) {
                            Log.e(TAG, response.toString());
                        }

                        int code = 0;
                        try {
                            code = response.getInt("code");
                        } catch (JSONException e) {
                            if (Utils.DEBUG) {
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        JSONObject requestResponse;
                        String token = "", customerName = "", description = "", units = "", receiptNo = "";
                        try {
                            requestResponse = response.getJSONObject("data");

                            JSONObject details;
                            try{
                                details = requestResponse.getJSONObject("details");
                                try {
                                    customerName = details.getString("customer_name");
                                    token = details.getString("token");
                                    description = details.getString("description");
                                    units = details.getString("units");
                                    receiptNo = details.getString("receipt_number");
                                }
                                catch (JSONException e) {
                                    if (Utils.DEBUG) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
                            }
                            catch (JSONException e) {
                                if (Utils.DEBUG) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        }
                        catch (JSONException e) {
                            if (Utils.DEBUG) {
                                Log.e(TAG, e.getMessage());
                            }
                        }



                        if (code == Utils.CODE_OK) {
                            //post to the registrantion server


                            try {
                                final String finalCustomerName = customerName;
                                final String finalToken = token;
                                final String finalDescription = description;
                                final String finalUnits = units;
                                final String finalReceiptNo = receiptNo;
                                Bridge.post(Utils.SAVE_TRANSACTION_URL)
                                        .body(
                                                response.put("phone",phoneNumber)
                                                .put("platform",Utils.PLATFORM_CODE)
                                                .put("charge",charge)
                                                .put("amount",amount)
                                        ).asString(new ResponseConvertCallback<String>() {
                                    @Override
                                    public void onResponse(@Nullable com.afollestad.bridge.Response response, @Nullable String s, @Nullable BridgeException e) {

                                        progressDialog.hide();
                                        //display success response here

                                        String message = "Congratulations <strong>"+client_name+"</strong>," +
                                                " your purchase completed successfully. Your UMEME Yaka Token is <strong>"+ finalToken +"</strong>. Thank you for using the service.";
                                        final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                                        ad.setCancelable(false);

                                        //create a purchase record and save it to shared prefs
                                        String acctDetails = accountNo+" - "+ finalCustomerName;
                                        Purchase purchase = new Purchase(acctDetails, finalDescription, finalUnits, finalReceiptNo, finalToken, amount);
                                        prefManager.savePurchase(purchase);

                            /*update the purchase status of the transaction*/
                                        prefManager.updateTransaction(transactionId, Utils.STATUS_COMPLETE, true, false);

                                        //process was successful, display the message in the dialog
                                        ad.setTitle(getString(R.string.purchase_successful));
                                        ad.setMessage(Utils.formatHtml(message));
                                        ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        //dismiss the dialog and go to the purchase history
                                                        // activity
                                                        dialog.dismiss();
                                                        Intent historyIntent = new Intent(MainActivity.this,
                                                                HistoryActivity.class);
                                                        startActivity(historyIntent);
                                                    }
                                                });
                                        ad.show();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hide the dialog
                        progressDialog.hide();

                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == Utils.CODE_UNAUTHORIZED || statusCode == Utils.CODE_FORBIDDEN) {
                            /*auth error, go back to the splash screen to regenerate the token*/
                            prefManager.resetAuthHeader();
                            Utils.showToastMessage(getApplicationContext(), getString(R.string.please_start_over));
                            finish();
                            startActivity(new Intent(MainActivity.this, SplashActivity.class));
                        }
                        else if (statusCode == Utils.CODE_GATEWAY_TIMEOUT) {
                            /*purchase is still pending*/
                            String message = "UMEME Yaka Token Purchase still <strong>IN PROGRESS</strong>. Please check the purchase status after a few minutes. Click OK to proceed";
                            /*show the status message in a dialog*/
                            final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.purchase_in_progress));
                            ad.setMessage(Utils.formatHtml(message));
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            finish();
                                            startActivity(new Intent(MainActivity.this, TransactionsActivity.class));
                                        }
                                    });

                            ad.show();
                        }
                        else {
                            /*update the purchase transaction status*/
                            prefManager.updateTransaction(transactionId, Utils.STATUS_FAILED, true, false);

                            String errorMessage = "UMEME Yaka Purchase has <strong>FAILED</strong>. Click OK to retry your Yaka purchase. Please note that you will NOT be charged any extra money";
                            AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.purchase_failed));
                            ad.setMessage(Utils.formatHtml(errorMessage));
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            finish();
                                            startActivity(new Intent(MainActivity.this, TransactionsActivity.class));
                                        }
                                    });

                            ad.show();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", authHeader);
                return headers;
            }
        };

        purchaseRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(purchaseRequest);
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
