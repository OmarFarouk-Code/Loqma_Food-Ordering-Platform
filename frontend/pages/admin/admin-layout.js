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

  document.getElementById('logout-btn').addEventListener('click', () => {
    // No auth yet. Replace with real logout when the auth pages exist.
    alert('Logout is not connected yet.');
  });
}

document.addEventListener('DOMContentLoaded', renderAdminLayout);