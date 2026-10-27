/**
 * Shared authentication and utility functions for Student ERP feature pages.
 */

function getToken() {
  return localStorage.getItem('token');
}

function authHeaders() {
  return { 'Authorization': 'Bearer ' + getToken() };
}

function authJsonHeaders() {
  return {
    'Authorization': 'Bearer ' + getToken(),
    'Content-Type': 'application/json'
  };
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = '/';
    return false;
  }
  return true;
}

async function apiFetch(url, opts = {}) {
  const headers = { ...authHeaders(), ...(opts.headers || {}) };
  const res = await fetch(url, { ...opts, headers });
  if (res.status === 401 || res.status === 403) {
    window.location.href = '/';
    throw new Error('Unauthorized');
  }
  return res;
}

async function apiPost(url, body) {
  const res = await apiFetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res;
}

async function apiPut(url, body) {
  const res = await apiFetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  return res;
}

async function apiDelete(url) {
  const res = await apiFetch(url, { method: 'DELETE' });
  return res;
}

function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function formatDateTime(dateStr) {
  if (!dateStr) return 'N/A';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

function formatCurrency(amount) {
  if (amount == null) return '₹0';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0
  }).format(amount);
}

function showToast(msg, type = 'success') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = 'toast ' + type;
  toast.textContent = msg;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

function openModal(id) {
  const el = typeof id === 'string' ? document.getElementById(id) : id;
  if (el) el.classList.add('show');
}

function closeModal(id) {
  const el = typeof id === 'string' ? document.getElementById(id) : id;
  if (el) el.classList.remove('show');
}

function getBadgeClass(status) {
  const s = (status || '').toLowerCase();
  if (['active', 'published', 'issued', 'paid', 'success', 'graded', 'completed', 'scheduled'].includes(s)) return 'badge-success';
  if (['pending', 'partial', 'draft', 'submitted', 'ongoing', 'reviewed'].includes(s)) return 'badge-warning';
  if (['overdue', 'cancelled', 'revoked', 'expired', 'failed', 'late', 'closed'].includes(s)) return 'badge-danger';
  if (['opened', 'theory', 'mid_term'].includes(s)) return 'badge-info';
  return 'badge-muted';
}

function formatFileSize(bytes) {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) { size /= 1024; i++; }
  return size.toFixed(1) + ' ' + units[i];
}

async function loadStudentProfile() {
  try {
    const res = await apiFetch('/api/students/me');
    if (res.ok) return await res.json();
  } catch (e) { console.error(e); }
  return null;
}

async function loadCurrentUserRoles() {
  const token = getToken();
  if (!token) return [];
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.roles || payload.authorities || [];
  } catch (e) { return []; }
}

function hasRole(roles, role) {
  return roles.some(r => r === role || r === 'ROLE_' + role);
}
