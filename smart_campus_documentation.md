# Smart Campus Management System
## Complete Architectural Analysis & Project Documentation

This documentation provides an in-depth analysis of the Smart Campus Management System, spanning the Android (Java/XML) frontend app, the Django REST Framework (Python/SQLite) backend server, database structures, UML diagrams, APIs, security, deployment, and build systems.

---

## 1. Project Overview
The **Smart Campus Management System** (SCMS) is an enterprise-grade mobile and web-enabled portal that automates academic and administrative processes of a higher education institution.

* **Project Name**: Smart Campus Management System (SCMS)
* **Real-world Use Case**: Managing student profiles, faculty records, resource sharing (notes & papers), notice boards, attendance tracking, grades/results reporting, and complaint resolution under a unified platform.
* **Roles Supported**:
  1. **Admin**: Manages system users (students, faculty), notices, complaints, and views high-level dashboard summaries.
  2. **Faculty**: Marks student attendance, uploads academic resources, publishes exam results, and views classroom timetables.
  3. **Student**: Accesses personal attendance records, views results, downloads notes/question papers, submits requests for certificates, raises hostel or academic complaints, and reviews active notices.

### Overall Application Workflow
```
                  ┌──────────────────────┐
                  │   Android Client UI  │
                  └──────────┬───────────┘
                             │ (Retrofit HTTP API Calls)
                             ▼
                  ┌──────────────────────┐
                  │    Django REST API   │
                  └──────────┬───────────┘
                             │ (Django ORM Database Access)
                             ▼
                  ┌──────────────────────┐
                  │   SQLite Database    │
                  └──────────────────────┘
```

---

## 2. Objectives
1. **Automation**: Eradicate manual, paper-based administrative tasks for attendance, exam results, and certificate requests.
2. **Role-Based Provisioning**: Ensure secure and separated user spaces for Admins, Faculty members, and Students.
3. **Resource Optimization**: Enable smooth digital distribution of course materials (Lecture Notes, PYQs) to minimize administrative overhead.
4. **Transparency & Feedback**: Establish a direct feedback loop via the Student Complaints and Administration response modules.

---

## 3. Problem Statement
Educational institutions face significant administrative inefficiencies:
* Attendance marked manually in registers is prone to human error and data manipulation.
* Notices posted on physical boards fail to reach off-campus students in real-time.
* Sharing academic notes via unorganized chat groups leads to file loss.
* Resolving campus complaints is slow due to paper-trail processing.
* Grading transcripts require manual calculations and physical delivery.

---

## 4. Technology Stack

| Layer | Component | Version / Context | Purpose |
| :--- | :--- | :--- | :--- |
| **Frontend** | Android SDK | SDK Target 36, Min 24 | Mobile operating platform |
| | Java | Java 11 | Primary language for application logic |
| | Layout | XML / View Binding | Declares UI layouts using Android UI views |
| | Components | Material Design 3 / CardView / RecyclerView | Styling, grids, list scrolls, input forms |
| **Backend** | Python | Python 3.x | Backend engine language |
| | Django | v6.0.6 | Core web framework |
| | Django REST Framework | v3.17.1 | API construction engine |
| | JWT Authentication | djangorestframework-simplejwt v5.5.1 | Stateless API access control tokens |
| **Database** | SQLite | SQLite 3 | Lightweight relational file-based storage |
| **Networking**| Retrofit | v3.0.0 | HTTP client interface for REST queries |
| | Gson | v2.14.0 | JSON serialization/deserialization |
| | OkHttp | OkHttp 3.x (Retrofit dependency) | Direct socket transport layer & HTTP interceptor |

---

## 5. Tools Used

### 1. Android Studio
* **Purpose**: Primary IDE for the frontend mobile application.
* **Role**: Used to compile Java sources, preview XML screens, launch emulator runtimes, manage version catalogs (`libs.versions.toml`), and package target APK bundles.

### 2. VS Code / PyCharm
* **Purpose**: IDE for Python backend scripting.
* **Role**: Configured with virtual env packages to manage database models, serializers, views, URLs, and debugging logs.

### 3. Postman
* **Purpose**: Sandbox testing environment for backend APIs.
* **Role**: Executed queries to inspect authentication headers, POST request payloads, file upload multipart boundaries, and verify server exception handling.

### 4. Git & GitHub
* **Purpose**: Distributed version control and collaboration platform.
* **Role**: Tracked source adjustments, managed branch conflicts, and served as remote backups for both codebases.

