package com.loqma.customer.service;

import com.loqma.customer.repository.CouponRepository;
import com.loqma.order.entity.Coupon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loqma.customer.dto.CartExtraResponse;
import com.loqma.customer.dto.CartItemResponse;
import com.loqma.customer.dto.CartReceiptResponse;
import com.loqma.customer.dto.CartResponse;
import com.loqma.customer.entity.CartItem;
import com.loqma.customer.repository.CartItemRepository;
import com.loqma.customer.repository.CartRepository;
import com.loqma.customer.repository.MenuItemRepository;
import com.loqma.owner.entity.ItemExtra;
import com.loqma.owner.entity.MenuItem;
import com.loqma.customer.entity.Cart;
import com.loqma.owner.entity.ItemSize;

@Service 
public class CartService {
    private final CouponRepository couponRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(CartItemRepository cartItemRepository, CartRepository cartRepository, CouponRepository couponRepository, MenuItemRepository menuItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.couponRepository = couponRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCartItems(String cartId) {
        if (cartId == null || cartId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CartID can't be null");
        }

        // A cart cannot be created here because it must be associated with a customer.
        if(!cartRepository.existsById(cartId)) {
            return CartResponse.builder().cartItems(List.of()).build();
        }

        List<CartItemResponse> items = cartItemRepository.findByCartId(cartId).stream()
            .map(this::toResponse)
            .toList();
        return CartResponse.builder().cartItems(items).build();
    }

    @Transactional
    public CartItemResponse addItem(String cartId, String menuItemId, String sizeId, List<String> extraIds, Integer quantity, String note) {
        if (menuItemId == null || menuItemId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
        MenuItem persistedMenuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));

        ItemSize persistedSize = null;
        if (sizeId != null && !sizeId.isBlank()) {
            persistedSize = persistedMenuItem.getSizes().stream()
                .filter(itemSize -> itemSize.getId().equals(sizeId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected size does not belong to menu item"));
        }

        List<ItemExtra> persistedExtras = new ArrayList<>();
        if (extraIds != null && !extraIds.isEmpty()) {
            persistedExtras = new ArrayList<>(extraIds.stream()
                .map(extraId -> persistedMenuItem.getExtras().stream()
                    .filter(itemExtra -> itemExtra.getId().equals(extraId))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected extra does not belong to menu item")))
                .toList());
        }

        Double linePrice = persistedMenuItem.getBasePrice();
        if (persistedSize != null) {
            linePrice += persistedSize.getPrice();
        }
        for (ItemExtra extra : persistedExtras) {
            linePrice += extra.getPrice();
        }
        linePrice *= quantity;

        CartItem cartItem = CartItem.builder()
            .cart(cart)
            .menuItem(persistedMenuItem)
            .selectedSize(persistedSize)
            .selectedExtras(persistedExtras)
            .quantity(quantity)
            .customerNote(note)
            .linePrice(linePrice)
            .build();
        CartItem savedCartItem = cartItemRepository.saveAndFlush(cartItem);
        calculateTotals(cart);
        return toResponse(savedCartItem);
    }

    @Transactional
    public CartItemResponse updateQuantity(String cartItemId, Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

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
        cartItemRepository.saveAndFlush(cartItem);
        calculateTotals(cartItem.getCart());
        return toResponse(cartItem);
    }

    @Transactional
    public void removeItem(String cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        calculateTotals(cart);
    }

    @Transactional
    public void clearCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        cartItemRepository.deleteAll(items);
        calculateTotals(cart);
    }

    public Double applyCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found");
        } else if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon is not active");
        } else if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Coupon Expired");
        } else {
            return coupon.getDiscountAmount();
        }
    }

    // Also saves and flushes to database
    private void calculateTotals(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        double subtotal = 0.0;

        for (CartItem item : items) {
            subtotal += item.getLinePrice();
        }

        double tax = 0.14 * subtotal;
        double deliveryFee = cart.getDeliveryFee() == null ? 0.0 : cart.getDeliveryFee();
        double serviceFee = cart.getServiceFee() == null ? 0.0 : cart.getServiceFee();
        double discount = cart.getDiscount() == null ? 0.0 : cart.getDiscount();

        cart.setSubtotal(subtotal);
        cart.setTax(tax);
        cart.setTotalPrice(Math.max(0.0, subtotal + deliveryFee + serviceFee + tax - discount));

        cartRepository.saveAndFlush(cart);
    }

    @Transactional(readOnly = true)
    public CartReceiptResponse getCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
        List<CartItemResponse> items = cartItemRepository.findByCartId(cartId).stream()
            .map(this::toResponse)
            .toList();
        return CartReceiptResponse.builder()
            .id(cart.getId())
            .items(items)
            .subtotal(cart.getSubtotal())
            .deliveryFee(cart.getDeliveryFee())
            .serviceFee(cart.getServiceFee())
            .tax(cart.getTax())
            .discount(cart.getDiscount())
            .totalPrice(cart.getTotalPrice())
            .minimumOrderAmount(cart.getMinimumOrderAmount())
            .build();
    }

    private CartItemResponse toResponse(CartItem item) {
        List<CartExtraResponse> extras = item.getSelectedExtras().stream()
            .map(extra -> CartExtraResponse.builder()
                .id(extra.getId())
                .name(extra.getName())
                .price(extra.getPrice())
                .build())
            .toList();

        return CartItemResponse.builder()
            .id(item.getId())
            .menuItemId(item.getMenuItem().getId())
            .menuItemName(item.getMenuItem().getName())
            .selectedSizeId(item.getSelectedSize() == null ? null : item.getSelectedSize().getId())
            .selectedSizeName(item.getSelectedSize() == null ? null : item.getSelectedSize().getSizeName())
            .selectedExtras(extras)
            .quantity(item.getQuantity())
            .customerNote(item.getCustomerNote())
            .linePrice(item.getLinePrice())
            .build();
    }
}
