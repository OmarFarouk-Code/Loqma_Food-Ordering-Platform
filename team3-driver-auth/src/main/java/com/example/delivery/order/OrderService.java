package com.example.delivery.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // THE shared code: every team moves an order through this one method
    @Transactional
    public Order moveToStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!OrderStatusRules.canMove(order.getStatus(), newStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move order from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