---

## 6. Project Architecture

### High-Level System Architecture Diagram
```
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       FRONTEND (Android Client)                              │
│  ┌─────────────────────────┐      ┌─────────────────────────┐     ┌───────────────────────┐  │
│  │     User Interface      │      │     Network Client      │     │  Local Preferences    │  │
│  │ (Activities, XML Views, │ ───> │   Retrofit Interface    │ ──> │ (SharedPreferences -  │  │
│  │   RecyclerView Lists)   │      │   (ApiService, Gson)    │     │      JWT Token)       │  │
│  └─────────────────────────┘      └────────────┬────────────┘     └───────────────────────┘  │
└────────────────────────────────────────────────┼─────────────────────────────────────────────┘
                                                 │ HTTPS Requests
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       BACKEND (Django Server)                                │
│        ┌─────────────────────────┐               ┌─────────────────────────────────┐         │
│        │      URL Routing        │ ────────────> │          REST ViewSets          │         │
│        │  (config/urls.py paths) │               │      (Class-Based API Views)     │         │
│        └─────────────────────────┘               └────────────────┬────────────────┘         │
│                                                                   │                          │
│                                                                   ▼                          │
│        ┌─────────────────────────┐               ┌─────────────────────────────────┐         │
│        │       Database          │ <──────────── │          Model Serializers      │         │
│        │ (SQLite Database File)  │  (Django ORM) │     (Validation & JSON Output)  │         │
│        └─────────────────────────┘               └─────────────────────────────────┘         │
└──────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. Frontend Structure
The Android application follows the standard Model-View-Controller (MVC) adaptation for Android development:
* **Java Package Root**: `com.example.mycampus`
  * **Network Layer** ([network/](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/network)): Contains API client initialization logic and endpoints interface.
    * [RetrofitClient.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/network/RetrofitClient.java): Singleton class configuring Retrofit configurations (Base URL, JSON serialization engine, authentication token interceptor).
    * [ApiService.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/network/ApiService.java): Lists all network method definitions with Retrofit annotation mappings.
  * **Model Layer** ([models/](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/models)): Local data structure classes mapped to JSON models using GSON annotations.
  * **View/Controller Layer**: Java class activities that manage user interaction, render views, handle clicks, and orchestrate network cycles.
    * [MainActivity.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/MainActivity.java): Controls user login submission and routing.
    * [admindashboardActivity.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/admindashboardActivity.java): Handles admin views.
    * [facultydsashboardActivity.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/facultydsashboardActivity.java): Coordinates faculty duties.
    * [StudentdashboardActivity.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/StudentdashboardActivity.java): Coordinates student features.

---

## 8. Backend Structure
The backend is structured around modular Django applications linked to a centralized configuration app (`campusapi/config`):
* **Django Apps**:
  * `accounts`: Customized user framework, credentials storage, and token issuance.
  * `students`: Core logic for student registries.
  * `faculty`: Core logic for academic faculty.
  * `attendance`: Marks daily presence records.
  * `results`: Uploads and lists student GPAs.
  * `certificates`: Handles institutional letters.
  * `resources`: Manages lecture slides and PYQs.
  * `notifications`: Broad announcements.
  * `complaints`: Feedback registration system.
  * `timetable`: Daily lecture structures.
  * `examination`: Midterm and endsem exam timetables.
  * `academics`: Class course profiles and subject lines.
  * `adminpanel`: Summaries and aggregates for administration dashboard metrics.

---

## 9. Database Design
SQLite serves as the relational database engine. Relationships are enforced through foreign keys targeting the user registry.

### Entity Relationship Diagram (ERD)
```
  ┌─────────────────┐             ┌────────────────────────┐
  │  ACADEMICS_CRSE │             │     ACCOUNTS_USER      │
  ├─────────────────┤             ├────────────────────────┤
  │ PK  id          │             │ PK  id                 │
  │     name        │             │     email (Unique)     │
  │     code        │             │     role               │
  └────────┬────────┘             │     is_superuser       │
           │ (1:N)                │     is_staff           │
           ▼                      └────┬──────┬──────┬─────┘
  ┌─────────────────┐                  │      │      │
  │  ACADEMICS_SBJT │                  │ (1:1)│ (1:1)│ (1:N)
  ├─────────────────┤                  │      │      │
  │ PK  id          │                  ▼      │      ▼
  │     name        │       ┌─────────────┐   │   ┌─────────────────────────┐
  │ FK  course_id   │       │STUDENTS_PRF │   │   │  ATTENDANCE_ATTENDANCE  │
  └────────┬────────┘       ├─────────────┤   │   ├─────────────────────────┤
           │                │ PK  id      │   │   │ PK  id                  │
           │ (1:N)          │ FK  user_id │   │   │ FK  student_id (User)   │
           ▼                └─────────────┘   │   │ FK  faculty_id (User)   │
  ┌─────────────────┐                         ▼   │     is_present          │
  │RESOURCE_RESOURCE│               ┌─────────────┐└─────────────────────────┘
  ├─────────────────┤               │ FACULTY_PRF │
  │ PK  id          │               ├─────────────┤
  │     title       │               │ PK  id      │
  │ FK  subject_id  │               │ FK  user_id │
  │ FK  uploaded_by │               └─────────────┘
  └─────────────────┘
