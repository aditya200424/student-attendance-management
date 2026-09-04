# Student Attendance Management — Streamlit Edition

This repository contains the original Spring Boot + HTML/JS project and a Streamlit/Python deployment entrypoint.

## Streamlit entrypoint

- `app.py` — Streamlit application
- `requirements.txt` — Python dependencies
- `data/*.json` — demo data used by the application

## Demo accounts

- Admin: `ADM001` / `admin123`
- Teacher: `TCH001` / `teacher123`
- Student: `STU001` / `student123`

## Deploy on Streamlit Community Cloud

1. Push `app.py`, `requirements.txt`, and the `data/` directory to GitHub.
2. Open Streamlit Community Cloud.
3. Create a new app from the GitHub repository.
4. Branch: `main`
5. Main file: `app.py`
6. Deploy.

### Important data note

The app currently keeps data in JSON files to stay compatible with the original project. On Streamlit Community Cloud, local file changes are not a reliable long-term database because deployments can restart or be rebuilt. This version is therefore best for a demo/college prototype. For production, move attendance data to a hosted database such as PostgreSQL, Supabase, or another persistent datastore.
