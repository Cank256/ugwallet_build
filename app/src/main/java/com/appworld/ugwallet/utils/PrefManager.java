package com.appworld.ugwallet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.appworld.ugwallet.models.PhoneNumber;
import com.appworld.ugwallet.models.Provider;
import com.appworld.ugwallet.models.Purchase;
import com.appworld.ugwallet.models.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by bernard on 12/31/16.
 * class that manages the app shared preferences
 */
public class PrefManager {

    public static final String PREF_SERVICES_AIRTIME = "pref_airtime_service_list";
    public static final String PREF_SERVICES_INTERNET = "pref_internet_service_list";
    public static final String PREF_SERVICES_YAKA= "pref_yaka_service_list";
    public static final String PREF_SERVICES_WATER = "pref_water_service_list";
    public static final String PREF_SERVICES_TV ="pref_tv_service_list" ;
    //shared preferences
    private SharedPreferences sharedPrefs;
    //declare the shared preferences editor
    private SharedPreferences.Editor editor;
    //context
    private Context _context;

    // Shared preferences file name
    private static final String PREF_NAME = "InternalAppData";

    private static final String PHONE_NUMBERS = "phone_numbers";
    private static final String SERVICE_PROVIDERS = "service_providers";
    private static final String TRANSACTIONS = "transactions";
    private static final String PURCHASES = "token_purchases";
    private static final String SEARCH_HISTORY = "SearchHistory";
    private static final String ACCOUNT_HISTORY = "AccountHistory";
    private static final String ACCOUNT_DETAILS = "account_details";
    private static final String PUSH_NOT_TOKEN = "push_notification_token";
    private static final double VISA_CHARGE_RATE = 0.04;

    /*keys used during the session*/
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_AUTH_HEADER = "authorization_header";
    private static final String TOKEN_EXPIRY_TIME = "token_expiry";

    public PrefManager(Context context) {
        this._context = context;
        sharedPrefs = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();
    }

    public void storePushNotificationId(String token)
    {
        editor.putString(PUSH_NOT_TOKEN, token);
        editor.commit();
    }

