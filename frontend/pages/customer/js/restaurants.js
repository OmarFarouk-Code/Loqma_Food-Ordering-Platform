// Loqma – Restaurant API client & shared renderer
// API endpoint: GET /api/customer/restaurants
// Response: PagedResponse<RestaurantSummaryDto>
//   { items: [...], page, size, totalItems, totalPages, hasNext }
// DTO shape:
//   { id, name, branch, cuisine, logo, coverImage, address, averageRating,
//     deliveryFee, minimumOrderAmount, prepTime, deliveryTimeMinutes,
//     isOpen, isFavorite, hasDeals }

// ─── API ──────────────────────────────────────────────────────────────────────

/**
 * Fetch paged restaurants from the backend with optional filters.
 * @param {Object} [params]
 * @param {string} [params.cuisine]
 * @param {number} [params.minRating]
 * @param {string} [params.search]
 * @param {boolean} [params.deals]
 * @param {number} [params.lat]
 * @param {number} [params.lng]
 * @param {number} [params.maxDeliveryTime]
 * @param {number} [params.page=0]
 * @param {number} [params.size=12]
 * @returns {Promise<{items: Object[], page: number, size: number, totalItems: number, totalPages: number, hasNext: boolean}>}
 */
async function fetchRestaurants(params = {}) {
  const BASE = (typeof API_BASE_URL !== "undefined") ? API_BASE_URL : "http://localhost:8080";
  const query = new URLSearchParams();

  if (params.cuisine) query.set("cuisine", params.cuisine);
  if (params.minRating != null) query.set("minRating", params.minRating);
  if (params.search) query.set("search", params.search);
  if (params.deals === true) query.set("deals", "true");
  if (params.lat != null && params.lng != null) {
    query.set("lat", params.lat);
    query.set("lng", params.lng);
  }
  if (params.maxDeliveryTime != null) query.set("maxDeliveryTime", params.maxDeliveryTime);
  if (params.page != null) query.set("page", params.page);
  if (params.size != null) query.set("size", params.size);

  const qs = query.toString();
  const url = `${BASE}/api/customer/restaurants${qs ? `?${qs}` : ""}`;

  const res = await fetch(url, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!res.ok) throw new Error(`Failed to load restaurants: ${res.status} ${res.statusText}`);

  const data = await res.json();
  if (Array.isArray(data)) {
    return {
      items: data,
      page: 0,
      size: data.length,
      totalItems: data.length,
      totalPages: 1,
      hasNext: false,
    };
  }
  return {
    items: Array.isArray(data?.items) ? data.items : [],
    page: data?.page ?? 0,
    size: data?.size ?? 10,
    totalItems: data?.totalItems ?? 0,
    totalPages: data?.totalPages ?? 0,
    hasNext: Boolean(data?.hasNext),
  };
}

/**
 * Toggle a restaurant as favourite in the database.
 * @param {string} restaurantId
 * @returns {Promise<{isFavorite: boolean}>}
 */
