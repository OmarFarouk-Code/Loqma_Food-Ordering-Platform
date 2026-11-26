// Fake data. Replace with API calls when the backend exists.
    const coupons = [
      { code: 'WELCOME30', amount: 30,  min: 150, expiry: '31 Dec 2026', used: 142, status: 'Active' },
      { code: 'LUNCH20',   amount: 20,  min: 100, expiry: '30 Jun 2026', used: 88,  status: 'Active' },
      { code: 'FRIDAY50',  amount: 50,  min: 250, expiry: '28 Feb 2026', used: 215, status: 'Active' },
      { code: 'RAMADAN100',amount: 100, min: 400, expiry: '15 Mar 2026', used: 0,   status: 'Scheduled' }
    ];
    const $ = id => document.getElementById(id);
    const egp = n => 'EGP ' + n.toFixed(2);

    function render() {
      const q = ($('filter').value || '').toLowerCase();
      const shown = coupons.filter(c => c.code.toLowerCase().includes(q));
      $('promo-rows').innerHTML = shown.map(c => `
        <tr>
          <td><span class="code-chip">${c.code}</span></td>
          <td class="fw-semibold">${egp(c.amount)}</td>
          <td class="text-muted">${egp(c.min)}</td>
          <td class="text-muted">${c.expiry}</td>
          <td><b>${c.used}</b> <span class="text-muted">redemptions</span></td>
          <td><span class="st ${c.status === 'Active' ? 'st-active' : 'st-scheduled'}">${c.status}</span></td>
        </tr>`).join('');
      $('promo-cards-list').innerHTML = coupons.map(c => {
        const locked = c.used > 0;
        return `
        <div class="promo-mcard">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div><b>${c.code}</b> <span class="badge ${c.status === 'Active' ? 'badge-active' : 'badge-sched'}">${c.status}</span></div>
              <div class="small fw-semibold mt-1">${egp(c.amount)} off &middot; Min. order ${egp(c.min)}</div>
              <div class="small text-muted">${locked ? 'Expires' : 'Starts'}: ${c.expiry} &middot; <b>Used ${c.used} times</b></div>
            </div>
          </div>
          <div class="${locked ? 'lock' : 'ok'} my-2">${locked ? '&#128274; Locked: Used coupons cannot be edited or deactivated.' : 'Zero customer redemptions recorded. Parameters can still be updated.'}</div>
          <div class="d-flex gap-2">
            <button class="btn btn-sm flex-fill ${locked ? 'btn-light disabled' : 'btn-outline-secondary'}" type="button" data-act="edit">Edit</button>
            <button class="btn btn-sm flex-fill ${locked ? 'btn-light disabled' : 'btn-outline-danger'}" type="button" data-act="deactivate">Deactivate</button>
          </div>
        </div>`;
      }).join('');
      document.querySelectorAll('.count').forEach(e => e.textContent = coupons.length);
      $('stat-issued').textContent = coupons.length;
      $('stat-redeemed').textContent = coupons.reduce((s, c) => s + c.used, 0);
    }

    $('filter').addEventListener('input', render);
    $('promo-cards-list').addEventListener('click', e => {
      if (e.target.dataset.act) alert('Not connected to the backend yet.');
    });

    $('coupon-form').addEventListener('submit', e => {
      e.preventDefault();
      const code = $('f-code').value.trim().toUpperCase();
      const amount = Number($('f-amount').value), min = Number($('f-min').value);
      const expiry = $('f-expiry').value;
      const dup = coupons.some(c => c.code === code);
      $('code-err').classList.toggle('d-none', !dup);
      $('f-code').classList.toggle('is-invalid', dup);
      if (!code || dup || !amount || !expiry) { if (!code || !amount || !expiry) alert('Fill in code, discount amount and expiry date.'); return; }coupons.unshift({ code, amount, min, expiry: new Date(expiry).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }), used: 0, status: 'Scheduled' });
      e.target.reset();
      render();
      showTab('list');
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

    render();
    showTab('list');
        