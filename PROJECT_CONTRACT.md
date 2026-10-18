
# Student Course Registration System
## Shared Project Contract

### Technology
- Java 17 or newer
- Maven
- Javalin web server
- HTML, CSS and JavaScript
- Jackson for JSON conversion
- JSON files for persistent storage

### Team responsibilities
- Binara: Student management
- Sithmi: Course management
- Vihangi: Registration management
- Kusal: Queue management and insertion sort

### Shared domain classes
- Person (abstract)
- Student extends Person
- Admin extends Person
- Course
- Registration
- QueueEntry

### JSON files
- data/students.json
- data/courses.json
- data/registrations.json
- data/queue.json

Each file stores a JSON array.

### Shared data fields

Student:
studentId, name, email, degreeProgram

Course:
courseCode, title, description, credits, capacity

Registration:
registrationId, studentId, courseCode, registrationDate, status

QueueEntry:
queueEntryId, studentId, courseCode, joinedAt

### API conventions

Students:
GET, POST /api/students
GET, PUT, DELETE /api/students/{studentId}

Courses:
GET, POST /api/courses
GET, PUT, DELETE /api/courses/{courseCode}

Registrations:
GET, POST /api/registrations
GET, PUT, DELETE /api/registrations/{registrationId}

Queue:
GET, POST /api/queue
GET, PUT, DELETE /api/queue/{queueEntryId}
POST /api/queue/process-next

### Business rules
1. IDs must be unique.
2. Required fields must be validated.
3. Duplicate active registrations must be prevented.
4. A course cannot exceed its capacity.
5. Students requesting a full course may join its queue.
6. Queue processing follows FIFO order.
7. Cancelled registrations do not count towards course capacity.
8. Sorting must use our own insertion sort implementation.

### Git workflow
- Keep main stable.
- Develop using feature branches.
- Commit small, meaningful changes.
- Merge completed work through reviewed pull requests.
- Discuss changes to shared classes and API contracts.
