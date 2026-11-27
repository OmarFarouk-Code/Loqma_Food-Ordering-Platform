const GLOBAL_HOURS_API = "http://localhost:8080/api/owner/operating-hours";
const STORE_STATUS_STORAGE_KEY = "loqma_store_open";

function getGlobalHeaders() {
  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  const token = localStorage.getItem("token");
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}
function applyGlobalStoreStatus(isOpen) {
  const openState = Boolean(isOpen);
  localStorage.setItem(STORE_STATUS_STORAGE_KEY, String(openState));

  document.querySelectorAll("#storeStatusPill").forEach((pill) => {
    pill.classList.toggle("is-closed", !openState);
  });

  document.querySelectorAll("#statusText").forEach((label) => {
    label.textContent = openState ? "Open now" : "Closed";
  });

  document.querySelectorAll("#openStatusToggle").forEach((toggle) => {
    toggle.checked = openState;
  });
}
window.syncStatus = applyGlobalStoreStatus;

async function loadGlobalStoreStatus() {
  const cached = localStorage.getItem(STORE_STATUS_STORAGE_KEY);
  if (cached !== null) {
    applyGlobalStoreStatus(cached === "true");
  }
  try {
    const response = await fetch(GLOBAL_HOURS_API, {
      method: "GET",
      headers: getGlobalHeaders(),
      cache: "no-store",
    });

    if (!response.ok) return;

    const data = await response.json();
    if (typeof data.open === "boolean") {
      applyGlobalStoreStatus(data.open);
    }
  } catch (err) {
    console.warn("Global status fetch skipped:", err.message);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  loadGlobalStoreStatus();
  document.querySelectorAll("#openStatusToggle").forEach((toggle) => {
    toggle.addEventListener("change", async function () {
      const nextOpen = this.checked;
      applyGlobalStoreStatus(nextOpen);

      try {
        const response = await fetch(`${GLOBAL_HOURS_API}/status`, {
          method: "PATCH",
          headers: getGlobalHeaders(),
          body: JSON.stringify({ open: nextOpen }),
        });

        if (response.ok) {
          const result = await response.json().catch(() => ({}));
          if (typeof result.open === "boolean") {
            applyGlobalStoreStatus(result.open);
          }
        }
      } catch (err) {
        console.warn("Could not persist store status:", err.message);
      }
    });
  });
});
