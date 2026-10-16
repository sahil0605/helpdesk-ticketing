function getUser() {
  const raw = localStorage.getItem('user');
  return raw ? JSON.parse(raw) : null;
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  location.href = 'login.html';
}

function requireLogin() {
  if (!localStorage.getItem('token') || !getUser()) {
    location.href = 'login.html';
  }
}

function isStaff() {
  const u = getUser();
  return u && (u.role === 'ORG_ADMIN' || u.role === 'SUPPORT_AGENT');
}

async function apiFetch(url, options = {}) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  const token = localStorage.getItem('token');
  if (token) headers.Authorization = 'Bearer ' + token;
  const res = await fetch(url, Object.assign({}, options, { headers }));
  if (res.status === 401 && !url.includes('/api/auth/')) {
    logout();
    throw new Error('Unauthorized');
  }
  const text = await res.text();
  let body = null;
  if (text) {
    try { body = JSON.parse(text); } catch { body = text; }
  }
  if (!res.ok) {
    const msg = (body && (body.message || body.error)) || res.statusText;
    throw new Error(msg);
  }
  return body;
}

function renderNav(active) {
  const u = getUser();
  const nav = document.getElementById('nav');
  if (!nav || !u) return;
  let html = '<a href="tickets.html">Tickets</a>';
  if (u.role === 'CUSTOMER') html += '<a href="create-ticket.html">New Ticket</a>';
  if (u.role === 'ORG_ADMIN') html += '<a href="users.html">Users</a>';
  html += '<span style="margin-left:auto">' + u.name + ' (' + u.role + ')</span>';
  html += '<button type="button" onclick="logout()">Logout</button>';
  nav.innerHTML = html;
}
