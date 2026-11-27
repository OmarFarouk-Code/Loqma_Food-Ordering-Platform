package com.loqma.customer.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentCustomerProvider {

    private static final String FAKE_CUSTOMER_ID = "sample-customer-1";

    public String getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof String userId
                && !"anonymousUser".equals(userId)) {
            return userId;
        }
        return FAKE_CUSTOMER_ID;
    }
}