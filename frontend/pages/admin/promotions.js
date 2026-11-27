// Calls the coupon backend: GET/POST /api/admin/coupons, PATCH /api/admin/coupons/{id}/activate|deactivate
const COUPONS_API = '/api/admin/coupons';
let coupons = [];

const $ = id => document.getElementById(id);
const egp = n => 'EGP ' + Number(n).toFixed(2);

// Backend field names -> the names this page already uses.
function toView(c) {
  const today = new Date(); today.setHours(0, 0, 0, 0);
  const date = new Date(c.expiryDate + 'T00:00:00');
  return {
    id: c.id,
    code: c.code,
    amount: Number(c.discountAmount),
    min: Number(c.minimumOrderAmount),
    expiry: date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
    used: c.usageCount,
    active: c.active,
    status: !c.active ? 'Inactive' : (date < today ? 'Expired' : 'Active')
  };
}

// Nothing that comes back from the API is ever parsed as HTML: nodes are built
// with createElement and filled with textContent.
function el(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function addCell(row, className, text) {
  const td = el('td', className, text);
  row.appendChild(td);
  return td;
}

async function loadCoupons() {
  try {
    const page = await api.get(COUPONS_API + '?size=100');
    coupons = page.content.map(toView);
    render();
  } catch (err) {
    showLoadError(err.message);
  }
}

function showLoadError(message) {
  const tbody = $('promo-rows');
  tbody.replaceChildren();
  const row = document.createElement('tr');
  const td = el('td', 'text-danger', 'Could not load coupons (' + message + '). Is the backend running on port 8080?');
  td.colSpan = 6;
  row.appendChild(td);
  tbody.appendChild(row);

  const cards = $('promo-cards-list');
  cards.replaceChildren();
  cards.appendChild(el('div', 'promo-mcard text-danger', 'Could not load coupons (' + message + ').'));
}
function render() {
  const q = ($('filter').value || '').toLowerCase();
  const shown = coupons.filter(c => c.code.toLowerCase().includes(q));

  const tbody = $('promo-rows');
  tbody.replaceChildren();
  if (shown.length === 0) {
    const row = document.createElement('tr');
    const td = el('td', 'text-muted', 'No coupons yet.');
    td.colSpan = 6;
    row.appendChild(td);
    tbody.appendChild(row);
  } else {
    shown.forEach(c => {
      const row = document.createElement('tr');
      addCell(row, '', '').appendChild(el('span', 'code-chip', c.code));
      addCell(row, 'fw-semibold', egp(c.amount));
      addCell(row, 'text-muted', egp(c.min));
      addCell(row, 'text-muted', c.expiry);
      const usage = addCell(row, '', '');
      usage.appendChild(el('b', '', String(c.used)));
      usage.appendChild(el('span', 'text-muted', ' redemptions'));
      addCell(row, '', '').appendChild(
        el('span', 'st ' + (c.status === 'Active' ? 'st-active' : 'st-scheduled'), c.status));
      tbody.appendChild(row);
    });
  }

  const cards = $('promo-cards-list');
  cards.replaceChildren();
  coupons.forEach(c => cards.appendChild(buildCard(c)));

  document.querySelectorAll('.count').forEach(e => e.textContent = coupons.length);
  $('stat-issued').textContent = coupons.length;
  $('stat-redeemed').textContent = coupons.reduce((s, c) => s + c.used, 0);
}

function actionButton(label, look, act, disabled) {
  const btn = el('button', 'btn btn-sm flex-fill ' + look, label);
  btn.type = 'button';
  btn.dataset.act = act;
  btn.disabled = Boolean(disabled);
  return btn;
}
function buildCard(c) {
  const locked = c.used > 0;
  const card = el('div', 'promo-mcard');
  card.dataset.id = c.id;

  const top = el('div', 'd-flex justify-content-between align-items-start');
  const left = document.createElement('div');

  const title = document.createElement('div');
  title.appendChild(el('b', '', c.code));
  title.appendChild(document.createTextNode(' '));
  title.appendChild(el('span', 'badge ' + (c.status === 'Active' ? 'badge-active' : 'badge-sched'), c.status));
  left.appendChild(title);

  left.appendChild(el('div', 'small fw-semibold mt-1',
    egp(c.amount) + ' off \u00b7 Min. order ' + egp(c.min)));
  left.appendChild(el('div', 'small text-muted',
    'Expires: ' + c.expiry + ' \u00b7 Used ' + c.used + ' times'));

  top.appendChild(left);
  card.appendChild(top);

  card.appendChild(el('div', (locked ? 'lock' : 'ok') + ' my-2',
    locked
      ? '\uD83D\uDD12 Locked: Used coupons cannot be edited or deactivated.'
      : 'Zero customer redemptions recorded. Parameters can still be updated.'));

  const actions = el('div', 'd-flex gap-2');
  actions.appendChild(actionButton('Edit', locked ? 'btn-light disabled' : 'btn-outline-secondary', 'edit', locked));
  actions.appendChild(actionButton(c.active ? 'Deactivate' : 'Activate',
    locked ? 'btn-light disabled' : 'btn-outline-danger', c.active ? 'deactivate' : 'activate', locked));
  card.appendChild(actions);

  return card;
}

$('filter').addEventListener('input', render);

$('promo-cards-list').addEventListener('click', async e => {
  const btn = e.target.closest('[data-act]');
  if (!btn) return;
  const id = btn.closest('.promo-mcard').dataset.id;
  const act = btn.dataset.act;
  if (act === 'edit') { alert('Edit is the next step.'); return; }
  btn.disabled = true;
  try {
    await api.request('PATCH', COUPONS_API + '/' + id + '/' + act);
    await loadCoupons();
  } catch (err) {
    btn.disabled = false;
    alert('Could not ' + act + ': ' + err.message);
  }
});

$('coupon-form').addEventListener('submit', async e => {
  e.preventDefault();
  const code = $('f-code').value.trim().toUpperCase();
  const amount = Number($('f-amount').value);
  const min = Number($('f-min').value) || 0;
  const expiry = $('f-expiry').value;
  if (!code || !amount || !expiry) { alert('Fill in code, discount amount and expiry date.'); return; }
  try {
    await api.post(COUPONS_API, {
      name: code,
      code: code,
      discountAmount: amount,
      minimumOrderAmount: min,
      expiryDate: expiry
    });
    $('code-err').classList.add('d-none');
    $('f-code').classList.remove('is-invalid');
    e.target.reset();
    showTab('list');
    await loadCoupons();
  } catch (err) {
    if (err.message.startsWith('409')) {
      $('code-err').classList.remove('d-none');
      $('f-code').classList.add('is-invalid');
    } else {
      alert('Could not save: ' + err.message);
    }
  }
});

// Mobile tabs: on small screens show either the list or the create form.
function showTab(t) {
  document.querySelectorAll('#promo-tabs button').forEach(b => b.classList.toggle('on', b.dataset.tab === t));
  $('create-col').classList.toggle('d-none', t !== 'create');
  $('create-col').classList.toggle('d-lg-block', true);
  $('list-col').classList.toggle('d-none', t !== 'list');
  $('list-col').classList.toggle('d-lg-block', true);
}
document.querySelectorAll('#promo-tabs button').forEach(b => b.addEventListener('click', () => showTab(b.dataset.tab)));

showTab('list');
loadCoupons();