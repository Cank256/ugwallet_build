package com.appworld.ugwallet.models;

/**
 * Created by bernard on 1/22/17.
 * model class to hold the tariff data
 */
public class Tariff {

    private String provider;
    private int minValue, maxValue, charge;

    public Tariff(String provider, int minValue, int maxValue, int charge)
    {
        this.provider = provider;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.charge = charge;
    }

    public String getProvider()
    {
        return this.provider;
    }

    public int getMinValue()
    {
        return this.minValue;
    }

    public int getMaxValue()
    {
        return this.maxValue;
    }

    public int getCharge()
    {
        return this.charge;
    }

}
