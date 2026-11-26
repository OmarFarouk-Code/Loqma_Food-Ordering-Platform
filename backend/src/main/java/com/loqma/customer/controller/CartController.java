package com.loqma.customer.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loqma.customer.dto.CartResponse;
import com.loqma.customer.service.CartService;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable String cartId) {
        return cartService.getCart(cartId);
    }
}
