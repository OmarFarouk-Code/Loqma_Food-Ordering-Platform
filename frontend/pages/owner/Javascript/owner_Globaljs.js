const statusToggle = document.getElementById("openStatusToggle");
const statusPill = document.getElementById("storeStatusPill");
const statusText = document.querySelector("#statusText");
const branchname = document.querySelector(".branch-name");
const ownername = document.querySelector(".username");
statusText.innerHTML = "Open now";
statusToggle.addEventListener("change", function () {
  if (this.checked) {
    statusPill.classList.remove("is-closed");
    statusText.innerHTML = "Open now";
  } else {
    statusPill.classList.add("is-closed");
    statusText.innerHTML = "Closed";
  }
});