```

---

## 10. Database Dictionary

### 1. `accounts_user`
* **Purpose**: Custom base user model managing unified application credentials.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `password` (VARCHAR(128)) - Encrypted password hash
  * `last_login` (DATETIME, Nullable)
  * `is_superuser` (BOOLEAN)
  * `email` (VARCHAR(254), Unique) - Login identifier
  * `role` (VARCHAR(10)) - `STUDENT`, `FACULTY`, or `ADMIN`
  * `username` (VARCHAR(150), Nullable, Unique)
  * `phone_number` (VARCHAR(20), Nullable)
  * `full_name` (VARCHAR(150))
  * `department` (VARCHAR(150))
  * `is_active` (BOOLEAN)
  * `is_staff` (BOOLEAN)
* **Relationships**: Relates to Profile, Result, Attendance, and Complaint tables.
* **Sample Record**: `(1, 'pbkdf2_sha256$...', 'admin@campus.edu', 'ADMIN', 'admin1', '987-654-3210', 'Admin User', 'CS', 1, 1)`

### 2. `students_studentprofile`
* **Purpose**: Profile directory for enrolled students.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `enrollment_number` (VARCHAR(50), Unique)
  * `branch` (VARCHAR(100))
  * `semester` (INTEGER)
  * `section` (VARCHAR(10))
  * `phone` (VARCHAR(20))
  * `gpa` (DECIMAL(4,2))
  * `user_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'ENROLL001', 'CSE', 4, 'A', '1234567801', 8.50, 6)`

### 3. `faculty_facultyprofile`
* **Purpose**: Profile directory for working faculty.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `faculty_id` (VARCHAR(50), Unique)
  * `department` (VARCHAR(100))
  * `designation` (VARCHAR(100))
  * `phone` (VARCHAR(20))
  * `specialization` (VARCHAR(200))
  * `user_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'FAC001', 'Computer Science', 'Assistant Professor', '9876543201', 'AI/ML', 2)`

### 4. `attendance_attendance`
* **Purpose**: Records daily presence registers marked by faculty.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `subject` (VARCHAR(150), Nullable)
  * `date` (DATE)
  * `status` (VARCHAR(10)) - `Present`, `Absent`, `Leave`
  * `remarks` (TEXT)
  * `is_present` (BOOLEAN)
  * `student_class` (INTEGER, Nullable)
  * `faculty_id` (INTEGER, Foreign Key -> `accounts_user.id`, Set Null, Nullable)
  * `student_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'Algorithms', '2026-06-30', 'Present', '', 1, 1, 2, 6)`

### 5. `results_result`
* **Purpose**: Records student term GPAs published by faculty.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `subject` (VARCHAR(150), Nullable)
  * `semester` (INTEGER)
  * `exam_type` (VARCHAR(100), Nullable)
  * `marks_obtained` (INTEGER, Nullable)
  * `max_marks` (INTEGER, Nullable)
  * `grade` (VARCHAR(10), Nullable)
  * `result_pdf` (VARCHAR(100), Nullable)
  * `gpa` (FLOAT, Nullable)
  * `has_backlog` (BOOLEAN)
  * `backlog_subjects` (TEXT, Nullable)
  * `marks` (INTEGER, Nullable)
  * `remarks` (TEXT)
  * `created_at` (DATETIME, Nullable)
  * `published_at` (DATETIME)
  * `published_by_id` (INTEGER, Foreign Key -> `accounts_user.id`, Set Null, Nullable)
  * `student_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'Algorithms', 4, '', 85, 100, 'A', '', 8.5, 0, '', 85, 'Pass', '2026-06-30 08:00:00', '2026-06-30 08:00:00', 2, 6)`

### 6. `resources_resource`
* **Purpose**: Academic notes and question papers uploaded by faculty.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `title` (VARCHAR(200))
  * `description` (TEXT)
  * `resource_type` (VARCHAR(50), Nullable) - `NOTE`, `PYQ`, `BOOK`
  * `pdf_file` (VARCHAR(100), Nullable) - PDF file path
  * `department` (VARCHAR(100), Nullable)
  * `created_at` (DATETIME)
  * `uploaded_at` (DATETIME, Nullable)
  * `year` (INTEGER, Nullable)
  * `subject_id` (INTEGER, Foreign Key -> `academics_subject.id`, Set Null, Nullable)
  * `uploaded_by_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'Lecture Notes on Algorithms', 'Sorting algorithms explained.', 'NOTE', 'resources/algo_sorting.pdf', 'CS', '2026-06-30 08:30:00', '2026-06-30 08:30:00', 2026, 1, 2)`

### 7. `complaints_complaint`
* **Purpose**: Grievances filed by students and resolved by admins.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `title` (VARCHAR(200))
  * `description` (TEXT)
  * `status` (VARCHAR(20)) - `pending`, `resolved`
  * `priority` (VARCHAR(20)) - `LOW`, `MEDIUM`, `HIGH`
  * `admin_reply` (TEXT)
  * `resolved_at` (DATETIME, Nullable)
  * `created_at` (DATETIME)
  * `resolved_by_id` (INTEGER, Foreign Key -> `accounts_user.id`, Set Null, Nullable)
  * `user_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'Wifi issue in Hostel A', 'Wifi is dropping connections.', 'pending', 'HIGH', '', NULL, '2026-06-30 08:45:00', NULL, 6)`

### 8. `certificates_certificate`
* **Purpose**: Academic letters and transcript requests.
* **Columns**:
  * `id` (INTEGER, Primary Key, Auto-Increment)
  * `title` (VARCHAR(200))
  * `issued_by` (VARCHAR(200))
  * `issue_date` (DATE, Nullable)
  * `certificate_file` (VARCHAR(100), Nullable)
  * `status` (VARCHAR(20)) - `PENDING`, `APPROVED`, `REJECTED`
  * `student_id` (INTEGER, Foreign Key -> `accounts_user.id`, Cascade)
* **Sample Record**: `(1, 'Bonafide Certificate', 'Administration Office', '2026-06-30', 'certificates/bonafide_1.pdf', 'APPROVED', 6)`

---

## 11. UML Diagrams

### Use Case Diagram
```
                     ┌──────────┐
                     │  Admin   │
                     └────┬─────┘
  ┌───────────────────────┼────────────────────────┐
  │                       │                        │
  ▼                       ▼                        ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Manage Users     │    │ View Dashboard   │    │ Review Complaints│
