package com.appworld.ugwallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.appworld.ugwallet.models.Purchase;
import com.appworld.ugwallet.utils.LogoutTimerUtil;
import com.appworld.ugwallet.utils.PrefManager;
import com.appworld.ugwallet.utils.Utils;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AgentHomeActivity extends AppCompatActivity implements View.OnClickListener,LogoutTimerUtil.LogOutListener {
    public final int PICK_CONTACT = 2015;
    private static final int AUTHORIZE_TOPUP_REQUEST_CODE = 23;
    private static final String TAG = AgentHomeActivity.class.getSimpleName();
    boolean refresh = true;
    AutoCompleteTextView visiblePhoneField = null;
    @BindView(R.id.balance_btn) Button btn_balance;
    @BindView(R.id.commission_btn) Button btn_commision;
    @BindView(R.id.withraw_btn) Button btn_withdraw;
    @BindView(R.id.topupWalletBtn) Button btn_topup;
    @BindView(R.id.buyAirtimeBtn) Button btn_airtime;
    @BindView(R.id.buyDataBtn) Button btn_data;
    @BindView(R.id.buyYakaBtn) Button btn_yaka;
    @BindView(R.id.payWaterBtn) Button btn_water;
    @BindView(R.id.payTVBtn) Button btn_PayTV;
    @BindView(R.id.viewHistoryButton) Button btn_history;
    @BindView(R.id.share_app_btn) Button btn_share;
    @BindView(R.id.rate_app_btn) Button btn_rate;
    @BindView(R.id.contactButton) Button btn_contactus;
    @BindView(R.id.sendMoneyBtn) Button btn_sendmoney;
    private JSONObject clientObject;
    private long back_pressed =0;
    private Handler handler;
    private long balanceCounter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_home);
        ButterKnife.bind(this);
        clientObject= PrefManager.getLastLoginUser(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("speedExceeded"));
        /*
        try {
            getSupportActionBar().setTitle("Ug Wallet ("+clientObject.getString("phone")+")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        //mTitleTextView.setText("My Own Title");
        try {
            mTitleTextView.setText("Ug Wallet ("+clientObject.getString("phone")+")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ImageButton refreshButton = (ImageButton) mCustomView
                .findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getWalletBalance();
            }
        });

        ImageButton logoutButton = (ImageButton) mCustomView
                .findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {

                                           @Override
                                           public void onClick(View view) {
                                               final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                               ad.setCancelable(false);

                                               ad.setTitle("Logout Confirmation");
                                               ad.setMessage(Utils.formatHtml("Are you sure you want to sign out?"));
                                               ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                                                   @Override
                                                   public void onClick(DialogInterface dialog,
                                                                       int which) {

                                                   }
                                               });
                                               ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                       new DialogInterface.OnClickListener() {

                                                           @Override
                                                           public void onClick(DialogInterface dialog,
                                                                               int which) {
                                                               doLogout(false);
                                                           }
                                                       });

                                               ad.show();
                                           }
                                       });
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        btn_withdraw.setOnClickListener(this);
        btn_topup.setOnClickListener(this);
        btn_airtime.setOnClickListener(this);
        btn_data.setOnClickListener(this);
        btn_yaka.setOnClickListener(this);
        btn_water.setOnClickListener(this);
        btn_PayTV.setOnClickListener(this);
        btn_history.setOnClickListener(this);
        btn_share.setOnClickListener(this);
        btn_rate.setOnClickListener(this);
        btn_contactus.setOnClickListener(this);
        btn_sendmoney.setOnClickListener(this);

        handler = new Handler();
//        getWalletBalance();
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String title = intent.getStringExtra("title");

            String message = intent.getStringExtra("message")

                    ;

            final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
            ad.setCancelable(false);

            ad.setTitle(title);
            ad.setMessage(Utils.formatHtml(message));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            dialog.dismiss();
                            getWalletBalance();
                        }
                    });

            ad.show();
        }
    };

    private void getWalletBalance() {
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Retrieving balance... Please Wait", false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        try {
            //Log.d(TAG, "Token "+clientObject.getString("token"));
            Bridge.get(Utils.WALLET_BALANCE_URL)
                    .header("Authorization","Token "+clientObject.getString("token"))
                    .asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                            refresh = false;
                            progressDialog.dismiss();
                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if(s==null){
                                refresh = false;
                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                btn_balance.setText("Wallet Bal: \nUgx. "+jsonObject.getString("balance"));
                                btn_commision.setText("Commission Bal: \nUgx. "+jsonObject.getString("commission"));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    final Runnable runnable = new Runnable() {
        public void run() {
            //Log.d("Runnable","Handler is working");
            if(balanceCounter == 5){ // just remove call backs
                handler.removeCallbacks(this);
                //Log.d("Runnable","ok");
            } else { // post again
                balanceCounter++;
                getWalletBalance();
                handler.postDelayed(this, 3000);
            }
        }
    };
    @Override
    public void onClick(View view) {
        LogoutTimerUtil.stopLogoutTimer();
        switch (view.getId()){
            case R.id.withraw_btn:
                try {
                    initiateWithdraw();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.topupWalletBtn:
                try {
                    initiateTopUp();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buyAirtimeBtn:
                try {
                    //initiateAirtimePurchase(Utils.PRODUCT_AIRTIME);
                    buyProduct(Utils.PRODUCT_AIRTIME);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buyDataBtn:
                try {
                    //initiateAirtimePurchase(Utils.PRODUCT_INTERNET);
                    buyProduct(Utils.PRODUCT_INTERNET);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buyYakaBtn:

                try {
                    //initiateAirtimePurchase(Utils.PRODUCT_YAKA);
                    buyProduct(Utils.PRODUCT_YAKA);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.payWaterBtn:

                try {
                    //initiateAirtimePurchase(Utils.PRODUCT_WATER);
                    buyProduct(Utils.PRODUCT_WATER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.payTVBtn:
                try {
                    //initiateAirtimePurchase(Utils.PRODUCT_TV);
                    buyProduct(Utils.PRODUCT_TV);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sendMoneyBtn:

                try {
                    //initiateAirtimePurchase(Utils.PRODUCT_SENDMONEY);
                     sendMoney();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.viewHistoryButton:
                startActivity(
                        new Intent(
                                AgentHomeActivity.this,AgentHistoryActivty.class
                        )
                                .putExtra("data",clientObject.toString())
                );
                break;
            case R.id.contactButton:
                startActivity(
                        new Intent(
                                AgentHomeActivity.this,ContactUsActivity.class
                        )
                                .putExtra("data",clientObject.toString())
                );
                break;
            case R.id.rate_app_btn:
                rateApp();
                break;
            case R.id.share_app_btn:
                shareApp();
                break;

        }
    }
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

    private void shareApp()
    {
        String shareText = getString(R.string.recommendation) + ": " + Utils.getAppShareLink();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(share, getString(R.string.app_share)));
    }


    void setupOptions(final DialogHolder holder, final JSONObject[] services_objects, final String product, String product_pref, final Spinner spinner) {
        String token;
        try {
            token = clientObject.getString("token");
            //Log.d(TAG, "Token: "+token);
        } catch (JSONException ex) {
            return;
        }
        if (product.equals(Utils.PRODUCT_YAKA)){
            //do nothing

        }else if (product.equals(Utils.PRODUCT_SENDMONEY)){
            String[] providerNames =new String[]{"Ug Wallet Account", "Airtel Money", "MTN Mobile Money"};

            ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, providerNames);
            spinner.setAdapter(providerAdapter);
        }
        else if(services_objects[0] ==null){
            final String finalProduct_pref = product_pref;
            String url = Utils.WALLET_GET_SERVICE_LIST+"?type="+product;
            if(product.equals(Utils.PRODUCT_WATER)){
                url = Utils.WALLET_NWSC_AREAS_LIST;
            }
            //Log.d(TAG, "service list url: "+url);
            Bridge.get(url).
                    header("Authorization","Token "+ token)
                    .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {

                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if(s==null){
                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                if(product.equals(Utils.PRODUCT_WATER)){
                                    setUpProviderWaterSpinner(new JSONObject(s),spinner);
                                }else{
                                    setUpProviderSpinner(new JSONObject(s),spinner);
                                }
                                services_objects[0] = new JSONObject(s);
                                PrefManager.saveServices(AgentHomeActivity.this, finalProduct_pref,new JSONObject(s).toString());
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
        }else {
            try {
                //Log.d(TAG, "services already retrieved: "+services_objects[0].toString());
                if(product.equals(Utils.PRODUCT_WATER)){
                    setUpProviderWaterSpinner(services_objects[0],spinner);
                }else {
                    setUpProviderSpinner(services_objects[0], spinner);
                }
            } catch (JSONException ex) {
                //Log.d(TAG, "setupprovider spinner exception");
            }
        }

        holder.providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //holder.ccp.setVisibility(View.VISIBLE);
                //get selected item
                if(product.equals(Utils.PRODUCT_SENDMONEY)){
                    if (i==0){
                        holder.phone_no_TextView.setText("Ug Wallet A/C Phone Number Eg. 0752200600");
                    }else if(i==1){
                        holder.phone_no_TextView.setText("Airtel Money Number Eg. 0752200600");
                    } else{
                        holder.phone_no_TextView.setText("MTN Mobile Money Number Eg. 0776220110");
                    }
                } else if(product.equals(Utils.PRODUCT_INTERNET) || product.equals(Utils.PRODUCT_AIRTIME)){
                    String selectedProvider = adapterView.getSelectedItem().toString();
                    if (selectedProvider.equals("Smile Internet")|| selectedProvider.equals("Smile Airtime")) {
                        holder.phone_no_TextView.setText("Smile Account Number e.g 1503000477");
                        //holder.ccp.setVisibility(View.GONE);
                    } else {
                        holder.phone_no_TextView.setText("Phone Number Eg. 0752200600 (For Transaction)");
                    }
                }
                try {
                    JSONObject object = services_objects[0].getJSONArray("results")
                            .getJSONObject(i);
                    if (object.getBoolean("has_price_list")){
                        setUpPackageSpinner(object, holder.packageSpinner);
                        if(product.equals(Utils.PRODUCT_TV)){
                            holder.amount_layout.setVisibility(View.GONE);
                            holder.details_spinner_layout.setVisibility(View.VISIBLE);
                        }
                    }else{
                        if(product.equals(Utils.PRODUCT_TV)){
                            holder.amount_layout.setVisibility(View.VISIBLE);
                            holder.details_spinner_layout.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Log.d(TAG, "onItemSelected: "+e.toString());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    boolean hasErrors(String s, int amount) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.getString("status").equals("FAILED")) {
                return true;
            }

            String names = "";
            String balance = "";
            if (jsonObject.getJSONObject("data").has("balance")) {
                if (jsonObject.getJSONObject("data").getInt("balance") > amount) {

                    return true;
                }
            }
        } catch (JSONException ex) {
            return true;
        }
        return false;
    }

    void handleErrors(String s, int amount) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.getString("status").equals("FAILED")) {
                new AlertDialog.Builder(AgentHomeActivity.this)
                        .setTitle("Error")
                        .setMessage(jsonObject.getString("message"))
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return;
            }

            String names = "";
            String balance = "";
            if (jsonObject.getJSONObject("data").has("balance")) {
                names = "\nOver Due Amount: " + jsonObject.getJSONObject("data").getString("balance");
                if (jsonObject.getJSONObject("data").getInt("balance") > amount) {
                    new AlertDialog.Builder(AgentHomeActivity.this)
                            .setTitle("Insufficient Amount")
                            .setMessage("Amount " + amount + " is not enough to clear the outstanding balance of " + jsonObject.getJSONObject("data").getInt("balance"))
                            .setCancelable(false)
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                    return;
                }
            }
        } catch (JSONException ex) {
            return;
        }
        return;
    }

    private boolean resultValue;
    public boolean getDialogValueBack(Context context, String title, String message, String positiveText, String negativeText)
    {
        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message mesg)
            {
                throw new RuntimeException();
            }
        };

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton(positiveText, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                resultValue = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.setNegativeButton(negativeText, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                resultValue = false;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.show();

        try{ Looper.loop(); }
        catch(RuntimeException e){}

        return resultValue;
    }
    boolean showConfirmationDialog(String s, int amount, String finalProduct_title, String phoneNumber) {
        int charge = 0;
        String names = "";
        String msisdn = "";
        String balance = "";
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.getJSONObject("data").has("balance"))
                balance = jsonObject.getJSONObject("data").getString("balance");
            if (jsonObject.getJSONObject("data").has("customer_name")) {
                names = "\nCustomer Names: " + jsonObject.getJSONObject("data").getString("customer_name");
            }
            if (jsonObject.getJSONObject("data").has("tariff_charge")) {
                charge = jsonObject.getJSONObject("data").getInt("tariff_charge");
            }
            msisdn = jsonObject.getJSONObject("data").getString("msisdn");
        } catch(JSONException ex) {
            return false;
        }

        final int finalCharge = charge;
        String title = "TransactionConfirmation";

        String message =
                        "Product Name: "+ finalProduct_title +
                                "\nAmount: "+amount+
                                "\nAccount No: "+ msisdn+
                                names+
                                "\nTransaction Number: "+ phoneNumber+
                                "\nCharge: "+charge+"" +
                                "\nBalance: " + balance+"" +
                                "\nTotal Amount Payable: "+(amount+charge);


        String positiveText = "OK";
        String negativeText = "CANCEL";

        boolean result = getDialogValueBack(AgentHomeActivity.this, title, message, positiveText, negativeText);
        return result;

    }

    boolean showMtnConfirmationDialog( int amount, int charge, String finalProduct_title, String phoneNumber) {
        //int charge = 1000;


        final int finalCharge = charge;
        String title = "TransactionConfirmation";

        String message =
                "Product Name: "+ finalProduct_title +
                        "\nAmount: "+amount+
                        "\nPhone No: "+  phoneNumber+
                        "\nCharge: "+charge+"" +
                        "\nTotal Amount Payable: "+(amount+charge);


        String positiveText = "OK";
        String negativeText = "CANCEL";

        boolean result = getDialogValueBack(AgentHomeActivity.this, title, message, positiveText, negativeText);
        return result;

    }

    void handleResponse(int amount, String s, String finalProduct_title, final AlertDialog withdraw_Alert) {
        if(Utils.DEBUG){
            Log.d(TAG, "onResponse: "+s);
        }
        if(s==null){
            Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("mm_status")) {
                if (jsonObject.getString("mm_status").equals("SUCCESS")) {
                    String message = "Your " + finalProduct_title + " transaction was <strong>SUCCESSFUL</strong>." +
                            "\nYour new Wallet Balance is Ugx. " + jsonObject.getString("client_balance_after_wallet_sell") +
                            "\nTransaction ID: " + jsonObject.getString("mm_transaction_id")
                            ;
                    final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                    ad.setCancelable(false);

                    ad.setTitle("Transaction Successful");
                    ad.setMessage(Utils.formatHtml(message));
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    getWalletBalance();
                                    withdraw_Alert.dismiss();
                                }
                            });

                    ad.show();
                    return;
                }
            }

            if (jsonObject.has("detail")){
                new AlertDialog.Builder(AgentHomeActivity.this)
                        .setTitle("Error")
                        .setMessage(jsonObject.getString("detail"))
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return;
            }
            if (jsonObject.has("error")){
                new AlertDialog.Builder(AgentHomeActivity.this)
                        .setTitle("Error")
                        .setMessage(jsonObject.getString("error"))
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return;
            }else if (jsonObject.has("message")) {
                String message = jsonObject.getString("message");

                String token = "";
                if (jsonObject.has("mm_token")) {
                    if (jsonObject.getString("mm_token").length() > 6) {
                        token = "\nThe Yaka token is <strong>" + jsonObject.getString("mm_token") +
                                "</strong>.";
                    }

                }
                message += token;
                //"details":{
                // "customer_name":"SSEBADUKA, MUS",
                // "receipt_number":"7131639968",
                // "token":"3277 3068 1199 9338 9043",
                // "token_value":"8474.58",
                // "units":11.1,
                // "debt_recovery":0,
                // "description":"11.1 KWh at UGX 769.00 per KWh"}}} | 2019-03-24 12:31:35 |           3647 |
                if (jsonObject.has("details")) {
                    JSONObject details = jsonObject.getJSONObject("details");
                    if (details.has("token")) {
                        Purchase purchase = new Purchase(
                                details.optString("customer_name", ""),
                                details.optString("description", ""),
                                details.optString("units", ""),
                                details.optString("receipt_number", ""),
                                details.optString("token", ""),
                                amount
                        );
                        PrefManager prefManager = new PrefManager(AgentHomeActivity.this);
                        prefManager.savePurchase(purchase);
                    }
                }


                final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                ad.setCancelable(false);

                ad.setTitle("Transaction Succeeded");
                ad.setMessage(Utils.formatHtml(message));
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                withdraw_Alert.dismiss();
                                getWalletBalance();

                            }
                        });

                ad.show();
            } else if (jsonObject.getString("mm_status").equals( "PENDING")) {
                /*purchase is still pending*/
                String message = "Your "+finalProduct_title+" transaction is still <strong>IN PROGRESS</strong>." +
                        " Please check the status after a few minutes. Transaction ID:"+
                        jsonObject.getString("mm_transaction_id");
                /*show the status message in a dialog*/
                final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                ad.setCancelable(false);

                ad.setTitle("Transaction in Progress");
                ad.setMessage(Utils.formatHtml(message));
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                withdraw_Alert.dismiss();
                            }
                        });

                ad.show();
            }
            else {
                /*update the transaction status*/

                AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                ad.setCancelable(false);

                ad.setTitle("Transaction Failed");

                ad.setMessage(finalProduct_title+ " Transaction Failed, Please try again");
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
        } catch (JSONException e1) {
            e1.printStackTrace();
            //Log.d(TAG, "onResponse: "+e1.toString());
        }
    }

    void postTransaction(String product, final String finalProduct_title, String mssid, final int amount,
                                int finalCharge, String finalArea_id, String phoneNumber,
                                String product_code, JSONObject clientObject, final AlertDialog withdraw_Alert) {
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
        String message = "Processing "+finalProduct_title+". Please Wait...";
        progressDialog.setMessage(message);
        progressDialog.show();
        String url = Utils.PURCHASE_PRODUCT_URL;

        String token = "";
        JSONObject params = null;
        try {
            token = clientObject.getString("token");
            params = new JSONObject()
                    .put("mssid",mssid)
                    .put("amount",amount)
                    .put("payable_amount",amount+ finalCharge)
                    .put("area_id", finalArea_id)
                    .put("phone",phoneNumber)
                    .put("product_code",
                            product_code
                    )
                    .put("description",
                            product_code
                    );
            if (product.equals(Utils.PRODUCT_SENDMONEY)) {
                String withdraw_type = "";
                if(product_code.equals("TRANSFER_TO_UGWALLET") == false) {
                    withdraw_type = "send_external";
                }
                params.put("withdraw", withdraw_type);
            }
        } catch (JSONException ex) {
            return;
        }
        Bridge.post(url)
                .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                .header("Content-Type","application/json")
                .header("Authorization","Token "+token)
                .body(params)
                .asString(new ResponseConvertCallback<String>() {
                    @Override
                    public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                        progressDialog.dismiss();

                        handleResponse(amount, s, finalProduct_title, withdraw_Alert);
                    }
                });

    }

    boolean validateForm(DialogHolder holder, JSONObject[] services_objects, String product, String amt, int amount, String phoneNumber, String accountNumber ) {
        if (product.equals(Utils.PRODUCT_SENDMONEY)) {
            if (holder.providerSpinner.getSelectedItemPosition() == 2 && phoneNumber.matches(Utils.MTN_REGEX)==false){
                holder.phone.setError("Enter a valid MTN Number");
                holder.phone.requestFocus();
                return false;
            }
            if (holder.providerSpinner.getSelectedItemPosition() == 1 && phoneNumber.matches(Utils.AIRTEL_REGEX)==false){
                holder.phone.setError("Enter a valid Airtel Number");
                holder.phone.requestFocus();
                return false;
            }
        }
        if(product.equals(Utils.PRODUCT_AIRTIME)
                ||product.equals(Utils.PRODUCT_YAKA)
                ||product.equals(Utils.PRODUCT_SENDMONEY)
                ||product.equals(Utils.PRODUCT_WATER)) {
            if (amt.length() == 0) {
                holder.amountField.setError(getString(R.string.required_field));
                holder.amountField.requestFocus();
                return false;

            }
            if (amount < 500) {
                holder.amountField.setError("Amount less than minimum");
                holder.amountField.requestFocus();
                return false;
            }
            if (amount > 5000000) {
                holder.amountField.setError("Amount greater than maximum (5000000)");
                holder.amountField.requestFocus();
                return false;
            }
        }

        try {
            if(product.equals(Utils.PRODUCT_TV) && !services_objects[0].getJSONArray("results")
                    .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                    .getBoolean("has_price_list")){
                if(amt.length()==0){
                    holder.amountField.setError(getString(R.string.required_field));
                    holder.amountField.requestFocus();
                    return false;

                }
                if(amount<500){
                    holder.amountField.setError("Amount less than minimum");
                    holder.amountField.requestFocus();
                    return false;
                }
                if(amount>5000000){
                    holder.amountField.setError("Amount greater than maximum (5000000)");
                    holder.amountField.requestFocus();
                    return false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean smileSelected = false;
        if (product.equals(Utils.PRODUCT_INTERNET) || product.equals(Utils.PRODUCT_AIRTIME)){
            String selectedProvider = holder.providerSpinner.getSelectedItem().toString();
            if (selectedProvider.equals("Smile Internet") || selectedProvider.equals("Smile Airtime")) {

                smileSelected = true;
            }
        }
        if ( TextUtils.isEmpty(phoneNumber) )
        {
            holder.phone.setError(getString(R.string.required_field));
            holder.phone.requestFocus();
            return false;
        }

        if ( phoneNumber.length() < 10 )
        {
            holder.phone.setError(getString(R.string.invalid_phone));
            holder.phone.requestFocus();
            return false;

        }
        if ( !(phoneNumber.startsWith("0")) && !(smileSelected))
        {
            holder.phone.setError("Number should start with 0");
            holder.phone.requestFocus();
            return false;

        }

        if (
                product.equals(Utils.PRODUCT_YAKA)||
                        product.equals(Utils.PRODUCT_TV)||
                        product.equals(Utils.PRODUCT_WATER)
                ){
            if ( TextUtils.isEmpty(accountNumber) )
            {
                holder.accountField.setError(getString(R.string.required_field));
                holder.accountField.requestFocus();
                return false;
            }
        }
        return true;
    }

    String [] getCodeAmount(String product, JSONObject[] services_objects, DialogHolder holder) {
        String code = "";
        String amt= holder.amountField.getText().toString().trim();
        if(!(product.equals(Utils.PRODUCT_AIRTIME)
                || product.equals(Utils.PRODUCT_YAKA)
                || product.equals(Utils.PRODUCT_SENDMONEY)
                || product.equals(Utils.PRODUCT_WATER)
        )){

            try {
                if(product.equals(Utils.PRODUCT_TV) && !services_objects[0].getJSONArray("results")
                        .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                        .getBoolean("has_price_list")){
                    code = services_objects[0].getJSONArray("results")
                            .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                            .getString("code");
                    amt = holder.amountField.getText().toString().trim();

                }else {
                    code = services_objects[0].getJSONArray("results")
                            .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                            .getJSONArray("details")
                            .getJSONObject(holder.packageSpinner.getSelectedItemPosition())
                            .getString("code");
                    amt = services_objects[0].getJSONArray("results")
                            .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                            .getJSONArray("details")
                            .getJSONObject(holder.packageSpinner.getSelectedItemPosition())
                            .getString("price");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else {
            switch (product){
                case Utils.PRODUCT_AIRTIME:
                    try {
                        code = services_objects[0].getJSONArray("results")
                                .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                                .getString("code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Utils.PRODUCT_YAKA:
                    code = "UMEME";
                    break;
                case Utils.PRODUCT_WATER:
                    code = "NWSC";
                    break;
                case Utils.PRODUCT_SENDMONEY:
                    if(holder.providerSpinner.getSelectedItemPosition()==0){
                        code = "TRANSFER_TO_UGWALLET";
                    }else if(holder.providerSpinner.getSelectedItemPosition()==1){
                        code = "airtel_money";
                    }
                    else {
                        code = "mtn_mobile_money";
                    }
                    break;
            }

        }

        String ar[] = new String[2];
        ar[0] = code;
        ar[1] = amt;
        return ar;
    }

    void postSendMoneyTransaction(String product, final String finalProduct_title, String transfer_type,final int amount,
                         int finalCharge, String phoneNumber,
                         JSONObject clientObject, final AlertDialog withdraw_Alert) {
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
        String message = "Processing "+finalProduct_title+". Please Wait...";
        progressDialog.setMessage(message);
        progressDialog.show();
        String url = Utils.SEND_FUNDS_URL;

        String token = "";
        JSONObject params = null;
        try {
            token = clientObject.getString("token");
            params = new JSONObject()
                    .put("amount",amount)
                    .put("payable_amount",amount+ finalCharge)
                    .put("phone",phoneNumber)
                    .put("transfer_type", transfer_type);

        } catch (JSONException ex) {
            return;
        }
        Bridge.post(url)
                .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                .header("Content-Type","application/json")
                .header("Authorization","Token "+token)
                .body(params)
                .asString(new ResponseConvertCallback<String>() {
                    @Override
                    public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                        progressDialog.dismiss();

                        handleResponse(amount, s, finalProduct_title, withdraw_Alert);
                    }
                });

    }

    void sendMoney() throws JSONException{
        String product_pref = "";
        String product_title = "Send Money";
        final String product = Utils.PRODUCT_SENDMONEY;
        final String finalProduct_title = product_title;

        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        final DialogHolder holder = new DialogHolder(AgentHomeActivity.this, product);
        holder.titleTextView.setText(product_title);
        final JSONObject[] services_objects = {PrefManager.getServices(AgentHomeActivity.this, product_pref)};

        setupOptions(holder, services_objects,  product, product_pref, holder.providerSpinner);
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setView(holder.view)
                .setCancelable(false)
                .show();

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        progressDialog.dismiss();

        holder.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = holder.amountField.getText().toString().trim();
                final String phoneNumber = holder.phone.getText().toString().trim();
                final String accountNumber = holder.accountField.getText().toString().trim();
                String acc = phoneNumber;

                String code = "";

                final String mssid = acc;

                View focusView = null;
                holder.amountField.setError(null);
                holder.phone.setError(null);


                String [] code_amt = getCodeAmount(product, services_objects, holder);
                code = code_amt[0];
                amt = code_amt[1];

                int i = 0;
                try{
                    i =Integer.parseInt(amt);
                }catch (Exception e){
                    e.printStackTrace();
                }
                final int amount = i;
                final String product_code = code;

                if (validateForm(holder,services_objects, product, amt, amount, phoneNumber, accountNumber)==false){
                    return;
                }

                /*
                final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                */
                //get charge
                // set area id if it is a water topup
                String area_id = "";
                String token = "";
                String balance = "";

                String transfer_type = "wallet_to_wallet";
                if(holder.providerSpinner.getSelectedItemPosition()> 0) {
                    transfer_type = "send_external";
                }


                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                final String finalArea_id = area_id;
                    /*
                    if(phoneNumber.matches(Utils.MTN_REGEX)) {
                        progressDialog.dismiss();
                        boolean result = showMtnConfirmationDialog(amount, finalProduct_title, phoneNumber);
                        if (result == false) {

                            return;
                        }
                    }

                    progressDialog.dismiss();
                    */
                    int finalCharge = 1000;
                    //Toast.makeText(AgentHomeActivity.this, "we waited", Toast.LENGTH_LONG).show();
                    if(holder.providerSpinner.getSelectedItemPosition() == 0){
                        finalCharge = 500;
                    }
                    /*
                    boolean result = showMtnConfirmationDialog(amount, finalCharge, finalProduct_title, phoneNumber);

                    if (result == false) {

                        return;
                    }
                      */
                    JSONObject params = null;
                    try {
                        token = clientObject.getString("token");
                        Log.w("send money product code", product_code);
                        params = new JSONObject()
                                .put("amount",amount)
                                .put("mssid",mssid)
                                .put("area_id",area_id)
                                .put("product_code", product_code)
                                .put("payable_amount", null)
                                .put("phone",phoneNumber);


                            String withdraw_type = "wallet_to_wallet";
                            if(holder.providerSpinner.getSelectedItemPosition()> 0) {
                                withdraw_type = "send_external";
                            }
                            params.put("withdraw", withdraw_type);

                } catch (JSONException ex) {
                    return;
                }
                String url = Utils.WALLET_VALIDATE_ACCOUNT_URL;
                final ProgressDialog dialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
                dialog.setCancelable(false);
                dialog.show();
                Bridge.post(url)
                    .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Token " + token)
                    .body(params)
                    .asString(new ResponseConvertCallback<String>() {
                          @Override
                          public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                              dialog.dismiss();
                              if (Utils.DEBUG) {
                                  Log.d(TAG, "onResponse: " + s);
                              }
                              if (s == null) {
                                  Utils.showToastMessage(AgentHomeActivity.this, "Please check your connection and try again");
                                  return;
                              }
                              if (hasErrors(s, amount)) {
                                  handleErrors(s, amount);
                                  return;
                              }
                              int charge = 0;
                              try {
                                  JSONObject jsonObject = new JSONObject(s);
                                  if (jsonObject.getJSONObject("data").has("tariff_charge")) {
                                      charge = jsonObject.getJSONObject("data").getInt("tariff_charge");
                                  }
                              } catch (JSONException ex) {
                                  return;
                              }

                              final int finalCharge = charge;

                              boolean result = showConfirmationDialog(s, amount, finalProduct_title, phoneNumber);
                              if (result == false) {
                                  return;
                              }
                              String xfer_type = "wallet_to_wallet";
                              if (holder.providerSpinner.getSelectedItemPosition() > 0) {
                                  xfer_type = "send_external";
                              }

                              postSendMoneyTransaction(product, finalProduct_title, xfer_type, amount,
                                      finalCharge, phoneNumber,
                                      clientObject, alert);
                          }
                      }
                );

            }
        });
    }

    void buyProduct(final String product ) throws JSONException{
        String product_title = "";

        String product_pref = PrefManager.PREF_SERVICES_AIRTIME;
        switch (product) {
            case Utils.PRODUCT_AIRTIME:
                product_pref = PrefManager.PREF_SERVICES_AIRTIME;
                product_title = "Airtime Topup";
                break;
            case Utils.PRODUCT_INTERNET:
                product_pref = PrefManager.PREF_SERVICES_INTERNET;
                product_title = "Internet Data Topup";
                break;
            case Utils.PRODUCT_TV:
                product_pref = PrefManager.PREF_SERVICES_TV;
                product_title = "PayTV Topup";
                break;
            case Utils.PRODUCT_YAKA:
                product_pref = PrefManager.PREF_SERVICES_YAKA;
                product_title = "Yaka Topup";
                break;
            case Utils.PRODUCT_WATER:
                product_pref = PrefManager.PREF_SERVICES_WATER;
                product_title = "NWSC Water Topup";
                break;
        }
        final String finalProduct_title = product_title;

        //product_title = "Airtime Topup";
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        final DialogHolder holder = new DialogHolder(AgentHomeActivity.this, product);
        holder.titleTextView.setText(product_title);
        final JSONObject[] services_objects = {PrefManager.getServices(AgentHomeActivity.this, product_pref)};

        setupOptions(holder, services_objects,  product, product_pref, holder.providerSpinner);
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setView(holder.view)
                .setCancelable(false)
                .show();

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        progressDialog.dismiss();

        holder.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = holder.amountField.getText().toString().trim();
                final String phoneNumber = holder.phone.getText().toString().trim();
                final String accountNumber = holder.accountField.getText().toString().trim();
                String acc = accountNumber;

                if(product.equals(Utils.PRODUCT_AIRTIME) ||
                        product.equals(Utils.PRODUCT_INTERNET))
                {
                    acc = phoneNumber;
                }
                String code = "";

                final String mssid = acc;

                View focusView = null;
                holder.amountField.setError(null);
                holder.phone.setError(null);

                /*
                try {
                    code = services_objects[0].getJSONArray("results")
                            .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                            .getString("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                */

                String [] code_amt = getCodeAmount(product, services_objects, holder);
                code = code_amt[0];
                amt = code_amt[1];

                int i = 0;
                try{
                    i =Integer.parseInt(amt);
                }catch (Exception e){
                    e.printStackTrace();
                }
                final int amount = i;
                final String product_code = code;

                if (validateForm(holder,services_objects, product, amt, amount, phoneNumber, accountNumber)==false){
                    return;
                }

                final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                //get charge
                // set area id if it is a water topup
                String area_id = "";
                String token = "";
                String balance = "";
                JSONObject params = null;
                if(product.equals(Utils.PRODUCT_WATER)){
                    try {
                        area_id = services_objects[0].getJSONArray("data")
                                .getJSONObject(holder.providerSpinner.getSelectedItemPosition())
                                .getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    token = clientObject.getString("token");
                    Log.w("send money product code", product_code);
                    params = new JSONObject()
                            .put("amount",amount)
                            .put("mssid",mssid)
                            .put("area_id",area_id)
                            .put("product_code", product_code)
                            .put("payable_amount", null);
                    if (product.equals(Utils.PRODUCT_INTERNET)||product.equals(Utils.PRODUCT_AIRTIME)){
                        String selectedProvider = holder.providerSpinner.getSelectedItem().toString();
                        if (selectedProvider.equals("Smile Internet") || selectedProvider.equals("Smile Airtime")){
                            params.put("phone", clientObject.getString("phone"));
                        }
                    } else {
                        params.put("phone",phoneNumber);
                    }

                    if (product.equals(Utils.PRODUCT_SENDMONEY)) {
                        String withdraw_type = "";
                        if(holder.providerSpinner.getSelectedItemPosition()> 0) {
                            withdraw_type = "send_external";
                        }
                        params.put("withdraw", withdraw_type);
                    }
                } catch (JSONException e) {

                }
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    final String finalArea_id = area_id;


                    String url = Utils.WALLET_VALIDATE_ACCOUNT_URL;

                    Bridge.post(url)
                                .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                                .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Token " + token)
                                .body(params)
                                .asString(new ResponseConvertCallback<String>() {
                                    @Override
                                    public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                        progressDialog.dismiss();
                                        if (Utils.DEBUG) {
                                            Log.d(TAG, "onResponse: " + s);
                                        }
                                        if (s == null) {
                                            Utils.showToastMessage(AgentHomeActivity.this, "Please check your connection and try again");
                                            return;
                                        }
                                        if (hasErrors(s, amount)) {
                                            handleErrors(s, amount);
                                            return;
                                        }
                                        int charge = 0;
                                        try {
                                            JSONObject jsonObject = new JSONObject(s);
                                            if (jsonObject.getJSONObject("data").has("tariff_charge")) {
                                                charge = jsonObject.getJSONObject("data").getInt("tariff_charge");
                                            }
                                        } catch (JSONException ex) {
                                            return;
                                        }

                                        final int finalCharge = charge;

                                        boolean result = showConfirmationDialog(s, amount, finalProduct_title, phoneNumber);
                                        if (result == false) {
                                            return;
                                        }

                                        String transactionPhone = phoneNumber;
                                        if (product.equals(Utils.PRODUCT_INTERNET)||product.equals(Utils.PRODUCT_AIRTIME)){
                                            String selectedProvider = holder.providerSpinner.getSelectedItem().toString();
                                            if (selectedProvider.equals("Smile Internet") || selectedProvider.equals("Smile Airtime")){
                                                try {
                                                    transactionPhone = clientObject.getString("phone");
                                                } catch (JSONException ex){

                                                }
                                            }
                                        }

                                        //Toast.makeText(AgentHomeActivity.this, "we waited", Toast.LENGTH_LONG).show();
                                        postTransaction(product, finalProduct_title, mssid, amount,
                                                finalCharge, finalArea_id, transactionPhone,
                                                product_code, clientObject, alert);
                                    }
                                });

                } catch (Exception e) {

                }
            }
        });
    }

    private void initiateAirtimePurchase(final String product) throws JSONException {
        View view = getLayoutInflater().inflate(R.layout.airtime_sale_layout,null,false);
        final EditText amountField = (EditText) view.findViewById(R.id.amount_field);
        final EditText accountField = (EditText) view.findViewById(R.id.account_number_field);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        TextView titleTextView = (TextView) view.findViewById(R.id.title);
        TextView providerTextView = (TextView) view.findViewById(R.id.provider_text);
        final TextView phone_no_TextView = (TextView) view.findViewById(R.id.phone_no_text);
        TextView account_no_TextView = (TextView) view.findViewById(R.id.txt_account_number);
        final AutoCompleteTextView phone = (AutoCompleteTextView) view.findViewById(R.id.phone_number_field);
        Button submit = (Button) view.findViewById(R.id.submit);
        Button pickSavedNo = (Button) view.findViewById(R.id.pick_saved_no_btn);
        Button saveNo = (Button) view.findViewById(R.id.save_no_btn);
        pickSavedNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.populateContactList(AgentHomeActivity.this, accountField);
            }
        });
        saveNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountField.setError(null);
                String meter_number = accountField.getText().toString().trim();
                if( TextUtils.isEmpty(meter_number) )
                {
                    accountField.setError(getString(R.string.missing_number));
                    accountField.requestFocus();
                }
                else
                {
                    Utils.savePhoneNumber(AgentHomeActivity.this, meter_number.replaceAll("\\D+",""));
                }
            }
        });

        final View amount_layout = view.findViewById(R.id.amount_layout);
        final View details_spinner_layout = view.findViewById(R.id.details_spinner_layout);
        View provider_spinner_layout = view.findViewById(R.id.provider_spinner_layout);
        View account_number_layout = view.findViewById(R.id.account_layout);

        final Spinner providerSpinner = (Spinner) view.findViewById(R.id.provider_spinner);
        final Spinner packageSpinner = (Spinner) view.findViewById(R.id.package_spinner);
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        String product_pref = "";
        String product_title = "";

        switch (product){
            case Utils.PRODUCT_AIRTIME:
                product_pref = PrefManager.PREF_SERVICES_AIRTIME;
                product_title = "Airtime Topup";
                details_spinner_layout.setVisibility(View.GONE);
                amount_layout.setVisibility(View.VISIBLE);
                break;
            case Utils.PRODUCT_INTERNET:
                product_pref = PrefManager.PREF_SERVICES_INTERNET;
                product_title = "Internet Data Topup";
                details_spinner_layout.setVisibility(View.VISIBLE);
                amount_layout.setVisibility(View.GONE);
                break;
            case Utils.PRODUCT_SENDMONEY:
                product_pref = "";
                product_title = "Send Money";
                details_spinner_layout.setVisibility(View.GONE);
                amount_layout.setVisibility(View.VISIBLE);
                providerTextView.setText("Select Transfer Option");
                break;
            case Utils.PRODUCT_TV:
                product_pref = PrefManager.PREF_SERVICES_TV;
                product_title = "PayTV Topup";
                details_spinner_layout.setVisibility(View.VISIBLE);
                amount_layout.setVisibility(View.GONE);
                account_no_TextView.setText("Enter Account No.");
                account_number_layout.setVisibility(View.VISIBLE);
                break;
            case Utils.PRODUCT_YAKA:
                product_pref = PrefManager.PREF_SERVICES_YAKA;
                product_title = "Yaka Topup";
                account_number_layout.setVisibility(View.VISIBLE);
                details_spinner_layout.setVisibility(View.GONE);
                provider_spinner_layout.setVisibility(View.GONE);
                account_no_TextView.setText("Enter Yaka Meter Number");
                phone_no_TextView.setText("Enter Phone Eg. 0752200600 (For Token)");
                amount_layout.setVisibility(View.VISIBLE);
                break;
            case Utils.PRODUCT_WATER:
                product_pref = PrefManager.PREF_SERVICES_WATER;
                product_title = "NWSC Water Topup";
                providerTextView.setText("Select Area");
                account_number_layout.setVisibility(View.VISIBLE);
                details_spinner_layout.setVisibility(View.GONE);
                provider_spinner_layout.setVisibility(View.VISIBLE);
                account_no_TextView.setText("Enter NWSC Meter Number");
                phone_no_TextView.setText("Enter Phone Eg. 0752200600 (For NWSC Receipt)");
                amount_layout.setVisibility(View.VISIBLE);
                break;
        }
        titleTextView.setText(product_title);
        final JSONObject[] services_objects = {PrefManager.getServices(AgentHomeActivity.this, product_pref)};
        if (product.equals(Utils.PRODUCT_YAKA)){
            progressDialog.dismiss();
            //do nothing

        }else if (product.equals(Utils.PRODUCT_SENDMONEY)){
            progressDialog.dismiss();
            String[] providerNames =new String[]{"Ug Wallet Account", "Airtel Money", "MTN Mobile Money"};

            ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, providerNames);
            providerSpinner.setAdapter(providerAdapter);
        }
        else if(services_objects[0] ==null){
            final String finalProduct_pref = product_pref;
            String url = Utils.WALLET_GET_SERVICE_LIST+"?type="+product;
            if(product.equals(Utils.PRODUCT_WATER)){
                url =Utils.WALLET_NWSC_AREAS_LIST;
            }
            Bridge.get(url).
                    header("Authorization","Token "+clientObject.getString("token"))
                    .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                            progressDialog.dismiss();
                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if(s==null){
                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                if(product.equals(Utils.PRODUCT_WATER)){
                                    setUpProviderWaterSpinner(new JSONObject(s),providerSpinner);
                                }else{
                                    setUpProviderSpinner(new JSONObject(s),providerSpinner);
                                }
                                services_objects[0] = new JSONObject(s);
                                PrefManager.saveServices(AgentHomeActivity.this, finalProduct_pref,new JSONObject(s).toString());

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }
                    });
        }else {
            progressDialog.dismiss();
            if(product.equals(Utils.PRODUCT_WATER)){
                setUpProviderWaterSpinner(services_objects[0],providerSpinner);
            }else {
                setUpProviderSpinner(services_objects[0],providerSpinner);
            }

        }

        providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //get selected item
                if(product.equals(Utils.PRODUCT_SENDMONEY)){
                    if (i==0){
                        phone_no_TextView.setText("Ug Wallet A/C Phone Number Eg. 0752200600");
                    }else if(i==1){
                        phone_no_TextView.setText("Airtel Money Number Eg. 0752200600");
                    } else{
                        phone_no_TextView.setText("MTN Mobile Money Number Eg. 0776220110");
                    }
                }
                try {
                    JSONObject object = services_objects[0].getJSONArray("results")
                            .getJSONObject(i);
                    if (object.getBoolean("has_price_list")){
                        setUpPackageSpinner(object,packageSpinner);
                        if(product.equals(Utils.PRODUCT_TV)){
                            amount_layout.setVisibility(View.GONE);
                            details_spinner_layout.setVisibility(View.VISIBLE);
                        }
                    }else{
                        if(product.equals(Utils.PRODUCT_TV)){
                            amount_layout.setVisibility(View.VISIBLE);
                            details_spinner_layout.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Log.d(TAG, "onItemSelected: "+e.toString());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final AlertDialog withdraw_Alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                withdraw_Alert.dismiss();
            }
        });
        final String finalProduct_title = product_title;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = amountField.getText().toString().trim();
                final String phoneNumber = phone.getText().toString().trim();
                final String accountNumber = accountField.getText().toString().trim();
                String acc = accountNumber;


                if(
                        product.equals(Utils.PRODUCT_AIRTIME) ||
                                product.equals(Utils.PRODUCT_INTERNET)
                                || product.equals(Utils.PRODUCT_SENDMONEY))
                {
                    acc = phoneNumber;
                }
                final String mssid = acc;
                boolean cancelSend = false;
                View focusView = null;
                amountField.setError(null);
                phone.setError(null);

