// Loqma - Home page behaviour

document.addEventListener("DOMContentLoaded", () => {
  setupSearchTabs();
  loadRestaurantsIntoPage({
    containerIds: ["featured-restaurants-grid", "popular-restaurants-grid"],
    featuredCount: 4,
  });
});


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

// Favourite buttons on static (non-API) cards, if any remain
function setupFavoriteButtons() {
  attachFavoriteListeners(document);
}