│ (Create/Delete)  │    │ Summary Metrics  │    │ (Reply/Resolve)  │
└──────────────────┘    └──────────────────┘    └──────────────────┘

                     ┌──────────┐
                     │ Faculty  │
                     └────┬─────┘
  ┌───────────────────────┼────────────────────────┐
  │                       │                        │
  ▼                       ▼                        ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ Mark Attendance  │    │ Publish Results  │    │ Upload Resources │
│ (Present/Absent) │    │ (Marks / GPA)    │    │ (PDF Notes/PYQs) │
└──────────────────┘    └──────────────────┘    └──────────────────┘

                     ┌──────────┐
                     │ Student  │
                     └────┬─────┘
  ┌───────────────────────┼────────────────────────┐
  │                       │                        │
  ▼                       ▼                        ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ View Attendance  │    │ File Complaints  │    │ Access Resources │
│ & Term Results   │    │ (Academic/Hostel)│    │ (Download PDF)   │
└──────────────────┘    └──────────────────┘    └──────────────────┘
```

### Class Diagram (Django DB Models Concept)
```
┌───────────────────────────────────────┐
│                 User                  │
├───────────────────────────────────────┤
│ +email: EmailField [Unique]           │
│ +role: CharField (Enum)               │
│ +full_name: CharField                 │
│ +is_superuser: Boolean                │
└──────────────────┬────────────────────┘
                   │ 1
                   │
                   │ 1..* (Foreign Key)
                   ▼
