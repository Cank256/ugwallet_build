package com.appworld.ugwallet.models;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bernard on 1/20/17.
 * model class to hold information about a successful Yaka token purchase
 */
public class Purchase {

    private String account, description, units, receiptNo, token, date;
    private int amount;

    public Purchase(String account, String description, String units, String receiptNo, String token, int amount)
    {
        this.account = account;
        this.description = description;
        this.units = units;
        this.receiptNo = receiptNo;
        this.token = token;
        this.amount = amount;

        Date today = Calendar.getInstance().getTime();
        Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        this.date = formatter.format(today);
    }

    public String getAccount()
    {
        return this.account;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getUnits()
    {
        return this.units;
    }

    public String getReceiptNo()
    {
        return this.receiptNo;
    }

    public String getToken()
    {
        return this.token;
    }

    public int getAmount()
    {
        return this.amount;
    }

    public String getDate() {
        return this.date;
    }

}
