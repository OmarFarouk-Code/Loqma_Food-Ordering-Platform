document.addEventListener("DOMContentLoaded", () => {
  initializePlaceholders();
  initializeCart();
});

const currencyFormatter = new Intl.NumberFormat("en-EG", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function initializePlaceholders() {
  // TODO: Replace these placeholders when a customer profile API is available.
  setText("user-name", "Guest");

  // TODO: Load the selected restaurant and delivery address from their APIs.
  setText("restaurant-name", "Restaurant");
  setText("delivery-address", "Add delivery address");
  setText("delivery-time", "—");
  setText("restaurant-logo", "R");

  // TODO: Replace with the loyalty points API.
  setText("points-earned", "—");
}

async function initializeCart() {
  const cartItems = document.getElementById("cart-items");
  if (!cartItems) return;

  const cartId = getCartId();
  if (!cartId) {
    renderEmptyCart("Your cart is empty.");
    setFeedback("TODO: Provide the signed-in customer's cart ID to load cart data.", true);
    return;
  }

  cartItems.addEventListener("click", handleCartAction);
  setFeedback("Loading your cart…");

  try {
    await loadCart(cartId);
    setFeedback("");
  } catch (error) {
    renderEmptyCart("We couldn't load your cart.");
    setFeedback(`Cart request failed: ${error.message}`, true);
  }
}

function getCartId() {
  // TODO: Read the cart ID from the authenticated customer session when available.
  const queryCartId = new URLSearchParams(window.location.search).get("cartId");
  return queryCartId || window.localStorage.getItem("cartId");
}

async function loadCart(cartId) {
  const cart = await api.get(`/api/carts/${cartId}/receipt`);
  renderCart(cart);
}

async function handleCartAction(event) {
  const button = event.target.closest("button[data-action]");
  if (!button || button.disabled) return;

  const cartId = getCartId();
  const itemId = button.dataset.itemId;
  if (!cartId || !itemId) return;

  const action = button.dataset.action;
  const currentQuantity = Number(button.dataset.quantity);

  button.disabled = true;
  setFeedback("");

  try {
    if (action === "increase" || action === "decrease") {
      const quantity = currentQuantity + (action === "increase" ? 1 : -1);
      if (quantity < 1) return;

      await api.request("PATCH", `/api/carts/${itemId}`, quantity);
    } else if (action === "remove") {
      await api.delete(`/api/carts/${itemId}`);
    } else {
      return;
    }

    await loadCart(cartId);
  } catch (error) {
    setFeedback(`Cart update failed: ${error.message}`, true);
  } finally {
    button.disabled = false;
  }
}

function renderCart(cart) {
  const cartItems = document.getElementById("cart-items");
  const items = Array.isArray(cart.items) ? cart.items : [];
  const subtotal = toNumber(cart.subtotal);
  const deliveryFee = toNumber(cart.deliveryFee);
  const serviceFee = toNumber(cart.serviceFee);
  const tax = toNumber(cart.tax);
  const discount = toNumber(cart.discount);
  const minimumOrder = toNumber(cart.minimumOrderAmount);
  const total = subtotal + deliveryFee + serviceFee + tax - discount;
  const itemCount = items.reduce((count, item) => count + toNumber(item.quantity), 0);

  cartItems.replaceChildren();

  if (items.length === 0) {
    renderEmptyCart("Your cart is empty.");
  } else {
    items.forEach((item, index) => cartItems.append(createCartItem(item, index)));
  }

  setText("items-count", itemCount);
  setText("subtotal", formatMoney(subtotal));
  setText("delivery-fee", formatMoney(deliveryFee));
  setText("service-fee", formatMoney(serviceFee));
  setText("tax", formatMoney(tax));
  setText("total", formatMoney(total));
  setText("checkout-total", formatMoney(total));
  setText("cart-badge", itemCount);
  setText("min-order-subtotal", currencyFormatter.format(subtotal));
  setText("min-order-min", currencyFormatter.format(minimumOrder));
  setText("min-order-text", items.length === 0
    ? "Your cart is empty."
    : minimumOrder > 0 && subtotal >= minimumOrder
    ? `Minimum order reached (${formatMoney(minimumOrder)})`
    : `Add ${formatMoney(Math.max(minimumOrder - subtotal, 0))} to reach the minimum order`);
  setText("footer-year", new Date().getFullYear());

  const progress = minimumOrder > 0 ? Math.min((subtotal / minimumOrder) * 100, 100) : 100;
  const progressBar = document.getElementById("min-order-progress-bar");
  const progressElement = document.getElementById("min-order-progress");
  if (progressBar) progressBar.style.width = `${progress}%`;
  if (progressElement) progressElement.setAttribute("aria-valuenow", String(Math.round(progress)));

  const minimumOrderBox = document.getElementById("min-order-box");
  if (minimumOrderBox) minimumOrderBox.classList.toggle("is-below", minimumOrder > 0 && subtotal < minimumOrder);

  const checkoutButton = document.getElementById("checkout-btn");
  if (checkoutButton) checkoutButton.setAttribute("aria-disabled", String(items.length === 0));
}

function createCartItem(item, index) {
  const article = document.createElement("article");
  article.className = "item";
  article.id = `item-${index + 1}`;
  article.dataset.itemId = item.id || "";

  const imageBox = document.createElement("span");
  imageBox.className = "item__img";
  article.append(imageBox);

  const body = document.createElement("div");
  body.className = "item__body";

  const name = document.createElement("h3");
  name.className = "item__name";
  name.textContent = item.menuItemName || "Menu item";
  body.append(name);

  const options = document.createElement("p");
  options.className = "item__options";
  options.textContent = [
    item.selectedSizeName,
    ...(Array.isArray(item.selectedExtras) ? item.selectedExtras.map((extra) => extra.name) : []),
  ].filter(Boolean).join(", ");
  body.append(options);

  if (item.customerNote) {
    const note = document.createElement("p");
    note.className = "item__note";
    note.textContent = item.customerNote;
    body.append(note);
  }

  const quantity = Math.max(1, Math.floor(toNumber(item.quantity)));
  const quantityControl = document.createElement("div");
  quantityControl.className = "qty";
  quantityControl.append(
    createActionButton("decrease", item.id, quantity, "Decrease quantity", "−"),
  );

  const quantityValue = document.createElement("span");
  quantityValue.textContent = String(quantity);
  quantityControl.append(quantityValue);
  quantityControl.append(
    createActionButton("increase", item.id, quantity, "Increase quantity", "+"),
  );
  body.append(quantityControl);
  article.append(body);

  article.append(createActionButton("remove", item.id, quantity, "Remove item", "×", "item__remove"));

  const price = document.createElement("span");
  price.className = "item__price";
  price.textContent = formatMoney(item.linePrice);
  article.append(price);

  return article;
}

function createActionButton(action, itemId, quantity, label, text, className = "") {
  const button = document.createElement("button");
  button.type = "button";
  button.dataset.action = action;
  button.dataset.itemId = itemId || "";
  button.dataset.quantity = String(quantity);
  button.setAttribute("aria-label", label);
  if (className) button.className = className;
  button.textContent = text;
  return button;
}

function renderEmptyCart(message) {
  const cartItems = document.getElementById("cart-items");
  if (!cartItems) return;

  cartItems.replaceChildren();
  const emptyMessage = document.createElement("p");
  emptyMessage.className = "cart-empty";
  emptyMessage.textContent = message;
  cartItems.append(emptyMessage);
  setText("items-count", "0");
  setText("cart-badge", "0");
  setText("subtotal", formatMoney(0));
  setText("delivery-fee", formatMoney(0));
  setText("service-fee", formatMoney(0));
  setText("tax", formatMoney(0));
  setText("total", formatMoney(0));
  setText("checkout-total", formatMoney(0));
  setText("min-order-subtotal", currencyFormatter.format(0));
  setText("min-order-min", currencyFormatter.format(0));
  setText("min-order-text", "Your cart is empty.");
  const progressBar = document.getElementById("min-order-progress-bar");
  const progressElement = document.getElementById("min-order-progress");
  if (progressBar) progressBar.style.width = "0%";
  if (progressElement) progressElement.setAttribute("aria-valuenow", "0");
  const minimumOrderBox = document.getElementById("min-order-box");
  minimumOrderBox?.classList.remove("is-below");
  document.getElementById("checkout-btn")?.setAttribute("aria-disabled", "true");
}

function setFeedback(message, isError = false) {
  let feedback = document.getElementById("cart-feedback");
  if (!feedback) {
    feedback = document.createElement("p");
    feedback.id = "cart-feedback";
    feedback.setAttribute("aria-live", "polite");
    const cartItems = document.getElementById("cart-items");
    cartItems?.before(feedback);
  }

  feedback.textContent = message;
  feedback.hidden = !message;
  feedback.setAttribute("role", isError ? "alert" : "status");
}

function setText(id, value) {
  const element = document.getElementById(id);
  if (element) element.textContent = String(value);
}

function toNumber(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
}

function formatMoney(value) {
  return `EGP ${currencyFormatter.format(toNumber(value))}`;
}
