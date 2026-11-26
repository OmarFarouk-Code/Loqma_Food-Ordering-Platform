// Register -> POST {API_BASE_URL}/api/auth/register
// Request:  { name, email, phone, password, role, addresses: [{ nickname, street, building }] }
//           role = CUSTOMER | RESTAURANT_OWNER | DRIVER   (addresses are sent for customers only)
//           Drivers also send: driver: { nationalId, plateNumber, drivingLicense }
//           Owners who pick images send multipart/form-data instead:
//             part "data" (application/json, same JSON as above), part "logo", part "coverImage"
//           Restaurant owners also send: restaurant: { name, branch, cuisine, address, phone,
//             gpsLocation, latitude, longitude, deliveryFee, minimumOrderAmount, prepTime, maxDeliveryDistance }
// Response: 201 (customer, account ready) | 202 (owner/driver, waiting for admin approval) | 400 validation | 409 duplicate

const MAX_ADDRESSES = 5;
const IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];
const LOGO_MAX_BYTES = 2 * 1024 * 1024; // 2 MB
const COVER_MAX_BYTES = 5 * 1024 * 1024; // 5 MB
const DEFAULT_MAP_VIEW = [30.0444, 31.2357]; // where the map opens before a pin is placed
const MAX_PASSWORD_BYTES = 72; // bcrypt only uses the first 72 bytes; newer Spring Security rejects longer ones

const form = document.getElementById("registerForm");
const notice = document.getElementById("notice");
const submitBtn = document.getElementById("submitBtn");
const spinner = document.getElementById("spinner");
const btnText = document.getElementById("btnText");
const nameInput = document.getElementById("name");
const emailInput = document.getElementById("email");
const phoneInput = document.getElementById("phone");
const passwordInput = document.getElementById("password");
const confirmInput = document.getElementById("confirm");
const addressSection = document.getElementById("addressSection");
const addressList = document.getElementById("addressList");
const addBtn = document.getElementById("addAddress");
const restaurantSection = document.getElementById("restaurantSection");
const restName = document.getElementById("restName");
const restBranch = document.getElementById("restBranch");
const restCuisine = document.getElementById("restCuisine");
const restAddress = document.getElementById("restAddress");
const restPhone = document.getElementById("restPhone");
const deliveryFee = document.getElementById("deliveryFee");
const minOrder = document.getElementById("minOrder");
const prepTime = document.getElementById("prepTime");
const maxDistance = document.getElementById("maxDistance");

const driverSection = document.getElementById("driverSection");
const locQuery = document.getElementById("locQuery");
const locSearchBtn = document.getElementById("locSearchBtn");
const useMyLocationBtn = document.getElementById("useMyLocation");
const locMapEl = document.getElementById("locMap");
const locReadout = document.getElementById("locReadout");
const nationalIdInput = document.getElementById("nationalId");
const plateInput = document.getElementById("plateNumber");
const licenseInput = document.getElementById("drivingLicense");

const NATIONAL_ID_RE = /^\d{14}$/; // Egyptian national ID: 14 digits
const PLATE_RE = /^[\p{L}\p{N}][\p{L}\p{N} -]{1,18}[\p{L}\p{N}]$/u; // 3-20 chars, letters/digits/space/hyphen
const LICENSE_RE = /^[A-Za-z0-9][A-Za-z0-9\-\/ ]{2,48}[A-Za-z0-9]$/; // 4-50 chars
const normalizePlate = (v) => v.trim().replace(/\s+/g, " ").toUpperCase();
const normalizeLicense = (v) => v.trim().replace(/\s+/g, " ").toUpperCase();
const normalizeNationalId = (v) => v.replace(/\s/g, "");

const PHONE_RE = /^\+?\d{10,15}$/;
const cleanPhone = (v) => v.replace(/[\s-]/g, "");

// Same rules as AuthService.isStrongPassword on the backend
const PASSWORD_RULES = {
  length: (p) => p.length >= 12,
  upper: (p) => /[A-Z]/.test(p),
  number: (p) => /[0-9]/.test(p),
  symbol: (p) => /[^A-Za-z0-9]/.test(p),
};

