package com.example.delivery.order;

import org.junit.jupiter.api.Test;

import static com.example.delivery.order.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusRulesTest {

    @Test
    void orderMovesForwardOneStepAtATime() {
        assertTrue(OrderStatusRules.canMove(PLACED, ACCEPTED));
        assertTrue(OrderStatusRules.canMove(ACCEPTED, PREPARING));
        assertTrue(OrderStatusRules.canMove(PREPARING, READY));
        assertTrue(OrderStatusRules.canMove(READY, PICKED_UP));
        assertTrue(OrderStatusRules.canMove(PICKED_UP, DELIVERED));
    }

    @Test
    void stepsCannotBeSkipped() {
        assertFalse(OrderStatusRules.canMove(PLACED, DELIVERED));
        assertFalse(OrderStatusRules.canMove(PLACED, PREPARING));
        assertFalse(OrderStatusRules.canMove(ACCEPTED, READY));
    }

    @Test
    void cannotGoBackwards() {
        assertFalse(OrderStatusRules.canMove(READY, PREPARING));
        assertFalse(OrderStatusRules.canMove(DELIVERED, PICKED_UP));
    }

    @Test
    void finishedOrdersCannotMove() {
        assertFalse(OrderStatusRules.canMove(DELIVERED, PLACED));
        assertFalse(OrderStatusRules.canMove(REJECTED, ACCEPTED));
        assertFalse(OrderStatusRules.canMove(CANCELLED, PLACED));
    }
    @Test
    void rejectAndCancelRules() {
        assertTrue(OrderStatusRules.canMove(PLACED, REJECTED));
        assertTrue(OrderStatusRules.canMove(PLACED, CANCELLED));
        assertTrue(OrderStatusRules.canMove(ACCEPTED, CANCELLED));
        assertFalse(OrderStatusRules.canMove(ACCEPTED, REJECTED));
        assertFalse(OrderStatusRules.canMove(PREPARING, CANCELLED));
    }
}
