const flightResults = document.querySelector("#flightResults");
const bookingResults = document.querySelector("#bookingResults");
const pendingApprovals = document.querySelector("#pendingApprovals");
const searchForm = document.querySelector("#searchForm");
const adminForm = document.querySelector("#adminForm");
const statusMessage = document.querySelector("#status");
const viewPanels = document.querySelectorAll("[data-view]");
const viewLinks = document.querySelectorAll("[data-view-link]");

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  loadFlights(new FormData(searchForm));
});

adminForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const message = await postForm("/admin/flights", new FormData(adminForm));
  showStatus(message);
  await loadPendingApprovals();
  showView("manager");
});

document.addEventListener("submit", async (event) => {
  if (!event.target.matches("[data-booking-form], [data-approval-form]")) {
    return;
  }

  event.preventDefault();
  const form = event.target;
  const isBooking = form.matches("[data-booking-form]");
  const response = isBooking
    ? await postJsonForm("/flights/book", new FormData(form))
    : { message: await postForm("/manager/approve", new FormData(form)) };

  showStatus(response.message);
  await Promise.all([loadFlights(new FormData(searchForm)), loadPendingApprovals(), loadBookings()]);
  showView(isBooking ? "bookings" : "search");
});

window.addEventListener("hashchange", () => {
  showView(currentView());
});

function showView(viewName) {
  viewPanels.forEach((panel) => {
    panel.hidden = panel.dataset.view !== viewName;
  });
  viewLinks.forEach((link) => {
    link.classList.toggle("active", link.dataset.viewLink === viewName);
  });
  if (window.location.hash !== `#${viewName}`) {
    history.replaceState(null, "", `#${viewName}`);
  }
}

function currentView() {
  const viewName = window.location.hash.replace("#", "");
  return document.querySelector(`[data-view="${viewName}"]`) ? viewName : "search";
}

async function loadFlights(formData = new FormData()) {
  const params = new URLSearchParams(formData);
  const response = await fetch(`/flights?${params.toString()}`);
  const flights = await response.json();
  flightResults.innerHTML = flights.map(renderFlight).join("") || "<p>No flights found.</p>";
}

async function loadPendingApprovals() {
  const response = await fetch("/admin/pending");
  const flights = await response.json();
  pendingApprovals.innerHTML = flights.map(renderPendingFlight).join("") || "<p>No pending approvals.</p>";
}

async function loadBookings() {
  const response = await fetch("/flights/bookings");
  const bookings = await response.json();
  bookingResults.innerHTML = bookings.map(renderBooking).join("") || "<p>No bookings confirmed yet.</p>";
}

async function postForm(url, formData) {
  const response = await fetch(url, {
    method: "POST",
    headers: { Accept: "application/json" },
    body: formData
  });
  return response.text();
}

async function postJsonForm(url, formData) {
  const response = await fetch(url, {
    method: "POST",
    headers: { Accept: "application/json" },
    body: formData
  });
  return response.json();
}

function showStatus(message) {
  statusMessage.hidden = false;
  statusMessage.textContent = message;
}

function renderFlight(flight) {
  return `
    <article class="flight-card">
      <strong>${escapeHtml(flight.flightId)}</strong>
      <p>${escapeHtml(flight.origin)} to ${escapeHtml(flight.destination)}</p>
      <p class="flight-meta">${escapeHtml(flight.seatClass)} · Rs. ${flight.price} · ${flight.availableSeats} seats</p>
      <form method="post" action="/flights/book" class="inline-form" data-booking-form>
        <input type="hidden" name="flightId" value="${escapeHtml(flight.flightId)}">
        <input type="hidden" name="seatClass" value="${escapeHtml(flight.seatClass)}">
        <input type="number" name="seatCount" min="1" max="${flight.availableSeats}" value="1" aria-label="Seat count">
        <button type="submit">Book</button>
      </form>
    </article>
  `;
}

function renderPendingFlight(flight) {
  return `
    <article class="flight-card">
      <strong>${escapeHtml(flight.flightId)}</strong>
      <p>${escapeHtml(flight.origin)} to ${escapeHtml(flight.destination)}</p>
      <p class="flight-meta">${escapeHtml(flight.seatClass)} · Rs. ${flight.price} · ${flight.totalSeats} seats</p>
      <form method="post" action="/manager/approve" class="inline-form" data-approval-form>
        <input type="hidden" name="flightId" value="${escapeHtml(flight.flightId)}">
        <input type="hidden" name="seatClass" value="${escapeHtml(flight.seatClass)}">
        <button type="submit">Approve</button>
      </form>
    </article>
  `;
}

function renderBooking(booking) {
  return `
    <article class="flight-card">
      <strong>${escapeHtml(booking.bookingId)}</strong>
      <p>${escapeHtml(booking.flightId)} · ${escapeHtml(booking.seatClass)}</p>
      <p class="flight-meta">${booking.count} seats · Rs. ${booking.totalPrice} · ${escapeHtml(booking.username)}</p>
    </article>
  `;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

loadFlights(new FormData(searchForm)).catch(() => {
  statusMessage.hidden = false;
  statusMessage.textContent = "Unable to load flights. Check that Spring Boot is running.";
});
loadPendingApprovals();
loadBookings();
showView(currentView());