/* ---------- helpers ---------- */
const REQUEST_TIMEOUT_MS = 15000;

// POST to the backend, giving up after 15s so the button never spins forever.
// A plain object is sent as JSON; a FormData is sent as multipart (the browser sets the boundary).
async function postRequest(path, body) {
  const isForm = body instanceof FormData;
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), REQUEST_TIMEOUT_MS);
  try {
    return await fetch(API_BASE_URL + path, {
      method: "POST",
      headers: isForm ? undefined : { "Content-Type": "application/json" },
      body: isForm ? body : JSON.stringify(body),
      signal: ctrl.signal,
    });
  } finally {
    clearTimeout(timer);
  }
}

async function serverMessage(res) {
  try {
    return (await res.json()).message || "";
  } catch (_) {
    return "";
  }
}

const selectedRole = () =>
  form.querySelector('input[name="role"]:checked').value;

function showNotice(type, title, text) {
  notice.className = "notice notice-" + type;
  notice.innerHTML = "";
  if (title) {
    const strong = document.createElement("strong");
    strong.textContent = title;
    notice.appendChild(strong);
  }
  notice.appendChild(document.createTextNode(text));
}
function hideNotice() {
  notice.className = "notice d-none";
}

function setError(input, message) {
  const err = input.closest(".field").querySelector(".field-error");
  input.classList.toggle("input-error", !!message);
  input.setAttribute("aria-invalid", message ? "true" : "false");
  err.textContent = message || "";
}

function setLoading(on) {
  submitBtn.disabled = on;
  spinner.classList.toggle("d-none", !on);
  btnText.textContent = on ? "Creating account..." : "Create account";
}

/* ---------- show / hide password ---------- */
document.querySelectorAll(".eye").forEach((btn) => {
  btn.addEventListener("click", () => {
    const input = btn.closest(".pw").querySelector("input");
    const show = input.type === "password";
    input.type = show ? "text" : "password";
    btn.innerHTML = show
      ? '<i class="bi bi-eye-slash"></i>'
      : '<i class="bi bi-eye"></i>';
    btn.setAttribute("aria-label", show ? "Hide password" : "Show password");
  });
});

/* ---------- live password checklist ---------- */
function updatePasswordRules() {
  const p = passwordInput.value;
  document.querySelectorAll("#pwRules li").forEach((li) => {
    const ok = PASSWORD_RULES[li.dataset.rule](p);
    li.classList.toggle("ok", ok);
    li.querySelector("i").className = ok
      ? "bi bi-check-circle-fill"
      : "bi bi-circle";
  });
}
const passwordIsStrong = () =>
  Object.values(PASSWORD_RULES).every((rule) => rule(passwordInput.value));

passwordInput.addEventListener("input", () => {
  updatePasswordRules();
  setError(passwordInput, "");
});
[
  locQuery,
  nameInput,
  emailInput,
  phoneInput,
  confirmInput,
  restName,
  restBranch,
  restCuisine,
  restAddress,
  restPhone,
  deliveryFee,
  minOrder,
  prepTime,
  maxDistance,
  nationalIdInput,
  plateInput,
  licenseInput,
].forEach((i) => i.addEventListener("input", () => setError(i, "")));

