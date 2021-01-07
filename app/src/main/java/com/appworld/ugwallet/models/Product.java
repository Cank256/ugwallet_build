package com.appworld.ugwallet.models;

/**
 * Created by bernard on 12/31/16.
 * model to represent the product entity
 */
public class Product {

    private String code, name, description, type;
    private int price;

    public Product(String code, String name, String description, String type, int price) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

}
