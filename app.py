import hashlib
import json
import os
import uuid
from datetime import date, timedelta
from pathlib import Path

import pandas as pd
import streamlit as st

# -----------------------------
# App configuration
# -----------------------------
st.set_page_config(
    page_title="Attendo — Student Attendance Management",
    page_icon="📚",
    layout="wide",
    initial_sidebar_state="expanded",
)

ROOT = Path(__file__).resolve().parent
DATA_DIR = ROOT / "data"
DATA_DIR.mkdir(exist_ok=True)

FILES = {
    "students": DATA_DIR / "students.json",
    "teachers": DATA_DIR / "teachers.json",
    "admins": DATA_DIR / "admins.json",
    "subjects": DATA_DIR / "subjects.json",
    "attendance": DATA_DIR / "attendance.json",
    "reports": DATA_DIR / "reports.json",
}

LOW_ATTENDANCE = 75.0

# -----------------------------
# Persistence helpers
# -----------------------------
def load_json(name, default=None):
    path = FILES[name]
    if not path.exists():
        return [] if default is None else default
    try:
        with path.open("r", encoding="utf-8") as f:
            data = json.load(f)
            return data if data is not None else ([] if default is None else default)
    except (json.JSONDecodeError, OSError):
        return [] if default is None else default


def save_json(name, data):
    path = FILES[name]
    tmp = path.with_suffix(path.suffix + ".tmp")
    with tmp.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    tmp.replace(path)