/* ---------- delivery addresses (customers only) ---------- */
function addAddressBlock() {
  const count = addressList.children.length;
  if (count >= MAX_ADDRESSES) return;

  const block = document.createElement("div");
  block.className = "address-block";
  block.innerHTML = `
    ${
      count > 0
        ? `<div class="address-block-head"><span>Address ${count + 1}</span>
        <button type="button" class="remove-address">Remove</button></div>`
        : ""
    }
    <div class="field">
      <label>Address nickname</label>
      <input type="text" class="form-control loq-input addr-nick" placeholder="Home">
      <div class="field-error"></div>
    </div>
    <div class="field">
      <label>Street name</label>
      <input type="text" class="form-control loq-input addr-street" placeholder="El-Tahrir Street">
      <div class="field-error"></div>
    </div>
    <div class="field">
      <label>
        <span class="d-md-none">Building and apartment</span>
        <span class="d-none d-md-inline">Building, floor, and apartment</span>
      </label>
      <input type="text" class="form-control loq-input addr-building" placeholder="Building 14, Apartment 3">
      <div class="field-error"></div>
    </div>`;

  block
    .querySelectorAll("input")
    .forEach((i) => i.addEventListener("input", () => setError(i, "")));
  const remove = block.querySelector(".remove-address");
  if (remove) {
    remove.addEventListener("click", () => {
      block.remove();
      addBtn.disabled = false;
      renumberAddresses();
    });
  }
  addressList.appendChild(block);
  addBtn.disabled = addressList.children.length >= MAX_ADDRESSES;
}

function renumberAddresses() {
  addressList.querySelectorAll(".address-block-head span").forEach((s, i) => {
    s.textContent = "Address " + (i + 2);
  });
}

addBtn.addEventListener("click", addAddressBlock);
addAddressBlock(); // first address is always there

/* ---------- logo + cover image uploads ---------- */
const PLACEHOLDER_ICON = '<i class="bi bi-image"></i>';

function setupUpload({ inputId, previewId, removeId, maxBytes }) {
  const input = document.getElementById(inputId);
  const preview = document.getElementById(previewId);
  const removeBtn = document.getElementById(removeId);
  let file = null;
  let url = null;

  function clear() {
    if (url) URL.revokeObjectURL(url);
    file = null;
    url = null;
    input.value = "";
    preview.innerHTML = PLACEHOLDER_ICON;
    removeBtn.classList.add("d-none");
  }

  input.addEventListener("change", () => {
    setError(input, "");
    const picked = input.files && input.files[0];
    if (!picked) {
      clear();
      return;
    }
    if (!IMAGE_TYPES.includes(picked.type)) {
      clear();
      setError(input, "Use a PNG, JPG or WebP image.");
      return;
    }
    if (picked.size > maxBytes) {
      clear();
      setError(
        input,
        "This image is too large. The limit is " +
          maxBytes / 1024 / 1024 +
          " MB.",
      );
      return;
    }
    if (url) URL.revokeObjectURL(url);
    file = picked;
    url = URL.createObjectURL(picked);
    const img = new Image();
    img.alt = "";
    img.onerror = () => {
      clear();
      setError(input, "We couldn't read that file as an image.");
    };
    img.src = url;
    preview.innerHTML = "";
    preview.appendChild(img);
    removeBtn.classList.remove("d-none");
  });

  removeBtn.addEventListener("click", () => {
    clear();
    setError(input, "");
  });
  return {
    input,
    get file() {
      return file;
    },
  };
}

const logoUpload = setupUpload({
  inputId: "logoFile",
  previewId: "logoPreview",
  removeId: "logoRemove",
  maxBytes: LOGO_MAX_BYTES,
});
const coverUpload = setupUpload({
  inputId: "coverFile",
  previewId: "coverPreview",
  removeId: "coverRemove",
  maxBytes: COVER_MAX_BYTES,
});

/* ---------- restaurant location -> latitude / longitude ---------- */
let geo = null; // { lat, lng } once a pin is placed
let map = null; // Leaflet map (only if the Leaflet script loaded)
let marker = null;

const fmtCoord = (n) => Number(n).toFixed(6);

function setPoint(lat, lng, { zoom, label, keepView } = {}) {
  geo = { lat, lng };
  setError(locQuery, "");
  locReadout.textContent =
    "Pinned at " +
    fmtCoord(lat) +
    ", " +
    fmtCoord(lng) +
    (label ? " (" + label + ")" : "");
  locReadout.classList.add("ok");
  if (!map) return;
  if (!marker) {
    marker = L.marker([lat, lng], { draggable: true }).addTo(map);
    marker.on("dragend", () => {
      const p = marker.getLatLng();
      setPoint(p.lat, p.lng, { keepView: true });
    });
  } else {
    marker.setLatLng([lat, lng]);
  }
  if (!keepView) map.setView([lat, lng], zoom || Math.max(map.getZoom(), 16));
}

