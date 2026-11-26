// Loqma - Restaurant List toolbar (tabs, sort, filter chips, search, pagination)
// Connects to GET /api/customer/restaurants via restaurants.js

const DEFAULT_COORDS = { lat: 30.0444, lng: 31.2357 }; // Cairo fallback for time/distance filtering
const PAGE_SIZE = 12;

const filterState = {
  tab: "all", // "all" | "favourites"
  sort: "rating_desc", // "rating_desc" | "time_asc" | "distance_asc"
  cuisine: "", // "" means all cuisines
  minRating: null, // 4.0 | 4.5 | null
  maxTime: null, // 30 | 45 | null
  deals: false,
  search: "",
  lat: null,
  lng: null,
  page: 0,
};

let loadedItems = [];
let activeRequestToken = 0;

document.addEventListener("DOMContentLoaded", () => {
  applyInitialUrlParams();
  if (typeof setupSearchTabs === "function") setupSearchTabs();
  setupTabs();
  setupSortMenu();
  setupCuisineMenu();
  setupToggleChips();
  setupSearchInputs();
  setupLoadMore();
  requestGeolocation();

  document.addEventListener("restaurant-filters-change", () => {
    filterState.page = 0;
    loadRestaurantList({ append: false });
  });

  document.addEventListener("restaurant-favorite-toggled", (e) => {
    const { restaurantId, isFavorite } = e.detail || {};
    const target = loadedItems.find((r) => r.id === restaurantId);
    if (target) target.isFavorite = isFavorite;
    setFavouritesCount(loadedItems.filter((r) => r.isFavorite).length);
    if (filterState.tab === "favourites") {
      const grid = document.getElementById("restaurant-list-grid");
      renderRestaurants(grid, loadedItems.filter((r) => r.isFavorite), { append: false });
    }
  });

  // Initial load
  loadRestaurantList({ append: false });
});

/* --------------------------------------------------------------------------
   Read URL params (e.g. ?cuisine=Pizza or ?search=burger from home.html)
   -------------------------------------------------------------------------- */
function applyInitialUrlParams() {
  const params = new URLSearchParams(window.location.search);
  const cuisine = params.get("cuisine");
  const search = params.get("search");

  if (cuisine) {
    filterState.cuisine = cuisine;
    const chip = document.getElementById("cuisine-chip");
    const chipLabel = document.getElementById("cuisine-chip-label");
    if (chipLabel) chipLabel.textContent = cuisine;
    if (chip) chip.classList.add("filter-chip--active");

    document.querySelectorAll("#cuisine-options .dropdown-item").forEach((item) => {
      item.classList.toggle("is-selected", item.dataset.cuisine === cuisine);
    });
  }

  if (search) {
    filterState.search = search;
    const mainInput = document.getElementById("main-search-input");
    const headerInput = document.querySelector(".header-search__input");
    if (mainInput) mainInput.value = search;
    if (headerInput) headerInput.value = search;
  }
}

/* --------------------------------------------------------------------------
   Geolocation (optional; used for deliveryTimeMinutes & maxDeliveryTime)
   -------------------------------------------------------------------------- */
function requestGeolocation() {
  if (!navigator.geolocation) return;
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      filterState.lat = pos.coords.latitude;
      filterState.lng = pos.coords.longitude;
    },
    () => {
      // Silently keep null until time/distance filter needs fallback
    },
    { timeout: 3000 }
  );
}

/* --------------------------------------------------------------------------
   Tell the rest of the page that the filters changed
   -------------------------------------------------------------------------- */
function notifyFiltersChanged() {
  document.dispatchEvent(new CustomEvent("restaurant-filters-change", { detail: { ...filterState } }));
}

/* --------------------------------------------------------------------------
   Tabs: All restaurants / Favourites
   -------------------------------------------------------------------------- */
function setupTabs() {
  const tabs = document.querySelectorAll(".list-tabs__tab");

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      tabs.forEach((otherTab) => {
        const isSelected = otherTab === tab;
        otherTab.classList.toggle("list-tabs__tab--active", isSelected);
        otherTab.setAttribute("aria-selected", String(isSelected));
      });

      filterState.tab = tab.dataset.tab;
      notifyFiltersChanged();
    });
  });
}

/* --------------------------------------------------------------------------
   Sort dropdown
   -------------------------------------------------------------------------- */
function setupSortMenu() {
  const options = document.querySelectorAll("#sort-options .dropdown-item");
  const valueLabel = document.getElementById("sort-value");

  options.forEach((option) => {
    option.addEventListener("click", () => {
      options.forEach((item) => item.classList.remove("is-selected"));
      option.classList.add("is-selected");

      if (valueLabel) valueLabel.textContent = option.textContent;
      filterState.sort = option.dataset.sort;
      notifyFiltersChanged();
    });
  });
}

/* --------------------------------------------------------------------------
   Cuisine dropdown chip
   -------------------------------------------------------------------------- */
function setupCuisineMenu() {
  const options = document.querySelectorAll("#cuisine-options .dropdown-item");
  const chip = document.getElementById("cuisine-chip");
  const chipLabel = document.getElementById("cuisine-chip-label");

  options.forEach((option) => {
    option.addEventListener("click", () => {
      options.forEach((item) => item.classList.remove("is-selected"));
      option.classList.add("is-selected");

      const cuisine = option.dataset.cuisine;
      filterState.cuisine = cuisine;

      if (chipLabel) chipLabel.textContent = cuisine || "Cuisine";
      if (chip) chip.classList.toggle("filter-chip--active", cuisine !== "");

      notifyFiltersChanged();
    });
  });
}

