# Forest - Version 1

Minimal full-stack implementation using Angular frontend, Spring Boot backend, and MySQL database.

Implemented Version 1 features:
- Secure user registration and login using JWT
- Create, read, update, delete top-level posts
- Global feed of posts
- Flat (non-nested) comments per post
- Author-only update/delete permissions on posts

## Structure
- `frontend/` Angular app
- `backend/` Spring Boot API
- `docker-compose.yml` MySQL

## Run locally

1) Start MySQL
```bash
docker compose up -d
```
MySQL is exposed on `localhost:3307`.

Connect to MySQL in terminal (Docker instance):
```bash
mysql -h 127.0.0.1 -P 3307 -u root -p
```
Password: `root`

Quick DB check:
```sql
SHOW DATABASES;
USE forest_db;
SHOW TABLES;
SELECT * FROM posts
```

2) Start backend
```bash
cd backend
mvn spring-boot:run
```

3) Start frontend
```bash
cd frontend
npm install
npm start
```

Frontend: `http://localhost:4200`
Backend: `http://localhost:8080`