function ensureMap() {
  if (map) return;
  if (!window.L) {
    locMapEl.classList.add("no-map");
    return;
  } // Leaflet didn't load: search + "my location" still work
  map = L.map(locMapEl).setView(DEFAULT_MAP_VIEW, 11);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution:
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  }).addTo(map);
  map.on("click", (e) =>
    setPoint(e.latlng.lat, e.latlng.lng, { keepView: true }),
  );
  if (geo) setPoint(geo.lat, geo.lng);
}

// Address text -> coordinates, using OpenStreetMap's free Nominatim search.
async function geocode(query) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 10000);
  try {
    const res = await fetch(
      "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=" +
        encodeURIComponent(query),
      { signal: ctrl.signal },
    );
    if (!res.ok) throw new Error("geocoder " + res.status);
    const results = await res.json();
    if (!results.length) return null;
    return {
      lat: parseFloat(results[0].lat),
      lng: parseFloat(results[0].lon),
      label: String(results[0].display_name || ""),
    };
  } finally {
    clearTimeout(timer);
  }
}

async function searchLocation() {
  const query = locQuery.value.trim() || restAddress.value.trim();
  if (!query) {
    setError(locQuery, "Type an address or landmark to search for.");
    locQuery.focus();
    return;
  }
  locSearchBtn.disabled = true;
  locSearchBtn.textContent = "Searching...";
  try {
    const hit = await geocode(query);
    if (!hit || !Number.isFinite(hit.lat) || !Number.isFinite(hit.lng)) {
      setError(
        locQuery,
        "We couldn't find that place. Try a street and area, or tap the map to drop the pin.",
      );
    } else {
      const label =
        hit.label.length > 90 ? hit.label.slice(0, 87) + "..." : hit.label;
      setPoint(hit.lat, hit.lng, { zoom: 17, label });
    }
  } catch (err) {
    setError(
      locQuery,
      "We couldn't reach the map service. Try again, use your current location, or tap the map.",
    );
  } finally {
    locSearchBtn.disabled = false;
    locSearchBtn.textContent = "Search";
  }
}
locSearchBtn.addEventListener("click", searchLocation);
locQuery.addEventListener("keydown", (e) => {
  if (e.key === "Enter") {
    e.preventDefault();
    searchLocation();
  }
});

useMyLocationBtn.addEventListener("click", () => {
  if (!navigator.geolocation) {
    setError(
      locQuery,
      "Your browser can't share your location. Search for your address instead.",
    );
    return;
  }
  useMyLocationBtn.disabled = true;
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      useMyLocationBtn.disabled = false;
      setPoint(pos.coords.latitude, pos.coords.longitude, {
        zoom: 17,
        label: "your current location",
      });
    },
    (err) => {
      useMyLocationBtn.disabled = false;
      setError(
        locQuery,
        err && err.code === 1
          ? "Location access is blocked. Allow it in your browser, or search for your address."
          : "We couldn't get your location. Search for your address or tap the map instead.",
      );
    },
    { enableHighAccuracy: true, timeout: 10000 },
  );
});

// Delivery address = customers, restaurant details = owners, courier details = drivers
function updateRoleUI() {
  const role = selectedRole();
  addressSection.classList.toggle("d-none", role !== "CUSTOMER");
  restaurantSection.classList.toggle("d-none", role !== "RESTAURANT_OWNER");
  driverSection.classList.toggle("d-none", role !== "DRIVER");
  if (role === "RESTAURANT_OWNER") {
    ensureMap();
    if (map) setTimeout(() => map.invalidateSize(), 100);
  }
}
form
  .querySelectorAll('input[name="role"]')
  .forEach((r) => r.addEventListener("change", updateRoleUI));
updateRoleUI();