async function toggleFavorite(restaurantId) {
  const BASE = (typeof API_BASE_URL !== "undefined") ? API_BASE_URL : "http://localhost:8080";
  const res = await fetch(`${BASE}/api/customer/restaurants/${encodeURIComponent(restaurantId)}/favorite`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
  if (!res.ok) throw new Error(`Failed to toggle favourite: ${res.status} ${res.statusText}`);
  return res.json();
}

// ─── Rendering ────────────────────────────────────────────────────────────────

/**
 * Build the HTML for a single restaurant card.
 * @param {Object} r  RestaurantSummaryDto
 * @returns {string}  HTML string
 */
function buildRestaurantCard(r) {
  const ratingHtml = r.averageRating != null
    ? `<div class="restaurant-card__rating">
         <span class="material-symbols-outlined restaurant-card__star">star</span>
         <span class="restaurant-card__rating-value">${Number(r.averageRating).toFixed(1)}</span>
       </div>`
    : "";

  let badgeHtml = "";
  if (r.isOpen === false) {
    badgeHtml = `<div class="restaurant-card__badge restaurant-card__badge--closed">
         <span class="material-symbols-outlined icon-14">schedule</span>
         <span>Closed</span>
       </div>`;
  } else if (r.hasDeals === true) {
    badgeHtml = `<div class="restaurant-card__badge restaurant-card__badge--deal">Deal</div>`;
  }

  let deliveryFeeText = "";
  if (r.deliveryFee != null) {
    deliveryFeeText = Number(r.deliveryFee) === 0 ? "Free delivery" : `EGP ${r.deliveryFee} delivery`;
  }

  let timeText = "";
  if (r.deliveryTimeMinutes != null) {
    timeText = `${r.deliveryTimeMinutes} min`;
  } else if (r.prepTime != null) {
    const prep = Math.round(Number(r.prepTime));
    timeText = `${prep}-${prep + 10} min`;
  }

  const metaDelivery = [timeText, deliveryFeeText].filter(Boolean).join(" · ");
  const locationOrBranch = r.branch || r.address;
  const cuisine = [r.cuisine, locationOrBranch].filter(Boolean).join(", ");

  const imgSrc = (r.coverImage && r.coverImage.trim() !== "")
    ? r.coverImage
    : (r.logo && r.logo.trim() !== "" ? r.logo : "https://placehold.co/400x240?text=Loqma");

  const imgClass = r.isOpen === false
    ? "restaurant-card__image restaurant-card__image--closed"
    : "restaurant-card__image";

  const isFav = Boolean(r.isFavorite);
  const favBtnClass = isFav ? "favorite-button is-favorite" : "favorite-button";
  const favIcon = isFav ? "favorite" : "favorite_border";

  return `
    <div class="col-12 col-sm-6 col-lg-3">
      <div class="restaurant-card" data-id="${escapeHtml(r.id ?? "")}">
        <div class="restaurant-card__image-box">
          <img class="${imgClass}" src="${escapeHtml(imgSrc)}" alt="${escapeHtml(r.name)}" loading="lazy" />
          <button
            class="${favBtnClass}"
            type="button"
            aria-label="Add ${escapeHtml(r.name)} to favourites"
            aria-pressed="${isFav}"
          >
            <span class="material-symbols-outlined favorite-button__icon">${favIcon}</span>
          </button>
          ${badgeHtml}
        </div>
        <div class="restaurant-card__info">
          <div class="restaurant-card__name-row">
            <h3 class="restaurant-card__name text-truncate">${escapeHtml(r.name)}</h3>
            ${ratingHtml}
          </div>
          ${cuisine ? `<p class="restaurant-card__meta text-truncate">${escapeHtml(cuisine)}</p>` : ""}
          ${metaDelivery ? `<p class="restaurant-card__meta">${escapeHtml(metaDelivery)}</p>` : ""}
        </div>
      </div>
    </div>`;
}

/**
 * Render a list of restaurants into a Bootstrap row container.
 * @param {HTMLElement} container  The .row element to populate
 * @param {Object[]}    restaurants
 * @param {Object}      [options]
 * @param {boolean}     [options.append=false]
 */
function renderRestaurants(container, restaurants, { append = false } = {}) {
  if (!container) return;

  if (!append && restaurants.length === 0) {
    container.innerHTML = `
      <div class="col-12">
        <p class="text-center text-muted py-4">No restaurants match your criteria.</p>
      </div>`;
    return;
  }

  const html = restaurants.map(buildRestaurantCard).join("");
  if (append) {
    container.insertAdjacentHTML("beforeend", html);
  } else {
    container.innerHTML = html;
  }
  attachFavoriteListeners(container);
  attachCardClickListeners(container);
}

function attachCardClickListeners(scope) {
  scope.querySelectorAll(".restaurant-card").forEach((card) => {
    if (card.dataset.clickWired === "true") return;
    card.dataset.clickWired = "true";
    card.addEventListener("click", (e) => {
      if (e.target.closest("button, a")) return;
      const id = card.dataset.id;
      if (id) window.location.href = `restaurant.html?id=${encodeURIComponent(id)}`;
    });
  });
}

/**
 * Show a skeleton / loading state inside the container.
 * @param {HTMLElement} container
 * @param {number}      count  Number of placeholder cards to show
 */
function renderSkeletons(container, count = 4) {
  if (!container) return;
  container.innerHTML = Array.from({ length: count }, () => `
    <div class="col-12 col-sm-6 col-lg-3">
      <div class="restaurant-card restaurant-card--skeleton">
        <div class="restaurant-card__image-box skeleton-box"></div>
        <div class="restaurant-card__info">
          <div class="skeleton-line skeleton-line--title mb-2"></div>
          <div class="skeleton-line mb-1"></div>
          <div class="skeleton-line skeleton-line--short"></div>
        </div>
      </div>
    </div>`).join("");
}

/**
 * Show an error message inside the container.
 * @param {HTMLElement} container
 * @param {string}      message
 */
function renderError(container, message) {
  if (!container) return;
  container.innerHTML = `
    <div class="col-12">
      <div class="alert alert-danger d-flex align-items-center gap-2" role="alert">
        <span class="material-symbols-outlined">error</span>
        <span>${escapeHtml(message)}</span>
      </div>
    </div>`;
}

// ─── Shared Search Tabs & Favourite button wiring ─────────────────────────────

function setupSearchTabs() {
  const restaurantsTab = document.getElementById("tab-restaurants");
  const dishesTab = document.getElementById("tab-dishes");
  const searchInput = document.getElementById("main-search-input");

  if (!restaurantsTab || !dishesTab || !searchInput) return;

  function selectTab(selectedTab, otherTab, placeholderText) {
    selectedTab.classList.add("search-tabs__tab--active");
    otherTab.classList.remove("search-tabs__tab--active");
    searchInput.placeholder = placeholderText;
  }

  restaurantsTab.addEventListener("click", () => {
    selectTab(restaurantsTab, dishesTab, "Search restaurants");
  });

  dishesTab.addEventListener("click", () => {
    selectTab(dishesTab, restaurantsTab, "Search dishes or ingredients");
  });
}

/**
 * Wire up favourite-toggle behaviour for every button inside `scope`.
 * @param {HTMLElement} scope
 */
function setFavoriteButtonState(btn, isFavorite) {
  btn.classList.toggle("is-favorite", isFavorite);
  btn.setAttribute("aria-pressed", String(isFavorite));
  const icon = btn.querySelector(".favorite-button__icon");
  if (icon) icon.textContent = isFavorite ? "favorite" : "favorite_border";
}

function attachFavoriteListeners(scope) {
  scope.querySelectorAll(".favorite-button").forEach((btn) => {
    if (btn.dataset.favWired === "true") return;
    btn.dataset.favWired = "true";
    btn.addEventListener("click", async (e) => {
      e.stopPropagation();
      const card = btn.closest(".restaurant-card");
      const restaurantId = card?.dataset.id;
      const previous = btn.classList.contains("is-favorite");
      const optimistic = !previous;

      setFavoriteButtonState(btn, optimistic);

      if (!restaurantId) return;

      btn.disabled = true;
      try {
        const result = await toggleFavorite(restaurantId);
        const confirmed = Boolean(result?.isFavorite);
        setFavoriteButtonState(btn, confirmed);
        document.dispatchEvent(new CustomEvent("restaurant-favorite-toggled", {
          detail: { restaurantId, isFavorite: confirmed },
        }));
      } catch (err) {
        console.error("[Loqma] Could not save favourite:", err);
        setFavoriteButtonState(btn, previous);
      } finally {
        btn.disabled = false;
      }
    });
  });
}

// ─── Search / filter ──────────────────────────────────────────────────────────

/**
 * Filter a restaurant list by a query string (name, cuisine, or branch).
 * @param {Object[]} restaurants
 * @param {string}   query
 * @returns {Object[]}
 */
function filterRestaurants(restaurants, query) {
  const q = query.trim().toLowerCase();
  if (!q) return restaurants;
  return restaurants.filter(
    (r) =>
      (r.name || "").toLowerCase().includes(q) ||
      (r.cuisine || "").toLowerCase().includes(q) ||
      (r.branch || "").toLowerCase().includes(q)
  );
}

// ─── Utility ──────────────────────────────────────────────────────────────────

function escapeHtml(str) {
  if (str == null) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

// ─── Homepage loader ──────────────────────────────────────────────────────────

/**
 * Load restaurants from the API and populate one or more grid containers on the homepage.
 *
 * @param {Object} options
 * @param {string|string[]} options.containerIds  One or more IDs of .row elements to fill.
 * @param {number}  [options.featuredCount=4]  How many cards go in the first container.
 */
async function loadRestaurantsIntoPage({ containerIds = [], featuredCount = 4 } = {}) {
  const ids = Array.isArray(containerIds) ? containerIds : [containerIds];
  const containers = ids.map((id) => document.getElementById(id)).filter(Boolean);

  if (containers.length === 0) return;

  containers.forEach((c) => renderSkeletons(c));

  try {
    const paged = await fetchRestaurants({ page: 0, size: 12 });
    const all = paged.items;

    if (containers.length === 1) {
      renderRestaurants(containers[0], all);
    } else {
      renderRestaurants(containers[0], all.slice(0, featuredCount));
      renderRestaurants(containers[1], all.slice(featuredCount));
    }

    // Wire live search & submit redirect to restaurant-list.html
    const searchInput = document.getElementById("main-search-input");
    const headerSearchInput = document.querySelector(".header-search__input");
    const searchSubmitBtn = document.querySelector(".hero-search__submit");

    function handleSearch(query) {
      const filtered = filterRestaurants(all, query);
      if (containers.length === 1 || query.trim() !== "") {
        renderRestaurants(containers[0], filtered);
        if (containers[1]) {
          renderRestaurants(containers[1], query.trim() ? [] : all.slice(featuredCount));
        }
      } else {
        renderRestaurants(containers[0], all.slice(0, featuredCount));
        renderRestaurants(containers[1], all.slice(featuredCount));
      }
    }

    function redirectToList(query) {
      const q = (query || "").trim();
      window.location.href = q
        ? `restaurant-list.html?search=${encodeURIComponent(q)}`
        : "restaurant-list.html";
    }

    if (searchInput) {
      searchInput.addEventListener("input", (e) => handleSearch(e.target.value));
      searchInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          redirectToList(searchInput.value);
        }
      });
    }
    if (searchSubmitBtn && searchInput) {
      searchSubmitBtn.addEventListener("click", () => redirectToList(searchInput.value));
    }
    if (headerSearchInput) {
      headerSearchInput.addEventListener("input", (e) => handleSearch(e.target.value));
      headerSearchInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          redirectToList(headerSearchInput.value);
        }
      });
    }
  } catch (err) {
    console.error("[Loqma] Could not load restaurants:", err);
    containers.forEach((c) => renderError(c, "Could not load restaurants. Please try again later."));
  }
}