┌───────────────────────────────────────┐
│              Attendance               │
├───────────────────────────────────────┤
│ +student: ForeignKey -> User          │
│ +faculty: ForeignKey -> User          │
│ +date: DateField                      │
│ +is_present: Boolean                  │
│ +status: CharField                    │
└───────────────────────────────────────┘
```

### Sequence Diagram (Authentication & Dashboard Load)
```
Android Client              Retrofit API              Django Auth API           SQLite DB
     │                           │                           │                      │
     │─── Enter Credentials ────>│                           │                      │
     │    (email, password)      │─── POST api/auth/login ──>│                      │
     │                           │    (payload data)         │─── Lookup User ─────>│
     │                           │                           │    credentials       │
     │                           │                           │<── Returns user ─────│
     │                           │                           │    record            │
     │                           │<── Send Access Token ─────│                      │
     │                           │    (JWT payload JSON)     │                      │
     │<── Store JWT Token ───────│                           │                      │
     │    (SharedPreferences)    │                           │                      │
     │                           │                           │                      │
     │─── Load Dashboard ───────>│                           │                      │
     │    (Header Authorization) │─── GET api/student-pr ───>│                      │
     │                           │    (Token verification)   │─── Fetch Profile ───>│
     │                           │                           │<── User details ─────│
     │<── Update Dashboard UI ───│<── JSON Profile Objects ──│                      │
```

---

## 12. API Dependency Mapping

| Android Activity | Retrofit Method | HTTP Endpoint | Django View | Serializer | DB Model |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **MainActivity** | `loginUser()` | `POST /api/auth/login/` | `LoginView` | `LoginSerializer` | `accounts.User` |
| **ManageUsersActivity** | `getUsers()` | `GET /api/users/` | `UserViewSet` | `UserSerializer` | `accounts.User` |
| **ManageUsersActivity** | `createUser()` | `POST /api/users/` | `UserViewSet` | `UserSerializer` | `accounts.User` |
| **ManageUsersActivity** | `deleteUser()` | `DELETE /api/users/{id}/` | `UserViewSet` | `UserSerializer` | `accounts.User` |
| **StudentdashboardActivity** | `getStudentProfiles()` | `GET /api/student-profiles/` | `StudentProfileViewSet` | `StudentProfileSerializer` | `students.StudentProfile` |
| **StudentResultsActivity** | `getResults()` | `GET /api/results/` | `ResultViewSet` | `ResultSerializer` | `results.Result` |
| **ResultManagementActivity**| `postResult()` | `POST /api/results/` | `ResultViewSet` | `ResultSerializer` | `results.Result` |
| **StudentCertificatesActivity**| `getCertificates()` | `GET /api/certificates/` | `CertificateViewSet` | `CertificateSerializer` | `certificates.Certificate` |
| **StudentCertificatesActivity**| `postCertificate()` | `POST /api/certificates/` | `CertificateViewSet` | `CertificateSerializer` | `certificates.Certificate` |
| **CampusNoticesActivity** | `getNotices()` | `GET /api/notices/` | `NotificationViewSet` | `NotificationSerializer` | `notifications.Notification` |
| **CampusNoticesActivity** | `postNotice()` | `POST /api/notices/` | `NotificationViewSet` | `NotificationSerializer` | `notifications.Notification` |
| **AttendanceActivity** | `getAttendance()` | `GET /api/attendance/` | `AttendanceViewSet` | `AttendanceSerializer` | `attendance.Attendance` |
| **AttendanceManagementActivity**| `postAttendance()` | `POST /api/attendance/` | `AttendanceViewSet` | `AttendanceSerializer` | `attendance.Attendance` |
| **StudentResourcesActivity**| `getNotes()` | `GET /api/notes/` | `NoteViewSet` | `NoteSerializer` | `resources.Resource` |
| **StudentResourcesActivity**| `getPyqs()` | `GET /api/pyqs/` | `PyqViewSet` | `PyqSerializer` | `resources.Resource` |
| **UploadResourceActivity** | `uploadNote()` | `POST /api/notes/` (Multipart) | `NoteViewSet` | `NoteSerializer` | `resources.Resource` |
| **StudentComplaintsActivity**| `getComplaints()` | `GET /api/complaints/` | `ComplaintViewSet` | `ComplaintSerializer` | `complaints.Complaint` |
| **StudentComplaintsActivity**| `postComplaint()` | `POST /api/complaints/` | `ComplaintViewSet` | `ComplaintSerializer` | `complaints.Complaint` |
| **ManageComplaintsActivity**| `updateComplaintStatus()`| `PATCH /api/complaints/{id}/` | `ComplaintViewSet` | `ComplaintSerializer` | `complaints.Complaint` |

> [!WARNING]
> **API Defect / Missing Endpoint**: The Android class `StudentProjectsActivity` attempts to query `GET api/projects/` and `POST api/projects/` targeting the model `Project.java`. However, there is **no projects app, database table, or url routing endpoint implemented in the Django backend**. Calling this will return an HTTP 404 response.

---

## 13. Authentication Flow
The system implements JWT (JSON Web Token) authentication for secure, stateless requests.

```
┌──────────────┐         Credentials (Email & Password)        ┌──────────────┐
│  Android UI  │ ────────────────────────────────────────────> │ Django Auth  │
│              │ <──────────────────────────────────────────── │   Service    │
└──────────────┘         Response (Access & Refresh Token)     └──────────────┘
       │
       ▼ Saved in