/* ---------- validation ---------- */
function validate() {
  let ok = true;
  const fail = (input, msg) => {
    setError(input, msg);
    ok = false;
  };

  if (nameInput.value.trim().length < 2)
    fail(nameInput, "Enter your full name.");
  if (!/^\S+@\S+\.\S+$/.test(emailInput.value.trim()))
    fail(emailInput, "Enter a valid email address.");
  if (!PHONE_RE.test(cleanPhone(phoneInput.value)))
    fail(phoneInput, "Enter a valid phone number.");
  if (!passwordIsStrong())
    fail(
      passwordInput,
      "Your password needs to meet all the requirements above.",
    );
  else if (
    new TextEncoder().encode(passwordInput.value).length > MAX_PASSWORD_BYTES
  )
    fail(
      passwordInput,
      "Your password is too long. Use 72 characters or fewer.",
    );
  if (confirmInput.value !== passwordInput.value || !confirmInput.value)
    fail(confirmInput, "Passwords don't match.");

  if (selectedRole() === "CUSTOMER") {
    addressList.querySelectorAll(".address-block").forEach((block) => {
      const nick = block.querySelector(".addr-nick");
      const street = block.querySelector(".addr-street");
      const building = block.querySelector(".addr-building");
      if (!nick.value.trim()) fail(nick, "Add a nickname, like Home or Work.");
      if (!street.value.trim()) fail(street, "Enter your street name.");
      if (!building.value.trim())
        fail(building, "Enter your building, floor and apartment.");
    });
  }
  if (selectedRole() === "RESTAURANT_OWNER") {
    if (restName.value.trim().length < 2)
      fail(restName, "Enter your restaurant's name.");
    if (!restBranch.value.trim())
      fail(restBranch, "Enter the branch name, like Downtown.");
    if (!restCuisine.value.trim())
      fail(restCuisine, "Enter your cuisine, like Egyptian or Pizza.");
    if (!restAddress.value.trim())
      fail(restAddress, "Enter your restaurant's address.");
    if (restPhone.value.trim() && !PHONE_RE.test(cleanPhone(restPhone.value)))
      fail(restPhone, "Enter a valid phone number, or leave it empty.");
    if (
      !geo ||
      !(geo.lat >= -90 && geo.lat <= 90 && geo.lng >= -180 && geo.lng <= 180)
    ) {
      fail(
        locQuery,
        "Pin your restaurant on the map: search for it, use your current location, or tap the map.",
      );
    }

    const num = (input, min, max, msg) => {
      const raw = input.value.trim();
      const n = Number(raw);
      if (raw === "" || !Number.isFinite(n) || n < min || n > max)
        fail(input, msg);
    };
    num(deliveryFee, 0, 1000, "Enter 0 or more (max 1000).");
    num(minOrder, 0, 10000, "Enter 0 or more (max 10000).");
    num(prepTime, 1, 240, "Enter 1 to 240 minutes.");
    num(maxDistance, 1, 100, "Enter 1 to 100 km.");
  }
  if (selectedRole() === "DRIVER") {
    if (!NATIONAL_ID_RE.test(normalizeNationalId(nationalIdInput.value)))
      fail(nationalIdInput, "Enter your 14-digit national ID.");
    const plate = normalizePlate(plateInput.value);
    if (!PLATE_RE.test(plate) || !/\d/.test(plate))
      fail(plateInput, "Enter a valid plate number, like ABC 1234.");
    if (!LICENSE_RE.test(normalizeLicense(licenseInput.value)))
      fail(licenseInput, "Enter your driving license number.");
  }
  return ok;
}

