package com.loqma.customer.service;

import com.loqma.customer.repository.CouponRepository;
import com.loqma.order.entity.Coupon;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.loqma.customer.dto.CartResponse;
import com.loqma.customer.dto.ExtraDto;
import com.loqma.customer.entity.CartItem;
import com.loqma.customer.repository.CartItemRepository;
import com.loqma.customer.repository.CartRepository;
import com.loqma.owner.entity.ItemExtra;
import com.loqma.owner.entity.MenuItem;
import com.loqma.customer.entity.Cart;
import com.loqma.owner.entity.ItemSize;

@Service 
public class CartService {
    private final CouponRepository couponRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    public CartService(CartItemRepository cartItemRepository, CartRepository cartRepository, CouponRepository couponRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.couponRepository = couponRepository;
    }

    private String generateCartItemId() {
        // Implement your logic to generate a unique ID for the cart item
        return java.util.UUID.randomUUID().toString();
    }

    public CartResponse getCart(String cartId) {
        if (cartId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CartID can't be null");
        }

        //Check if cart already exists
        if(!cartRepository.existsById(cartId)) {
            //Create Cart
            Cart cart = new Cart();
            cart.setId(cartId);
            cartRepository.save(cart);
        }

        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        return CartResponse.builder().cartItems(items).build();
    }

    public void addItem(Cart cart, MenuItem menuItem, ItemSize size, List<ItemExtra> extras, int quantity, String note) {
        Double linePrice = menuItem.getBasePrice();

        if (size != null) {
            linePrice += size.getPrice();
        }

        if (extras != null) {
            for (ItemExtra extra : extras) {
                linePrice += extra.getPrice();
            }
        }

        linePrice *= quantity;

        CartItem cartItem = new CartItem(generateCartItemId(), cart, menuItem, size, extras, quantity, note, linePrice);

        cartItemRepository.save(cartItem);
    }

    public void updateQuantity(String cartItemId, Integer newQuantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
            Double linePrice = cartItem.getMenuItem().getBasePrice();

            if (cartItem.getSelectedSize() != null) {
                linePrice += cartItem.getSelectedSize().getPrice();
            }

            if (cartItem.getSelectedExtras() != null) {
                for (ItemExtra extra : cartItem.getSelectedExtras()) {
                    linePrice += extra.getPrice();
                }
            }

            linePrice *= newQuantity;
            cartItem.setLinePrice(linePrice);

            cartItemRepository.save(cartItem);
        }
    }

    public void removeItem(String cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(String cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        cartItemRepository.deleteAll(items);
    }

    public Double applyCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found");
        } else if (coupon.getExpiryDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Coupon Expired");
        } else {
            return coupon.getDiscountAmount();
        }
    }

    // TODO public void validateMinimumOrder()
}
