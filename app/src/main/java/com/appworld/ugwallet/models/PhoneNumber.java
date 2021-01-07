package com.appworld.ugwallet.models;

/**
 * Created by bernard on 1/2/17.
 * class that represents the person's phone number
 */
public class PhoneNumber {

    private String name, number;

    public PhoneNumber(String name, String number)
    {
        this.name = name;
        this.number = number;
    }

    public String getName()
    {
        return this.name;
    }

    public String getNumber()
    {
        return this.number;
    }

}
