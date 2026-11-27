package com.loqma.customer.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.loqma.customer.dto.AddCartItemRequest;
import com.loqma.customer.dto.CartItemResponse;
import com.loqma.customer.dto.CartReceiptResponse;
import com.loqma.customer.dto.CartResponse;
import com.loqma.customer.service.CartService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{cartId}")
    public CartResponse getCartItems(@PathVariable String cartId) {
        return cartService.getCartItems(cartId);
    }

    @GetMapping("/{cartId}/receipt")
    public CartReceiptResponse getReceipt(@PathVariable String cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping("/{cartId}")
    public CartItemResponse addItem(@PathVariable String cartId, @RequestBody AddCartItemRequest cartItem) {
        return cartService.addItem(
            cartId,
            cartItem.getMenuItemId(),
            cartItem.getSelectedSizeId(),
            cartItem.getSelectedExtraIds(),
            cartItem.getQuantity(),
            cartItem.getCustomerNote()
        );
    }

    @PatchMapping("/{cartItemId}")
    public CartItemResponse updateQuantity(@PathVariable String cartItemId, @RequestBody Integer quantity) {
        return cartService.updateQuantity(cartItemId, quantity);
    }

    @DeleteMapping("/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCartItem(@PathVariable String cartItemId) {
        cartService.removeItem(cartItemId);
    }
}