/* ---------- submit ---------- */
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  hideNotice();
  if (!validate()) {
    const firstBad = form.querySelector(".input-error");
    if (firstBad) firstBad.focus();
    return;
  }

  const role = selectedRole();
  const payload = {
    name: nameInput.value.trim(),
    email: emailInput.value.trim(),
    phone: phoneInput.value.replace(/[\s-]/g, ""),
    password: passwordInput.value,
    role,
    addresses:
      role === "CUSTOMER"
        ? [...addressList.querySelectorAll(".address-block")].map((b) => ({
            nickname: b.querySelector(".addr-nick").value.trim(),
            street: b.querySelector(".addr-street").value.trim(),
            building: b.querySelector(".addr-building").value.trim(),
          }))
        : [],
  };
  if (role === "DRIVER") {
    payload.driver = {
      nationalId: normalizeNationalId(nationalIdInput.value),
      plateNumber: normalizePlate(plateInput.value),
      drivingLicense: normalizeLicense(licenseInput.value),
    };
  }
  if (role === "RESTAURANT_OWNER") {
    payload.restaurant = {
      name: restName.value.trim(),
      branch: restBranch.value.trim(),
      cuisine: restCuisine.value.trim(),
      address: restAddress.value.trim(),
      phone: restPhone.value.trim() ? cleanPhone(restPhone.value) : null,
      gpsLocation: fmtCoord(geo.lat) + "," + fmtCoord(geo.lng), // "lat,lng" -> Restaurant.gpsLocation
      latitude: Number(fmtCoord(geo.lat)),
      longitude: Number(fmtCoord(geo.lng)),
      deliveryFee: Number(deliveryFee.value),
      minimumOrderAmount: Number(minOrder.value),
      prepTime: Number(prepTime.value),
      maxDeliveryDistance: Number(maxDistance.value),
    };
  }

  // Owners who picked a logo and/or cover image send multipart; everyone else sends plain JSON.
  let body = payload;
  const logoFile = role === "RESTAURANT_OWNER" ? logoUpload.file : null;
  const coverFile = role === "RESTAURANT_OWNER" ? coverUpload.file : null;
  if (logoFile || coverFile) {
    body = new FormData();
    body.append(
      "data",
      new Blob([JSON.stringify(payload)], { type: "application/json" }),
    );
    if (logoFile) body.append("logo", logoFile);
    if (coverFile) body.append("coverImage", coverFile);
  }

  let done = false;
  setLoading(true);
  try {
    // The 201 body is plain text, so we never call res.json() on success.
    const res = await postRequest("/api/auth/register", body);

    if (res.status === 201 || res.status === 202) {
      // 202 = accepted, waiting for review
      done = true; // keep the button disabled so the form can't be sent twice
      spinner.classList.add("d-none");
      btnText.textContent =
        role === "CUSTOMER" ? "Account created" : "Request submitted";
      if (role === "CUSTOMER") {
        showNotice("success", "Account created", "Taking you to log in...");
        setTimeout(() => {
          window.location.href = "login.html";
        }, 1500);
      } else {
        showNotice(
          "success",
          "Request submitted",
          "Your registration is waiting for admin approval. You can log in once an admin has accepted it.",
        );
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
      return;
    }

    const msg = await serverMessage(res);
    if (res.status === 409) {
      const m = msg.toLowerCase();
      const target = m.includes("national")
        ? [nationalIdInput, "This national ID is already registered."]
        : m.includes("plate")
          ? [plateInput, "This plate number is already registered."]
          : m.includes("licen")
            ? [licenseInput, "This driving license is already registered."]
            : m.includes("phone")
              ? [phoneInput, "This phone number is already in use."]
              : m.includes("email")
                ? [emailInput, "This email is already in use."]
                : null;
      if (target) {
        setError(target[0], target[1]);
        target[0].focus();
      } else {
        // Server didn't say which detail clashed (see server.error.include-message).
        showNotice(
          "error",
          "Already registered",
          "Your email, phone number or courier details are already in use. Check them and try again.",
        );
      }
    } else if (res.status === 400) {
      showNotice(
        "error",
        "Check your details",
        msg ||
          "Some of your details were not accepted. Review the form and try again.",
      );
    } else {
      showNotice(
        "error",
        "Something went wrong",
        msg || "Please try again in a moment.",
      );
    }
  } catch (err) {
    if (err.name === "AbortError") {
      showNotice("error", "The server took too long", "Try again in a moment.");
    } else {
      showNotice(
        "error",
        "Cannot reach the server",
        "Make sure the backend is running, then try again.",
      );
    }
  } finally {
    if (!done) setLoading(false);
  }
});
