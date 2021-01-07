package com.appworld.ugwallet.models;

/**
 * model that represents the service provider entity
 */
public class Provider {

    private String code, name, waitMessage;

    public Provider(String code, String name, String message)
    {
        this.code = code;
        this.name = name;
        this.waitMessage = message;
    }

    public String getCode()
    {
        return this.code;
    }

    public String getName()
    {
        return this.name;
    }

    public String getWaitMessage()
    {
        return this.waitMessage;
    }

}
