/* ============================================================
   API client — talks to the Spring Boot backend at :8080
   ============================================================ */
const API_BASE = 'http://localhost:8080/api';

const Api = {
  token() { return localStorage.getItem('at_token'); },

  headers(json = true) {
    const h = {};
    if (json) h['Content-Type'] = 'application/json';
    const t = this.token();
    if (t) h['Authorization'] = `Bearer ${t}`;
    return h;
  },

  async request(method, path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
      method,
      headers: this.headers(),
      body: body ? JSON.stringify(body) : undefined
    });
    let data;
    try { data = await res.json(); } catch (e) { data = { success: res.ok, message: res.statusText }; }
    if (!res.ok || data.success === false) {
      throw new Error(data.message || `Request failed (${res.status})`);
    }
    return data;
  },

  get(path) { return this.request('GET', path); },
  post(path, body) { return this.request('POST', path, body); },
  put(path, body) { return this.request('PUT', path, body); },
  del(path) { return this.request('DELETE', path); },

  // ---- Auth ----
  login(id, password, role) {
    return this.request('POST', '/auth/login', { id, password, role });
  },

  // ---- Students ----
  getStudents(params = {}) {
    const qs = new URLSearchParams(params).toString();
    return this.get(`/students${qs ? '?' + qs : ''}`);
  },
  getStudent(id) { return this.get(`/students/${id}`); },
  createStudent(data) { return this.post('/students', data); },
  updateStudent(id, data) { return this.put(`/students/${id}`, data); },
  deleteStudent(id) { return this.del(`/students/${id}`); },

  // ---- Teachers ----
  getTeachers() { return this.get('/teachers'); },
  createTeacher(data) { return this.post('/teachers', data); },
  deleteTeacher(id) { return this.del(`/teachers/${id}`); },

  // ---- Subjects ----
  getSubjects() { return this.get('/subjects'); },
  createSubject(data) { return this.post('/subjects', data); },
  deleteSubject(id) { return this.del(`/subjects/${id}`); },

  // ---- Attendance ----
  markAttendance(data) { return this.post('/attendance/mark', data); },
  updateAttendance(studentId, subjectId, date, status) {
    const qs = new URLSearchParams({ studentId, subjectId, date, status }).toString();
    return this.put(`/attendance/update?${qs}`);
  },
  undoAttendance() { return this.post('/attendance/undo'); },
  getStudentAttendance(id) { return this.get(`/attendance/student/${id}`); },
  getStudentPercentage(id) { return this.get(`/attendance/student/${id}/percentage`); },
  getClassAttendance(subjectId, date) {
    const qs = new URLSearchParams({ subjectId, date }).toString();
    return this.get(`/attendance/class?${qs}`);
  },
  getLeaderboard(topN = 10) { return this.get(`/attendance/leaderboard?topN=${topN}`); },

  // ---- Reports ----
  getDashboardSummary() { return this.get('/reports'); },
  getMonthlyTrend(days = 30) { return this.get(`/reports/monthly?days=${days}`); },
  getDepartmentWise() { return this.get('/reports/department-wise'); },
  getStudentReport(id) { return this.get(`/reports/student/${id}`); }
};
