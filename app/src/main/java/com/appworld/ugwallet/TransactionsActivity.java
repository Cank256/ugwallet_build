package com.appworld.ugwallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.appworld.ugwallet.adapters.TransactionListAdapter;
import com.appworld.ugwallet.models.Purchase;
import com.appworld.ugwallet.models.Transaction;
import com.appworld.ugwallet.utils.DataConnection;
import com.appworld.ugwallet.utils.NotificationUtils;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TransactionsActivity extends BaseActivity {

    private static String TAG = TransactionsActivity.class.getSimpleName();

    private DataConnection connection;
    private PrefManager prefManager;
    private ArrayList<Transaction> transactions;
    private TransactionListAdapter adapter;
    private int amount;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String authHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        String title = getResources().getString(R.string.recent_transactions);
        Utils.setActionBarTitle(TransactionsActivity.this, getSupportActionBar(), title);

        //initialize the utility classes
        connection = new DataConnection(getApplicationContext());
        prefManager = new PrefManager(getApplicationContext());
        /*get the valid auth header*/
        authHeader = prefManager.getAuthHeader();

        //setup the broad cast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Utils.processNotification(TransactionsActivity.this, intent);
            }
        };

        transactions = prefManager.getTransactions();
        ListView transactionList = (ListView) findViewById(R.id.transactionList);
        TextView emptyResult = (TextView) findViewById(R.id.emptyResult);
        transactionList.setEmptyView(emptyResult);

        //initialize the adapter
        if (transactions != null) {
            Collections.reverse(transactions);
            adapter = new TransactionListAdapter(this, transactions);

            transactionList.setAdapter(adapter);

            /*add the help text in a dialog*/
            AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
            ad.setCancelable(false);
            ad.setTitle("Help Information");
            ad.setMessage(getString(R.string.retry_help));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            //dismiss the dialog
                            dialog.dismiss();
                        }
                    });

            ad.show();
        }

        transactionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (connection.isConnectingToInternet()) {
                    //get the transaction model in question
                    Transaction transaction = transactions.get(position);
                    amount = transaction.getAmount();

                    /*get the status as per the shared prefs to determine how to proceed*/
                    if (!TextUtils.isEmpty(transaction.getStatus()) && transaction.getStatus().equalsIgnoreCase(Utils.STATUS_COMPLETE)) {
                        /*payment succeeded to go ahead and check the purchase status*/
                        checkPurchaseStatus(transaction, position);
                    }
                    else {
                        /*check the payment status*/
                        checkTransactionStatus(transaction, position);
                    }
                } else {
                    Utils.showToastMessage(getApplicationContext(), getString(R.string.no_internet));
                }
            }
        });

    }

    private void checkTransactionStatus(final Transaction transaction, final int position) {
        String dialogBody = "Checking Payment Transaction. A moment please ....";
        final ProgressDialog progressDialog = Utils.createProgressDialog(TransactionsActivity.this, dialogBody, false);
        progressDialog.show();

        String url = Utils.TRANSACTION_CHECK_URL + "?id=" + transaction.getChargeId();
        /*start the volley request*/
        JsonObjectRequest transactionCheckRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        if( Utils.DEBUG ) {
                            Log.d(TAG, response.toString());
                        }

                        JSONObject data;
                        String status = "", statusMessage = "";
                        try {
                            data = response.getJSONObject("data");

                            status = data.getString("status");
                            statusMessage = data.getString("status_message");
                        }
                        catch (JSONException e)
                        {
                            if( Utils.DEBUG ) {
                                Log.e("Exception", e.toString());
                            }
                        }

                        progressDialog.hide();

                        /*check the returned status*/
                        if (status.equalsIgnoreCase(Utils.STATUS_COMPLETE)) {
                            /*update transaction status then go ahead to retry the purchase*/
                            prefManager.updateTransaction(transaction.getChargeId(), Utils.STATUS_COMPLETE, false, false);
                            retryPurchase(transaction, true);
                        }
                        else if (status.equalsIgnoreCase(Utils.STATUS_PENDING)) {
                            String message = "Your Mobile Money transaction is still <strong>IN PROGRESS</strong>. Please check the status after a few minutes";
                            /*show the status message in a dialog*/
                            final AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.payment_in_progress));
                            ad.setMessage(Utils.formatHtml(message));
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
                        else {
                            /*show the status message in a dialog*/
                            final AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.payment_failed));
                            statusMessage = statusMessage + ". You can keep the record OR delete it from your statement";
                            ad.setMessage(statusMessage);
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "KEEP",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "DELETE",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            int deletePosition = transactions.size() - (position + 1);
                                            prefManager.deleteTransaction(deletePosition);
                                            transactions.remove(position);
                                            adapter.notifyDataSetChanged();
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
                            startActivity(new Intent(TransactionsActivity.this, SplashActivity.class));
                        }
                        else if (statusCode == Utils.CODE_NOT_FOUND) {
                            /*transaction not found*/
                            int deletePosition = transactions.size() - (position + 1);
                            prefManager.deleteTransaction(deletePosition);
                            transactions.remove(position);
                            adapter.notifyDataSetChanged();
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

        transactionCheckRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(transactionCheckRequest);
    }

    private void retryPurchase(final Transaction transaction, boolean initialRequest) {
        /*set up the required parameters*/
        JSONObject detailsObject = new JSONObject();
        try {
            detailsObject.put("contact_phone", transaction.getPhone());
            if( !TextUtils.isEmpty(transaction.getRecipientEmail()) ) {
                detailsObject.put("email", transaction.getRecipientEmail());
            }
        } catch (JSONException e) {
            if (Utils.DEBUG) {
                e.printStackTrace();
            }
        }

        JSONObject purchaseParams = new JSONObject();
        String purchaseId = "PUR" + transaction.getChargeId();
        boolean incrementPurchaseRetries = false;
        if (!initialRequest) {
            /*if it is not the initial purchase request, modify the purchase ID with a retry counter*/
            int retryCount = transaction.getRetries() + 1;
            purchaseId += "_" + String.valueOf(retryCount);
            incrementPurchaseRetries = true;
        }

        try {
            purchaseParams.put("account_code", Utils.ACCOUNT_CODE);
            purchaseParams.put("transaction_id", purchaseId);
            purchaseParams.put("amount", amount);
            purchaseParams.put("msisdn", transaction.getAccountNo());
            purchaseParams.put("product_code", Utils.VENDOR_PRODUCT_CODE);
            purchaseParams.put("description", Utils.VENDOR_PRODUCT_NAME);
            purchaseParams.put("details", detailsObject);
        } catch (JSONException e) {
            if (Utils.DEBUG) {
                e.printStackTrace();
            }
        }

        String dialogBody = "Retrying Token Purchase. A moment please ....";
        final ProgressDialog progressDialog = Utils.createProgressDialog(TransactionsActivity.this, dialogBody, false);
        progressDialog.show();

        final boolean finalIncrementPurchaseRetries = incrementPurchaseRetries;
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

//                        progressDialog.hide();

                        if (code == Utils.CODE_OK) {
                            try {
                                final String finalCustomerName = customerName;
                                final String finalToken = token;
                                final String finalDescription = description;
                                final String finalUnits = units;
                                final String finalReceiptNo = receiptNo;
                                Bridge.post(Utils.SAVE_TRANSACTION_URL)
                                        .body(
                                                response.put("phone",transaction.getPhone())
                                                        .put("platform",Utils.PLATFORM_CODE)
                                                        .put("charge",transaction.getAmount())
                                                        .put("amount",transaction.getAmount())
                                        ).asString(new ResponseConvertCallback<String>() {
                                    @Override
                                    public void onResponse(@Nullable com.afollestad.bridge.Response response, @Nullable String s, @Nullable BridgeException e) {
                                        progressDialog.dismiss();
                                        String message = "Congratulations <strong>"+ finalCustomerName +"</strong>," +
                                                " your purchase completed successfully. Your UMEME Yaka Token is <strong>"+ finalToken +"</strong>. Thank you for using the service.";
                                        final AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
                                        ad.setCancelable(false);

                                        //create a purchase record and save it to shared prefs
                                        String acctDetails = transaction.getAccountNo()+" - "+ finalCustomerName;
                                        Purchase purchase = new Purchase(acctDetails, finalDescription, finalUnits, finalReceiptNo, finalToken, amount);
                                        prefManager.savePurchase(purchase);

                                /*update the purchase status*/
                                        prefManager.updateTransaction(transaction.getChargeId(), Utils.STATUS_COMPLETE, true, finalIncrementPurchaseRetries);

                                        //process was successful, display the message in the dialog
                                        ad.setTitle(getString(R.string.purchase_successful));
                                        ad.setMessage(Utils.formatHtml(message));
                                        ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                /*dismiss the dialog and go to the purchase history activity*/
                                                        dialog.dismiss();
                                                        Intent historyIntent = new Intent(TransactionsActivity.this,
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
                            startActivity(new Intent(TransactionsActivity.this, SplashActivity.class));
                        }
                        else {
                            /*update the purchase status*/
                            prefManager.updateTransaction(transaction.getChargeId(), Utils.STATUS_FAILED, true, finalIncrementPurchaseRetries);
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

        purchaseRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(purchaseRequest);
    }

    private void checkPurchaseStatus(final Transaction transaction, final int position) {
        String dialogBody = "Checking Purchase Transaction. A moment please ....";
        final ProgressDialog progressDialog = Utils.createProgressDialog(TransactionsActivity.this, dialogBody, false);
        progressDialog.show();

        String purchaseId = "PUR"+transaction.getChargeId();
        if (transaction.getRetries() > 0) {
            purchaseId += "_" + String.valueOf(transaction.getRetries());
        }
        String url = Utils.PURCHASE_CHECK_URL + "?collectionAccount=" + Utils.ACCOUNT_CODE + "&id=" + purchaseId;
        /*start the volley request*/
        JsonObjectRequest transactionCheckRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        if( Utils.DEBUG ) {
                            Log.d(TAG, response.toString());
                        }

                        JSONObject data = new JSONObject(), details;
                        String status = "";
                        try {
                            data = response.getJSONObject("data");

                            status = data.getString("status");
                        }
                        catch (JSONException e)
                        {
                            if( Utils.DEBUG ) {
                                Log.e("Exception", e.toString());
                            }
                        }

                        String token = "", customerName = "", description = "", units = "", receiptNo = "";
                        try {
                            details = data.getJSONObject("details");
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
                        catch (JSONException e)
                        {
                            if( Utils.DEBUG ) {
                                Log.e("Exception", e.toString());
                            }
                        }

                        progressDialog.hide();

                        /*check the returned status*/
                        String message;
                        if (status.equalsIgnoreCase(Utils.STATUS_COMPLETE)) {
                            message = "Congratulations <strong>"+customerName+"</strong>," +
                                    " your purchase completed successfully. Your UMEME Yaka Token is <strong>"+token+"</strong>. Thank you for using the service.";
                            final AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
                            ad.setCancelable(false);

                            /*determine whether or not to save the purchase record in shared prefs*/
                            if (!TextUtils.isEmpty(transaction.getPurchaseStatus()) && !transaction.getPurchaseStatus().equalsIgnoreCase(Utils.STATUS_COMPLETE)) {
                                //create a purchase record and save it to shared prefs
                                String acctDetails = transaction.getAccountNo()+" - "+customerName;
                                Purchase purchase = new Purchase(acctDetails, description, units, receiptNo, token, amount);
                                prefManager.savePurchase(purchase);

                                /*update the purchase status*/
                                prefManager.updateTransaction(transaction.getChargeId(), Utils.STATUS_COMPLETE, true, false);
                            }

                            //process was successful, display the message in the dialog
                            ad.setTitle(getString(R.string.purchase_successful));
                            ad.setMessage(Utils.formatHtml(message));
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "KEEP",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            Intent historyIntent = new Intent(TransactionsActivity.this,
                                                    HistoryActivity.class);
                                            startActivity(historyIntent);
                                        }
                                    });
                            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "DELETE",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            int deletePosition = transactions.size() - (position + 1);
                                            prefManager.deleteTransaction(deletePosition);
                                            transactions.remove(position);
                                            adapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    });

                            ad.show();
                        }
                        else if (status.equalsIgnoreCase(Utils.STATUS_PENDING)) {
                            message = "Purchase transaction still <strong>IN PROGRESS</strong>. Please check purchase status after a few minutes";
                            /*show the status message in a dialog*/
                            final AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.purchase_in_progress));
                            ad.setMessage(Utils.formatHtml(message));
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
                        else {
                            message = "UMEME Yaka Purchase <strong>FAILED</strong>. Click OK to retry your Yaka purchase. Please note that you will NOT be charged any extra money";
                            /*show the status message in a dialog*/
                            final AlertDialog ad = new AlertDialog.Builder(TransactionsActivity.this).create();
                            ad.setCancelable(false);

                            ad.setTitle(getString(R.string.purchase_failed));
                            ad.setMessage(Utils.formatHtml(message));
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            retryPurchase(transaction, false);
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
                            startActivity(new Intent(TransactionsActivity.this, SplashActivity.class));
                        }
                        else if (statusCode == Utils.CODE_NOT_FOUND) {
                            retryPurchase(transaction, true);
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

        transactionCheckRequest.setRetryPolicy(new DefaultRetryPolicy(
                Utils.VOLLEY_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(transactionCheckRequest);
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
