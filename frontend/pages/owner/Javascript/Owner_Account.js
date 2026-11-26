const API_BASE_URL = "http://localhost:8080/api/owner/restaurant";

let currentProfile = {};

const ownerNameInput =
  document.getElementById("ownerName") ||
  document.getElementById("fullName") ||
  document.querySelector('input[name="ownerName"]');
const ownerPhoneInput = document.getElementById("ownerPhone");
const ownerEmailInput = document.getElementById("ownerEmail");
const restaurantNameInput = document.getElementById("restaurantName");
const branchNameInput = document.getElementById("branchName");
const primaryCuisineSelect = document.getElementById("primaryCuisine");
const fullAddressInput = document.getElementById("fullAddress");
const restaurantPhoneInput = document.getElementById("restaurantPhone");
const logoInput =
  document.getElementById("logoUrl") || document.getElementById("logo");
const coverImageInput =
  document.getElementById("coverImageUrl") ||
  document.getElementById("coverImage");
const deliveryFeeInput = document.getElementById("deliveryFee");
const minOrderInput =
  document.getElementById("minimumOrderAmount") ||
  document.getElementById("minOrder");
const gpsInput = document.getElementById("gpsLocation");

const saveChangesBtn = document.getElementById("saveChangesBtn");
const logoutBtn = document.getElementById("logoutBtn");

function getHeaders() {
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

async function loadAccountData() {
  try {
    const response = await fetch(API_BASE_URL, {
      method: "GET",
      headers: getHeaders(),
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch profile: ${response.status}`);
    }

    const data = await response.json();
    currentProfile = data;

    if (ownerNameInput) {
      ownerNameInput.value = data.ownerName || "";
    }
    if (restaurantNameInput) {
      restaurantNameInput.value = data.name || "";
    }
    if (branchNameInput) {
      branchNameInput.value = data.branch || "";
    }
    if (fullAddressInput) {
      fullAddressInput.value = data.address || "";
    }
    if (restaurantPhoneInput) {
      restaurantPhoneInput.value = data.phone || "";
    }
    if (logoInput) {
      logoInput.value = data.logo || "";
    }
    if (coverImageInput) {
      coverImageInput.value = data.coverImage || "";
    }
    if (deliveryFeeInput && data.deliveryFee != null) {
      deliveryFeeInput.value = data.deliveryFee;
    }
    if (minOrderInput && data.minimumOrderAmount != null) {
      minOrderInput.value = data.minimumOrderAmount;
    }
    if (gpsInput) {
      gpsInput.value = data.gpsLocation || "";
    }
    if (ownerNameInput) ownerNameInput.value = data.ownerName || "";
    if (ownerPhoneInput)
      ownerPhoneInput.value = data.ownerPhone ?? data.phone ?? "";
    if (ownerEmailInput)
      ownerEmailInput.value = data.ownerEmail ?? data.email ?? "";

    if (data.cuisine && primaryCuisineSelect) {
      const exists = Array.from(primaryCuisineSelect.options).some(
        (opt) => opt.value.toLowerCase() === data.cuisine.toLowerCase(),
      );
      if (!exists) {
        primaryCuisineSelect.add(new Option(data.cuisine, data.cuisine));
      }
      primaryCuisineSelect.value = data.cuisine;
    }
  } catch (error) {
    console.error("Error loading account data:", error);
  }
}

async function saveAccountData() {
  if (!saveChangesBtn) return;

  saveChangesBtn.disabled = true;
  const originalHTML = saveChangesBtn.innerHTML;
  saveChangesBtn.textContent = "Saving...";

  const resolvedOwnerNameInput =
    ownerNameInput ||
    document.getElementById("ownerName") ||
    document.getElementById("fullName") ||
    document.querySelector('input[name="ownerName"]');

  const payload = {
    ownerName: ownerNameInput ? ownerNameInput.value.trim() || null : null,
    ownerPhone: ownerPhoneInput ? ownerPhoneInput.value.trim() || null : null,
    name: restaurantNameInput ? restaurantNameInput.value.trim() : "",
    branch: branchNameInput ? branchNameInput.value.trim() : "",
    cuisine: primaryCuisineSelect ? primaryCuisineSelect.value.trim() : "",
    logo: logoInput
      ? logoInput.value.trim() || null
      : (currentProfile.logo ?? null),
    coverImage: coverImageInput
      ? coverImageInput.value.trim() || null
      : (currentProfile.coverImage ?? null),
    address: fullAddressInput
      ? fullAddressInput.value.trim() || null
      : (currentProfile.address ?? null),
    phone: restaurantPhoneInput
      ? restaurantPhoneInput.value.trim() || null
      : (currentProfile.phone ?? null),
    deliveryFee: deliveryFeeInput
      ? Number(deliveryFeeInput.value)
      : Number(currentProfile.deliveryFee ?? 0),
    minimumOrderAmount: minOrderInput
      ? Number(minOrderInput.value)
      : Number(currentProfile.minimumOrderAmount ?? 0),
    gpsLocation: gpsInput
      ? gpsInput.value.trim() || null
      : (currentProfile.gpsLocation ?? null),
  };

  try {
    const response = await fetch(API_BASE_URL, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({}));
      throw new Error(
        errorBody.message || `Update failed (${response.status})`,
      );
    }

    currentProfile = await response.json();
    if (resolvedOwnerNameInput && currentProfile.ownerName) {
      resolvedOwnerNameInput.value = currentProfile.ownerName;
    }
    alert("Changes saved successfully!");
  } catch (error) {
    console.error("Error saving changes:", error);
    alert(error.message);
  } finally {
    saveChangesBtn.disabled = false;
    saveChangesBtn.innerHTML = originalHTML;
  }
}

function handleLogout() {
  /* will be handled when the regiester and login is done
  localStorage.removeItem("token");
  window.location.reload();*/
}

document.addEventListener("DOMContentLoaded", () => {
  loadAccountData();
  if (saveChangesBtn) {
    saveChangesBtn.addEventListener("click", saveAccountData);
  }
  if (logoutBtn) {
    logoutBtn.addEventListener("click", handleLogout);
  }
});