//getting the product code
                String code= "";

                if(!(product.equals(Utils.PRODUCT_AIRTIME)
                        || product.equals(Utils.PRODUCT_YAKA)
                        || product.equals(Utils.PRODUCT_SENDMONEY)
                        || product.equals(Utils.PRODUCT_WATER)
                )){

                    try {
                        if(product.equals(Utils.PRODUCT_TV) && !services_objects[0].getJSONArray("results")
                                .getJSONObject(providerSpinner.getSelectedItemPosition())
                                .getBoolean("has_price_list")){
                            code = services_objects[0].getJSONArray("results")
                                    .getJSONObject(providerSpinner.getSelectedItemPosition())
                                    .getString("code");
                            amt = amountField.getText().toString().trim();

                        }else {
                            code = services_objects[0].getJSONArray("results")
                                    .getJSONObject(providerSpinner.getSelectedItemPosition())
                                    .getJSONArray("details")
                                    .getJSONObject(packageSpinner.getSelectedItemPosition())
                                    .getString("code");
                            amt = services_objects[0].getJSONArray("results")
                                    .getJSONObject(providerSpinner.getSelectedItemPosition())
                                    .getJSONArray("details")
                                    .getJSONObject(packageSpinner.getSelectedItemPosition())
                                    .getString("price");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else {
                    switch (product){
                        case Utils.PRODUCT_AIRTIME:
                            try {
                                code = services_objects[0].getJSONArray("results")
                                        .getJSONObject(providerSpinner.getSelectedItemPosition())
                                        .getString("code");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case Utils.PRODUCT_YAKA:
                            code = "UMEME";
                            break;
                        case Utils.PRODUCT_WATER:
                            code = "NWSC";
                            break;
                        case Utils.PRODUCT_SENDMONEY:
                            if(providerSpinner.getSelectedItemPosition()==0){
                                code = "TRANSFER_TO_UGWALLET";
                            }else if(providerSpinner.getSelectedItemPosition()==1){
                                code = "airtel_money";
                            }
                                else {
                                code = "mtn_mobile_money";
                            }
                            break;
                    }

                }
                int i = 0;
                try{
                    i =Integer.parseInt(amt);
                }catch (Exception e){
                    e.printStackTrace();
                }
                final int amount = i;
                final String product_code = code;
                if(product.equals(Utils.PRODUCT_AIRTIME)
                        ||product.equals(Utils.PRODUCT_YAKA)
                        ||product.equals(Utils.PRODUCT_SENDMONEY)
                        ||product.equals(Utils.PRODUCT_WATER))
                        {
                    if(amt.length()==0){
                        amountField.setError(getString(R.string.required_field));
                        focusView = amountField;
                        cancelSend = true;

                    }
                    if(amount<500){
                        amountField.setError("Amount less than minimum");
                        focusView = amountField;
                        cancelSend = true;
                    }
                    if(amount>5000000){
                        amountField.setError("Amount greater than maximum (5000000)");
                        focusView = amountField;
                        cancelSend = true;
                    }
                }
                try {
                    if(product.equals(Utils.PRODUCT_TV) && !services_objects[0].getJSONArray("results")
                            .getJSONObject(providerSpinner.getSelectedItemPosition())
                            .getBoolean("has_price_list")){
                        if(amt.length()==0){
                            amountField.setError(getString(R.string.required_field));
                            focusView = amountField;
                            cancelSend = true;

                        }
                        if(amount<500){
                            amountField.setError("Amount less than minimum");
                            focusView = amountField;
                            cancelSend = true;
                        }
                        if(amount>5000000){
                            amountField.setError("Amount greater than maximum (5000000)");
                            focusView = amountField;
                            cancelSend = true;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if ( TextUtils.isEmpty(phoneNumber) )
                {
                    phone.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = phone;
                }


                if ( phoneNumber.length() < 10 )
                {
                    phone.setError(getString(R.string.invalid_phone));
                    cancelSend = true;
                    focusView = phone;

                }
                if ( !(phoneNumber.startsWith("0")) )
                {
                    phone.setError("Number should start with 0");
                    cancelSend = true;
                    focusView = phone;

                }
                if (
                        product.equals(Utils.PRODUCT_YAKA)||
                                product.equals(Utils.PRODUCT_TV)||
                                product.equals(Utils.PRODUCT_WATER)
                        ){
                    if ( TextUtils.isEmpty(accountNumber) )
                    {
                        accountField.setError(getString(R.string.required_field));
                        cancelSend = true;
                        focusView = accountField;
                    }
                }
                if(cancelSend){
                    focusView.requestFocus();
                    return;

                }
                final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
                progressDialog.setCancelable(false);
                progressDialog.show();
                //get charge
                // set area id if it is a water topup
                String area_id = "";
                if(product.equals(Utils.PRODUCT_WATER)){
                    try {
                        area_id = services_objects[0].getJSONArray("data")
                                .getJSONObject(providerSpinner.getSelectedItemPosition())
                                .getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    final String finalArea_id = area_id;
                    String url = Utils.WALLET_VALIDATE_ACCOUNT_URL;
                    if(product.equals(Utils.PRODUCT_SENDMONEY)){
                        url = Utils.WALLET_VALIDATE_MOBILE_MONEY;
                    }
                    Bridge.post(url)
                            .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                            .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                            .header("Content-Type","application/json")
                            .header("Authorization","Token "+clientObject.getString("token"))
                            .body(
                                    new JSONObject()
                                            .put("amount",amount)
                                            .put("mssid",mssid)
                                            .put("phone",phoneNumber)
                                            .put("area_id",area_id)
                                            .put("product_code",
                                                    product_code
                                            )
                            )
                            .asString(new ResponseConvertCallback<String>() {
                                @Override
                                public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                    progressDialog.dismiss();
                                    if(Utils.DEBUG){
                                        Log.d(TAG, "onResponse: "+s);
                                    }
                                    if(s==null){
                                        Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                        return;
                                    }

                                    try {
                                        JSONObject jsonObject = new JSONObject(s);
                                        if (jsonObject.getString("status").equals("FAILED")){
                                            new AlertDialog.Builder(AgentHomeActivity.this)
                                                    .setTitle("Error")
                                                    .setMessage(jsonObject.getString("message"))
                                                    .setCancelable(false)
                                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    }).show();
                                            return;
                                        }

                                        String names = "";
                                        String balance = "";
                                        if (jsonObject.getJSONObject("data").has("balance")){
                                            names = "\nOver Due Amount: "+jsonObject.getJSONObject("data").getString("balance");
                                            if(jsonObject.getJSONObject("data").getInt("balance")>amount){
                                                new AlertDialog.Builder(AgentHomeActivity.this)
                                                        .setTitle("Insufficient Amount")
                                                        .setMessage("Amount "+amount+" is not enough to clear the outstanding balance of "+jsonObject.getJSONObject("data").getInt("balance"))
                                                        .setCancelable(false)
                                                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        }).show();
                                                return;
                                            }
                                        }
                                        int charge = 0;
                                        if (jsonObject.getJSONObject("data").has("customer_name")){
                                            names = "\nCustomer Names: "+jsonObject.getJSONObject("data").getString("customer_name");
                                        }
                                        if (jsonObject.getJSONObject("data").has("tariff_charge")){
                                            charge = jsonObject.getJSONObject("data").getInt("tariff_charge");
                                        }

                                        final int finalCharge = charge;
                                        new AlertDialog.Builder(AgentHomeActivity.this)
                                                .setTitle("Transaction Confirmation")
                                                .setMessage(
                                                        "Product Name: "+ finalProduct_title +
                                                                "\nAmount: "+amount+
                                                                "\nAccount No: "+jsonObject.getJSONObject("data").getString("msisdn")+
                                                                names+
                                                                "\nTransaction Number: "+ phoneNumber+
                                                                "\nCharge: "+charge+"" +
                                                                balance+
                                                                "\nTotal Amount Payable: "+(amount+charge)
                                                )
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                        try {
                                                            String message = "Processing "+finalProduct_title+". Please Wait...";
                                                            progressDialog.setMessage(message);
                                                            progressDialog.show();
                                                            String url = Utils.WALLET_AIRTIME_PURCHASE;
                                                            /*if(product.equals(Utils.PRODUCT_SENDMONEY)){
                                                                url = Utils.WALLET_SEND_MOBILE_MONEY;
                                                            }*/
                                                            Bridge.post(url)
                                                                    .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                                                                    .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                                                                    .header("Content-Type","application/json")
                                                                    .header("Authorization","Token "+clientObject.getString("token"))
                                                                    .body(
                                                                            new JSONObject()
                                                                                    .put("mssid",mssid)
                                                                                    .put("amount",amount)
                                                                                    .put("payable_amount",amount+ finalCharge)
                                                                                    .put("area_id", finalArea_id)
                                                                                    .put("phone",phoneNumber)
                                                                                    .put("product_code",
                                                                                            product_code
                                                                                    )
                                                                                    .put("description",
                                                                                            product_code
                                                                                    )
                                                                    )
                                                                    .asString(new ResponseConvertCallback<String>() {
                                                                        @Override
                                                                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                                                            progressDialog.dismiss();
                                                                            progressDialog.setMessage("Loading data... Please Wait");
                                                                            if(Utils.DEBUG){
                                                                                Log.d(TAG, "onResponse: "+s);
                                                                            }
                                                                            if(s==null){
                                                                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                                                                return;
                                                                            }
                                                                            try {
                                                                                JSONObject jsonObject = new JSONObject(s);
                                                                                if(jsonObject.has("mm_status")) {
                                                                                    if (jsonObject.getString("mm_status").equals("SUCCESS")) {
                                                                                        String token = "";
                                                                                        if (jsonObject.has("mm_token")) {
                                                                                            if (jsonObject.getString("mm_token").length() > 6) {
                                                                                                token = "\nThe Yaka token is <strong>" + jsonObject.getString("mm_token") +
                                                                                                        "</strong>.";
                                                                                            }

                                                                                        }

                                                                                        String message = "Your " + finalProduct_title + " transaction was <strong>SUCCESSFUL</strong>." +
                                                                                                token +
                                                                                                "\nYour new Wallet Balance is Ugx. " + jsonObject.getString("client_balance_after_wallet_sell") +
                                                                                                "\nTransaction ID: " + jsonObject.getString("mm_transaction_id")

                                                                                        ;

                                                                                        final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                                                                        ad.setCancelable(false);

                                                                                        ad.setTitle("Transaction Successful");
                                                                                        ad.setMessage(Utils.formatHtml(message));
                                                                                        ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                                                                new DialogInterface.OnClickListener() {

                                                                                                    @Override
                                                                                                    public void onClick(DialogInterface dialog,
                                                                                                                        int which) {
                                                                                                        dialog.dismiss();
                                                                                                        getWalletBalance();
                                                                                                        withdraw_Alert.dismiss();
                                                                                                    }
                                                                                                });

                                                                                        ad.show();
                                                                                        return;
                                                                                    }
                                                                                }

                                                                                if (jsonObject.has("detail")){
                                                                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                                                                            .setTitle("Error")
                                                                                            .setMessage(jsonObject.getString("detail"))
                                                                                            .setCancelable(false)
                                                                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                                    dialogInterface.dismiss();
                                                                                                }
                                                                                            }).show();
                                                                                    return;
                                                                                }
                                                                                if (jsonObject.has("error")){
                                                                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                                                                            .setTitle("Error")
                                                                                            .setMessage(jsonObject.getString("error"))
                                                                                            .setCancelable(false)
                                                                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                                    dialogInterface.dismiss();
                                                                                                }
                                                                                            }).show();
                                                                                    return;
                                                                                }

                                                                                else if (jsonObject.getString("mm_status").equals( "PENDING")) {
                            /*purchase is still pending*/
                                                                                    String message = "Your "+finalProduct_title+" transaction is still <strong>IN PROGRESS</strong>." +
                                                                                            " Please check the status after a few minutes. Transaction ID:"+
                                                                                            jsonObject.getString("mm_transaction_id");
                            /*show the status message in a dialog*/
                                                                                    final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                                                                    ad.setCancelable(false);

                                                                                    ad.setTitle("Transaction in Progress");
                                                                                    ad.setMessage(Utils.formatHtml(message));
                                                                                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                                                            new DialogInterface.OnClickListener() {

                                                                                                @Override
                                                                                                public void onClick(DialogInterface dialog,
                                                                                                                    int which) {
                                                                                                    dialog.dismiss();
                                                                                                    withdraw_Alert.dismiss();
                                                                                                }
                                                                                            });

                                                                                    ad.show();
                                                                                }
                                                                                else {
                            /*update the transaction status*/

                                                                                    AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                                                                    ad.setCancelable(false);

                                                                                    ad.setTitle("Transaction Failed");

                                                                                    ad.setMessage(finalProduct_title+ " Transaction Failed, Please try again");
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
                                                                            } catch (JSONException e1) {
                                                                                e1.printStackTrace();
                                                                                //Log.d(TAG, "onResponse: "+e1.toString());
                                                                            }
                                                                        }
                                                                    });
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).show();
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //request topup here
                if(true) return;




                try {
                    Bridge.post(Utils.CLIENT_TRANSACTION_URL)
                            .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                            .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                            .body(
                                    new JSONObject()
                                            .put("platform",Utils.PLATFORM_CODE)
                                            .put("phone",clientObject.getString("phone"))
                                            .put("amount",amount)
                                            .put("request_type","withdraw")
                            ).asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                            progressDialog.dismiss();
                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if(s==null){
                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if(jsonObject.has("pk")){
                                    String message ="";
                                    if (jsonObject.getString("mm_status").equals("SUCCESS")){
                                        message = "Your withdraw request of UGX. "+amount+ " was processed successfully."
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }else  if(jsonObject.getString("mm_status").equals("PENDING")){
                                        message = "Your withdraw request of UGX. "+amount+ " is still being processed."
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }else {
                                        message = "Your withdraw request of UGX. "+amount+ " Faild. Please try again or contact support"
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }

                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                            .setTitle("Notification")
                                            .setMessage(message)
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    withdraw_Alert.dismiss();
                                                }
                                            })
                                            .show();

                                }else
                                if(jsonObject.has("error")){
                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                            .setTitle("Withdraw Error")
                                            .setMessage(getErrors(jsonObject.getJSONArray("error")))
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                    return;
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }

                        private String getErrors(JSONArray array) throws JSONException {
                            String str="";
                            for (int i = 0; i < array.length(); i++) {
                                str+=array.getString(i)+"\n";
                            }
                            return str;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private void setUpProviderWaterSpinner(JSONObject services_objects, Spinner providerSpinner) throws JSONException {
        JSONArray jsonArray = services_objects.getJSONArray("data");
        String[] providerNames = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            providerNames[i] = jsonArray.getJSONObject(i).getString("id");
        }
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, providerNames);
        providerSpinner.setAdapter(providerAdapter);
    }

    private void setUpPackageSpinner(JSONObject object, Spinner providerSpinner) throws JSONException {
        JSONArray jsonArray = object.getJSONArray("details");
        //Log.d(TAG, "setUpPackageSpinner: "+jsonArray.toString());
        String[] providerNames = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            providerNames[i] = jsonArray.getJSONObject(i).getString("name");
        }
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, providerNames);
        providerSpinner.setAdapter(providerAdapter);
    }

    private void setUpProviderSpinner(JSONObject services_objects, Spinner providerSpinner) throws JSONException {
        JSONArray jsonArray = services_objects.getJSONArray("results");
        String[] providerNames = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            providerNames[i] = jsonArray.getJSONObject(i).getString("name");
        }
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, providerNames);
        providerSpinner.setAdapter(providerAdapter);
    }


    private void initiateWithdraw() throws JSONException {
        View view = getLayoutInflater().inflate(R.layout.withdraw_layout,null,false);
        final EditText amountField = (EditText) view.findViewById(R.id.amount_field);
        final Spinner withdrawSpinner = (Spinner)view.findViewById(R.id.withdraw_type_spinner);
        String[] withdrawTypes = new String[]{"Wallet","Commision"};
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, withdrawTypes);
        withdrawSpinner.setAdapter(providerAdapter);
        //withdrawSpinner.setVisibility(View.GONE);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        Button submit = (Button) view.findViewById(R.id.submit);
        final AlertDialog withdraw_Alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withdraw_Alert.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = amountField.getText().toString().trim();
                amountField.setError(null);
                if(amt.length()==0){
                    amountField.setError(getString(R.string.required_field));
                    amountField.requestFocus();
                    return;
                }
                final int amount = Integer.parseInt(amt);
                if(amount<1500){
                    amountField.setError("Amount less than minimum");
                    amountField.requestFocus();
                    return;
                }
                if(amount>5000000){
                    amountField.setError("Amount greater than maximum (5000000)");
                    amountField.requestFocus();
                    return;
                }
                //request withdraw here
                String withdraw_type = "wallet";
                int selectedItemPosition =withdrawSpinner.getSelectedItemPosition();
                //Log.d(TAG, "selectedItemPosition " + selectedItemPosition);
                if (selectedItemPosition==1){
                    withdraw_type = "commission";
                }

                final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
                progressDialog.setCancelable(false);
                progressDialog.show();


                String token = "";
                String product_code = "WITHDRAW";
                JSONObject params = null;

                String url = Utils.WITHDRAW_FUNDS_URL;
                /*
                String url = Utils.CLIENT_TRANSACTION_URL;
                if (withdraw_type.equals("wallet")){
                    url = Utils.WALLET_AIRTIME_PURCHASE;
                }
                */
                //Log.d(TAG, "withdraw_type: " + withdraw_type + " url: "+url);
                try {
                    params = new JSONObject()
                            .put("platform", Utils.PLATFORM_CODE)
                            .put("phone", clientObject.getString("phone"))
                            .put("amount", amount)
                            .put("withdraw_type", withdraw_type);
                    /*
                    token = clientObject.getString("token");
                    if (withdraw_type.equals("commission")) {

                        params = new JSONObject()
                                .put("platform", Utils.PLATFORM_CODE)
                                .put("phone", clientObject.getString("phone"))
                                .put("amount", amount)
                                .put("request_type", "withdraw");
                    } else {
                        String phone_number = clientObject.getString("phone");
                        if (phone_number.matches(Utils.MTN_REGEX)) {
                            product_code = Utils.PRODUCT_MTN_MONEY;
                        } else {
                            product_code = Utils.PRODUCT_AIRTEL_MONEY;
                        }
                        Log.d(TAG, "product code: " + product_code);

                        params = new JSONObject()
                                .put("mssid", phone_number)
                                .put("amount",amount)
                                .put("payable_amount",amount)
                                .put("area_id", null)
                                .put("phone", clientObject.getString("phone"))
                                .put("product_code",
                                        product_code
                                ).put("withdraw", withdraw_type)
                                .put("description",
                                        product_code
                                );

                    }
                    */
                } catch (JSONException ex) {
                    return;
                }

                try {
                    Bridge.post(url)
                            .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                            .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                            .header("Content-Type","application/json")
                            .header("Authorization","Token "+clientObject.getString("token"))
                            .body(params).asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                            progressDialog.dismiss();
                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if (response.isSuccess() == false) {
                                Utils.showToastMessage(AgentHomeActivity.this,"There was an error while processing your request. Please try again later");
                                return;
                            }
                            if(s==null){
                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if(jsonObject.has("pk")){
                                    String message ="";
                                    if (jsonObject.getString("mm_status").equals("SUCCESS")){
                                        message = "Your withdraw request of UGX. "+amount+ " was processed successfully."
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }else  if(jsonObject.getString("mm_status").equals("PENDING")){
                                        message = "Your withdraw request of UGX. "+amount+ " is still being processed."
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }else {
                                        message = "Your withdraw request of UGX. "+amount+ " Faild. Please try again or contact support"
                                                +"\nTransaction ID: "+jsonObject.getString("mm_transaction_id");
                                    }

                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                            .setTitle("Notification")
                                            .setMessage(message)
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    withdraw_Alert.dismiss();
                                                }
                                            })
                                            .show();

                                }else if(jsonObject.has("message")) {
                                    String message = jsonObject.getString("message");

                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                            .setTitle("Notification")
                                            .setMessage(message)
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    withdraw_Alert.dismiss();
                                                    getWalletBalance();
                                                }
                                            })
                                            .show();
                                } else if(jsonObject.has("error")){
                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                            .setTitle("Withdraw Error")
                                            .setMessage(jsonObject.getString("error"))
                                            //.setMessage(getErrors(jsonObject.getJSONArray("error")))
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                    return;
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }

                        private String getErrors(JSONArray array) throws JSONException {
                            String str="";
                            for (int i = 0; i < array.length(); i++) {
                                str+=array.getString(i)+"\n";
                            }
                            return str;
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private void sendTopupRequest(final String provider, final int amount, final String phoneNumber) {
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Loading data... Please Wait", false);
        progressDialog.setCancelable(false);
        progressDialog.show();


        //get charge
        try {

            Bridge.post(Utils.CLIENT_GET_CHARGE)
                    .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                    .header("Content-Type","application/json")
                    .header("Authorization","Token "+clientObject.getString("token"))
                    .body(
                            new JSONObject()
                                    .put("amount",amount)
                                    .put("mssid",phoneNumber)
                    )
                    .asString(new ResponseConvertCallback<String>() {
                        @Override
                        public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                            progressDialog.dismiss();
                            if(Utils.DEBUG){
                                Log.d(TAG, "onResponse: "+s);
                            }
                            if(s==null){
                                Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                return;
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.has("detail")){
                                    new AlertDialog.Builder(AgentHomeActivity.this)
                                            .setTitle("Error")
                                            .setMessage(jsonObject.getString("detail"))
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                    return;
                                }
                                final int charge = jsonObject.getInt("charge");
                                new AlertDialog.Builder(AgentHomeActivity.this)
                                        .setTitle("Transaction Confirmation")
                                        .setMessage(
                                                "Product Name: Wallet TopUp\n" +
                                                        "Amount: "+amount+"\n" +
                                                        "Charge: "+charge+"\n" +
                                                        "Total Amount Payable: "+(amount+charge)+"\n" +
                                                        "Transaction Number: "+ phoneNumber
                                        )
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                try {
                                                    String message = getResources().getString(R.string.wait_message, ""+(amount+charge));
                                                    progressDialog.setMessage(message);
                                                    progressDialog.show();
                                                    Bridge.post(Utils.WALLET_TOPUP_URL)
                                                            .readTimeout(Utils.VOLLEY_TIMEOUT_MS)
                                                            .connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
                                                            .header("Content-Type","application/json")
                                                            .header("Authorization","Token "+clientObject.getString("token"))
                                                            .body(
                                                                    new JSONObject()
                                                                            .put("mssid",phoneNumber)
                                                                            .put("amount",amount)
                                                                            .put("topup_amount",(amount+charge))
                                                            )
                                                            .asString(new ResponseConvertCallback<String>() {
                                                                @Override
                                                                public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                                                                    progressDialog.dismiss();
                                                                    progressDialog.setMessage("Loading data... Please Wait");
                                                                    if(Utils.DEBUG){
                                                                        Log.d(TAG, "onResponse: "+s);
                                                                    }
                                                                    if(s==null){
                                                                        Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                                                                        return;
                                                                    }
                                                                    try {
                                                                        JSONObject jsonObject = new JSONObject(s);
                                                                        if (jsonObject.has("detail")){
                                                                            new AlertDialog.Builder(AgentHomeActivity.this)
                                                                                    .setTitle("Error")
                                                                                    .setMessage(jsonObject.getString("detail"))
                                                                                    .setCancelable(false)
                                                                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                                            dialogInterface.dismiss();
                                                                                        }
                                                                                    }).show();
                                                                            return;
                                                                        }
                                                                        else if (jsonObject.has("momo_status") && jsonObject.getString("momo_status").equals( "SUCCESS")) {
                                                                            /*purchase is still pending*/
                                                                            String message = "Your Mobile Money transaction was <strong>SUCCESSFUL</strong>. Your new Wallet Balance is "+jsonObject.getString("client_balance");
                                                                            /*show the status message in a dialog*/
                                                                            final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                                                            ad.setCancelable(false);

                                                                            ad.setTitle("Transaction Successful");
                                                                            ad.setMessage(Utils.formatHtml(message));
                                                                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                                                    new DialogInterface.OnClickListener() {

                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog,
                                                                                                            int which) {
                                                                                            dialog.dismiss();
                                                                                            //withdraw_Alert.dismiss();
                                                                                            getWalletBalance();
                                                                                        }
                                                                                    });

                                                                            ad.show();
                                                                        }


                                                                        //else if (jsonObject.getString("momo_status").equals( "PENDING")) {
                                                                        /*purchase is still pending*/
                                                                        else if (jsonObject.has("message") && jsonObject.getString("message").equals("Mobile money collection has been initiated")) {
                                                                            String message = "Your Mobile Money transaction is still <strong>IN PROGRESS</strong>. " +
                                                                                    "Please allow the process to complete by responding to all the prompts." +
                                                                                    " Remember to use the refresh button at the top right hand corner for your new " +
                                                                                    "balance to reflect in the wallet balance.";
                                                                            /*show the status message in a dialog*/
                                                                            final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                                                            ad.setCancelable(false);

                                                                            ad.setTitle(getString(R.string.payment_in_progress));
                                                                            ad.setMessage(Utils.formatHtml(message));
                                                                            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                                                                                    new DialogInterface.OnClickListener() {

                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialog,
                                                                                                            int which) {
                                                                                            dialog.dismiss();
                                                                                            //withdraw_Alert.dismiss();
                                                                                            handler.removeCallbacks(runnable);
                                                                                            balanceCounter = 1;
                                                                                            handler.postDelayed(runnable, 3000);
                                                                                        }
                                                                                    });

                                                                            ad.show();
                                                                        }
                                                                        else {
                                                                            /*update the transaction status*/

                                                                            AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
                                                                            ad.setCancelable(false);

                                                                            ad.setTitle(getString(R.string.payment_failed));

                                                                            ad.setMessage("Payment Failed, Please try again");
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
                                                                    } catch (JSONException e1) {
                                                                        e1.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).show();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    private String getRegisteredPhone() {
        String registeredPhone = "";
        try {
            registeredPhone = clientObject.getString("phone");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return registeredPhone;
    }

    private void authorizeTopup(final String provider, final int amount, final String phoneNumber ){
        final ProgressDialog progressDialog = Utils.createProgressDialog(AgentHomeActivity.this, "Authorizing. Please wait ...", false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        try {
            Bridge.post(Utils.AUTHORIZE_TOPUP_URL)
                    .body(
                            new JSONObject()
                                    .put("platform",Utils.PLATFORM_CODE)
                                    .put("phone",phoneNumber.toString().trim())
                    ).asString(new ResponseConvertCallback<String>() {
                @Override
                public void onResponse(@Nullable Response response, @Nullable String s, @Nullable BridgeException e) {
                    progressDialog.dismiss();
                    if(Utils.DEBUG){
                        Log.d(TAG, "onResponse: "+s);
                    }
                    if(s==null){
                        Utils.showToastMessage(AgentHomeActivity.this,"Please check your connection and try again");
                        return;

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        if(jsonObject.has("status")){
                            Intent intent = new Intent(AgentHomeActivity.this, OTPActivity.class);
                            intent.putExtra("phone", phoneNumber);
                            intent.putExtra("provider", provider);
                            intent.putExtra("amount", amount);
                            intent.setType("authorize_topup");
                            startActivityForResult(intent, AUTHORIZE_TOPUP_REQUEST_CODE);
                        }else if(jsonObject.has("error")){
                            new AlertDialog.Builder(AgentHomeActivity.this)
                                    .setTitle("Registration Error")
                                    .setMessage(jsonObject.getString("error"))
                                    .setCancelable(false)
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void initiateTopUp() throws JSONException {
        View view = getLayoutInflater().inflate(R.layout.topup_layout,null,false);
        final EditText amountField = (EditText) view.findViewById(R.id.amount_field);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        Button submit = (Button) view.findViewById(R.id.submit);
        ImageButton selectContact = (ImageButton) view.findViewById(R.id.pick_phone);
        final AutoCompleteTextView phone = (AutoCompleteTextView) view.findViewById(R.id.phone_number_field);
        clientObject= PrefManager.getLastLoginUser(this);
        try {
            String phone_number = clientObject.getString("phone");
            String pattern = "(\\+?256|0)(\\d{9})";
            Pattern r = Pattern.compile(pattern);

            Matcher m = r.matcher(phone_number);
            if (m.find( )) {
                phone.setText("0" + m.group(2) );

            } else {
                phone.setText(phone_number);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Spinner providerSpinner = (Spinner) view.findViewById(R.id.provider_spinner);

        String[] providerNames = new String[]{"Airtel Money","MTN Mobile Money"};
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, providerNames);
        providerSpinner.setAdapter(providerAdapter);
        providerSpinner.setVisibility(View.GONE);
        final AlertDialog withdraw_Alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show();

        selectContact.setOnClickListener(new View.OnClickListener(){


               @Override
                public void onClick(View v) {
                    visiblePhoneField = phone;
                    refresh = false;
                    Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(i, PICK_CONTACT);
                }

        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withdraw_Alert.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt = amountField.getText().toString().trim();
                final String phoneNumber = phone.getText().toString().trim();
                boolean cancelSend = false;
                View focusView = null;
                amountField.setError(null);
                phone.setError(null);
                if(amt.length()==0){
                    amountField.setError(getString(R.string.required_field));
                    focusView = amountField;
                    cancelSend = true;

                }
                int i = 0;
                try {
                    i = Integer.parseInt(amt);
                }catch (Exception e){
                    e.printStackTrace();
                }
                final int amount = i;
                if(amount<500){
                    amountField.setError("Amount less than minimum");
                    focusView = amountField;
                    cancelSend = true;
                }
                if(amount>5000000){
                    amountField.setError("Amount greater than maximum (5000000)");
                    focusView = amountField;
                    cancelSend = true;
                }

                if ( TextUtils.isEmpty(phoneNumber) )
                {
                    phone.setError(getString(R.string.required_field));
                    cancelSend = true;
                    focusView = phone;
                }

                if ( phoneNumber.length() < 10 )
                {
                    phone.setError(getString(R.string.invalid_phone));
                    cancelSend = true;
                    focusView = phone;

                }
                if ( !(phoneNumber.startsWith("0")) )
                {
                    phone.setError("Number should start with 0");
                    cancelSend = true;
                    focusView = phone;

                }
                if(cancelSend){
                    focusView.requestFocus();
                    return;

                }

                //validate phone number
                String provider = "airtel";
                if (providerSpinner.getSelectedItemPosition()==1){
                    provider = "mtn";
                }

                String registeredPhone = getRegisteredPhone();
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                withdraw_Alert.dismiss();
                if (!registeredPhone.contains(phoneNumber.replaceFirst("0", "")) && phoneNumber.matches(Utils.MTN_REGEX)) {
                    authorizeTopup(provider, amount, phoneNumber);
                } else {
                    sendTopupRequest(provider, amount, phoneNumber);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.menu_wallet_home,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_refresh){
            getWalletBalance();
        }
        if (item.getItemId() == R.id.action_logout) {
            final AlertDialog ad = new AlertDialog.Builder(AgentHomeActivity.this).create();
            ad.setCancelable(false);

            ad.setTitle("Logout Confirmation");
            ad.setMessage(Utils.formatHtml("Are you sure you want to sign out?"));
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {

                }
            });
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            doLogout(false);
                        }
                    });

            ad.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        if (refresh) {
            getWalletBalance();
        }
        refresh = true;
        */
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && refresh) {
            getWalletBalance();
        }
        refresh = true;
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            //Intent intent = new Intent(Intent.ACTION_MAIN);
            //intent.addCategory(Intent.CATEGORY_HOME);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(intent);
            super.onBackPressed();
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        } else {
            Toast toast = Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check that it is the SecondActivity with an OK result
        if (requestCode == AUTHORIZE_TOPUP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
                //sendTopupRequest(provider, withdraw_Alert, amount, phoneNumber);
                String phoneNumber = data.getStringExtra("phone");
                String provider = data.getStringExtra("provider");
                int amount = data.getIntExtra("amount", 1000);
                sendTopupRequest(provider, amount, phoneNumber);
            }
        }

        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = cursor.getString(column);
            //Log.d("phone number", phoneNumber);
            if (visiblePhoneField != null){
                visiblePhoneField.setText(phoneNumber);
                visiblePhoneField = null;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogoutTimerUtil.startLogoutTimer(this, this);
        Log.e(TAG, "OnStart () &&& Starting timer");
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        LogoutTimerUtil.startLogoutTimer(this, this);
        Log.e(TAG, "User interacting with screen");
    }


    @Override
    protected void onPause() {
        super.onPause();
        LogoutTimerUtil.stopLogoutTimer();
        Log.e(TAG, "onPause()");
    }


    /**
     * Performing idle time logout
     */
    @Override
    public void doLogout(boolean session_timeout) {
        // write your stuff here
        LogoutTimerUtil.stopLogoutTimer();
        //super.onBackPressed();
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("session_timeout", session_timeout);
        startActivity(intent);
        finish();
    }


}
