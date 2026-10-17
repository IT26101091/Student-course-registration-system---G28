# Student Course Registration System

A Java OOP group project using Javalin for the web server, HTML/CSS/JavaScript for the frontend, and JSON files for persistence.

## Team modules

- Binara — student management (`data/students.json`)
- Sithmi — course management (`data/courses.json`)
- Vihangi — registration management (`data/registrations.json`)
- Kusal — queue management and insertion sort (`data/queue.json`)

Confirm this allocation, class design, and endpoint contract as a group before implementing CRUD.

## Requirements

- JDK 17 or newer
- IntelliJ IDEA
- Maven (or IntelliJ's bundled Maven)
- GitHub Desktop

## Run in IntelliJ

1. Open this folder as a Maven project.
2. Set Project SDK to JDK 17 or newer.
3. Reload Maven when IntelliJ prompts you to do so.
4. Run `com.coursehub.Main`.
5. Open <http://localhost:7070>.
6. Test <http://localhost:7070/api/health> — it should return JSON with `status: running`.

Alternatively, from the project root run:

```bash
mvn clean compile exec:java
```

## Architecture

- `model/` — domain objects shared between modules
- `repository/` — JSON file read/write code
- `service/` — validation and business rules
- `routes/` — Javalin API routes
- `util/` — data structures and algorithms
- `src/main/resources/public/` — HTML, CSS and JavaScript
- `data/` — the four separate JSON data files

## Team Git workflow

Create one feature branch per member. Commit and push small changes, then open a pull request for review. Do not work directly on `main`. Coordinate edits to shared classes and merge the API/model contract before implementing full CRUD.

## Current status

This is a starting scaffold only. The CRUD APIs, authentication, JSON repository classes, queue persistence, and insertion sort UI still need to be implemented and tested.
