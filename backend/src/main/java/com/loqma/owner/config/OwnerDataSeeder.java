package com.loqma.owner.config;

import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import com.loqma.owner.entity.ItemExtra;
import com.loqma.owner.entity.ItemSize;
import com.loqma.owner.entity.MenuItem;
import com.loqma.owner.entity.Restaurant;
import com.loqma.owner.entity.RestaurantOwner;
import com.loqma.owner.repository.OwnerMenuItemRepository;
import com.loqma.owner.repository.OwnerRepository;
import com.loqma.owner.repository.OwnerRestaurantRepository;
import com.loqma.owner.service.CurrentOwnerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Creates one test owner, one restaurant and a few menu items. Runs once (skips if already there). */
@Component
@RequiredArgsConstructor
public class OwnerDataSeeder implements CommandLineRunner {

    private final OwnerRepository ownerRepository;
    private final OwnerRestaurantRepository restaurantRepository;
    private final OwnerMenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (ownerRepository.findByEmail(CurrentOwnerProvider.FAKE_OWNER_EMAIL).isPresent()) {
            return;
        }

        RestaurantOwner owner = new RestaurantOwner();
        owner.setName("Test Owner");
        owner.setEmail(CurrentOwnerProvider.FAKE_OWNER_EMAIL);
        owner.setPhone("01000000001");
        owner.setPassword(passwordEncoder.encode("Test1234!"));
        owner.setRole(UserRole.RESTAURANT_OWNER);
        owner.setStatus(AccountStatus.ACTIVE);
        owner.setRestaurantName("Loqma Test Kitchen");
        owner.setBranch("Nasr City");
        owner = ownerRepository.save(owner);

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name("Loqma Test Kitchen")
                .branch("Nasr City")
                .cuisine("Egyptian")
                .address("12 Abbas El Akkad St, Nasr City")
                .phone("01000000002")
                .deliveryFee(20.0)
                .minimumOrderAmount(50.0)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        MenuItem pizza = item(restaurant, "Mains", "Margherita Pizza", "Tomato, mozzarella, basil", 90.0, true);
        pizza.getSizes().add(ItemSize.builder().menuItem(pizza).sizeName("Large").price(130.0).build());
        pizza.getExtras().add(ItemExtra.builder().menuItem(pizza).name("Extra cheese").price(15.0).build());

        menuItemRepository.saveAll(List.of(
                item(restaurant, "Starters", "Hummus", "Chickpea dip with tahini", 35.0, true),
                item(restaurant, "Starters", "Baba Ghanoush", "Smoked eggplant dip", 40.0, false),
                pizza,
                item(restaurant, "Mains", "Koshary", "Rice, lentils, pasta, tomato sauce", 45.0, true),
                item(restaurant, "Mains", "Grilled Chicken", "Half chicken with rice", 120.0, false),
                item(restaurant, "Drinks", "Fresh Orange Juice", "Squeezed to order", 30.0, true),
                item(restaurant, "Drinks", "Mint Lemonade", "Lemon and fresh mint", 28.0, true),
                item(restaurant, "Desserts", "Om Ali", "Warm milk and pastry dessert", 50.0, true)
        ));
    }

    private MenuItem item(Restaurant r, String category, String name, String desc,
                          double price, boolean available) {
        return MenuItem.builder()
                .restaurant(r)
                .category(category)
                .name(name)
                .description(desc)
                .basePrice(price)
                .isAvailable(available)
                .build();
    }
}