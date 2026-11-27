// Shared admin layout. Every admin page loads this file, so the nav is written once.
// A page needs: <body data-page="menu">, #admin-topbar, #admin-sidebar, #admin-content.
const ADMIN_LINKS = [
  { page: 'dashboard',  label: 'Dashboard',  href: 'dashboard.html' },
  { page: 'approvals',  label: 'Approvals',  href: 'approvals.html' },
  { page: 'orders',     label: 'Orders',     href: 'orders.html' },
  { page: 'promotions', label: 'Promotions', href: 'promotions.html' },
  
];

function renderAdminLayout() {
  const current = document.body.dataset.page;

  document.getElementById('admin-topbar').innerHTML = `
    <nav class="navbar bg-white border-bottom px-3 gap-3">
      <button id="menu-toggle" class="btn btn-outline-dark btn-sm d-sm-none" type="button"
              aria-label="Toggle menu" aria-controls="admin-sidebar" aria-expanded="false">&#9776;</button>
      <span class="fw-bold fs-4 text-danger">Loqma <span class="badge bg-dark fs-6 align-middle">ADMIN</span></span>
      <input class="form-control form-control-sm w-25 d-none d-md-block" placeholder="Search orders, partners...">
      <div class="d-flex align-items-center gap-3">
        <span class="badge text-bg-success d-none d-md-inline">All systems operational</span>
        <span class="rounded-circle bg-danger text-white d-inline-flex justify-content-center align-items-center" style="width:32px;height:32px">A</span>
      </div>
    </nav>`;

  document.getElementById('admin-sidebar').innerHTML = `
    <div class="d-flex flex-column">
      <ul class="nav nav-pills flex-column p-3 gap-1 flex-grow-1">
        ${ADMIN_LINKS.map(l => `
          <li class="nav-item">
            <a class="nav-link ${l.page === current ? 'active' : ''}" href="${l.href}">${l.label}</a>
          </li>`).join('')}
      </ul>
      <div class="p-3 border-top">
        <div class="small fw-semibold">Ahmed Mansour</div>
        <div class="small text-muted mb-2">System Admin</div>
        <button id="logout-btn" class="btn btn-link text-dark p-0">Log out</button>
      </div>
    </div>`;

  // Mobile hamburger: the sidebar is hidden on phones until the button is tapped.
  const sidebar = document.getElementById('admin-sidebar');
  const toggle = document.getElementById('menu-toggle');
  function setMenu(open) {
    sidebar.classList.toggle('open', open);
    toggle.setAttribute('aria-expanded', String(open));
    toggle.innerHTML = open ? '&#10005;' : '&#9776;';
  }
  toggle.addEventListener('click', () => setMenu(!sidebar.classList.contains('open')));
  sidebar.querySelectorAll('.nav-link').forEach(a => a.addEventListener('click', () => setMenu(false)));
  document.addEventListener('keydown', e => { if (e.key === 'Escape') setMenu(false); });

  document.getElementById('logout-btn').addEventListener('click', () => {
    localStorage.removeItem('loqma_token');
    localStorage.removeItem('loqma_role');
    window.location.href = '../auth/login.html';
  });
}

document.addEventListener('DOMContentLoaded', renderAdminLayout);