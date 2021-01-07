package com.appworld.ugwallet.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.appworld.ugwallet.AgentHomeActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.appworld.ugwallet.R;
import com.appworld.ugwallet.models.PhoneNumber;
import com.appworld.ugwallet.models.Purchase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by bernard on 12/31/16.
 * class with the global methods and constants
 */
public class Utils {

    /*remember to add 50 to the given tariff charge*/

    public static final String USERNAME = ""; //
    public static final String PASSWORD = ""; //
    public static final String ACCOUNT_CODE = "";
    public static final String APP_NAME = "Ug Wallet";
    public static final String VENDOR_PRODUCT_CODE = "UMEME";
    public static final String VENDOR_PRODUCT_NAME = "UMEME Yaka TopUp";

    private static final String API_URL = "https://app.ugmart.ug/api";
    public static final String CUSTOM_USER_AGENT = "UGMART-UA-94550486";

    public static final String FORM_DATA_URL = API_URL + "/payment-providers?app="+ACCOUNT_CODE;

    public static final String LOGIN_URL = API_URL + "/login";
    public static final String CHARGE_URL = API_URL + "/request-payment";
    public static final String PURCHASE_URL = API_URL + "/products/purchase";
    public static final String VALIDATE_URL = API_URL + "/products/validate";
    public static final String TRANSACTION_CHECK_URL = API_URL + "/transactions";
    public static final String PURCHASE_CHECK_URL = API_URL + "/purchases";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_FAILED = "FAILED";

    public static final int MIN_AMOUNT = 2000;
    public static final int CODE_OK = 200;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_GATEWAY_TIMEOUT = 504;

    //public static final String WALLET_HOST_URL = "http://ec2-35-178-151-58.eu-west-2.compute.amazonaws.com:8005";
    //public static final String WALLET_HOST_URL = "http://192.168.5.199:8005";
    public static final String WALLET_HOST_URL = "https://ugwallets.com";
    public static final String AREA_CODES_URL = "https://app.ugmart.ug/api/products/choice-list?code=NWSC";
    public static final String REGISTRATION_URL = WALLET_HOST_URL+"/api/clients/";
    public static final String VALIDATE_OTP_URL = WALLET_HOST_URL+"/api/validate-otp/";
    public static final String AUTHORIZE_TOPUP_URL = WALLET_HOST_URL+"/api/authorize-topup/";
    public static final String PLATFORM_CODE = "ugwallet";
    //old login
//    public static final String CLIENT_LOGIN_URL = WALLET_HOST_URL+"/api/client-login/";
    public static final String CLIENT_LOGIN_URL = WALLET_HOST_URL+"/api/auth-jwt/";
    public static final String CLIENT_TRANSACTION_URL = WALLET_HOST_URL+"/api/client-earnings2/";
    public static final String CLIENT_WALLET_PURCHASES_URL = WALLET_HOST_URL+"/api/client-wallet-purchases/";
    public static final String CLIENT_WALLET_TOPUPS_URL = WALLET_HOST_URL+"/api/client-wallet-topups/";
    public static final String CLIENT_FORGOT_PASSWORD_URL = WALLET_HOST_URL+"/api/client-forgot-password/";
    public static final String CLIENT_VERIFY_CODE_URL = WALLET_HOST_URL+"/api/client-password-reset-code/";
    public static final String CLIENT_CHANGE_PASSWORD_URL = WALLET_HOST_URL+"/api/client-password-change/";
    public static final String SAVE_TRANSACTION_URL = WALLET_HOST_URL+"/api/transactions/" ;
    public static final String WALLET_VALIDATE_ACCOUNT_URL = WALLET_HOST_URL+"/api/validate-account/";
    public static final String WALLET_TOPUP_URL = WALLET_HOST_URL+"/api/topup-wallet/";
    public static final String WITHDRAW_FUNDS_URL = WALLET_HOST_URL+"/api/withdraw-funds/";
    public static final String SEND_FUNDS_URL = WALLET_HOST_URL+"/api/send-funds/";
    public static final String PURCHASE_PRODUCT_URL = WALLET_HOST_URL+"/api/purchase-product/";
    public static final String CLIENT_GET_CHARGE = WALLET_HOST_URL+"/api/charge/";
    public static final String WALLET_BALANCE_URL = WALLET_HOST_URL+"/api/wallet-balance/";
    public static final String WALLET_GET_SERVICE_LIST = WALLET_HOST_URL+"/api/get-services/";
    public static final String WALLET_NWSC_AREAS_LIST = WALLET_HOST_URL+"/api/get-nwcs-areas/";
    public static final String WALLET_AIRTIME_PURCHASE = WALLET_HOST_URL+"/api/purchase-airtime/";
    public static final String WALLET_DEVICE_REG = WALLET_HOST_URL+"/api/device-reg/";
    //public static final String WALLET_TOPUS_NEW = WALLET_HOST_URL+"/api/wallet-topups/";
    public static final String WALLET_TOPUS_NEW = WALLET_HOST_URL+"/api/history/topups/";
    //public static final String WALLET_TRANSACTIONS_NEW = WALLET_HOST_URL+"/api/wallet-transactions/";
    public static final String WALLET_TRANSACTIONS_NEW = WALLET_HOST_URL+"/api/history/transactions/";
    //public static final String WALLET_EARNINGS_NEW = WALLET_HOST_URL+"/api/point-earnings/";
    public static final String WALLET_EARNINGS_NEW = WALLET_HOST_URL+"/api/history/commission/";
    public static final String WALLET_VALIDATE_MOBILE_MONEY = WALLET_HOST_URL+"/api/validate-send-money/";;
    public static final String WALLET_SEND_MOBILE_MONEY = WALLET_HOST_URL+"/api/send-money/";
    private static final String NEW_LINE = "\n";
    public static final String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
    public static final String MTN_REGEX = "^(256|0)(76|77|78|39)\\d{7}";
    public static final String UG_PHONE_REGEX = "^(7|3)\\d{8}$";
    public static final String AIRTEL_REGEX = "^(256|0)(70|75)\\d{7}";

