// Loqma – Restaurant API client & homepage renderer
// API endpoint: GET /api/customer/restaurants
// DTO shape: { id, name, branch, cuisine, logo, averageRating,
//              deliveryFee, minimumOrderAmount, prepTime, isOpen }

// ─── API ──────────────────────────────────────────────────────────────────────

/**
 * Fetch all restaurants from the backend.
 * @returns {Promise<RestaurantSummaryDto[]>}
 */
async function fetchRestaurants() {
  const BASE = (typeof API_BASE_URL !== "undefined") ? API_BASE_URL : "http://localhost:8080";
  const res = await fetch(`${BASE}/api/customer/restaurants`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });
  if (!res.ok) throw new Error(` Failed to load restaurants: ${res.status} ${res.statusText}`);
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
         <span class="restaurant-card__rating-value">${r.averageRating.toFixed(1)}</span>
       </div>`
    : "";

  const badgeHtml = r.isOpen === false
    ? `<div class="restaurant-card__badge restaurant-card__badge--closed">
         <span class="material-symbols-outlined icon-14">schedule</span>
         <span>Closed</span>
       </div>`
    : "";

  const deliveryFeeText = r.deliveryFee != null ? `EGP ${r.deliveryFee} delivery` : "";
  const prepTimeText    = r.prepTime    != null ? `${r.prepTime}–${r.prepTime + 10} min` : "";
  const metaDelivery    = [prepTimeText, deliveryFeeText].filter(Boolean).join("  ");

  const cuisine = [r.cuisine, r.branch].filter(Boolean).join(", ");

  const imgSrc = r.logo && r.logo.trim() !== ""
    ? r.logo
    : "https://placehold.co/400x240?text=No+Image";

  const imgClass = r.isOpen === false
    ? "restaurant-card__image restaurant-card__image--closed"
    : "restaurant-card__image";

  return `
    <div class="col-12 col-sm-6 col-lg-3">
      <div class="restaurant-card" data-id="${r.id ?? ""}">
        <div class="restaurant-card__image-box">
          <img class="${imgClass}" src="${imgSrc}" alt="${escapeHtml(r.name)}" loading="lazy" />
          <button
            class="favorite-button"
            type="button"
            aria-label="Add ${escapeHtml(r.name)} to favourites"
            aria-pressed="false"
          >
            <span class="material-symbols-outlined favorite-button__icon">favorite_border</span>
          </button>
          ${badgeHtml}
        </div>
        <div class="restaurant-card__info">
          <div class="restaurant-card__name-row">
            <h3 class="restaurant-card__name text-truncate">${escapeHtml(r.name)}</h3>
            ${ratingHtml}
          </div>
          ${cuisine ? `<p class="restaurant-card__meta text-truncate">${escapeHtml(cuisine)}</p>` : ""}
          ${metaDelivery ? `<p class="restaurant-card__meta">${metaDelivery}</p>` : ""}
        </div>
      </div>
    </div>`;
}

/**
 * Render a list of restaurants into a Bootstrap row container.
 * @param {HTMLElement} container  The .row element to populate
 * @param {Object[]}    restaurants
 */
function renderRestaurants(container, restaurants) {
  if (!container) return;

  if (restaurants.length === 0) {
    container.innerHTML = `
      <div class="col-12">
        <p class="text-center text-muted py-4">No restaurants available right now.</p>
      </div>`;
    return;
  }

  container.innerHTML = restaurants.map(buildRestaurantCard).join("");
  attachFavoriteListeners(container);
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

// ─── Favourite button wiring ──────────────────────────────────────────────────

/**
 * Wire up favourite-toggle behaviour for every button inside `scope`.
 * @param {HTMLElement} scope
 */
function attachFavoriteListeners(scope) {
  scope.querySelectorAll(".favorite-button").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const isFavorite = btn.classList.toggle("is-favorite");
      btn.setAttribute("aria-pressed", isFavorite);
      const icon = btn.querySelector(".favorite-button__icon");
      if (icon) icon.textContent = isFavorite ? "favorite" : "favorite_border";
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
      (r.name     || "").toLowerCase().includes(q) ||
      (r.cuisine  || "").toLowerCase().includes(q) ||
      (r.branch   || "").toLowerCase().includes(q)
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

// ─── Main loader ──────────────────────────────────────────────────────────────

/**
 * Load restaurants from the API and populate one or more grid containers.
 *
 * @param {Object} options
 * @param {string|string[]} options.containerIds  One or more IDs of .row elements to fill.
 *   If a single string is given, all containers receive the full list.
 *   Pass an array to split [featured, nearYou] from the same fetch.
 * @param {number}  [options.featuredCount=4]  How many cards go in the first container.
 */
async function loadRestaurantsIntoPage({ containerIds = [], featuredCount = 4 } = {}) {
  const ids = Array.isArray(containerIds) ? containerIds : [containerIds];
  const containers = ids.map((id) => document.getElementById(id)).filter(Boolean);

  if (containers.length === 0) return;

  // Show skeletons while loading
  containers.forEach((c) => renderSkeletons(c));

  try {
    const all = await fetchRestaurants();

    if (containers.length === 1) {
      renderRestaurants(containers[0], all);
    } else {
      // First container → featured (first N)
      renderRestaurants(containers[0], all.slice(0, featuredCount));
      // Second container → the rest (popular / near you)
      renderRestaurants(containers[1], all.slice(featuredCount));
    }

    // Wire live search if the hero input is present
    const searchInput = document.getElementById("main-search-input");
    const headerSearchInput = document.querySelector(".header-search__input");

    function handleSearch(query) {
      const filtered = filterRestaurants(all, query);
      containers.forEach((c) => renderRestaurants(c, filtered));
    }

    if (searchInput) {
      searchInput.addEventListener("input", (e) => handleSearch(e.target.value));
    }
    if (headerSearchInput) {
      headerSearchInput.addEventListener("input", (e) => handleSearch(e.target.value));
    }
  } catch (err) {
    console.error("[Loqma] Could not load restaurants:", err);
    containers.forEach((c) => renderError(c, "Could not load restaurants. Please try again later."));
  }
}