┌──────────────┐
│SharedPrefs   │ (Token used in OkHttp Interceptor: "Bearer <token>")
└──────────────┘
```

1. **Credentials Dispatch**: The user inputs their email and password in [MainActivity.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/MainActivity.java). The credentials map is posted to `/api/auth/login/`.
2. **Token Generation**: Django validates the credentials, updates user log-in stats, generates a refresh/access token pair via `simplejwt`, and returns them along with serialized user properties (id, role, username).
3. **Stateless Storage**: Android caches the tokens in `SharedPreferences` named `MY_CAMPUS_PREFS`.
4. **Header Interceptor**: Subsequent authenticated requests construct the `Authorization` header containing `Bearer <access_token>` handled dynamically via [RetrofitClient.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/network/RetrofitClient.java#L25-L44).
5. **Decryption & Permission Checks**: The Django server verifies the signature of the incoming header JWT token. If valid, it assigns the matching User object to `request.user` and executes ViewSet role permission filters (`IsAdmin`, `IsFaculty`).

---

## 14. Data Flow Journey
Here is the step-by-step journey of data when a student submits a new grievance complaint:

1. **Android Input**: The student enters a title and description in `StudentComplaintsActivity` and clicks **Submit**.
2. **Client Validation**: The activity validates that inputs are not empty.
3. **Retrofit Mapping**: Retrofit parses the input fields into a `Complaint` java model and initiates `postComplaint(token, complaint)`.
4. **Serialization to Payload**: Gson serializes the `Complaint` java model into a JSON payload.
5. **Transport Layer**: OkHttp attaches the cached token from `SharedPreferences` to the `Authorization` header and transmits the payload over HTTP POST.
6. **Backend Routing**: Django matches the endpoint `api/complaints/` and delegates processing to `ComplaintViewSet`.
7. **Permissions & Middleware**: The JWT Authentication middleware decodes the token header, matches it to the database user, and checks permissions.
8. **Serializer Parse & Validate**: `ComplaintSerializer` validates the incoming JSON fields.
9. **Database Save**: `perform_create()` runs, mapping the active student user to the complaint, and calls `.save()` to insert a new row in the `complaints_complaint` SQLite table.
10. **JSON Response**: Django returns the created record with an HTTP 201 status code.
11. **Adapter Update**: Retrofit's `Callback` on the Android client receives the response, appends the new `Complaint` object to the local `ArrayList`, and calls `adapter.notifyDataSetChanged()` to update the RecyclerView list dynamically.

---

## 15. Module-wise Analysis

### Module Dependency Diagram
```
                    ┌───────────────────────────┐
                    │       Authentication      │
                    │      (JWT Login / Reg)    │
                    └─────────────┬─────────────┘
                                  │
                                  ▼
                    ┌───────────────────────────┐
                    │      Dashboard Router     │
                    │  (Role-based Navigation)  │
                    └─────────────┬─────────────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         ▼                        ▼                        ▼
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   Admin Panel    │     │   Faculty Panel  │     │  Student Panel   │
├──────────────────┤     ├──────────────────┤     ├──────────────────┤
│ - User Management│     │ - Mark Attendance│     │ - View Attendance│
│ - View Summary   │     │ - Upload Notes   │     │ - View Results   │
│ - Resolve Griev  │     │ - Publish Grades │     │ - File Grievance │
└────────┬─────────┘     └────────┬─────────┘     └────────┬─────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │ Uses
                                  ▼
                    ┌───────────────────────────┐
                    │        Shared APIs        │
                    │(Timetable, Exam, Course)  │
                    └─────────────┬─────────────┘
                                  │ Saves to
                                  ▼
                    ┌───────────────────────────┐
                    │      SQLite Database      │
                    └───────────────────────────┘