    private static final String ACCOUNT_NO = "Account No: ";
    private static final String ACCOUNT_NAME = "Account Name: ";
    private static final String PRODUCT = "Product Name: ";
    private static final String AMOUNT = "Amount: UGX. ";
    private static final String BALANCE = "UMEME Balance: UGX. ";
    private static final String CHARGE = "Charge: UGX. ";
    private static final String VISA_FEES = "Visa Admin Fees: UGX. ";
    private static final String PROVIDER = "Payment Provider: ";
    private static final String PHONE_NO = "Transaction No: ";
    private static final String TOTAL_PAYABLE = "Total Amount Payable: UGX. ";
    private static final String TRANS_CONTACT_NO = "Contact No: ";

    public static final int VOLLEY_TIMEOUT_MS = 600000; //600 secs - 10mins
    public static final boolean DEBUG = false;

    public final static String APP_P_NAME = "com.ugmart.ugwallet";
    private final static String CONTACT_NO = "+256 776 220110";

    //data required for the email process
    public static final String TYPE_OF_EMAIL = "message/rfc822";
    private static final String DEVICE = "Device: ";
    private static final String SDK_VERSION = "SDK Version: ";
    private static final String MODEL = "Model: ";
    private static final String APP_VERSION = "Ug Wallet App Build Version: ";
    private static final String DIVIDER_STRING = "----------";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
    public static final String PRODUCT_AIRTIME = "AIRTIME" ;
    public static final String PRODUCT_INTERNET = "INTERNET";
    public static final String PRODUCT_YAKA = "YAKA";
    public static final String PRODUCT_WATER = "WATER";
    public static final String PRODUCT_TV = "TV";
    public static final String PRODUCT_SENDMONEY = "SEND_MONEY";
    public static final String PRODUCT_MTN_MONEY = "mtn_mobile_money";
    public static final String PRODUCT_AIRTEL_MONEY = "airtel_money";



    /**
     * method used to get the app share URI
     * **/
    public static Uri getAppShareLink() {
        return Uri.parse("https://play.google.com/store/apps/details?id=" + APP_P_NAME);
    }

    /**
     * method used to get the contact number URI
     * **/
    public static Uri getContactLink() {
        return Uri.parse("tel:"+CONTACT_NO);
    }

    /**
     * method used to show toast messages in the app
     * **/
    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * method used to get the current app version code
     * **/
    public static int getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    /**
     * Method that returns the version name of the app
     * **/
    private static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method that compiles the device information to be used when writing feedback email
     * **/
    public static StringBuilder getDeviseInfoForFeedback(Context context) {
        StringBuilder infoStringBuilder = new StringBuilder();
        infoStringBuilder.append(DEVICE).append(android.os.Build.DEVICE);
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(SDK_VERSION).append(Build.VERSION.SDK_INT);
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(MODEL).append(android.os.Build.MODEL);
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(APP_VERSION).append( getAppVersionName(context) );
        infoStringBuilder.append(NEW_LINE);
        infoStringBuilder.append(DIVIDER_STRING);
        infoStringBuilder.append(NEW_LINE);
        return infoStringBuilder;
    }

