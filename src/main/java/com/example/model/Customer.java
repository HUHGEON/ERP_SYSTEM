package com.example.model;

public class Customer {
    private int id;
    private String customerName;

    public Customer(int id, String customerName) {
        this.id = id;
        this.customerName = customerName;
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }

    @Override
    public String toString() { return customerName; }
}
