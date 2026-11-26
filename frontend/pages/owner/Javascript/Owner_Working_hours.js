const HOURS_API_URL = "http://localhost:8080/api/owner/operating-hours";

const DAYS_ORDER = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

const weeklySchedule = document.querySelector(".Weekly-Schedule");
const specialDaysList = document.getElementById("specialDaysList");
const addSpecialDayBtn = document.getElementById("addSpecialDayBtn");
const scheduleErrorAlert = document.getElementById("scheduleErrorAlert");
const discardChangesBtn = document.getElementById("discardChangesBtn");
const saveScheduleBtn = document.getElementById("saveScheduleBtn");

let initialWeeklySnapshot = "";
let initialSpecialSnapshot = "";

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

function toComparableMinutes(timeStr, isEnd = false) {
  if (!timeStr) return null;
  const [h, m] = timeStr.split(":").map(Number);
  let total = h * 60 + m;
  if (isEnd && h < 5) {
    total += 24 * 60;
  }
  return total;
}

function getPeriodInputPairs(container) {
  const inputs = Array.from(container.querySelectorAll('input[type="time"]'));
  const pairs = [];
  for (let i = 0; i < inputs.length; i += 2) {
    if (inputs[i] && inputs[i + 1]) {
      pairs.push({ startInput: inputs[i], endInput: inputs[i + 1] });
    }
  }
  return pairs;
}

function validateAllSchedules() {
  let hasAnyError = false;

  document.querySelectorAll(".day-schedule-row").forEach((row) => {
    const selectedMode = row.querySelector(
      'input[type="radio"]:checked',
    )?.value;
    const container = row.querySelector(".periods-container");
    if (!container) return;

    const oldError = container.querySelector(".period-error-msg");
    if (oldError) oldError.remove();

    if (selectedMode !== "open") return;

    const periodGroups = getPeriodInputPairs(container);
    let dayHasError = false;

    periodGroups.forEach(({ startInput, endInput }) => {
      const startMins = toComparableMinutes(startInput.value, false);
      const endMins = toComparableMinutes(endInput.value, true);

      const isInvalid =
        startMins === null || endMins === null || endMins <= startMins;

      [startInput, endInput].forEach((input) => {
        if (isInvalid) {
          input.classList.remove("text-dark");
          input.classList.add("text-danger");
          input.style.backgroundColor = "#fdeeee";
        } else {
          input.classList.remove("text-danger");
          input.classList.add("text-dark");
          input.style.backgroundColor = "#efefef";
        }
      });

      if (isInvalid) {
        dayHasError = true;
        hasAnyError = true;
      }
    });

    if (dayHasError) {
      const errorDiv = document.createElement("div");
      errorDiv.className =
        "period-error-msg w-100 text-danger d-flex align-items-center gap-1 mt-1";
      errorDiv.style.fontSize = "0.75rem";
      errorDiv.innerHTML = `
        <i class="fa-solid fa-circle-exclamation"></i>
        <span>Closing time must be after opening time</span>
      `;
      container.appendChild(errorDiv);
    }
  });

  if (scheduleErrorAlert) {
    scheduleErrorAlert.classList.toggle("d-none", !hasAnyError);
    scheduleErrorAlert.classList.toggle("d-flex", hasAnyError);
  }

  if (saveScheduleBtn) {
    saveScheduleBtn.disabled = hasAnyError;
    saveScheduleBtn.style.opacity = hasAnyError ? "0.6" : "1";
  }

  return !hasAnyError;
}