/* --------------------------------------------------------------------------
   Rating, delivery time and Deals chips
   -------------------------------------------------------------------------- */
function setupToggleChips() {
  const chips = document.querySelectorAll(".filter-chip[data-filter]");

  chips.forEach((chip) => {
    chip.addEventListener("click", () => {
      const filterName = chip.dataset.filter;
      const wasActive = chip.classList.contains("filter-chip--active");

      document.querySelectorAll(`.filter-chip[data-filter="${filterName}"]`).forEach((groupChip) => {
        groupChip.classList.remove("filter-chip--active");
        groupChip.setAttribute("aria-pressed", "false");
      });

      if (!wasActive) {
        chip.classList.add("filter-chip--active");
        chip.setAttribute("aria-pressed", "true");
      }

      filterState[filterName] = wasActive ? defaultValueFor(filterName) : parseChipValue(chip.dataset.value);
      notifyFiltersChanged();
    });
  });
}

function parseChipValue(value) {
  if (value === "true") return true;
  return Number(value);
}

function defaultValueFor(filterName) {
  return filterName === "deals" ? false : null;
}

/* --------------------------------------------------------------------------
   Search inputs (hero & header)
   -------------------------------------------------------------------------- */
function setupSearchInputs() {
  const mainInput = document.getElementById("main-search-input");
  const headerInput = document.querySelector(".header-search__input");
  const submitBtn = document.querySelector(".hero-search__submit");

  let debounceTimer = null;

  function triggerSearch(value, immediate = false) {
    filterState.search = value.trim();
    if (mainInput && mainInput.value !== value) mainInput.value = value;
    if (headerInput && headerInput.value !== value) headerInput.value = value;

    clearTimeout(debounceTimer);
    if (immediate) {
      notifyFiltersChanged();
    } else {
      debounceTimer = setTimeout(() => notifyFiltersChanged(), 300);
    }
  }

  [mainInput, headerInput].forEach((input) => {
    if (!input) return;
    input.addEventListener("input", (e) => triggerSearch(e.target.value, false));
    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        triggerSearch(input.value, true);
      }
    });
  });

  if (submitBtn && mainInput) {
    submitBtn.addEventListener("click", () => triggerSearch(mainInput.value, true));
  }
}

/* --------------------------------------------------------------------------
   Load more pagination
   -------------------------------------------------------------------------- */
function setupLoadMore() {
  const btn = document.getElementById("load-more-btn");
  if (!btn) return;
  btn.addEventListener("click", () => {
    filterState.page += 1;
    loadRestaurantList({ append: true });
  });
}

/* --------------------------------------------------------------------------
   Fetch from API & render
   -------------------------------------------------------------------------- */
async function loadRestaurantList({ append = false } = {}) {
  const grid = document.getElementById("restaurant-list-grid");
  const loadMoreWrapper = document.getElementById("load-more-wrapper");
  if (!grid) return;

  const requestToken = ++activeRequestToken;

  if (!append) {
    loadedItems = [];
    renderSkeletons(grid, 8);
    if (loadMoreWrapper) loadMoreWrapper.classList.add("d-none");
  }

  const needsLocation =
    filterState.maxTime != null ||
    filterState.sort === "time_asc" ||
    filterState.sort === "distance_asc";

  const lat = filterState.lat ?? (needsLocation ? DEFAULT_COORDS.lat : null);
  const lng = filterState.lng ?? (needsLocation ? DEFAULT_COORDS.lng : null);

  try {
    const paged = await fetchRestaurants({
      cuisine: filterState.cuisine,
      minRating: filterState.minRating,
      search: filterState.search,
      deals: filterState.deals,
      lat,
      lng,
      maxDeliveryTime: filterState.maxTime,
      page: filterState.page,
      size: PAGE_SIZE,
    });

    if (requestToken !== activeRequestToken) return;

    loadedItems = append ? [...loadedItems, ...paged.items] : paged.items;

    let displayItems = [...loadedItems];

    if (filterState.tab === "favourites") {
      displayItems = displayItems.filter((r) => r.isFavorite);
    }

    if (filterState.sort === "time_asc" || filterState.sort === "distance_asc") {
      displayItems.sort((a, b) => {
        const aTime = a.deliveryTimeMinutes ?? a.prepTime ?? Number.MAX_SAFE_INTEGER;
        const bTime = b.deliveryTimeMinutes ?? b.prepTime ?? Number.MAX_SAFE_INTEGER;
        return aTime - bTime;
      });
    }

    renderRestaurants(grid, displayItems, { append: false });

    const openCount = displayItems.filter((r) => r.isOpen !== false).length;
    setResultsSummary(openCount);
    setFavouritesCount(loadedItems.filter((r) => r.isFavorite).length);

    if (loadMoreWrapper) {
      loadMoreWrapper.classList.toggle("d-none", !paged.hasNext);
    }
  } catch (err) {
    if (requestToken !== activeRequestToken) return;
    console.error("[Loqma] Could not load restaurant list:", err);
    if (!append) {
      renderError(grid, "Could not load restaurants. Please try again later.");
    }
  }
}

/* --------------------------------------------------------------------------
   Helpers the rest of the page can call after loading data
   -------------------------------------------------------------------------- */
function setResultsSummary(openCount) {
  const el = document.getElementById("results-summary");
  if (el) el.textContent = `Showing ${openCount} open spots`;
}

function setFavouritesCount(count) {
  const el = document.getElementById("favourites-count");
  if (el) el.textContent = `(${count})`;
}