def sha256(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def new_id(prefix):
    return prefix + uuid.uuid4().hex[:8].upper()


# -----------------------------
# Data access
# -----------------------------
def students():
    return load_json("students")


def teachers():
    return load_json("teachers")


def admins():
    return load_json("admins")


def subjects():
    return load_json("subjects")


def attendance():
    return load_json("attendance")


def find_student(student_id):
    return next((s for s in students() if s.get("studentId") == student_id), None)


def find_teacher(teacher_id):
    return next((t for t in teachers() if t.get("teacherId") == teacher_id), None)


def find_subject(subject_id):
    return next((s for s in subjects() if s.get("subjectId") == subject_id), None)


def authenticate(identifier, password, role):
    role = role.upper()
    pwd = sha256(password)
    if role == "ADMIN":
        pool = admins()
        key = "adminId"
    elif role == "TEACHER":
        pool = teachers()
        key = "teacherId"
    else:
        pool = students()
        key = "studentId"

    for item in pool:
        matches_id = str(item.get(key, "")).lower() == identifier.strip().lower()
        matches_email = str(item.get("email", "")).lower() == identifier.strip().lower()
        matches_roll = role == "STUDENT" and str(item.get("rollNumber", "")).lower() == identifier.strip().lower()
        if (matches_id or matches_email or matches_roll) and item.get("passwordHash") == pwd:
            return {
                "id": item.get(key),
                "name": item.get("name", "User"),
                "role": role,
            }
    return None


def attendance_percentage(student_id, subject_id=None):
    records = [a for a in attendance() if a.get("studentId") == student_id]
    if subject_id:
        records = [a for a in records if a.get("subjectId") == subject_id]
    if not records:
        return 0.0
    if subject_id:
        countable = len(records)
    else:
        countable = sum(a.get("status") != "HOLIDAY" for a in records)
    if countable == 0:
        return 0.0
    present = sum(a.get("status") in {"PRESENT", "LATE"} for a in records)
    return round(present * 100 / countable, 2)


def class_students(department, semester, section, search=""):
    q = search.strip().lower()
    rows = [
        s for s in students()
        if s.get("department") == department
        and str(s.get("semester")) == str(semester)
        and s.get("section") == section
    ]
    if q:
        rows = [
            s for s in rows
            if q in str(s.get("name", "")).lower()
            or q in str(s.get("studentId", "")).lower()
            or q in str(s.get("rollNumber", "")).lower()
        ]
    return rows


def status_for(student_id, subject_id, day):
    key = f"{student_id}_{subject_id}_{day}"
    return next((a.get("status") for a in attendance() if a.get("attendanceId") == key), None)


def save_class_attendance(subject_id, teacher_id, day, department, semester, section, statuses):
    records = attendance()
    index = {a.get("attendanceId"): i for i, a in enumerate(records)}
    saved = 0
    for sid, status in statuses.items():
        key = f"{sid}_{subject_id}_{day}"
        row = {
            "attendanceId": key,
            "studentId": sid,
            "subjectId": subject_id,
            "teacherId": teacher_id,
            "date": day,
            "status": status,
            "department": department,
            "semester": str(semester),
            "section": section,
            "markedAt": int(pd.Timestamp.now().timestamp() * 1000),
        }
        if key in index:
            records[index[key]] = row
        else:
            index[key] = len(records)
            records.append(row)
        saved += 1
    save_json("attendance", records)
    return saved


def delete_student(sid):
    rows = [s for s in students() if s.get("studentId") != sid]
    save_json("students", rows)


def delete_teacher(tid):
    rows = [t for t in teachers() if t.get("teacherId") != tid]
    save_json("teachers", rows)


def delete_subject(subid):
    rows = [s for s in subjects() if s.get("subjectId") != subid]
    save_json("subjects", rows)


# -----------------------------
# Styling
# -----------------------------
st.markdown(
    """
    <style>
    .block-container { padding-top: 1.5rem; padding-bottom: 3rem; }
    .hero { padding: 1.5rem 1.7rem; border-radius: 18px; background: linear-gradient(135deg,#111827,#374151); color: white; margin-bottom: 1rem; }
    .hero h1 { margin: 0 0 .35rem 0; font-size: 2.1rem; }
    .hero p { margin: 0; opacity: .82; }
    .metric-card { padding: 1rem 1.1rem; border: 1px solid rgba(128,128,128,.22); border-radius: 14px; background: rgba(128,128,128,.05); }
    .small-muted { color: #6b7280; font-size: .88rem; }
    .status-good { color: #15803d; font-weight: 700; }
    .status-low { color: #b91c1c; font-weight: 700; }
    </style>
    """,
    unsafe_allow_html=True,
)


def metric(label, value, help_text=""):
    st.markdown('<div class="metric-card">', unsafe_allow_html=True)
    st.metric(label, value, help=help_text or None)
    st.markdown('</div>', unsafe_allow_html=True)


# -----------------------------
# Login
# -----------------------------
def login_page():
    st.markdown(
        '<div class="hero"><h1>📚 Attendo</h1><p>Student Attendance Management System</p></div>',
        unsafe_allow_html=True,
    )
    left, right = st.columns([1.2, 1])
    with left:
        st.subheader("Simple, fast attendance management")
        st.write("Manage students, teachers, subjects, attendance, reports and performance from one dashboard.")
        st.info("Demo credentials are listed below the login form.")
    with right:
        with st.form("login_form"):
            role = st.selectbox("Login as", ["ADMIN", "TEACHER", "STUDENT"])
            identifier = st.text_input("ID / Email / Roll Number")
            password = st.text_input("Password", type="password")
            submitted = st.form_submit_button("Sign in", use_container_width=True, type="primary")
            if submitted:
                user = authenticate(identifier, password, role)
                if user:
                    st.session_state.user = user
                    st.rerun()
                else:
                    st.error("Invalid credentials. Check your role, ID and password.")
        with st.expander("Demo credentials"):
            st.write("**Admin:** ADM001 / admin123")
            st.write("**Teacher:** TCH001 / teacher123")
            st.write("**Student:** STU001 / student123")


# -----------------------------
# Shared shell
# -----------------------------
def shell_header(title, subtitle=""):
    st.markdown(f'<div class="hero"><h1>{title}</h1><p>{subtitle}</p></div>', unsafe_allow_html=True)


def sidebar():
    user = st.session_state.user
    st.sidebar.title("📚 Attendo")
    st.sidebar.caption(f"{user['name']} • {user['role']}")
    if st.sidebar.button("Log out", use_container_width=True):
        st.session_state.clear()
        st.rerun()


# -----------------------------
# Admin
# -----------------------------
def admin_dashboard():
    sidebar()
    shell_header("Admin Dashboard", "Overview, people, subjects and attendance analytics")
    page = st.sidebar.radio("Navigation", ["Overview", "Students", "Teachers", "Subjects", "Reports"], label_visibility="collapsed")

    if page == "Overview":
        today = date.today().isoformat()
        recs = [a for a in attendance() if a.get("date") == today]
        present = sum(a.get("status") in {"PRESENT", "LATE"} for a in recs)
        absent = sum(a.get("status") == "ABSENT" for a in recs)
        total_classes = len({f"{s.get('department')}-{s.get('semester')}-{s.get('section')}" for s in students()})
        pct = round(present * 100 / len(recs), 2) if recs else 0
        c1,c2,c3,c4 = st.columns(4)
        with c1: metric("Students", len(students()))
        with c2: metric("Teachers", len(teachers()))
        with c3: metric("Today's attendance", f"{pct}%")
        with c4: metric("Classes", total_classes)

        st.divider()
        c1,c2 = st.columns(2)
        with c1:
            st.subheader("Attendance trend — last 30 days")
            rows=[]
            allrec=attendance()
            for i in range(29,-1,-1):
                d=(date.today()-timedelta(days=i)).isoformat()
                day=[a for a in allrec if a.get("date")==d]
                p=sum(a.get("status") in {"PRESENT","LATE"} for a in day)
                rows.append({"Date":d,"Attendance %":round(p*100/len(day),2) if day else 0})
            st.line_chart(pd.DataFrame(rows).set_index("Date"))
        with c2:
            st.subheader("Department-wise average")
            deps=sorted({s.get("department") or "Unknown" for s in students()})
            rows=[]
            for dep in deps:
                ss=[s for s in students() if (s.get("department") or "Unknown")==dep]
                avg=round(sum(attendance_percentage(s["studentId"]) for s in ss)/len(ss),2) if ss else 0
                rows.append({"Department":dep,"Average %":avg})
            st.bar_chart(pd.DataFrame(rows).set_index("Department"))

        st.subheader("Top attendance leaderboard")
        leaderboard=[]
        for s in students():
            leaderboard.append({"Student ID":s["studentId"],"Name":s["name"],"Department":s.get("department",""),"Attendance %":attendance_percentage(s["studentId"])})
        leaderboard=sorted(leaderboard,key=lambda x:x["Attendance %"],reverse=True)[:10]
        st.dataframe(pd.DataFrame(leaderboard), use_container_width=True, hide_index=True)

    elif page == "Students":
        st.subheader("Manage students")
        with st.expander("➕ Add student", expanded=False):
            with st.form("add_student"):
                a,b,c=st.columns(3)
                with a: sid=st.text_input("Student ID (optional)"); roll=st.text_input("Roll number")
                with b: name=st.text_input("Name"); email=st.text_input("Email")
                with c: dept=st.text_input("Department", value="Computer Science"); sem=st.text_input("Semester", value="3"); sec=st.text_input("Section", value="A")
                phone=st.text_input("Phone")
                submitted=st.form_submit_button("Create student", type="primary")
                if submitted:
                    if not roll or not name:
                        st.error("Roll number and name are required.")
                    else:
                        sid=sid.strip() or new_id("STU")
                        rows=students()
                        if any(s.get("studentId")==sid for s in rows): st.error("Student ID already exists.")
                        else:
                            rows.append({"studentId":sid,"rollNumber":roll,"name":name,"email":email,"passwordHash":sha256(roll),"department":dept,"semester":sem,"section":sec,"phone":phone,"role":"STUDENT"})
                            save_json("students",rows); st.success(f"Student {sid} created. Default password is the roll number."); st.rerun()
        q=st.text_input("Search by ID, roll number, name or department")
        rows=students()
        if q:
            ql=q.lower(); rows=[s for s in rows if ql in str(s).lower()]
        st.dataframe(pd.DataFrame([{k:v for k,v in s.items() if k!="passwordHash"} for s in rows]),use_container_width=True,hide_index=True)
        if rows:
            selected=st.selectbox("Select student to delete",[f"{s['studentId']} — {s['name']}" for s in rows])
            if st.button("Delete selected student"):
                delete_student(selected.split(" — ")[0]); st.success("Student deleted."); st.rerun()

    elif page == "Teachers":
        st.subheader("Manage teaching staff")
        with st.expander("➕ Add teacher"):
            with st.form("add_teacher"):
                a,b=st.columns(2)
                with a: tid=st.text_input("Teacher ID (optional)"); name=st.text_input("Name"); email=st.text_input("Email")
                with b: dept=st.text_input("Department", value="Computer Science"); password=st.text_input("Password", value="teacher123")
                submitted=st.form_submit_button("Create teacher", type="primary")
                if submitted:
                    tid=tid.strip() or new_id("TCH")
                    rows=teachers()
                    if any(t.get("teacherId")==tid for t in rows): st.error("Teacher ID already exists.")
                    else:
                        rows.append({"teacherId":tid,"name":name,"email":email,"passwordHash":sha256(password),"department":dept,"subjectIds":[],"assignedClasses":[],"role":"TEACHER"})
                        save_json("teachers",rows); st.success("Teacher created."); st.rerun()
        st.dataframe(pd.DataFrame([{k:v for k,v in t.items() if k!="passwordHash"} for t in teachers()]),use_container_width=True,hide_index=True)
        if teachers():
            selected=st.selectbox("Select teacher to delete",[f"{t['teacherId']} — {t['name']}" for t in teachers()])
            if st.button("Delete selected teacher"):
                delete_teacher(selected.split(" — ")[0]); st.success("Teacher deleted."); st.rerun()

    elif page == "Subjects":
        st.subheader("Subjects & assignment")
        with st.expander("➕ Add subject"):
            with st.form("add_subject"):
                a,b,c=st.columns(3)
                with a: sid=st.text_input("Subject ID (optional)"); name=st.text_input("Subject name")
                with b: code=st.text_input("Subject code"); dept=st.text_input("Department", value="Computer Science")
                with c: sem=st.text_input("Semester", value="3"); teacher=st.selectbox("Teacher", ["None"]+[f"{t['teacherId']} — {t['name']}" for t in teachers()])
                submitted=st.form_submit_button("Create subject", type="primary")
                if submitted:
                    sid=sid.strip() or new_id("SUB")
                    rows=subjects()
                    if any(s.get("subjectId")==sid for s in rows): st.error("Subject ID already exists.")
                    else:
                        tid=teacher.split(" — ")[0] if teacher!="None" else None
                        rows.append({"subjectId":sid,"subjectName":name,"subjectCode":code,"department":dept,"semester":sem,"teacherId":tid})
                        save_json("subjects",rows); st.success("Subject created."); st.rerun()
        st.dataframe(pd.DataFrame(subjects()),use_container_width=True,hide_index=True)
        if subjects():
            selected=st.selectbox("Select subject to delete",[f"{s['subjectId']} — {s['subjectName']}" for s in subjects()])
            if st.button("Delete selected subject"):
                delete_subject(selected.split(" — ")[0]); st.success("Subject deleted."); st.rerun()

    else:
        st.subheader("Student reports")
        sid=st.selectbox("Student",[s["studentId"]+" — "+s["name"] for s in students()]) if students() else None
        if sid:
            student_id=sid.split(" — ")[0]
            s=find_student(student_id)
            pct=attendance_percentage(student_id)
            c1,c2,c3=st.columns(3)
            with c1: metric("Overall attendance",f"{pct}%")
            with c2: metric("Records",len([a for a in attendance() if a.get("studentId")==student_id]))
            with c3: metric("Status","Low attendance" if pct<LOW_ATTENDANCE else "Eligible")
            rows=[]
            for sub in subjects():
                rows.append({"Subject":sub.get("subjectName"),"Code":sub.get("subjectCode"),"Attendance %":attendance_percentage(student_id,sub.get("subjectId"))})
            st.dataframe(pd.DataFrame(rows),use_container_width=True,hide_index=True)
            hist=[a for a in attendance() if a.get("studentId")==student_id]
            if hist:
                df=pd.DataFrame(hist).sort_values("date")
                df["Present"] = df["status"].isin(["PRESENT","LATE"]).astype(int)
                st.line_chart(df.set_index("date")["Present"].rolling(7,min_periods=1).mean()*100)


# -----------------------------
# Teacher
# -----------------------------
def teacher_dashboard():
    sidebar()
    user=st.session_state.user
    teacher=find_teacher(user["id"])
    shell_header("Teacher Dashboard", "Take attendance, review history and search students")
    page=st.sidebar.radio("Navigation",["Take Attendance","History","Students"],label_visibility="collapsed")

    if page == "Take Attendance":
        teacher_subjects=[s for s in subjects() if s.get("teacherId")==user["id"]]
        if not teacher_subjects:
            teacher_subjects=subjects()
        if not teacher_subjects:
            st.warning("No subjects are configured yet.")
            return
        sub_label=st.selectbox("Subject",[f"{s['subjectId']} — {s['subjectName']}" for s in teacher_subjects])
        sub=find_subject(sub_label.split(" — ")[0])
        c1,c2,c3=st.columns(3)
        with c1: dept=st.text_input("Department",value=(teacher or {}).get("department","Computer Science"))
        with c2: sem=st.text_input("Semester",value="3")
        with c3: sec=st.text_input("Section",value="A")
        day=st.date_input("Date",value=date.today())
        rows=class_students(dept,sem,sec)
        if not rows:
            st.info("No students found for this department / semester / section.")
            return
        st.subheader(f"Mark students ({len(rows)})")
        col1,col2=st.columns([1,1])
        with col1:
            mark_all=st.button("Mark all present",use_container_width=True)
        with col2:
            clear_all=st.button("Mark all absent",use_container_width=True)
        if mark_all:
            for s in rows: st.session_state[f"status_{s['studentId']}"]= "PRESENT"
        if clear_all:
            for s in rows: st.session_state[f"status_{s['studentId']}"]= "ABSENT"
        statuses={}
        for s in rows:
            existing=status_for(s["studentId"],sub["subjectId"],day.isoformat()) or "PRESENT"
            current=st.session_state.get(f"status_{s['studentId']}",existing)
            statuses[s["studentId"]]=st.selectbox(s["name"], ["PRESENT","ABSENT","LATE","LEAVE","HOLIDAY"], index=["PRESENT","ABSENT","LATE","LEAVE","HOLIDAY"].index(current), key=f"select_{s['studentId']}")
            st.session_state[f"status_{s['studentId']}"]=statuses[s["studentId"]]
        if st.button("Save attendance",type="primary",use_container_width=True):
            n=save_class_attendance(sub["subjectId"],user["id"],day.isoformat(),dept,sem,sec,statuses)
            st.success(f"Attendance saved for {n} students on {day.isoformat()}.")

    elif page == "History":
        teacher_subjects=[s for s in subjects() if s.get("teacherId")==user["id"]] or subjects()
        if not teacher_subjects: return
        sub_label=st.selectbox("Subject",[f"{s['subjectId']} — {s['subjectName']}" for s in teacher_subjects])
        day=st.date_input("Date",value=date.today())
        subid=sub_label.split(" — ")[0]
        rows=[a for a in attendance() if a.get("subjectId")==subid and a.get("date")==day.isoformat()]
        display=[]
        for a in rows:
            s=find_student(a.get("studentId")) or {}
            display.append({"Student ID":a.get("studentId"),"Name":s.get("name",""),"Roll Number":s.get("rollNumber",""),"Status":a.get("status")})
        st.dataframe(pd.DataFrame(display),use_container_width=True,hide_index=True)

    else:
        q=st.text_input("Search students")
        rows=students()
        if q:
            q=q.lower(); rows=[s for s in rows if q in str(s).lower()]
        out=[]
        for s in rows:
            out.append({"Student ID":s["studentId"],"Roll Number":s.get("rollNumber"),"Name":s.get("name"),"Department":s.get("department"),"Semester":s.get("semester"),"Section":s.get("section"),"Attendance %":attendance_percentage(s["studentId"])})
        st.dataframe(pd.DataFrame(out),use_container_width=True,hide_index=True)


# -----------------------------
# Student
# -----------------------------
def student_dashboard():
    sidebar()
    user=st.session_state.user
    s=find_student(user["id"])
    if not s:
        st.error("Student profile not found.")
        return
    shell_header("My Attendance", f"Welcome, {s['name']}")
    page=st.sidebar.radio("Navigation",["Overview","Subjects","History","Profile"],label_visibility="collapsed")

    if page == "Overview":
        pct=attendance_percentage(s["studentId"])
        recs=[a for a in attendance() if a.get("studentId")==s["studentId"]]
        c1,c2,c3=st.columns(3)
        with c1: metric("Overall attendance",f"{pct}%")
        with c2: metric("Total records",len(recs))
        with c3: metric("Eligibility","Eligible" if pct>=LOW_ATTENDANCE else "Below 75%")
        if pct<LOW_ATTENDANCE:
            st.error("⚠️ Your attendance is below the 75% threshold.")
        st.subheader("Recent attendance")
        recent=sorted(recs,key=lambda x:x.get("date",""),reverse=True)[:30]
        if recent:
            df=pd.DataFrame([{"Date":a.get("date"),"Subject":(find_subject(a.get("subjectId")) or {}).get("subjectName",a.get("subjectId")),"Status":a.get("status")} for a in recent])
            st.dataframe(df,use_container_width=True,hide_index=True)
        else: st.info("No attendance records yet.")

    elif page == "Subjects":
        rows=[]
        for sub in subjects():
            records=[a for a in attendance() if a.get("studentId")==s["studentId"] and a.get("subjectId")==sub.get("subjectId")]
            if records:
                rows.append({"Subject":sub.get("subjectName"),"Code":sub.get("subjectCode"),"Attendance %":attendance_percentage(s["studentId"],sub.get("subjectId")),"Classes":len(records)})
        st.dataframe(pd.DataFrame(rows),use_container_width=True,hide_index=True)
        if rows:
            st.bar_chart(pd.DataFrame(rows).set_index("Subject")["Attendance %"])

    elif page == "History":
        recs=[a for a in attendance() if a.get("studentId")==s["studentId"]]
        if recs:
            df=pd.DataFrame([{"Date":a.get("date"),"Subject":(find_subject(a.get("subjectId")) or {}).get("subjectName",a.get("subjectId")),"Status":a.get("status")} for a in recs]).sort_values("Date",ascending=False)
            st.dataframe(df,use_container_width=True,hide_index=True)
        else: st.info("No attendance records yet.")

    else:
        st.subheader("My profile")
        profile={"Student ID":s.get("studentId"),"Roll Number":s.get("rollNumber"),"Name":s.get("name"),"Email":s.get("email"),"Department":s.get("department"),"Semester":s.get("semester"),"Section":s.get("section"),"Phone":s.get("phone")}
        st.table(pd.DataFrame(profile.items(),columns=["Field","Value"]))


# -----------------------------
# Main
# -----------------------------
if "user" not in st.session_state:
    login_page()
else:
    role=st.session_state.user["role"]
    if role == "ADMIN": admin_dashboard()
    elif role == "TEACHER": teacher_dashboard()
    else: student_dashboard()