```

### 1. User Authentication & Profile Module
* **Backend**: `accounts` app.
* **Frontend**: `MainActivity`, `RetrofitClient`.
* **API Details**: `POST /api/auth/login/`, `GET /api/auth/profile/`.
* **Flow**: Validates logins, issues JWT tokens, and routes users to their respective dashboards based on roles.

### 2. User Administration Module
* **Backend**: `adminpanel` app.
* **Frontend**: `ManageUsersActivity`, `dialog_add_user.xml`.
* **API Details**: `/api/users/`, `/api/student-profiles/`, `/api/faculty-profiles/`.
* **Flow**: Admins create and delete student and faculty accounts, and set up profiles.

### 3. Attendance Tracking Module
* **Backend**: `attendance` app.
* **Frontend**: `AttendanceActivity`, `AttendanceManagementActivity`, `StudentAttendanceActivity`.
* **API Details**: `/api/attendance/`, `/api/attendance/mark/`, `/api/attendance/update/`.
* **Flow**: Faculty marks student attendance for a course; students view their overall attendance percentage.

### 4. Academic Grading Module
* **Backend**: `results` app.
* **Frontend**: `ResultManagementActivity`, `StudentResultsActivity`, `dialog_add_result.xml`.
* **API Details**: `/api/results/`, `/api/results/upload/`.
* **Flow**: Faculty enters grade details, GPAs, and backlog statuses; students view their term GPAs.

### 5. Learning Resources Sharing Module
* **Backend**: `resources` app.
* **Frontend**: `ResourcesActivity`, `StudentResourcesActivity`, `UploadResourceActivity`.
* **API Details**: `/api/notes/`, `/api/pyqs/`.
* **Flow**: Faculty uploads files via multipart POST; students filter and download notes and question papers.

---

## 16. Gradle & Build System

### 1. Project-level Build Gradle
* Location: [build.gradle.kts (Project)](file:///d:/8th%20sem%20project/frontend/MyCampus/build.gradle.kts)
* Manages plugins globally without applying them to modules directly (e.g., `alias(libs.plugins.android.application) apply false`).

### 2. Module-level Build Gradle
* Location: [build.gradle.kts (Module app)](file:///d:/8th%20sem%20project/frontend/MyCampus/app/build.gradle.kts)
* Declares application package IDs, compile targets, and third-party libraries:
  * `compileSdk`: SDK 36 (Android 15)
  * `minSdk`: SDK 24 (Android 7.0)
  * `targetSdk`: SDK 36
  * `sourceCompatibility`/`targetCompatibility`: Java 11

### 3. Version Catalog (`libs.versions.toml`)
* Location: [libs.versions.toml](file:///d:/8th%20sem%20project/frontend/MyCampus/gradle/libs.versions.toml)
* Standardizes dependency versions:
  * Android Gradle Plugin (AGP): `9.2.1`
  * Retrofit: `3.0.0`
  * Material Components: `1.14.0`
  * AppCompat: `1.7.1`

### Build and Compilation Pipeline
```
  ┌──────────────┐      ┌──────────────┐      ┌─────────────────────────┐
  │ Source Code  │ ───> │ Resources    │ ───> │ Android Asset Packaging │ ──┐
  │ (Java files) │      │ (Layout XML) │      │      Tool (AAPT2)       │   │
  └──────┬───────┘      └──────────────┘      └─────────────────────────┘   │
         │ Compilation                                                      │ Merges
         ▼                                                                  ▼
  ┌──────────────┐                             ┌────────────────────────┐
  │  Dex Tool    │ ──────────────────────────> │   Signed APK Package   │
  │ (Class->Dex) │                             │      (app-debug)       │
  └──────────────┘                             └────────────────────────┘
