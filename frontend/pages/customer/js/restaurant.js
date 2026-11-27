document.addEventListener("DOMContentLoaded", () => {
    const categoryTabs = document.getElementById("category-tabs");
    categoryTabs.addEventListener("click", event => {
        const selectedTab = event.target.closest(".category-tab");
        if (!selectedTab) return;

        categoryTabs.querySelectorAll(".category-tab").forEach(tab => {
            tab.classList.toggle("category-tab--active", tab === selectedTab);
        });
    });

    loadMenuItems(); //requires restaurant id as parameter
    loadReviews(); //requires restaurant id as parameter
})

const restaurantURL = `http://127.0.0.1:8080/api/customer/restaurants`;
const menuURL = `http://127.0.0.1:8080/api/restaurants`;


async function loadMenuItems(/*restaurantId*/) {
    const categoryTabs = document.getElementById("category-tabs");
    const menuSections = document.getElementById("menu-sections");
    menuSections.innerHTML = ""; // TODO: include failed to load menu message if GET fails

    const response = await fetch(menuURL/*+`/${restaurantId}/menu`*/, {
        method: "GET",
        headers: { "Content-Type": "application/json"}
    });
    const menuItems = await response.json();

    // console.log(menuItems);

    menuItems.categories.forEach((category, index) => {
        categoryTabs.innerHTML += `<a href="#category-${category.name}" class="category-tab${index === 0 ? " category-tab--active" : ""}">${category.name}</a>`

        menuSections.innerHTML += `
        <section class="menu-section" id="category-${category.name}">
            <h2 class="menu-section__title" id="category-${category.name}-title">${category.name}</h2>
            <div class="menu-grid" id="${category.name}"></div>
        </section>
        `;
        
        const categoryList = document.getElementById(`${category.name}`);
        let count = 1;
        category.items.forEach(menuItem => {
            if(menuItem.description == null) menuItem.description = ""
            categoryList.innerHTML += `
            <article class="menu-card" id="menu-item-${count}" data-item-id="${menuItem.id}">
            <div class="menu-card__body">
                <div>
                <div class="menu-card__name-row">
                    <h3 class="menu-card__name" id="menu-item-${count}-name">${menuItem.name}</h3>
                </div>
                <p class="menu-card__desc" id="menu-item-${count}-description">${menuItem.description}</p>
                </div>
                <p class="menu-card__price" id="menu-item-${count}-price">${menuItem.price} EGP</p>
            </div>
            <div class="menu-card__image-box">
                <img class="menu-card__image" id="menu-item-${count}-image">
                <button type="button" class="menu-card__add" id="menu-item-${count}-add"><span class="material-symbols-outlined icon-20">add</span></button>
            </div>
            </article>
            `;
            count++;
        })
    });
}


async function loadReviews(/*resaurantId*/) {
    const response = await fetch(restaurantURL+/*`/restaurantId*/`/reviews`, {
        method: "GET",
        headers: { "Content-Type": "application/json"}
    });
    const restaurantReviews = await response.json();

    const averageRating = restaurantReviews.averageRating;

    document.getElementById("reviews-average").innerText = averageRating;
    document.getElementById("reviews-average-mobile").innerText = averageRating

    document.getElementById("reviews-stars").style = `--rating:${averageRating}`;
    document.getElementById("reviews-stars").ariaLabel = `${averageRating} out of 5`;

    document.getElementById("reviews-total").innerText = restaurantReviews.totalReviews;
    document.getElementById("reviews-total-mobile").innerText = restaurantReviews.totalReviews;

    for (var i = 5; i > 0; i--) {
        let width = restaurantReviews.totalReviews > 0 ? Math.round(((restaurantReviews.ratingCounts?.[i] ?? 0) / restaurantReviews.totalReviews) * 100) : 0; // TODO: may need to catch an exception, then let width=0
        document.getElementById(`rating-${i}-bar`).style = `width:${width}%`;
        document.getElementById(`rating-${i}-percent`).innerText = `${width}%`
    }

    const reviewList = document.getElementById("review-list");
    let count = 0;
    restaurantReviews.reviews.items.forEach(review => {
        reviewList.innerHTML += `
            <article class="review" id="review-${count}">
                <div class="review__head"><span id="review-${count}-author">${review.customerName}</span><span class="review__date" id="review-${count}-date">${review.createdAt}</span></div>
                <span class="stars" id="review-${count}-stars" style="--rating:${review.rating}" aria-label="${review.rating} out of 5"></span>
                <p class="review__text" id="review-${count}-text">${review.comment}</p>
            </article>
        `;

        count++;
    });
}