function renderPeriods(container, periods) {
  if (periods.length <= 1) {
    const p = periods[0] || { start: "11:00", end: "23:30" };
    container.className =
      "periods-container d-flex flex-wrap align-items-center gap-2 gap-sm-3 ps-md-5 ms-md-5";
    container.innerHTML = `
      <input
        type="time"
        class="form-control border-0 rounded-3 px-3 py-2 text-dark fw-medium shadow-none w-auto"
        style="background-color: #efefef; min-width: 135px"
        value="${p.start}"
      />
      <span class="text-secondary small">to</span>
      <input
        type="time"
        class="form-control border-0 rounded-3 px-3 py-2 text-dark fw-medium shadow-none w-auto"
        style="background-color: #efefef; min-width: 135px"
        value="${p.end}"
      />
      <button
        type="button"
        class="btn btn-link text-dark text-decoration-none fw-medium small px-2 py-1 add-period-btn"
      >
        + Add break / second period
      </button>
    `;
  } else {
    container.className =
      "periods-container d-flex flex-column gap-2 ps-md-5 ms-md-5";
    container.innerHTML = periods
      .map(
        (p, idx) => `
        <div class="period-item d-flex flex-wrap align-items-center gap-2 gap-sm-3">
          <span class="text-secondary" style="font-size: 0.75rem; min-width: 52px">
            Period ${idx + 1}
          </span>
          <input
            type="time"
            class="form-control border-0 rounded-3 px-3 py-2 text-dark fw-medium shadow-none w-auto"
            style="background-color: #efefef; min-width: 135px"
            value="${p.start}"
          />
          <span class="text-secondary small">to</span>
          <input
            type="time"
            class="form-control border-0 rounded-3 px-3 py-2 text-dark fw-medium shadow-none w-auto"
            style="background-color: #efefef; min-width: 135px"
            value="${p.end}"
          />
          ${
            idx === 0
              ? `<span class="badge rounded-2 fw-normal text-secondary px-2 py-1 ms-auto" style="background-color: #f3f3f3; font-size: 0.75rem">Lunch shift</span>`
              : `<button type="button" class="btn btn-link text-secondary text-decoration-none p-1 ms-auto remove-period-btn" aria-label="Remove period">
                  <i class="fa-solid fa-xmark"></i>
                </button>`
          }
        </div>
      `,
      )
      .join("");
  }
}

function setDayState(row, mode, periods = [{ start: "11:00", end: "23:30" }]) {
  const targetRadio = row.querySelector(`input[type="radio"][value="${mode}"]`);
  if (targetRadio) targetRadio.checked = true;

  row.querySelectorAll('input[type="radio"]').forEach((radio) => {
    const label = row.querySelector(`label[for="${radio.id}"]`);
    if (radio.checked) {
      radio.classList.remove("border-secondary");
      radio.classList.add("border-dark");
      if (label && mode === "dayoff") {
        label.classList.remove("text-secondary");
        label.classList.add("text-dark", "fw-medium");
      }
    } else {
      radio.classList.remove("border-dark");
      radio.classList.add("border-secondary");
      if (label) {
        label.classList.remove("text-dark", "fw-medium");
        label.classList.add("text-secondary");
      }
    }
  });

  let container = row.querySelector(".periods-container");
  if (!container) {
    container = row.children[1];
    container.classList.add("periods-container");
  }

  if (mode === "dayoff") {
    container.className = "periods-container ps-md-5 ms-md-5";
    container.innerHTML = `<span class="fst-italic text-secondary small">Closed all day</span>`;
  } else if (mode === "24hours") {
    container.className = "periods-container ps-md-5 ms-md-5";
    container.innerHTML = `<span class="fst-italic text-secondary small">Open 24 hours</span>`;
  } else {
    renderPeriods(container, periods);
  }
}

