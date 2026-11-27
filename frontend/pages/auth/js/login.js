// Login -> POST {API_BASE_URL}/api/auth/login
// Request:  { email, password }
// Response: 200 { token, role } | 401 wrong details | 403 account pending / rejected / deactivated

// Where each role lands after login. Change these to your real page names.
const ROLE_HOME = {
  CUSTOMER: "../customer/home.html",
  RESTAURANT_OWNER: "../owner/menu.html",
  DRIVER: "../driver/available-orders.html",
  ADMIN: "../admin/dashboard.html",
};

const form = document.getElementById("loginForm");
const notice = document.getElementById("notice");
const submitBtn = document.getElementById("submitBtn");
const spinner = document.getElementById("spinner");
const btnText = document.getElementById("btnText");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");

/* ---------- helpers ---------- */
const REQUEST_TIMEOUT_MS = 15000;

// POST JSON to the backend, giving up after 15s so the button never spins forever.
async function postJson(path, body) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), REQUEST_TIMEOUT_MS);
  try {
    return await fetch(API_BASE_URL + path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      signal: ctrl.signal,
    });
  } finally {
    clearTimeout(timer);
  }
}

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
  btnText.textContent = on ? "Logging in..." : "Log in";
}

// Spring only returns "message" if server.error.include-message=always is set.
async function serverMessage(res) {
  try {
    return (await res.json()).message || "";
  } catch (_) {
    return "";
  }
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

[emailInput, passwordInput].forEach((i) =>
  i.addEventListener("input", () => setError(i, "")),
);

/* ---------- guest prompt (e.g. send people here with ?reason=login-required) ---------- */
if (new URLSearchParams(location.search).get("reason") === "login-required") {
  showNotice("info", "Log in to continue", "You need an account to do that.");
}

/* ---------- submit ---------- */
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  hideNotice();

  const email = emailInput.value.trim();
  const password = passwordInput.value;
  let valid = true;
  if (!/^\S+@\S+\.\S+$/.test(email)) {
    setError(emailInput, "Enter a valid email address.");
    valid = false;
  }
  if (!password) {
    setError(passwordInput, "Enter your password.");
    valid = false;
  }
  if (!valid) return;

  let redirecting = false;
  setLoading(true);
  try {
    const res = await postJson("/api/auth/login", { email, password });

    if (res.ok) {
      const data = await res.json(); // { token, role }
      localStorage.setItem("loqma_token", data.token);
      localStorage.setItem("loqma_role", data.role);
      redirecting = true; // keep the button disabled until the page changes
      window.location.href = ROLE_HOME[data.role] || "../../index.html";
      return;
    }

    const msg = (await serverMessage(res)).toLowerCase();
    if (res.status === 401) {
      showNotice(
        "error",
        "Wrong details",
        "The email or password is incorrect. Check them and try again.",
      );
    } else if (res.status === 403) {
      if (msg.includes("rejected")) {
        showNotice(
          "error",
          "Registration rejected",
          "An admin rejected your registration. Contact support for help.",
        );
      } else if (msg.includes("deactivated")) {
        showNotice(
          "error",
          "Account deactivated",
          "Your account has been deactivated. Contact support for help.",
        );
      } else if (msg.includes("pending")) {
        showNotice(
          "warning",
          "Pending approval",
          "Your registration is still waiting for admin approval. Try again once it has been accepted.",
        );
      } else {
        // Server didn't send a message (see server.error.include-message), so stay neutral.
        showNotice(
          "warning",
          "You can't log in yet",
          "Your registration may be waiting for admin approval, or the account was rejected or deactivated.",
        );
      }
    } else {
      showNotice(
        "error",
        "Something went wrong",
        "Please try again in a moment.",
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
    if (!redirecting) setLoading(false);
  }
});
