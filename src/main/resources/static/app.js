const flightResults = document.querySelector("#flightResults");
const pendingApprovals = document.querySelector("#pendingApprovals");
const searchForm = document.querySelector("#searchForm");
const statusMessage = document.querySelector("#status");

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  loadFlights(new FormData(searchForm));
});

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

function renderFlight(flight) {
  return `
    <article class="flight-card">
      <strong>${escapeHtml(flight.flightId)}</strong>
      <p>${escapeHtml(flight.origin)} to ${escapeHtml(flight.destination)}</p>
      <p class="flight-meta">${escapeHtml(flight.seatClass)} · Rs. ${flight.price} · ${flight.availableSeats} seats</p>
      <form method="post" action="/flights/book" class="inline-form">
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
      <form method="post" action="/manager/approve" class="inline-form">
        <input type="hidden" name="flightId" value="${escapeHtml(flight.flightId)}">
        <input type="hidden" name="seatClass" value="${escapeHtml(flight.seatClass)}">
        <button type="submit">Approve</button>
      </form>
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
