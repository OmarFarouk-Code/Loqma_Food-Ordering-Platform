// Loqma - Restaurant List toolbar (tabs, sort, filter chips)
//
// This file only manages the UI state. Whenever something changes it fires a
// "restaurant-filters-change" event with the current filters, so the code that
// calls the API (api.js) can listen to it:
//
//   document.addEventListener("restaurant-filters-change", (event) => {
//     loadRestaurants(event.detail);   // { tab, sort, cuisine, minRating, maxTime, deals }
//   });

const filterState = {
  tab: "all", // "all" | "favourites"
  sort: "rating_desc", // "rating_desc" | "time_asc" | "distance_asc"
  cuisine: "", // "" means all cuisines
  minRating: null, // 4.0 | 4.5 | null
  maxTime: null, // 30 | 45 | null
  deals: false,
};

document.addEventListener("DOMContentLoaded", () => {
  setupTabs();
  setupSortMenu();
  setupCuisineMenu();
  setupToggleChips();
});

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
        otherTab.setAttribute("aria-selected", isSelected);
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

      valueLabel.textContent = option.textContent;
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

      // Show the chosen cuisine on the chip and highlight it
      chipLabel.textContent = cuisine || "Cuisine";
      chip.classList.toggle("filter-chip--active", cuisine !== "");

      notifyFiltersChanged();
    });
  });
}

/* --------------------------------------------------------------------------
   Rating, delivery time and Deals chips
   - chips with the same data-filter behave like radio buttons (one at a time)
   - clicking the active chip again turns it off
   -------------------------------------------------------------------------- */
function setupToggleChips() {
  const chips = document.querySelectorAll(".filter-chip[data-filter]");

  chips.forEach((chip) => {
    chip.addEventListener("click", () => {
      const filterName = chip.dataset.filter;
      const wasActive = chip.classList.contains("filter-chip--active");

      // Turn off every chip in the same group
      document.querySelectorAll(`.filter-chip[data-filter="${filterName}"]`).forEach((groupChip) => {
        groupChip.classList.remove("filter-chip--active");
        groupChip.setAttribute("aria-pressed", "false");
      });

      // Turn this one on (unless it was already on)
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
   Helpers the rest of the page can call after loading data
   -------------------------------------------------------------------------- */
function setResultsSummary(openCount) {
  document.getElementById("results-summary").textContent = `Showing ${openCount} open spots`;
}

function setFavouritesCount(count) {
  document.getElementById("favourites-count").textContent = `(${count})`;
}
