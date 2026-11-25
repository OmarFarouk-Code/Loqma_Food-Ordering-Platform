package com.loqma.customer.service;

import org.springframework.stereotype.Component;

@Component
public class CurrentCustomerProvider {

    private static final String FAKE_CUSTOMER_ID = "sample-customer-1";

    public String getCurrentCustomerId() {
        return FAKE_CUSTOMER_ID;
    }
}