function formatDisplayDate(isoDate) {
  if (!isoDate) return "";
  const date = new Date(isoDate + "T00:00:00");
  return date.toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function createSpecialDayCard({ title, rawDate, isClosed, start, end }) {
  const card = document.createElement("div");
  card.className =
    "special-day-item rounded-3 px-3 py-3 d-flex flex-wrap justify-content-between align-items-center gap-2";
  card.style.backgroundColor = "#f7f7f7";
  card.dataset.title = title || "Holiday / Special Hours";
  card.dataset.rawDate = rawDate;
  card.dataset.isClosed = isClosed ? "true" : "false";
  card.dataset.start = start || "14:00";
  card.dataset.end = end || "02:00";

  const iconClass = isClosed
    ? "fa-regular fa-calendar"
    : "fa-solid fa-wand-magic-sparkles";

  const badgeHtml = isClosed
    ? `<span class="badge rounded-2 fw-normal text-secondary px-2 py-1" style="background-color: #e9e9e9; font-size: 0.75rem">Closed all day</span>`
    : `<span class="badge rounded-pill fw-medium text-dark px-3 py-1 bg-white border" style="font-size: 0.75rem">${start} – ${end}</span>`;

  card.innerHTML = `
    <div class="d-flex align-items-center gap-3">
      <i class="${iconClass} text-secondary fs-5"></i>
      <div>
        <div class="fw-bold text-dark small">${card.dataset.title}</div>
        <div class="text-secondary" style="font-size: 0.75rem">${formatDisplayDate(rawDate)}</div>
      </div>
    </div>
    <div class="d-flex align-items-center gap-2 ms-auto">
      ${badgeHtml}
      <button type="button" class="btn btn-link text-secondary p-1 text-decoration-none" aria-label="Edit special day">
        <i class="fa-solid fa-pen" style="font-size: 0.8rem"></i>
      </button>
      <button type="button" class="btn btn-link text-secondary p-1 text-decoration-none" aria-label="Delete special day">
        <i class="fa-regular fa-trash-can" style="font-size: 0.8rem"></i>
      </button>
    </div>
  `;
  return card;
}

// 1. Load Operating Hours & Store Status from ScheduleResponse
async function loadOperatingHours() {
  try {
    const response = await fetch(HOURS_API_URL, {
      method: "GET",
      headers: getHeaders(),
      cache: "no-store",
    });

    if (!response.ok) {
      throw new Error(`GET failed: ${response.status}`);
    }

    const data = await response.json();

    if (typeof data.open === "boolean" && typeof syncStatus === "function") {
      syncStatus(data.open);
    }

    const allHours = Array.isArray(data.hours) ? data.hours : [];
    const regularHours = allHours.filter((h) => !h.isSpecialDay);
    const specialHours = allHours.filter((h) => h.isSpecialDay);

    if (regularHours.length === 0) {
      DAYS_ORDER.forEach((day) => {
        const row = document.querySelector(
          `.day-schedule-row[data-day="${day}"]`,
        );
        if (row) setDayState(row, "open", [{ start: "11:00", end: "23:30" }]);
      });
    } else {
      const groupedByDay = {};
      regularHours.forEach((item) => {
        const dayKey = (item.dayOfWeek || "").toUpperCase();
        if (!groupedByDay[dayKey]) groupedByDay[dayKey] = [];
        groupedByDay[dayKey].push(item);
      });

      DAYS_ORDER.forEach((day) => {
        const row = document.querySelector(
          `.day-schedule-row[data-day="${day}"]`,
        );
        if (!row) return;

        const entries = groupedByDay[day];
        if (!entries || entries.length === 0) {
          setDayState(row, "open", [{ start: "11:00", end: "23:30" }]);
          return;
        }

        const first = entries[0];
        if (first.isDayOff) {
          setDayState(row, "dayoff");
        } else if (first.is24Hours) {
          setDayState(row, "24hours");
        } else {
          const periods = entries.map((e) => ({
            start: (e.openingTime || "11:00").slice(0, 5),
            end: (e.closingTime || "23:30").slice(0, 5),
          }));
          setDayState(row, "open", periods);
        }
      });
    }

    specialDaysList.innerHTML = "";
    specialHours.forEach((s) => {
      if (!s.specialDate) return;
      const card = createSpecialDayCard({
        title: "Holiday / Special Hours",
        rawDate: s.specialDate,
        isClosed: Boolean(s.isDayOff),
        start: (s.openingTime || "14:00").slice(0, 5),
        end: (s.closingTime || "02:00").slice(0, 5),
      });
      specialDaysList.appendChild(card);
    });
  } catch (err) {
    console.warn("Could not load saved hours from backend:", err.message);
    DAYS_ORDER.forEach((day) => {
      const row = document.querySelector(
        `.day-schedule-row[data-day="${day}"]`,
      );
      if (row) setDayState(row, "open", [{ start: "11:00", end: "23:30" }]);
    });
  } finally {
    initialWeeklySnapshot = weeklySchedule.innerHTML;
    initialSpecialSnapshot = specialDaysList.innerHTML;
    validateAllSchedules();
  }
}

// 2. Weekly Schedule Event Listeners
weeklySchedule.addEventListener("input", (e) => {
  if (e.target.type === "time") {
    validateAllSchedules();
  }
});

weeklySchedule.addEventListener("change", (e) => {
  const row = e.target.closest(".day-schedule-row");
  if (!row) return;

  if (e.target.type === "radio") {
    setDayState(row, e.target.value);
    validateAllSchedules();
  } else if (e.target.type === "time") {
    validateAllSchedules();
  }
});

weeklySchedule.addEventListener("click", (e) => {
  const addBtn = e.target.closest(".add-period-btn");
  if (addBtn) {
    const container = addBtn.closest(".periods-container");
    const existing = getPeriodInputPairs(container).map((p) => ({
      start: p.startInput.value || "11:00",
      end: p.endInput.value || "16:00",
    }));
    existing.push({ start: "18:00", end: "23:30" });
    renderPeriods(container, existing);
    validateAllSchedules();
    return;
  }

  const removeBtn = e.target.closest(".remove-period-btn");
  if (removeBtn) {
    const container = removeBtn.closest(".periods-container");
    removeBtn.closest(".period-item").remove();
    const remaining = getPeriodInputPairs(container).map((p) => ({
      start: p.startInput.value,
      end: p.endInput.value,
    }));
    renderPeriods(container, remaining);
    validateAllSchedules();
  }
});

// 3. Special Days Add / Edit / Delete
function openSpecialDayForm(existingCard = null) {
  const existingForm = document.getElementById("inlineSpecialDayForm");
  if (existingForm) existingForm.remove();

  const isEdit = Boolean(existingCard);
  const titleVal = isEdit ? existingCard.dataset.title || "" : "";
  const dateVal = isEdit ? existingCard.dataset.rawDate || "" : "";
  const closedVal = isEdit ? existingCard.dataset.isClosed === "true" : true;
  const startVal = isEdit ? existingCard.dataset.start || "14:00" : "14:00";
  const endVal = isEdit ? existingCard.dataset.end || "02:00" : "02:00";

  const formCard = document.createElement("div");
  formCard.id = "inlineSpecialDayForm";
  formCard.className = "rounded-3 p-3 border bg-white mb-2";
  formCard.innerHTML = `
    <div class="row g-2 align-items-end">
      <div class="col-12 col-sm-5">
        <label class="form-label small text-secondary mb-1">Occasion / Holiday</label>
        <input type="text" id="sdTitle" class="form-control form-control-sm rounded-3" placeholder="e.g. Eid Al-Adha" value="${titleVal}" />
      </div>
      <div class="col-12 col-sm-4">
        <label class="form-label small text-secondary mb-1">Date</label>
        <input type="date" id="sdDate" class="form-control form-control-sm rounded-3" value="${dateVal}" />
      </div>
      <div class="col-12 col-sm-3">
        <div class="form-check mb-1">
          <input class="form-check-input" type="checkbox" id="sdClosed" ${closedVal ? "checked" : ""} />
          <label class="form-check-label small text-dark" for="sdClosed">Closed all day</label>
        </div>
      </div>
      <div id="sdTimeFields" class="col-12 d-flex align-items-center gap-2 mt-2 ${closedVal ? "d-none" : ""}">
        <input type="time" id="sdStart" class="form-control form-control-sm rounded-3 w-auto" value="${startVal}" />
        <span class="small text-secondary">to</span>
        <input type="time" id="sdEnd" class="form-control form-control-sm rounded-3 w-auto" value="${endVal}" />
      </div>
      <div class="col-12 d-flex justify-content-end gap-2 mt-2">
        <button type="button" id="sdCancelBtn" class="btn btn-sm btn-light rounded-pill px-3">Cancel</button>
        <button type="button" id="sdSaveBtn" class="btn btn-sm text-white rounded-pill px-3" style="background-color: #c81000">
          ${isEdit ? "Update" : "Add"}
        </button>
      </div>
    </div>
  `;

  specialDaysList.prepend(formCard);

  const sdClosed = formCard.querySelector("#sdClosed");
  const sdTimeFields = formCard.querySelector("#sdTimeFields");
  sdClosed.addEventListener("change", () => {
    sdTimeFields.classList.toggle("d-none", sdClosed.checked);
  });

  formCard.querySelector("#sdCancelBtn").addEventListener("click", () => {
    formCard.remove();
  });

  formCard.querySelector("#sdSaveBtn").addEventListener("click", () => {
    const title =
      formCard.querySelector("#sdTitle").value.trim() || "Special Day";
    const rawDate = formCard.querySelector("#sdDate").value;
    if (!rawDate) {
      alert("Please select a date.");
      return;
    }

    const newCard = createSpecialDayCard({
      title,
      rawDate,
      isClosed: sdClosed.checked,
      start: formCard.querySelector("#sdStart").value,
      end: formCard.querySelector("#sdEnd").value,
    });

    if (existingCard) {
      existingCard.replaceWith(newCard);
    } else {
      specialDaysList.appendChild(newCard);
    }
    formCard.remove();
  });
}

addSpecialDayBtn.addEventListener("click", () => openSpecialDayForm());

specialDaysList.addEventListener("click", (e) => {
  const deleteBtn = e.target.closest('button[aria-label="Delete special day"]');
  if (deleteBtn) {
    deleteBtn.closest(".special-day-item").remove();
    return;
  }

  const editBtn = e.target.closest('button[aria-label="Edit special day"]');
  if (editBtn) {
    openSpecialDayForm(editBtn.closest(".special-day-item"));
  }
});

// 4. Collect Payload Matching ScheduleUpdateRequest ({ hours: [...] })
function collectSchedulePayload() {
  const hours = [];

  document.querySelectorAll(".day-schedule-row").forEach((row) => {
    const dayOfWeek = row.dataset.day;
    const mode =
      row.querySelector('input[type="radio"]:checked')?.value || "open";
    const container = row.querySelector(".periods-container");

    if (mode === "dayoff") {
      hours.push({
        dayOfWeek,
        openingTime: null,
        closingTime: null,
        isDayOff: true,
        is24Hours: false,
        isSpecialDay: false,
        specialDate: null,
      });
    } else if (mode === "24hours") {
      hours.push({
        dayOfWeek,
        openingTime: "00:00:00",
        closingTime: "23:59:00",
        isDayOff: false,
        is24Hours: true,
        isSpecialDay: false,
        specialDate: null,
      });
    } else {
      const pairs = getPeriodInputPairs(container);
      pairs.forEach(({ startInput, endInput }) => {
        hours.push({
          dayOfWeek,
          openingTime: `${startInput.value}:00`,
          closingTime: `${endInput.value}:00`,
          isDayOff: false,
          is24Hours: false,
          isSpecialDay: false,
          specialDate: null,
        });
      });
    }
  });

  document.querySelectorAll(".special-day-item").forEach((card) => {
    const rawDate = card.dataset.rawDate;
    if (!rawDate) return;
    const isClosed = card.dataset.isClosed === "true";

    hours.push({
      dayOfWeek: null,
      openingTime: isClosed ? null : `${card.dataset.start}:00`,
      closingTime: isClosed ? null : `${card.dataset.end}:00`,
      isDayOff: isClosed,
      is24Hours: false,
      isSpecialDay: true,
      specialDate: rawDate,
    });
  });

  return { hours };
}

saveScheduleBtn.addEventListener("click", async () => {
  if (!validateAllSchedules()) return;

  const payload = collectSchedulePayload();
  const originalText = saveScheduleBtn.textContent;
  saveScheduleBtn.disabled = true;
  saveScheduleBtn.textContent = "Saving...";

  try {
    const response = await fetch(HOURS_API_URL, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errBody = await response.json().catch(() => ({}));
      throw new Error(errBody.message || `Save failed (${response.status})`);
    }

    initialWeeklySnapshot = weeklySchedule.innerHTML;
    initialSpecialSnapshot = specialDaysList.innerHTML;
    alert("Operating hours saved successfully!");
  } catch (err) {
    console.error("Error saving operating hours:", err);
    alert(err.message);
  } finally {
    saveScheduleBtn.disabled = false;
    saveScheduleBtn.textContent = originalText;
  }
});

discardChangesBtn.addEventListener("click", () => {
  if (initialWeeklySnapshot) {
    weeklySchedule.innerHTML = initialWeeklySnapshot;
  }
  if (initialSpecialSnapshot !== undefined) {
    specialDaysList.innerHTML = initialSpecialSnapshot;
  }
  validateAllSchedules();
});

// 5. Sync Live Store Status Switch with Backend
document.querySelectorAll("#openStatusToggle").forEach((toggle) => {
  toggle.addEventListener("change", async function () {
    try {
      await fetch(`${HOURS_API_URL}/status`, {
        method: "PATCH",
        headers: getHeaders(),
        body: JSON.stringify({ open: this.checked }),
      });
    } catch (err) {
      console.warn("Could not update store status:", err.message);
    }
  });
});

document.addEventListener("DOMContentLoaded", () => {
  loadOperatingHours();
});
