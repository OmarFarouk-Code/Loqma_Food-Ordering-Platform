document.addEventListener("DOMContentLoaded", () => {
    const categoryTabs = document.getElementById("category-tabs");
    categoryTabs.addEventListener("click", event => {
        const selectedTab = event.target.closest(".category-tab");
        if (!selectedTab) return;

        categoryTabs.querySelectorAll(".category-tab").forEach(tab => {
            tab.classList.toggle("category-tab--active", tab === selectedTab);
        });
    });

    const params = new URLSearchParams(window.location.search);
    const restaurantId = params.get("id") || params.get("restaurantId");
    if (!restaurantId) {
        const menuSections = document.getElementById("menu-sections");
        if (menuSections) {
            menuSections.innerHTML = `<p class="text-muted py-4">No restaurant selected.</p>`;
        }
        return;
    }

    loadMenuItems(restaurantId);
    loadReviews(restaurantId);
});

const baseUrl = (typeof API_BASE_URL !== "undefined") ? API_BASE_URL : "http://localhost:8080";
const restaurantURL = `${baseUrl}/api/customer/restaurants`;
const menuURL = `${baseUrl}/api/restaurants`;

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

async function loadMenuItems(restaurantId) {
    const categoryTabs = document.getElementById("category-tabs");
    const menuSections = document.getElementById("menu-sections");
    categoryTabs.innerHTML = "";
    menuSections.innerHTML = "";

    try {
        const response = await fetch(`${menuURL}/${encodeURIComponent(restaurantId)}/menu`, {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });
        if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
        const menuItems = await response.json();
        const categories = Array.isArray(menuItems?.categories) ? menuItems.categories : [];

        if (categories.length === 0) {
            menuSections.innerHTML = `<p class="text-muted py-4">No menu items available yet.</p>`;
            return;
        }

        let itemCounter = 1;
        categories.forEach((category, index) => {
            const catSlug = `category-${index}`;
            const catName = escapeHtml(category.name);
            categoryTabs.innerHTML += `<a href="#${catSlug}" class="category-tab${index === 0 ? " category-tab--active" : ""}">${catName}</a>`;

            menuSections.innerHTML += `
            <section class="menu-section" id="${catSlug}">
                <h2 class="menu-section__title" id="${catSlug}-title">${catName}</h2>
                <div class="menu-grid" id="${catSlug}-grid"></div>
            </section>
            `;

            const categoryList = document.getElementById(`${catSlug}-grid`);
            (category.items || []).forEach(menuItem => {
                const count = itemCounter++;
                const desc = escapeHtml(menuItem.description ?? "");
                const photoAttr = menuItem.photo ? ` src="${escapeHtml(menuItem.photo)}"` : "";
                categoryList.innerHTML += `
                <article class="menu-card" id="menu-item-${count}" data-item-id="${escapeHtml(menuItem.id)}">
                <div class="menu-card__body">
                    <div>
                    <div class="menu-card__name-row">
                        <h3 class="menu-card__name" id="menu-item-${count}-name">${escapeHtml(menuItem.name)}</h3>
                    </div>
                    <p class="menu-card__desc" id="menu-item-${count}-description">${desc}</p>
                    </div>
                    <p class="menu-card__price" id="menu-item-${count}-price">${escapeHtml(menuItem.price)} EGP</p>
                </div>
                <div class="menu-card__image-box">
                    <img class="menu-card__image" id="menu-item-${count}-image"${photoAttr} alt="${escapeHtml(menuItem.name)}">
                    <button type="button" class="menu-card__add" id="menu-item-${count}-add"><span class="material-symbols-outlined icon-20">add</span></button>
                </div>
                </article>
                `;
            });
        });
    } catch (err) {
        console.error("[Loqma] Could not load menu:", err);
        menuSections.innerHTML = `<p class="text-danger py-4">Could not load menu. Please try again later.</p>`;
    }
}


async function loadReviews(restaurantId) {
    try {
        const response = await fetch(`${restaurantURL}/${encodeURIComponent(restaurantId)}/reviews`, {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });
        if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
        const restaurantReviews = await response.json();

        const avgNum = restaurantReviews.averageRating != null ? Number(restaurantReviews.averageRating) : 0;
        const averageRating = Number.isFinite(avgNum) ? avgNum : 0;
        const totalReviews = restaurantReviews.totalReviews ?? 0;

        document.getElementById("reviews-average").innerText = averageRating.toFixed(1);
        document.getElementById("reviews-average-mobile").innerText = averageRating.toFixed(1);

        document.getElementById("reviews-stars").style = `--rating:${averageRating}`;
        document.getElementById("reviews-stars").ariaLabel = `${averageRating} out of 5`;

        document.getElementById("reviews-total").innerText = totalReviews;
        document.getElementById("reviews-total-mobile").innerText = totalReviews;

        for (let i = 5; i > 0; i--) {
            const width = totalReviews > 0 ? Math.round(((restaurantReviews.ratingCounts?.[i] ?? 0) / totalReviews) * 100) : 0;
            document.getElementById(`rating-${i}-bar`).style = `width:${width}%`;
            document.getElementById(`rating-${i}-percent`).innerText = `${width}%`;
        }

        const reviewList = document.getElementById("review-list");
        reviewList.innerHTML = "";
        const items = restaurantReviews.reviews?.items || [];
        items.forEach((review, count) => {
            reviewList.innerHTML += `
                <article class="review" id="review-${count}">
                    <div class="review__head"><span id="review-${count}-author">${escapeHtml(review.customerName)}</span><span class="review__date" id="review-${count}-date">${escapeHtml(review.createdAt ?? "")}</span></div>
                    <span class="stars" id="review-${count}-stars" style="--rating:${Number(review.rating) || 0}" aria-label="${escapeHtml(review.rating)} out of 5"></span>
                    <p class="review__text" id="review-${count}-text">${escapeHtml(review.comment ?? "")}</p>
                </article>
            `;
        });
    } catch (err) {
        console.error("[Loqma] Could not load reviews:", err);
    }
}