    /**
     * method used to initiate the app rating procedure
     * **/
    private static void rateApp(Context context)
    {
        Uri uri = Uri.parse("market://details?id=" + APP_P_NAME);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, getAppShareLink()));
        }
    }

    /**
     * method used to display a dialog with the notification message
     * **/
    private static void createNotificationDialog(final Context context, String title, String message,
                                                 boolean update)
    {
        AlertDialog ad = new AlertDialog.Builder(context).create();
        ad.setCancelable(false);

        ad.setTitle(title);
        ad.setMessage(message);
        //if its an update notification, add two buttons
        if ( update )
        {
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            rateApp(context);
                        }
                    });

            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        else
        {
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        ad.show();
    }

    public static void processNotification(final Context context, Intent intent)
    {
        // checking for type intent filter
        if (intent.getAction().equals(Utils.REGISTRATION_COMPLETE)) {
            // gcm successfully registered
            // now subscribe to `global` topic to receive app wide notifications
            FirebaseMessaging.getInstance().subscribeToTopic(Utils.TOPIC_GLOBAL);

        } else if (intent.getAction().equals(Utils.PUSH_NOTIFICATION)) {
            // new push notification is received

            String message = intent.getStringExtra("message");
            String title = intent.getStringExtra("title");
            boolean update = intent.getExtras().containsKey("update") && intent.getExtras
                    ().getBoolean("update");
            createNotificationDialog(context, title, message, update);
        }
        else if ( intent.getExtras() != null )
        {
            Bundle extras = intent.getExtras();
            if(extras.containsKey("message") && extras.containsKey("title") && extras.containsKey
                    ("update")) {
                // extract the extra-data in the Notification
                boolean update = extras.getBoolean("update");
                String message = extras.getString("message");
                String title = extras.getString("title");
                createNotificationDialog(context, title, message, update);
            }
        }
    }

    public static String capitalize(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    public static ProgressDialog createProgressDialog(Context context, String message, Boolean cancelable)
    {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgressNumberFormat(null);
            progressDialog.setProgressPercentFormat(null);
        }

        return progressDialog;
    }

    public static String generateUniqueId()
    {
        String uniqueId = UUID.randomUUID().toString();
        int lastIndex = uniqueId.lastIndexOf("-");

        return uniqueId.substring(lastIndex+1).toUpperCase();
    }

    public static int getMinutesFromMillis(long milliSecs)
    {
        return (int) ((milliSecs / (1000 * 60)) % 60);
    }

    /**
     * Method that compiles the transaction information for user approval before initiating the transaction
     * **/
    public static StringBuilder getAlertInformation( String accountName, String accountNo,String product, int amount, int balance, int charge, String provider, String phone) {
        StringBuilder infoStringBuilder = new StringBuilder();

        infoStringBuilder.append(ACCOUNT_NAME).append(accountName);
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(ACCOUNT_NO).append(accountNo);
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(PRODUCT).append(product);
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);

        if (balance > 0) {
            infoStringBuilder.append(BALANCE).append( formatCurrency(balance) );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        }

        infoStringBuilder.append(AMOUNT).append( formatCurrency(amount) );
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);

        if ( provider.contains("VISA") )
        {
            infoStringBuilder.append(PROVIDER).append( provider );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
            infoStringBuilder.append(VISA_FEES).append( formatCurrency(charge) );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
            infoStringBuilder.append(TOTAL_PAYABLE).append( formatCurrency(charge+amount) );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
            infoStringBuilder.append(TRANS_CONTACT_NO).append( phone );
        }
        else
        {
            infoStringBuilder.append(CHARGE).append( formatCurrency(charge) );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
            infoStringBuilder.append(TOTAL_PAYABLE).append( formatCurrency(charge+amount) );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
            infoStringBuilder.append(PROVIDER).append( provider );
            infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
            infoStringBuilder.append(PHONE_NO).append( phone );
        }

        return infoStringBuilder;
    }

    public static StringBuilder getPurchaseDetails(Purchase purchase)
    {
        StringBuilder infoStringBuilder = new StringBuilder();

        infoStringBuilder.append(formatHtml("<strong>Account:</strong> ")).append( purchase.getAccount() );
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(formatHtml("<strong>Description:</strong> ")).append( purchase.getDescription() );
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(formatHtml("<strong>Amount:</strong> UGX. ")).append( formatCurrency(purchase.getAmount()) );
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(formatHtml("<strong>Total Units:</strong> ")).append( purchase.getUnits() );
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(formatHtml("<strong>Token:</strong> ")).append( purchase.getToken() );
        infoStringBuilder.append(NEW_LINE).append(NEW_LINE);
        infoStringBuilder.append(formatHtml("<strong>Receipt No:</strong> ")).append( purchase.getReceiptNo() );

        return infoStringBuilder;
    }

    /**
     * method that converts integer value to currency
     */
    public static String formatCurrency(int amount) {
        String output;
        String num = output = String.valueOf(amount);
        int l = num.length();

        for(int i = l; i > -1; i = i-3){
            if( i != 0 && i != l ) {
                output = output.substring(0, i)+','+output.substring(i);
            }
        }

        return output;
    }

    public static Spanned formatHtml(String htmlText) {
        Spanned text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text = Html.fromHtml(htmlText, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        }
        else {
            text = Html.fromHtml(htmlText);
        }

        return text;
    }

    /**
     * Method that displays the error from the volley request in a toast
     * **/
    public static void processVolleyError(Context context, VolleyError error, String tag) {
        String message = VolleyErrorHelper.getMessage(error, context);
        if( DEBUG ) {
            Log.e(tag, "Error: " + message);
        }
        showToastMessage(context, message);
    }

    /**
     * Method that sets the action bar title for the activity
     * **/
    public static void setActionBarTitle(Context context, ActionBar actionBar, CharSequence title) {
        TextView textView = new TextView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        textView.setText(title);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setTextColor(ContextCompat.getColor(context, R.color.white));

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        if ( !title.toString().equalsIgnoreCase( context.getString(R.string.app_name) ) )
        {
            textView.setTextSize(20);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            textView.setTextSize(23);
            textView.setGravity(Gravity.CENTER);
        }

        actionBar.setCustomView(textView);
    }

    public static void savePhoneNumber(final Context context, final String number)
    {
        PrefManager prefManager = new PrefManager(context);
        ArrayList<PhoneNumber> phoneNumbers = prefManager.getPhoneNumbers();
        if (phoneNumbers != null) {
            for(PhoneNumber phoneNumber: phoneNumbers ){
                if (phoneNumber.getNumber().equals(number)){
                    new android.support.v7.app.AlertDialog.Builder(context)
                            .setTitle("Duplicate Number")
                            .setMessage("The phone number " + number + "has already been saved.")
                            .setCancelable(false)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                    return;
                }
            }
        }

        /*get the name_prompt layout and inflate a dialog box which prompts the person to enter the contact name*/
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptView = inflater.inflate(R.layout.name_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);

        final EditText userInput = (EditText) promptView.findViewById(R.id.contact_name_field);

        // set dialog message
        alertDialogBuilder
                .setTitle(context.getString(R.string.save_number_as))
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                //check if the user entered something
                                String contact_name = userInput.getText().toString().trim();
                                if( TextUtils.isEmpty(contact_name) )
                                {
                                    showToastMessage(context, context.getString(R.string.save_number_as));
                                }
                                else
                                {
                                    //save the contact details and dismiss the dialog
                                    PrefManager prefManager = new PrefManager(context);
                                    PhoneNumber phoneNumber = new PhoneNumber(contact_name, number);
                                    prefManager.savePhoneNumber(phoneNumber);
                                    dialog.dismiss();

                                    new android.support.v7.app.AlertDialog.Builder(context)
                                            .setTitle("Phone no. saved")
                                            .setMessage("The phone number has been saved.")
                                            .setCancelable(false)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        alertDialogBuilder.create().show();

    }

    /*public method that populates a list of all saved contacts and presents the list in a dialog*/
    public static void populateContactList(Context context, final EditText resultField)
    {
        //initialize the pref manager and get the numbers from a given type
        PrefManager prefManager = new PrefManager(context);
        final List<PhoneNumber> phoneNumbers = prefManager.getPhoneNumbers();

        //create the alert dialog if there are saved contacts
        if( phoneNumbers != null )
        {
            ArrayList<String> contactList = new ArrayList<>();
            //loop through the phone number array list and form the list
            for(int i=0; i < phoneNumbers.size(); i++)
            {
                PhoneNumber phoneNumber = phoneNumbers.get(i);
                String item = phoneNumber.getName()+" - "+phoneNumber.getNumber();
                contactList.add(item);
            }

            String[] contactsArray = contactList.toArray(new String[contactList.size()]);
            //create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            String title = context.getString(R.string.pick_saved_no_dialog);
            if (resultField.getId() == R.id.phone_number_field){
                title = context.getString(R.string.pick_saved_phone_dialog);

            }
            builder.setTitle(title);
            builder.setItems(contactsArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {

                    //get the phone number that is selected
                    String number = phoneNumbers.get(position).getNumber();
                    //set the number as the input for the result field
                    resultField.setText(number);

                }
            });
            builder.create().show();
        }
        else
        {
            showToastMessage(context, "No saved numbers found");
        }
    }

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    //    SimpleDateFormat sdf1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    static SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM yyyy HH:mm");

    public static String formart_date(String tstamp) throws ParseException {
        return sdf1.format(sdf.parse(tstamp.substring(0, 19)));
    }
}
