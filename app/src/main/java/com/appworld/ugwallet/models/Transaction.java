package com.appworld.ugwallet.models;

import com.appworld.ugwallet.utils.Utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bernard on 1/3/17.
 * model class to hold transaction data
 */
public class Transaction {

    private String chargeId, accountNo, productName, phone, provider, date, recipientEmail, status, purchaseStatus;
    private int amount, retries;

    public Transaction(String chargeId, String accountNo, String product, int amount, String phone, String provider)
    {
        this.chargeId = chargeId;
        this.accountNo = accountNo;
        this.productName = product;
        this.amount = amount;
        this.phone = phone;
        this.provider = provider;
        this.recipientEmail = "";
        this.status = Utils.STATUS_PENDING;
        this.purchaseStatus = Utils.STATUS_PENDING;
        this.retries = 0;

        Date today = Calendar.getInstance().getTime();
        Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        this.date = formatter.format(today);
    }

    public String getChargeId(){
        return this.chargeId;
    }

    public String getAccountNo(){
        return this.accountNo;
    }

    public String getProductName(){
        return this.productName;
    }

    public String getPhone(){
        return this.phone;
    }

    public String getProvider(){
        return this.provider;
    }

    public int getAmount()
    {
        return this.amount;
    }

    public String getDate()
    {
        return this.date;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurchaseStatus() {
        return purchaseStatus;
    }

    public void setPurchaseStatus(String purchaseStatus) {
        this.purchaseStatus = purchaseStatus;
    }

    public int getRetries() {
        return retries;
    }

    public void incrementRetries() {
        this.retries += 1;
    }
}
