package com.loqma.customer;

import com.loqma.customer.dto.CartItemResponse;
import com.loqma.customer.entity.Cart;
import com.loqma.customer.entity.CartItem;
import com.loqma.customer.repository.CartItemRepository;
import com.loqma.customer.repository.CartRepository;
import com.loqma.customer.repository.CouponRepository;
import com.loqma.customer.repository.MenuItemRepository;
import com.loqma.customer.service.CartService;
import com.loqma.owner.entity.ItemExtra;
import com.loqma.owner.entity.ItemSize;
import com.loqma.owner.entity.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private MenuItemRepository menuItemRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartItemRepository, cartRepository, couponRepository, menuItemRepository);
    }

    @Test
    void addItemLeavesIdNullBeforePersistAndComputesTotals() {
        Cart cart = Cart.builder()
                .id("cart-1")
                .deliveryFee(20.0)
                .serviceFee(5.0)
                .discount(10.0)
                .build();

        ItemSize large = ItemSize.builder().id("sz-1").sizeName("Large").price(30.0).build();
        ItemExtra cheese = ItemExtra.builder().id("ex-1").name("Cheese").price(15.0).build();
        MenuItem burger = MenuItem.builder()
                .id("item-1")
                .name("Smash Burger")
                .basePrice(100.0)
                .sizes(List.of(large))
                .extras(List.of(cheese))
                .build();

        when(cartRepository.findById("cart-1")).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById("item-1")).thenReturn(Optional.of(burger));
        when(cartItemRepository.saveAndFlush(any(CartItem.class))).thenAnswer(inv -> {
            CartItem item = inv.getArgument(0);
            assertNull(item.getId(), "New CartItem must have null id for JPA persist");
            item.setId("generated-ci-1");
            return item;
        });
        when(cartItemRepository.findByCartId("cart-1")).thenAnswer(inv -> {
            CartItem saved = CartItem.builder()
                    .id("generated-ci-1")
                    .cart(cart)
                    .menuItem(burger)
                    .selectedSize(large)
                    .selectedExtras(List.of(cheese))
                    .quantity(2)
                    .linePrice(290.0)
                    .build();
            return List.of(saved);
        });

        CartItemResponse response = cartService.addItem("cart-1", "item-1", "sz-1", List.of("ex-1"), 2, "No onions");
        assertEquals("generated-ci-1", response.getId());
        assertEquals(290.0, response.getLinePrice());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).saveAndFlush(cartCaptor.capture());
        assertEquals(290.0, cartCaptor.getValue().getSubtotal());
        assertEquals(290.0 * 0.14, cartCaptor.getValue().getTax(), 0.001);
        assertEquals(290.0 + 20.0 + 5.0 + (290.0 * 0.14) - 10.0, cartCaptor.getValue().getTotalPrice(), 0.001);
    }
}
