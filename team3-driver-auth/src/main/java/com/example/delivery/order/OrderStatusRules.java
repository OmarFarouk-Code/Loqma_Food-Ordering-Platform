package com.example.delivery.order;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.example.delivery.order.OrderStatus.*;

public final class OrderStatusRules {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(PLACED,    EnumSet.of(ACCEPTED, REJECTED, CANCELLED));
        ALLOWED.put(ACCEPTED,  EnumSet.of(PREPARING, CANCELLED));
        ALLOWED.put(PREPARING, EnumSet.of(READY));
        ALLOWED.put(READY,     EnumSet.of(PICKED_UP));
        ALLOWED.put(PICKED_UP, EnumSet.of(DELIVERED));
        ALLOWED.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(REJECTED,  EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private OrderStatusRules() {}

    public static boolean canMove(OrderStatus from, OrderStatus to) {
        return ALLOWED.get(from).contains(to);
    }

    public static Set<OrderStatus> nextOptions(OrderStatus from) {
        return ALLOWED.get(from);
    }
}
