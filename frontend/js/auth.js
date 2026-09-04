/* ============================================================
   Auth / session helpers shared across all pages
   ============================================================ */
const Auth = {
  save(session) {
    localStorage.setItem('at_token', session.token);
    localStorage.setItem('at_user', JSON.stringify({ id: session.id, name: session.name, role: session.role }));
  },
  user() {
    const raw = localStorage.getItem('at_user');
    return raw ? JSON.parse(raw) : null;
  },
  isLoggedIn() { return !!localStorage.getItem('at_token'); },
  logout() {
    localStorage.removeItem('at_token');
    localStorage.removeItem('at_user');
    window.location.href = 'login.html';
  },
  /** Redirect to login if not authenticated, or if role mismatch. */
  guard(requiredRole) {
    const u = this.user();
    if (!this.isLoggedIn() || !u) {
      window.location.href = 'login.html';
      return null;
    }
    if (requiredRole && u.role !== requiredRole) {
      window.location.href = 'login.html';
      return null;
    }
    return u;
  }
};

/* ---------- Lightweight toast notifications (no external dep needed) ---------- */
function showToast(message, type = 'info') {
  let anchor = document.querySelector('.toast-anchor');
  if (!anchor) {
    anchor = document.createElement('div');
    anchor.className = 'toast-anchor';
    document.body.appendChild(anchor);
  }
  const colors = {
    info: '#0a0a0a', success: '#16794f', error: '#a3211c', warn: '#96660a'
  };
  const toast = document.createElement('div');
  toast.style.cssText = `
    background:${colors[type] || colors.info}; color:#fff; padding:14px 20px;
    border-radius:12px; margin-top:10px; font-family:'Inter',sans-serif; font-size:0.88rem;
    box-shadow:0 8px 24px rgba(0,0,0,0.25); min-width:240px; max-width:360px;
    animation:slideIn .25s ease; font-weight:500;
  `;
  toast.textContent = message;
  anchor.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all .3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3200);
}

const styleEl = document.createElement('style');
styleEl.textContent = `@keyframes slideIn { from { opacity:0; transform: translateX(20px);} to {opacity:1; transform:translateX(0);} }`;
document.head.appendChild(styleEl);

/* ---------- Dark mode toggle (persisted) ---------- */
function initDarkMode() {
  const stored = localStorage.getItem('at_theme');
  if (stored === 'dark') document.body.classList.add('dark');
  document.querySelectorAll('.dark-toggle').forEach(t => {
    t.addEventListener('click', () => {
      document.body.classList.toggle('dark');
      localStorage.setItem('at_theme', document.body.classList.contains('dark') ? 'dark' : 'light');
    });
  });
}
document.addEventListener('DOMContentLoaded', initDarkMode);