```

---

## 17. Android Resource Structure
The `app/src/main/res` folder contains the application UI resources:

* `layout/`: XML layouts detailing screen wireframes.
* `drawable/`: Vector graphics and layout background shapes (e.g., `bg_login_gradient.xml`).
* `mipmap/`: Launcher icon configurations.
* `values/`:
  * `colors.xml`: Standardizes the application color palette.
  * `strings.xml`: Localizes application text labels and error prompts.
  * `themes.xml`: Defines material color themes and action bar states.

---

## 18. Security Analysis
1. **Password Hashing**: Django hashes passwords using `PBKDF2` with a `SHA-256` signature before database insertions.
2. **Stateless API Authorization**: Handled via secure JSON Web Tokens. Token validity defaults to **30 minutes** for access tokens and **7 days** for refresh tokens.
3. **Role-Based Access Controls (RBAC)**: Custom Django decorators (`IsAdmin`, `IsFaculty`, `IsStudent`) filter HTTP requests to verify permissions before executing model queries.

---

## 19. Error Handling Documentation

* **Client Validation**: Java controllers check input fields using text length and pattern matching. Null fields are flagged immediately without triggering network requests.
* **Retrofit Failures**: Handled using the `onFailure` callback. Network timeouts or connection issues display localized Toast prompts.
* **HTTP Status Code Mapping**:
  * `200 OK` / `201 Created`: Request succeeded.
  * `400 Bad Request`: Validation failure.
  * `401 Unauthorized`: Token expired or invalid.
  * `403 Forbidden`: Role permission violation.
  * `404 Not Found`: Model resource missing.
* **Serializer Validation**: Serializers raise validation exceptions if data types do not match the expected formats, returning a `400 Bad Request` with field-specific error logs.

---

## 20. Testing Strategy
* **Unit Testing (Backend)**: Tested using Django's `APITestCase` framework. Tests verify CRUD endpoints, authentication headers, and role-based request filtering.
* **Integration Testing**: Postman collections test API request parameters and JSON responses.
* **Manual UI Testing**: Developers use the Android Emulator to verify screen flows and layouts across different roles.

---

## 21. Deployment Guide

### Backend Server Setup
1. Open a terminal in `d:\8th sem project\backend\campusapi`.
2. Create a Python virtual environment:
   ```powershell
   python -m venv venv
   ```
3. Activate the environment:
   ```powershell
   .\venv\Scripts\Activate.ps1
   ```
4. Install backend dependencies:
   ```powershell
   pip install -r ..\requirements.txt
   ```
5. Apply database migrations:
   ```powershell
   python manage.py migrate
   ```
6. Generate test data:
   ```powershell
   python manage.py generate_test_data
   ```
7. Start the local server on port 8001:
   ```powershell
   python manage.py runserver 8001
   ```

### Frontend App Setup
1. Import the project folder `d:\8th sem project\frontend\MyCampus` in Android Studio.
2. Let Gradle sync project-level version catalogs.
3. **Important**: Since the server runs on localhost, update the server URL in [RetrofitClient.java](file:///d:/8th%20sem%20project/frontend/MyCampus/app/src/main/java/com/example/mycampus/network/RetrofitClient.java#L12) to use the **emulator loopback address**:
   ```java
   private static final String BASE_URL = "http://10.0.2.2:8001/";
   ```
4. Deploy the application to an Android Emulator or a connected physical test device.

---

## 22. Future Scope
1. **Push Notifications**: Integrate Firebase Cloud Messaging (FCM) to send real-time alerts for notices and complaints.
2. **Production-grade Database**: Replace SQLite with PostgreSQL for higher concurrency and performance.
3. **Biometric Logins**: Use fingerprint and facial recognition systems for secure app access.
4. **QR Code Attendance**: Enable students to scan dynamic QR codes displayed by faculty to log attendance.
5. **AI Student Analytics**: Implement machine learning models to track and analyze student performance trends over semesters.

---

## 23. Advantages & Limitations

### Advantages
* **Stateless Operations**: JWT authentication eliminates server session storage overhead.
* **Clean Code Separation**: Modular Django apps and Android packages make maintenance easier.
* **Robust Access Controls**: Custom permissions protect administrative actions from unauthorized access.

### Limitations
* **SQLite Database**: Not suitable for high concurrency or production environments.
* **Missing Endpoints**: The projects module in the frontend app is not implemented in the backend database.
* **No Offline Syncing**: The app requires a constant internet connection to load dashboards and records.

---

## 24. Conclusion
The **Smart Campus Management System** is a modular, role-based application that automates academic workflows. By integrating a Django REST API backend with an Android Java client, the system simplifies attendance marking, resource sharing, grade publishing, and grievance tracking, making it an excellent technical reference for educational portal development.