    public void storeSessionDetails(String accessToken, int expiresIn) {
        /*calculate the expiry time*/
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, expiresIn);
        editor.putLong(TOKEN_EXPIRY_TIME, calendar.getTimeInMillis());

        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putInt(KEY_EXPIRES_IN, expiresIn);
        /*create the authorization header and store it in shared prefs*/
        editor.putString(KEY_AUTH_HEADER, "Bearer "+accessToken);
        editor.commit();
    }

    public long getTokenExpiryTime() {
        return sharedPrefs.getLong(TOKEN_EXPIRY_TIME, System.currentTimeMillis());
    }

    public void resetAuthHeader() {
        editor.putString(KEY_AUTH_HEADER, "");
        editor.commit();
    }

    public String getAuthHeader() {
        return sharedPrefs.getString(KEY_AUTH_HEADER, "");
    }

    /*method to get the account details stored in the shared prefs*/
    public ArrayList<PhoneNumber> getAccountDetails()
    {
        //get the string format of the numbers from the shared prefs
        String numbers = sharedPrefs.getString(ACCOUNT_DETAILS, "");
        Gson gson = new Gson();
        Type contactListType = new TypeToken<ArrayList<PhoneNumber>>(){}.getType();

        return gson.fromJson(numbers, contactListType);

    }

    public void saveAccountDetails(PhoneNumber phoneNumber)
    {
        //get all the saved numbers and add the new number
        ArrayList<PhoneNumber> accounts = getAccountDetails() == null ? new ArrayList<PhoneNumber>() : getAccountDetails();
        accounts.add(phoneNumber);

        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type contactListType = new TypeToken<ArrayList<PhoneNumber>>(){}.getType();
        String contacts = gson.toJson(accounts, contactListType);

        //commit the new changes to the shared prefs
        editor.putString(ACCOUNT_DETAILS, contacts);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Account number added " + accounts.size());
        }
    }

    /*method to get the search history list stored in the shared prefs*/
    public ArrayList<String> getSearchHistory(Boolean forAcctNo)
    {
        //get the string format of the history from the shared prefs
        String history = forAcctNo ? sharedPrefs.getString(ACCOUNT_HISTORY, "") : sharedPrefs.getString(SEARCH_HISTORY, "");
        Gson gson = new Gson();
        return gson.fromJson(history, new TypeToken<ArrayList<String>>(){}.getType());
    }

    public void addSearchInput(String input, Boolean forAcctNo)
    {
        ArrayList<String> history = getSearchHistory(forAcctNo) == null ? new ArrayList<String>() : getSearchHistory(forAcctNo);
        if( !history.contains(input) )
        {
            //only add an entry if it doesn't exist
            history.add(input);
            //convert the arrayList back to string for storage
            Gson gson = new Gson();
            String searchHistory = gson.toJson(history, new TypeToken<ArrayList<String>>(){}.getType());
            if ( forAcctNo )
            {
                editor.putString(ACCOUNT_HISTORY, searchHistory);
            }
            else
            {
                editor.putString(SEARCH_HISTORY, searchHistory);
            }
            editor.commit();
            if( Utils.DEBUG ) {
                Log.e("SharedPrefs", "Search Input added " + history.size());
            }
        }
    }

    /*method to get the phone numbers stored in the shared prefs*/
    public ArrayList<PhoneNumber> getPhoneNumbers()
    {
        //get the string format of the numbers from the shared prefs
        String numbers = sharedPrefs.getString(PHONE_NUMBERS, "");
        Gson gson = new Gson();
        Type contactListType = new TypeToken<ArrayList<PhoneNumber>>(){}.getType();

        return gson.fromJson(numbers, contactListType);

    }

    /*method to save phone number to the shared prefs*/
    public void savePhoneNumber(PhoneNumber phoneNumber)
    {
        //get all the saved numbers and add the new number
        ArrayList<PhoneNumber> savedNumbers = getPhoneNumbers() == null ? new ArrayList<PhoneNumber>() : getPhoneNumbers();
        savedNumbers.add(phoneNumber);

        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type contactListType = new TypeToken<ArrayList<PhoneNumber>>(){}.getType();
        String contacts = gson.toJson(savedNumbers, contactListType);

        //commit the new changes to the shared prefs
        editor.putString(PHONE_NUMBERS, contacts);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Phone number added " + savedNumbers.size());
        }
    }

    /*method to delete the phone number from the array stored in the shared prefs*/
    public void deletePhoneNumber(int position)
    {
        //get all saved numbers
        ArrayList<PhoneNumber> savedNumbers = getPhoneNumbers();

        savedNumbers.remove(position);
        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type contactListType = new TypeToken<ArrayList<PhoneNumber>>(){}.getType();
        String contacts = gson.toJson(savedNumbers, contactListType);

        //commit the new changes to the shared prefs
        editor.putString(PHONE_NUMBERS, contacts);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Phone number deleted " + savedNumbers.size());
        }
    }

    /*method to save service providers to the shared prefs*/
    public void saveServiceProviders(ArrayList<Provider> providers) {
        //convert the array list to a string for storage
        Gson gson = new Gson();
        Type providerListType = new TypeToken<ArrayList<Provider>>(){}.getType();
        String providerList = gson.toJson(providers, providerListType);

        //commit the new changes to the shared prefs
        editor.putString(SERVICE_PROVIDERS, providerList);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Providers saved " + providers.size());
        }
    }

    /*method to get the transactions stored in the shared prefs*/
    public ArrayList<Transaction> getTransactions()
    {
        //get the string format of the transactions from the shared prefs
        String transactions = sharedPrefs.getString(TRANSACTIONS, "");
        Gson gson = new Gson();
        Type transactionListType = new TypeToken<ArrayList<Transaction>>(){}.getType();

        return gson.fromJson(transactions, transactionListType);

    }

    /*method to save transaction to the shared prefs*/
    public void saveTransaction(Transaction transaction)
    {
        //get all the saved transactions and add the new one
        ArrayList<Transaction> savedTransactions = getTransactions() == null ? new ArrayList<Transaction>() : getTransactions();
        savedTransactions.add(transaction);

        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type transactionListType = new TypeToken<ArrayList<Transaction>>(){}.getType();
        String transactions = gson.toJson(savedTransactions, transactionListType);

        //commit the new changes to the shared prefs
        editor.putString(TRANSACTIONS, transactions);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Transaction added " + savedTransactions.size());
        }
    }

    /*method to update the transaction status*/
    public void updateTransaction(String transactionId, String status, boolean forPurchase, boolean updatePurchaseRetries) {
        //get all the saved transactions and edit the appropriate one
        ArrayList<Transaction> savedTransactions = getTransactions();
        for (int i = 0; i < savedTransactions.size(); i++) {
            Transaction transaction = savedTransactions.get(i);
            if (forPurchase) {
                /*update the purchase status*/
                if (!TextUtils.isEmpty(transaction.getChargeId()) && transaction.getChargeId().equalsIgnoreCase(transactionId)) {
                    transaction.setPurchaseStatus(status);
                    if (updatePurchaseRetries) {
                        /*if true, increment the retry count for the purchase*/
                        transaction.incrementRetries();
                    }
                    break;
                }
            }
            else {
                /*update the payment status*/
                if (!TextUtils.isEmpty(transaction.getChargeId()) && transaction.getChargeId().equalsIgnoreCase(transactionId)) {
                    transaction.setStatus(status);
                    break;
                }
            }
        }

        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type transactionListType = new TypeToken<ArrayList<Transaction>>(){}.getType();
        String transactions = gson.toJson(savedTransactions, transactionListType);

        //commit the new changes to the shared prefs
        editor.putString(TRANSACTIONS, transactions);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Transaction Updated");
        }
    }

    /*method used to remove transaction record from shared prefs when the payment fails due to auth error*/
    public void removeTransaction(String transactionId) {
        //get all the saved transactions and edit the appropriate one
        ArrayList<Transaction> savedTransactions = getTransactions();
        for (int i = 0; i < savedTransactions.size(); i++) {
            Transaction transaction = savedTransactions.get(i);
            if (!TextUtils.isEmpty(transaction.getChargeId()) && transaction.getChargeId().equalsIgnoreCase(transactionId)) {
                savedTransactions.remove(i);
                break;
            }
        }

        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type transactionListType = new TypeToken<ArrayList<Transaction>>(){}.getType();
        String transactions = gson.toJson(savedTransactions, transactionListType);

        //commit the new changes to the shared prefs
        editor.putString(TRANSACTIONS, transactions);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Transaction Updated");
        }
    }

    /*method to delete the transaction from the array stored in the shared prefs*/
    public void deleteTransaction(int position)
    {
        //get all saved transactions
        ArrayList<Transaction> savedTransactions = getTransactions();

        savedTransactions.remove(position);
        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type transactionListType = new TypeToken<ArrayList<Transaction>>(){}.getType();
        String transactions = gson.toJson(savedTransactions, transactionListType);

        //commit the new changes to the shared prefs
        editor.putString(TRANSACTIONS, transactions);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Transaction deleted " + savedTransactions.size());
        }
    }

    /*method to get the purchases stored in the shared prefs*/
    public ArrayList<Purchase> getPurchases()
    {
        //get the string format of the purchases from the shared prefs
        String purchases = sharedPrefs.getString(PURCHASES, "");
        Gson gson = new Gson();
        Type purchaseListType = new TypeToken<ArrayList<Purchase>>(){}.getType();

        return gson.fromJson(purchases, purchaseListType);

    }

    /*method to save purchase to the shared prefs*/
    public void savePurchase(Purchase purchase)
    {
        //get all the saved purchases and add the new one
        ArrayList<Purchase> savedPurchases = getPurchases() == null ? new ArrayList<Purchase>() : getPurchases();
        savedPurchases.add(purchase);

        //convert the array list back to a string for storage
        Gson gson = new Gson();
        Type purchaseListType = new TypeToken<ArrayList<Purchase>>(){}.getType();
        String purchases = gson.toJson(savedPurchases, purchaseListType);

        //commit the new changes to the shared prefs
        editor.putString(PURCHASES, purchases);
        editor.commit();
        if( Utils.DEBUG ) {
            Log.e("SharedPrefs", "Purchase added " + savedPurchases.size());
        }


    }

    //save last agent login number
    public static void saveLastLoginNumber(Context context,String phone){
        context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).edit().putString("last_login_phone",phone)
                .apply();

    }
    public static String getLastLoginNumber(Context context){
       return context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).getString("last_login_phone","");

    }

    public static void saveLastLoginUserDetails(Context context, String s) {
        context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).edit().putString("last_login_user",s)
                .apply();
    }
    public static JSONObject getLastLoginUser(Context context){
        try {
            return new JSONObject(context.getSharedPreferences(
                    PREF_NAME,context.MODE_PRIVATE
            ).getString("last_login_user",""));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void saveLastRegistrationNumber(Context context,String phone){
        context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).edit().putString("last_registration_phone",phone)
                .apply();

    }
    public static String getLastRegistrationNumber(Context context){
        return context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).getString("last_registration_phone","");

    }

    public static void saveLastRegistrationEmail(Context context,String email){
        context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).edit().putString("last_registration_email", email)
                .apply();

    }
    public static String getLastRegistrationEmail(Context context){
        return context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).getString("last_registration_email","");

    }
    public static void saveLastRegistrationPin(Context context,String pin){
        context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).edit().putString("last_registration_pin", pin)
                .apply();

    }
    public static String getLastRegistrationPin(Context context){
        return context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).getString("last_registration_pin","");

    }

    public static void scrubPreference(Context context, String key){
        context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE).edit().remove(key).apply();
    }
    public static void scrubRegistrationDetails(Context context){
        scrubPreference(context, "last_registration_phone");
        scrubPreference(context, "last_registration_email");
        scrubPreference(context, "last_registration_pin");
    }
    public static void saveServices(Context context,String service, String s) {
        context.getSharedPreferences(
                PREF_NAME,context.MODE_PRIVATE
        ).edit().putString(service,s)
                .apply();
    }
    public static JSONObject getServices(Context context, String service){
        try {
            return new JSONObject(context.getSharedPreferences(
                    PREF_NAME,context.MODE_PRIVATE
            ).getString(service,""));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
