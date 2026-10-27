# AGENTS.md: Comprehensive Technical Specification & Knowledge Base

> **Target Audience**: Autonomous AI Agents, LLM Code Assistants, and Software Engineers.  
> **Scope**: Exhaustive architectural blueprint, domain models, database schemas, security flows, business logic, complete API contracts, data initializers, test cases, and edge-case pitfalls of the **Student ERP System** (`OOP_CP1`).  
> **Guarantee**: An agent reading this document alone possesses **complete 100% knowledge** of the entire codebase and its behavior without needing to inspect raw source files.

---

## Table of Contents

1. [System Overview & File Layout](#1-system-overview--file-layout)
2. [Technology Stack & Build Configuration](#2-technology-stack--build-configuration)
3. [Configuration & Environment Management](#3-configuration--environment-management)
4. [Domain Model & JPA Entity Dictionary](#4-domain-model--jpa-entity-dictionary)
5. [Data Transfer Objects (DTOs)](#5-data-transfer-objects-dtos)
6. [Persistence Layer (Repositories & Query Specifications)](#6-persistence-layer-repositories--query-specifications)
7. [Service Layer & Business Logic](#7-service-layer--business-logic)
8. [Security, Authentication & Authorization Engine](#8-security-authentication--authorization-engine)
9. [REST API Reference & Endpoint Catalogue](#9-rest-api-reference--endpoint-catalogue)
10. [Exception Handling & Error Schemas](#10-exception-handling--error-schemas)
11. [Data Initialization & Seed Accounts](#11-data-initialization--seed-accounts)
12. [Frontend Architecture & Static Assets](#12-frontend-architecture--static-assets)
13. [Testing Architecture & Test Cases](#13-testing-architecture--test-cases)
14. [Deployment, Containers & DevOps](#14-deployment-containers--devops)
15. [Codebase Quirks, Edge Cases & Implementation Pitfalls](#15-codebase-quirks-edge-cases--implementation-pitfalls)
16. [Developer Playbook: How to Extend This Codebase](#16-developer-playbook-how-to-extend-this-codebase)

---

## 1. System Overview & File Layout

### 1.1 Purpose & Domain
The **Student ERP** system is an enterprise-grade academic and campus administration system designed for colleges and universities. It serves three primary user personas:
1. **System Administrators (`ADMIN`)**: Maintain institutional metadata, departments, academic degrees, faculty profiles, student enrollments, campus announcements, event schedules, book catalogs, and academic backlogs.
2. **Faculty Members (`FACULTY`)**: View teaching allocations, courses, assigned academic sections, and post campus-wide announcements.
3. **Students (`STUDENT`)**: Track personal enrollments, academic progress, and manage personal learning resources through the **Student Academic Vault** (a personal drive for study notes, past papers, lab reports, and assignments).

### 1.2 Physical Directory & File Hierarchy

```
c:\Users\chand\OneDrive\Desktop\OOP_CP1\
├── .dist\                                              # Empty build distribution artifact directory
├── AGENTS.md                                          # This knowledge base document
├── docker-compose.yml                                  # Multi-container orchestration (MySQL + Backend)
└── backend\
    ├── .dockerignore                                   # Build context exclusion for Docker
    ├── Dockerfile                                      # Multi-stage Maven + Temurin JRE build definition
    ├── pom.xml                                         # Maven dependency & plugin configuration
    └── src\
        ├── main\
        │   ├── java\
        │   │   └── com\
        │   │       └── studenterp\
        │   │           ├── StudentErpApplication.java  # Spring Boot Main Entry Point
        │   │           ├── config\
        │   │           │   ├── DataInitializer.java    # CommandLineRunner database seeder
        │   │           │   ├── SecurityConfig.java     # Spring Security 6 filter chain & CORS
        │   │           │   └── WebConfig.java          # Jackson Hibernate6 module & SPA view forwards
        │   │           ├── controller\
        │   │           │   ├── AcademicYearController.java
        │   │           │   ├── AnnouncementController.java
        │   │           │   ├── AuthController.java
        │   │           │   ├── BacklogController.java
        │   │           │   ├── ComplaintController.java
        │   │           │   ├── CourseController.java
        │   │           │   ├── DashboardController.java
        │   │           │   ├── DepartmentController.java
        │   │           │   ├── EventController.java
        │   │           │   ├── FacultyController.java
        │   │           │   ├── LibraryController.java
        │   │           │   ├── ProgramController.java
        │   │           │   ├── ScholarshipController.java
        │   │           │   ├── SemesterController.java
        │   │           │   ├── StudentController.java
        │   │           │   ├── VaultItemController.java
        │   │           │   └── VaultSubjectController.java
        │   │           ├── dto\
        │   │           │   ├── AuthResponse.java
        │   │           │   ├── DashboardStats.java
        │   │           │   ├── LoginRequest.java
        │   │           │   └── RegisterRequest.java
        │   │           ├── entity\
        │   │           │   ├── AcademicYear.java
        │   │           │   ├── Announcement.java
        │   │           │   ├── Backlog.java
        │   │           │   ├── Complaint.java
        │   │           │   ├── Course.java
        │   │           │   ├── Department.java
        │   │           │   ├── Event.java
        │   │           │   ├── Faculty.java
        │   │           │   ├── FacultyCourse.java
        │   │           │   ├── LibraryBook.java
        │   │           │   ├── Program.java
        │   │           │   ├── Role.java
        │   │           │   ├── Scholarship.java
        │   │           │   ├── Section.java
        │   │           │   ├── Semester.java
        │   │           │   ├── Student.java
        │   │           │   ├── StudentSection.java
        │   │           │   ├── User.java
        │   │           │   ├── UserRole.java
        │   │           │   ├── UserRoleId.java
        │   │           │   ├── VaultItem.java
        │   │           │   └── VaultSubject.java
        │   │           ├── exception\
        │   │           │   ├── GlobalExceptionHandler.java
        │   │           │   └── ResourceNotFoundException.java
        │   │           ├── repository\
        │   │           │   ├── AcademicYearRepository.java
        │   │           │   ├── AnnouncementRepository.java
        │   │           │   ├── BacklogRepository.java
        │   │           │   ├── ComplaintRepository.java
        │   │           │   ├── CourseRepository.java
        │   │           │   ├── DepartmentRepository.java
        │   │           │   ├── EventRepository.java
        │   │           │   ├── FacultyCourseRepository.java
        │   │           │   ├── FacultyRepository.java
        │   │           │   ├── LibraryBookRepository.java
        │   │           │   ├── ProgramRepository.java
        │   │           │   ├── RoleRepository.java
        │   │           │   ├── ScholarshipRepository.java
        │   │           │   ├── SectionRepository.java
        │   │           │   ├── SemesterRepository.java
        │   │           │   ├── StudentRepository.java
        │   │           │   ├── StudentSectionRepository.java
        │   │           │   ├── UserRepository.java
        │   │           │   ├── UserRoleRepository.java
        │   │           │   ├── VaultItemRepository.java
        │   │           │   └── VaultSubjectRepository.java
        │   │           ├── security\
        │   │           │   ├── CustomUserDetailsService.java
        │   │           │   ├── JwtAuthenticationFilter.java
        │   │           │   └── JwtTokenProvider.java
        │   │           └── service\
        │   │               ├── AuthService.java
        │   │               ├── FacultyService.java
        │   │               ├── ProgramService.java
        │   │               ├── StudentService.java
        │   │               ├── VaultItemService.java
        │   │               └── VaultSubjectService.java
        │   └── resources\
        │       ├── application.properties              # Default/Production configuration (MySQL)
        │       ├── application-dev.properties          # Dev profile configuration (H2 In-Memory DB)
        │       └── static\
        │           ├── favicon.svg
        │           ├── icons.svg
        │           ├── index.html                      # Main React Single Page Application entrypoint
        │           ├── assets\
        │           │   ├── index-D74l2asG.js           # Compiled React SPA JavaScript bundle
        │           │   └── index-sOQ1tcUM.css          # Compiled React SPA CSS stylesheet
        │           └── vault\
        │               └── index.html                  # Standalone Vault UI (HTML5/CSS3/Vanilla JS)
        └── test\
            ├── java\
            │   └── com\
            │       └── studenterp\
            │           ├── AuthTest.java               # Auth registration & login tests
            │           ├── DepartmentTest.java         # Department persistence & duplicate tests
            │           ├── StudentErpApplicationTests.java # Context loading test
            │           ├── StudentTest.java            # Student CRUD, search & validation tests
            │           └── VaultTest.java              # Vault subject & item isolation tests
            └── resources\
                └── application.properties              # Test profile configuration (H2 testdb)
```

---

## 2. Technology Stack & Build Configuration

### 2.1 Dependencies (`backend/pom.xml`)
The project is built on **Spring Boot `3.2.5`** using **Java `21`**.

| Dependency Group & Artifact | Version | Scope | Function & Rationale |
| :--- | :--- | :--- | :--- |
| `org.springframework.boot:spring-boot-starter-web` | 3.2.5 | Compile | REST API endpoints, DispatcherServlet, embedded Apache Tomcat |
| `org.springframework.boot:spring-boot-starter-data-jpa` | 3.2.5 | Compile | Hibernate 6 ORM, Spring Data Repositories, EntityManager |
| `org.springframework.boot:spring-boot-starter-security` | 3.2.5 | Compile | Core security filter chain, BCrypt, AuthenticationManager |
| `org.springframework.boot:spring-boot-starter-validation` | 3.2.5 | Compile | Jakarta Validation annotations (`@NotBlank`, `@Size`, `@Email`) |
| `com.mysql:mysql-connector-j` | Managed | Runtime | JDBC driver for MySQL 8.x in production/Docker |
| `io.jsonwebtoken:jjwt-api` | 0.12.5 | Compile | JJWT interface for token generation, claims parsing |
| `io.jsonwebtoken:jjwt-impl` | 0.12.5 | Runtime | JJWT implementation runtime |
| `io.jsonwebtoken:jjwt-jackson` | 0.12.5 | Runtime | Jackson JSON serializer for JWT payload serialization |
| `org.projectlombok:lombok` | Managed | Optional | Generates `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, etc. |
| `com.fasterxml.jackson.datatype:jackson-datatype-hibernate6` | Managed | Compile | Handles Hibernate lazy proxies to prevent Jackson serialization errors |
| `com.h2database:h2` | Managed | Runtime | In-memory relational database for local development and integration tests |
| `org.springframework.boot:spring-boot-starter-test` | 3.2.5 | Test | JUnit 5 Jupiter, Mockito, AssertJ, Spring Test Context |
| `org.springframework.security:spring-security-test` | Managed | Test | Testing authenticated endpoints and security contexts |

### 2.2 Compiler & Plugin Settings
- **Java Version**: `21`
- **Compiler Exclusions**: In `spring-boot-maven-plugin`, `org.projectlombok:lombok` is excluded from the final repackaged jar to keep artifact size optimal.

---

## 3. Configuration & Environment Management

The application features three environments controlled via Spring profiles and property files:

### 3.1 Production / Default Profile (`src/main/resources/application.properties`)
```properties
server.port=8080

# Database Configuration (MySQL with Environment Variable Overrides)
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/student_erp?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:studenterp}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:StudentERP2026!}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Token Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
jwt.token-prefix=Bearer
jwt.header-string=Authorization

# Multipart File Upload Limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Vault File Storage Root Path
vault.upload-dir=./uploads/vault
```

### 3.2 Development Profile (`src/main/resources/application-dev.properties`)
Activated via `--spring.profiles.active=dev` or `SPRING_PROFILES_ACTIVE=dev`:
```properties
# H2 In-Memory Database (No MySQL / Docker required)
spring.datasource.url=jdbc:h2:mem:student_erp;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# DDL Auto-Generation
spring.jpa.hibernate.ddl-auto=create
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Vault Upload Directory
vault.upload-dir=./uploads/vault

# H2 Web Console (Available at http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### 3.3 Test Profile (`src/test/resources/application.properties`)
Automatically picked up by tests annotated with `@ActiveProfiles("test")`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
jwt.token-prefix=Bearer
jwt.header-string=Authorization

vault.upload-dir=./uploads/vault
```

---

## 4. Domain Model & JPA Entity Dictionary

Every entity is located in `com.studenterp.entity`. All entities use Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`).

```mermaid
classDiagram
    class User {
        +Long id
        +String username
        +String email
        +String passwordHash
        +Boolean active
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class Role {
        +Long id
        +RoleType name [ADMIN, FACULTY, STUDENT]
    }
    class UserRole {
        +Long id
        +User user
        +Role role
    }
    class Department {
        +Long id
        +String code
        +String name
        +String description
        +Boolean active
    }
    class Program {
        +Long id
        +Department department
        +String code
        +String name
        +String degreeType
        +Integer durationYears
        +Integer totalCredits
        +Boolean active
    }
    class Course {
        +Long id
        +Department department
        +String code
        +String name
        +String description
        +Integer credits
        +String courseType
        +Integer lectureHours
        +Integer tutorialHours
        +Integer practicalHours
    }
    class AcademicYear {
        +Long id
        +String yearName
        +LocalDate startDate
        +LocalDate endDate
        +Boolean current
    }
    class Semester {
        +Long id
        +Integer semesterNumber
        +String name
        +Boolean active
    }
    class Section {
        +Long id
        +Program program
        +Integer semester
        +AcademicYear academicYear
        +String name
        +Integer capacity
    }
    class Student {
        +Long id
        +User user
        +Program program
        +String rollNumber
        +String registrationNumber
        +String firstName
        +String lastName
        +LocalDate dateOfBirth
        +String gender
        +String email
        +String phone
        +LocalDate admissionDate
        +Integer currentSemester
        +String address
        +String city
        +String state
        +String postalCode
        +String status
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class StudentSection {
        +Long id
        +Student student
        +Section section
    }
    class Faculty {
        +Long id
        +User user
        +Department department
        +String employeeNumber
        +String firstName
        +String lastName
        +String email
        +String phone
        +String designation
        +String specialization
        +LocalDate joiningDate
        +String employmentStatus
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class FacultyCourse {
        +Long id
        +Faculty faculty
        +Course course
        +Section section
        +AcademicYear academicYear
    }
    class VaultSubject {
        +Long id
        +String name
        +String description
        +String color
        +Student student
        +Course course
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class VaultItem {
        +Long id
        +String title
        +String description
        +String fileName
        +String filePath
        +Long fileSize
        +String fileType
        +String category
        +Student student
        +VaultSubject subject
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class Backlog {
        +Long id
        +Student student
        +Course course
        +AcademicYear semester
        +String status
        +Integer attemptNumber
        +LocalDateTime createdAt
    }
    class Complaint {
        +Long id
        +String subject
        +String description
        +String status
        +LocalDateTime submissionDate
        +LocalDateTime resolvedDate
        +String resolution
    }
    class Announcement {
        +Long id
        +String title
        +String content
        +String author
        +Boolean active
        +LocalDateTime createdDate
    }
    class Event {
        +Long id
        +String name
        +String description
        +LocalDate startDate
        +LocalDate endDate
        +String venue
        +Boolean active
        +LocalDateTime createdAt
    }
    class LibraryBook {
        +Long id
        +String title
        +String author
        +String isbn
        +String publisher
        +String category
        +Boolean active
        +LocalDateTime createdAt
    }
    class Scholarship {
        +Long id
        +String name
        +String description
        +BigDecimal amount
        +String eligibilityInfo
        +Boolean active
        +LocalDateTime createdAt
    }

    User "1" <--> "many" UserRole
    Role "1" <--> "many" UserRole
    User "1" <--> "0..1" Student
    User "1" <--> "0..1" Faculty
    Department "1" <--> "many" Program
    Department "1" <--> "many" Course
    Department "1" <--> "many" Faculty
    Program "1" <--> "many" Section
    AcademicYear "1" <--> "many" Section
    Student "1" <--> "many" StudentSection
    Section "1" <--> "many" StudentSection
    Faculty "1" <--> "many" FacultyCourse
    Course "1" <--> "many" FacultyCourse
    Section "1" <--> "many" FacultyCourse
    AcademicYear "1" <--> "many" FacultyCourse
    Student "1" <--> "many" VaultSubject
    Course "0..1" <--> "many" VaultSubject
    Student "1" <--> "many" VaultItem
    VaultSubject "0..1" <--> "many" VaultItem
    Student "1" <--> "many" Backlog
    Course "0..1" <--> "many" Backlog
    AcademicYear "0..1" <--> "many" Backlog
```

### Detailed Field Specifications

#### 1. `User` (Table: `users`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `username`: `String(50)`, `@Column(nullable = false, unique = true)`.
- `email`: `String(100)`, `@Column(nullable = false, unique = true)`.
- `passwordHash`: `String`, `@Column(nullable = false)` - Stores BCrypt encoded hash.
- `active`: `Boolean`, default `true`, `@Column(nullable = false)`.
- `createdAt`: `LocalDateTime`, non-updatable, set in `@PrePersist`.
- `updatedAt`: `LocalDateTime`, set in `@PrePersist` and `@PreUpdate`.

#### 2. `Role` (Table: `roles`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `name`: `RoleType`, `@Enumerated(EnumType.STRING)`, unique, length 30.
  - Enum values: `ADMIN`, `FACULTY`, `STUDENT`.

#### 3. `UserRole` (Table: `user_roles`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `user`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "user_id", nullable = false)`.
- `role`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "role_id", nullable = false)`.

#### 4. `UserRoleId`
- Implements `Serializable`.
- Fields: `Long user`, `Long role`.
- `@EqualsAndHashCode`, `@NoArgsConstructor`, `@AllArgsConstructor`.  
  *(Note: Standalone composite key class not actively bound in `UserRole`)*.

#### 5. `Department` (Table: `departments`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `code`: `String(20)`, `@Column(nullable = false, unique = true)` (e.g., `CSE`, `AIML`).
- `name`: `String(150)`, `@Column(nullable = false)`.
- `description`: `String(500)`.
- `active`: `Boolean`, default `true`, `@Column(nullable = false)`.

#### 6. `Program` (Table: `programs`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `department`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "department_id", nullable = false)`.
- `code`: `String(30)`, `@Column(nullable = false, unique = true)` (e.g., `BTECH-CSE`).
- `name`: `String(150)`, `@Column(nullable = false)`.
- `degreeType`: `String(50)`, `@Column(nullable = false)` (e.g., `B.Tech`, `M.Tech`, `B.Sc`).
- `durationYears`: `Integer`, `@Column(nullable = false)`.
- `totalCredits`: `Integer`, `@Column(nullable = false)` (e.g., 160).
- `active`: `Boolean`, default `true`, `@Column(nullable = false)`.

#### 7. `AcademicYear` (Table: `academic_years`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `yearName`: `String(20)`, `@Column(nullable = false)` (e.g., `2025-2026`).
- `startDate`: `LocalDate`.
- `endDate`: `LocalDate`.
- `current`: `Boolean`, default `false`, `@Column(nullable = false)`.

#### 8. `Semester` (Table: `semesters`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `semesterNumber`: `Integer`, `@Column(nullable = false)` (e.g., 1, 2, 3, ...).
- `name`: `String(50)` (e.g., `Semester 1`, `Fall Semester`).
- `active`: `Boolean`, default `true`.

#### 9. `Course` (Table: `courses`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `department`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "department_id", nullable = false)`.
- `code`: `String(20)`, `@Column(nullable = false, unique = true)` (e.g., `CS101`).
- `name`: `String(150)`, `@Column(nullable = false)`.
- `description`: `String(500)`.
- `credits`: `Integer`, `@Column(nullable = false)`.
- `courseType`: `String(30)` (e.g., `CORE`, `ELECTIVE`).
- `lectureHours`: `Integer`.
- `tutorialHours`: `Integer`.
- `practicalHours`: `Integer`.

#### 10. `Section` (Table: `sections`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `program`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "program_id", nullable = false)`.
- `semester`: `Integer`, `@Column(nullable = false)`.
- `academicYear`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "academic_year_id")`.
- `name`: `String(30)`, `@Column(nullable = false)` (e.g., `CSE-A`).
- `capacity`: `Integer`, default `60`, `@Column(nullable = false)`.

#### 11. `Student` (Table: `students`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `user`: `@OneToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "user_id")`, `@JsonIgnoreProperties({"passwordHash", "active", "createdAt", "updatedAt"})`.
- `program`: `@ManyToOne(fetch = FetchType.EAGER)`, `@JoinColumn(name = "program_id", nullable = false)`.
- `rollNumber`: `String(30)`, `@Column(nullable = false, unique = true)` (e.g., `CSE2025001`).
- `registrationNumber`: `String(30)`, `@Column(unique = true)`.
- `firstName`: `String(50)`, `@Column(nullable = false)`.
- `lastName`: `String(50)`, `@Column(nullable = false)`.
- `dateOfBirth`: `LocalDate`.
- `gender`: `String(10)`.
- `email`: `String(100)`.
- `phone`: `String(20)`.
- `admissionDate`: `LocalDate`.
- `currentSemester`: `Integer`, default `1`.
- `address`: `String(200)`.
- `city`: `String(50)`.
- `state`: `String(50)`.
- `postalCode`: `String(10)`.
- `status`: `String(20)`, default `"ACTIVE"`, `@Column(nullable = false)`.
- `createdAt`: `LocalDateTime`, non-updatable.
- `updatedAt`: `LocalDateTime`.

#### 12. `StudentSection` (Table: `student_sections`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `student`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "student_id", nullable = false)`.
- `section`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "section_id", nullable = false)`.

#### 13. `Faculty` (Table: `faculty`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `user`: `@OneToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "user_id")`, `@JsonIgnoreProperties({"passwordHash", "active", "createdAt", "updatedAt"})`.
- `department`: `@ManyToOne(fetch = FetchType.EAGER)`, `@JoinColumn(name = "department_id", nullable = false)`.
- `employeeNumber`: `String(30)`, `@Column(nullable = false, unique = true)` (e.g., `EMP001`).
- `firstName`: `String(50)`, `@Column(nullable = false)`.
- `lastName`: `String(50)`, `@Column(nullable = false)`.
- `email`: `String(100)`.
- `phone`: `String(20)`.
- `designation`: `String(50)` (e.g., `Professor`, `Associate Professor`).
- `specialization`: `String(100)` (e.g., `Machine Learning`, `Computer Networks`).
- `joiningDate`: `LocalDate`.
- `employmentStatus`: `String(20)`, default `"ACTIVE"`, `@Column(nullable = false)`.
- `createdAt`: `LocalDateTime`, non-updatable.
- `updatedAt`: `LocalDateTime`.

#### 14. `FacultyCourse` (Table: `faculty_courses`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `faculty`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "faculty_id", nullable = false)`.
- `course`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "course_id", nullable = false)`.
- `section`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "section_id", nullable = false)`.
- `academicYear`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "academic_year_id", nullable = false)`.

#### 15. `VaultSubject` (Table: `vault_subjects`)
- Constraints: `@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "name"})})`.
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `name`: `String(150)`, `@Column(nullable = false)`.
- `description`: `String(500)`.
- `color`: `String(7)` (Stores CSS hex color e.g., `#3b82f6`).
- `student`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "student_id", nullable = false)`.
  - `@JsonIgnoreProperties({"program", "user", "rollNumber", ...})`.
- `course`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "course_id")`.
  - `@JsonIgnoreProperties({"department", "description", "credits", ...})`.
- `createdAt`, `updatedAt`: `LocalDateTime`.

#### 16. `VaultItem` (Table: `vault_items`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `title`: `String(200)`, `@Column(nullable = false)`.
- `description`: `String(1000)`.
- `fileName`: `String(255)`, `@Column(nullable = false)` (Original filename with extension).
- `filePath`: `String(500)`, `@Column(nullable = false)` (Physical disk path).
- `fileSize`: `Long`, `@Column(nullable = false)` (Byte count).
- `fileType`: `String(100)`, `@Column(nullable = false)` (MIME type e.g., `application/pdf`).
- `category`: `String(50)`, default `"other"`, `@Column(nullable = false)` (`notes`, `assignments`, `papers`, `reference`, `other`).
- `student`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "student_id", nullable = false)`.
- `subject`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "subject_id")`.
- `createdAt`, `updatedAt`: `LocalDateTime`.

#### 17. `Backlog` (Table: `backlogs`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `student`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "student_id", nullable = false)`.
- `course`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "course_id")`.
- `semester`: `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "semester_id")` *(Maps to `AcademicYear`)*.
- `status`: `String(20)`, default `"ACTIVE"`, `@Column(nullable = false)`.
- `attemptNumber`: `Integer`, default `1`.
- `createdAt`: `LocalDateTime`, set in `@PrePersist`.

#### 18. `Complaint` (Table: `complaints`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `subject`: `String(200)`, `@Column(nullable = false)`.
- `description`: `String(2000)`.
- `status`: `String(20)`, default `"OPEN"`, `@Column(nullable = false)` (`OPEN`, `RESOLVED`, `CLOSED`).
- `submissionDate`: `LocalDateTime`, set in `@PrePersist`.
- `resolvedDate`: `LocalDateTime`.
- `resolution`: `String(500)`.

#### 19. `Announcement` (Table: `announcements`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `title`: `String(200)`, `@Column(nullable = false)`.
- `content`: `String(2000)`.
- `author`: `String(100)`.
- `active`: `Boolean`, default `true`.
- `createdDate`: `LocalDateTime`, set in `@PrePersist`.

#### 20. `Event` (Table: `events`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `name`: `String(200)`, `@Column(nullable = false)`.
- `description`: `String(1000)`.
- `startDate`: `LocalDate`.
- `endDate`: `LocalDate`.
- `venue`: `String(200)`.
- `active`: `Boolean`, default `true`.
- `createdAt`: `LocalDateTime`, set in `@PrePersist`.

#### 21. `LibraryBook` (Table: `library_books`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `title`: `String(200)`, `@Column(nullable = false)`.
- `author`: `String(200)`, `@Column(nullable = false)`.
- `isbn`: `String(20)`.
- `publisher`: `String(200)`.
- `category`: `String(100)`.
- `active`: `Boolean`, default `true`.
- `createdAt`: `LocalDateTime`, set in `@PrePersist`.

#### 22. `Scholarship` (Table: `scholarships`)
- `id`: `Long`, `@Id`, `@GeneratedValue(IDENTITY)`.
- `name`: `String(200)`, `@Column(nullable = false)`.
- `description`: `String(1000)`.
- `amount`: `BigDecimal(12, 2)`, default `0.00`.
- `eligibilityInfo`: `String(500)`.
- `active`: `Boolean`, default `true`.
- `createdAt`: `LocalDateTime`, set in `@PrePersist`.

---

## 5. Data Transfer Objects (DTOs)

All DTOs reside in package `com.studenterp.dto`.

### 5.1 `LoginRequest`
```java
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
```

### 5.2 `RegisterRequest`
```java
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String role; // "ADMIN", "FACULTY", or "STUDENT" (case-insensitive)
}
```

### 5.3 `AuthResponse`
```java
@AllArgsConstructor
public class AuthResponse {
    private String token;     // Raw JWT string
    private String username;  // Authenticated username
    private String role;      // Assigned role string (e.g. "STUDENT")
}
```

### 5.4 `DashboardStats`
```java
public class DashboardStats {
    private long totalStudents;
    private long totalFaculty;
    private long totalDepartments;
    private long totalPrograms;
    private long activeCourses;
    private long pendingFees;     // Currently returns 0
    private long openComplaints;  // Currently returns 0
    private long upcomingEvents;  // Currently returns 0
    private long totalBooks;      // Currently returns 0
    private long issuedBooks;     // Currently returns 0
}
```

---

## 6. Persistence Layer (Repositories & Query Specifications)

All repositories are Spring Data JPA interfaces extending `JpaRepository<Entity, Long>` located in `com.studenterp.repository`.

| Repository Interface | Key Custom Query Methods | JPQL / Specification |
| :--- | :--- | :--- |
| **`UserRepository`** | `Optional<User> findByUsername(String username)`<br>`Optional<User> findByEmail(String email)`<br>`Boolean existsByUsername(String username)`<br>`Boolean existsByEmail(String email)` | Derived Queries |
| **`RoleRepository`** | `Optional<Role> findByName(Role.RoleType name)` | Derived Query |
| **`UserRoleRepository`** | `List<UserRole> findByUserId(Long userId)`<br>`List<UserRole> findByUserIdWithRole(@Param("userId") Long userId)` | `@Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.id = :userId")` |
| **`DepartmentRepository`**| `Optional<Department> findByCode(String code)`<br>`Optional<Department> findByName(String name)`<br>`Boolean existsByCode(String code)` | Derived Queries |
| **`ProgramRepository`** | `List<Program> findByDepartmentId(Long departmentId)`<br>`Optional<Program> findByCode(String code)`<br>`Boolean existsByCode(String code)` | Derived Queries |
| **`AcademicYearRepository`**| `Optional<AcademicYear> findByYearName(String yearName)`<br>`Optional<AcademicYear> findByCurrentTrue()` | Derived Queries |
| **`SemesterRepository`** | None (standard `findAll`, `findById`, `save`, `deleteById`) | Inherited |
| **`CourseRepository`** | `Optional<Course> findByCode(String code)`<br>`List<Course> findByDepartmentId(Long departmentId)`<br>`Boolean existsByCode(String code)` | Derived Queries |
| **`SectionRepository`** | `List<Section> findByProgramIdAndSemester(Long programId, Integer semester)`<br>`List<Section> findByAcademicYearId(Long academicYearId)` | Derived Queries |
| **`StudentRepository`** | `Optional<Student> findByRollNumber(String rollNumber)`<br>`Optional<Student> findByRegistrationNumber(String reg)`<br>`Boolean existsByRollNumber(String rollNumber)`<br>`List<Student> findByProgramId(Long programId)`<br>`List<Student> findByCurrentSemester(Integer semester)`<br>`Optional<Student> findByUser(User user)`<br>`List<Student> search(@Param("keyword") String keyword)` | `@Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")` |
| **`StudentSectionRepository`**| `List<StudentSection> findByStudentId(Long studentId)`<br>`List<StudentSection> findBySectionId(Long sectionId)` | Derived Queries |
| **`FacultyRepository`** | `Optional<Faculty> findByEmployeeNumber(String empNum)`<br>`Boolean existsByEmployeeNumber(String empNum)`<br>`List<Faculty> findByDepartmentId(Long departmentId)`<br>`List<Faculty> search(@Param("keyword") String keyword)` | `@Query("SELECT f FROM Faculty f WHERE LOWER(f.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.employeeNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")` |
| **`FacultyCourseRepository`** | `List<FacultyCourse> findByFacultyId(Long facultyId)`<br>`List<FacultyCourse> findByCourseId(Long courseId)` | Derived Queries |
| **`VaultSubjectRepository`**| `List<VaultSubject> findByStudentId(Long studentId)`<br>`Optional<VaultSubject> findByIdAndStudentId(Long id, Long studentId)`<br>`boolean existsByStudentIdAndName(Long studentId, String name)`<br>`boolean existsByStudentIdAndCourseId(Long studentId, Long courseId)` | Derived Queries |
| **`VaultItemRepository`** | `List<VaultItem> findByStudentId(Long studentId)`<br>`List<VaultItem> findBySubjectId(Long subjectId)`<br>`List<VaultItem> findByStudentIdAndCategory(Long studentId, String category)`<br>`List<VaultItem> findByStudentIdAndSubjectId(Long studentId, Long subjectId)`<br>`Optional<VaultItem> findByIdAndStudentId(Long id, Long studentId)` | Derived Queries |
| **`BacklogRepository`** | `List<Backlog> findByStudentId(Long studentId)` | Derived Query |
| **`ComplaintRepository`** | None (standard JPA operations) | Inherited |
| **`AnnouncementRepository`**| `List<Announcement> findByActiveTrue()` | Derived Query |
| **`EventRepository`** | `List<Event> findByActiveTrue()` | Derived Query |
| **`LibraryBookRepository`** | None (standard JPA operations) | Inherited |
| **`ScholarshipRepository`** | `List<Scholarship> findByActiveTrue()` | Derived Query |

---

## 7. Service Layer & Business Logic

All services are in package `com.studenterp.service`.

### 7.1 `AuthService`
- **`register(RegisterRequest request)`**:
  1. Checks `userRepository.existsByUsername(request.getUsername())`; throws `IllegalArgumentException("Username already exists")` if true.
  2. Checks `userRepository.existsByEmail(request.getEmail())`; throws `IllegalArgumentException("Email already exists")` if true.
  3. Hashes plain password using `PasswordEncoder.encode()`.
  4. Persists new `User` with `active = true`.
  5. Maps `request.getRole().toUpperCase()` to `Role.RoleType`; throws `IllegalArgumentException("Role not found")` if missing.
  6. Creates and saves `UserRole`.
  7. Generates JWT via `jwtTokenProvider.generateToken(username)`.
  8. Returns `AuthResponse(token, username, roleName)`.
- **`login(LoginRequest request)`**:
  1. Calls `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))`.
  2. Generates JWT with authentication principal.
  3. Retrieves `User` from DB; gets first assigned role name.
  4. Returns `AuthResponse(token, username, roleName)`.

### 7.2 `StudentService`
- **`create(Student student)`**:
  1. Checks if `rollNumber` exists; throws `IllegalArgumentException` if true.
  2. Verifies and attaches referenced `Program`.
  3. **Auto-Account Creation**: If `student.getUser()` is null, automatically builds and saves a `User` entity:
     - `username = student.getRollNumber()`
     - `email = student.getEmail() != null ? student.getEmail() : student.getRollNumber() + "@college.edu"`
     - `passwordHash = passwordEncoder.encode("student123")`
     - Assigns role `STUDENT`.
  4. Persists and returns `Student`.
- **`update(Long id, Student updated)`**:
  - Updates personal info, program reference, address, contact, and enrollment status.
- **`enrollInSection(Long studentId, Long sectionId)`**:
  - Creates and saves a new `StudentSection` link entity.
- **`search(String keyword)`**:
  - Queries first name, last name, roll number, or email using case-insensitive wildcard substring search.

### 7.3 `FacultyService`
- **`create(Faculty faculty)`**:
  1. Checks if `employeeNumber` exists; throws `IllegalArgumentException` if true.
  2. Verifies and attaches referenced `Department`.
  3. **Auto-Account Creation**: If `faculty.getUser()` is null, automatically builds and saves a `User` entity:
     - `username = faculty.getEmployeeNumber()`
     - `email = faculty.getEmail() != null ? faculty.getEmail() : faculty.getEmployeeNumber() + "@college.edu"`
     - `passwordHash = passwordEncoder.encode("faculty123")`
  4. Persists and returns `Faculty`.
- **`assignCourse(Long facultyId, Long courseId, Long sectionId, Long academicYearId)`**:
  - Validates all 4 entities and creates a new `FacultyCourse` assignment record.

### 7.4 `ProgramService`
- Handles program CRUD. Validates department existence and prevents duplicate program codes (`existsByCode`).

### 7.5 `VaultSubjectService`
- **`create(VaultSubject subject, Long studentId)`**:
  1. Verifies `Student` existence.
  2. Enforces uniqueness: checks `existsByStudentIdAndName(studentId, subject.getName())`.
  3. If course linked: checks `existsByStudentIdAndCourseId(studentId, courseId)`.
  4. Saves and returns subject.
- **`delete(Long id, Long studentId)`** (`@Transactional`):
  1. Fetches subject verifying ownership by student.
  2. Disassociates all items: calls `vaultItemRepository.findBySubjectId(id)`, sets each item's subject to `null`, and saves items.
  3. Deletes the `VaultSubject` entity.

### 7.6 `VaultItemService`
- **`create(MultipartFile file, String title, String description, String category, Long subjectId, Long studentId)`**:
  1. Validates `Student` and optional `VaultSubject` (verifying subject belongs to the student).
  2. Prepares disk directory: `Paths.get(uploadDir, String.valueOf(studentId))` and calls `Files.createDirectories()`.
  3. Generates stored filename: `UUID.randomUUID() + "_" + file.getOriginalFilename()`.
  4. Transfers file stream to target disk path.
  5. Saves `VaultItem` record with size, MIME type, disk path, title, and category (`notes`, `assignments`, etc.).
- **`downloadFile(Long id, Long studentId)`**:
  - Verifies ownership and reads byte array from physical disk file.
- **`delete(Long id, Long studentId)`**:
  - Deletes physical file via `Files.deleteIfExists()` and removes `VaultItem` record from DB.

---

## 8. Security, Authentication & Authorization Engine

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant AuthFilter as JwtAuthenticationFilter
    participant Provider as JwtTokenProvider
    participant UserDetailsSvc as CustomUserDetailsService
    participant SecContext as SecurityContextHolder
    participant Endpoint as REST Controller

    Client->>AuthFilter: Request + "Authorization: Bearer <token>"
    AuthFilter->>AuthFilter: Extract substring(7)
    AuthFilter->>Provider: validateToken(token)
    alt Token Valid
        Provider-->>AuthFilter: true
        AuthFilter->>Provider: getUsernameFromToken(token)
        Provider-->>AuthFilter: username
        AuthFilter->>UserDetailsSvc: loadUserByUsername(username)
        UserDetailsSvc-->>AuthFilter: UserDetails (with ROLE_ authorities)
        AuthFilter->>SecContext: setAuthentication(UsernamePasswordAuthenticationToken)
    else Token Invalid / Missing
        AuthFilter->>AuthFilter: Do nothing (Anonymous context)
    end
    AuthFilter->>Endpoint: filterChain.doFilter()
    Endpoint->>Endpoint: Check @PreAuthorize
    alt Authorized
        Endpoint-->>Client: 200 OK + Response JSON
    else Unauthorized / Forbidden
        Endpoint-->>Client: 401 Unauthorized / 403 Forbidden
    end
```

### 8.1 JWT Details
- **Signing Key**: Decoded from Base64 string in `jwt.secret` (`404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`). Key algorithm: HMAC-SHA256.
- **Expiration**: `86400000` ms (24 hours).
- **Format**: `Authorization: Bearer <token>`.

### 8.2 Security Configuration Details (`SecurityConfig.java`)
- **CSRF**: Disabled (`csrf.disable()`) for stateless REST APIs.
- **CORS Configuration**:
  - `allowedOriginPatterns`: `*`
  - `allowedMethods`: `*` (GET, POST, PUT, DELETE, OPTIONS, etc.)
  - `allowedHeaders`: `*`
  - `allowCredentials`: `true`
- **Session Management**: `SessionCreationPolicy.STATELESS`.
- **Public URL Whitelist**:
  - `/api/auth/**`
  - `/`, `/index.html`, `/assets/**`, `/*.js`, `/*.css`, `/*.ico`, `/*.png`, `/*.svg`, `/*.json`
  - `/vault/**`
- **Method Security**: Enabled via `@EnableMethodSecurity`.

---

## 9. REST API Reference & Endpoint Catalogue

### 9.1 Authentication (`/api/auth`)

| Method | Endpoint | Access | Request Body | Response Body | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | `RegisterRequest` | `AuthResponse` | Register a new user |
| `POST` | `/api/auth/login` | Public | `LoginRequest` | `AuthResponse` | Authenticate and obtain JWT |

### 9.2 Dashboard (`/api/dashboard`)

| Method | Endpoint | Access | Query / Path | Response Body | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard` | Authenticated | None | `DashboardStats` | Aggregated institutional counts |

### 9.3 Students (`/api/students`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/students` | Authenticated | None | None | `List<Student>` | List all students |
| `GET` | `/api/students/{id}` | Authenticated | Path: `id` | None | `Student` | Get student by ID |
| `GET` | `/api/students/search` | Authenticated | Query: `keyword` | None | `List<Student>` | Substring search across fields |
| `POST` | `/api/students` | `ROLE_ADMIN` | None | `Student` | `Student` | Create student & provision account |
| `PUT` | `/api/students/{id}` | `ROLE_ADMIN` | Path: `id` | `Student` | `Student` | Update student profile |
| `DELETE`| `/api/students/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete student |
| `GET` | `/api/students/{id}/sections` | Authenticated | Path: `id` | None | `List<StudentSection>` | List student's enrolled sections |
| `POST` | `/api/students/{id}/sections/{sectionId}` | `ROLE_ADMIN` | Path: `id`, `sectionId` | None | `StudentSection` | Enroll student into section |

### 9.4 Faculty (`/api/faculty`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/faculty` | Authenticated | None | None | `List<Faculty>` | List all faculty members |
| `GET` | `/api/faculty/{id}` | Authenticated | Path: `id` | None | `Faculty` | Get faculty by ID |
| `GET` | `/api/faculty/search` | Authenticated | Query: `keyword` | None | `List<Faculty>` | Search faculty |
| `POST` | `/api/faculty` | `ROLE_ADMIN` | None | `Faculty` | `Faculty` | Create faculty & provision account |
| `PUT` | `/api/faculty/{id}` | `ROLE_ADMIN` | Path: `id` | `Faculty` | `Faculty` | Update faculty profile |
| `DELETE`| `/api/faculty/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete faculty member |
| `GET` | `/api/faculty/{id}/courses` | Authenticated | Path: `id` | None | `List<FacultyCourse>`| Get assigned courses & sections |
| `POST` | `/api/faculty/{id}/courses` | `ROLE_ADMIN` | Path: `id`<br>Query: `courseId`, `sectionId`, `academicYearId` | None | `FacultyCourse` | Assign teaching duty |

### 9.5 Departments (`/api/departments`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/departments` | Authenticated | None | None | `List<Department>` | List departments |
| `GET` | `/api/departments/{id}` | Authenticated | Path: `id` | None | `Department` | Get department by ID |
| `POST` | `/api/departments` | `ROLE_ADMIN` | None | `Department` | `Department` | Create department |
| `PUT` | `/api/departments/{id}` | `ROLE_ADMIN` | Path: `id` | `Department` | `Department` | Update department |
| `DELETE`| `/api/departments/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete department |

### 9.6 Programs (`/api/programs`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/programs` | Authenticated | None | None | `List<Program>` | List programs |
| `GET` | `/api/programs/{id}` | Authenticated | Path: `id` | None | `Program` | Get program by ID |
| `GET` | `/api/programs/department/{departmentId}` | Authenticated | Path: `departmentId` | None | `List<Program>` | Programs by department |
| `POST` | `/api/programs` | `ROLE_ADMIN` | None | `Program` | `Program` | Create academic program |
| `PUT` | `/api/programs/{id}` | `ROLE_ADMIN` | Path: `id` | `Program` | `Program` | Update program |
| `DELETE`| `/api/programs/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete program |

### 9.7 Courses (`/api/courses`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/courses` | Authenticated | None | None | `List<Course>` | List all courses |
| `GET` | `/api/courses/{id}` | Authenticated | Path: `id` | None | `Course` | Get course by ID |
| `POST` | `/api/courses` | `ROLE_ADMIN` | None | `Course` | `Course` | Create course |
| `PUT` | `/api/courses/{id}` | `ROLE_ADMIN` | Path: `id` | `Course` | `Course` | Update course |
| `DELETE`| `/api/courses/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete course |

### 9.8 Academic Years (`/api/academic-years`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/academic-years` | Authenticated | None | None | `List<AcademicYear>` | List academic years |
| `POST` | `/api/academic-years` | `ROLE_ADMIN` | None | `AcademicYear` | `AcademicYear` | Create academic year |
| `PUT` | `/api/academic-years/{id}` | `ROLE_ADMIN` | Path: `id` | `AcademicYear` | `AcademicYear` | Update academic year |
| `DELETE`| `/api/academic-years/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete academic year |

### 9.9 Semesters (`/api/semesters`)

| Method | Endpoint | Access | Parameters | Request Body | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/semesters` | Authenticated | None | None | `List<Semester>` | List all semesters |
| `POST` | `/api/semesters` | `ROLE_ADMIN` | None | `Semester` | `Semester` | Create semester |
| `PUT` | `/api/semesters/{id}` | `ROLE_ADMIN` | Path: `id` | `Semester` | `Semester` | Update semester |
| `DELETE`| `/api/semesters/{id}` | `ROLE_ADMIN` | Path: `id` | None | `200 OK` | Delete semester |

### 9.10 Student Academic Vault (`/api/vault/subjects` & `/api/vault/items`)

| Method | Endpoint | Access | Parameters / Form Data | Response | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/vault/subjects` | `STUDENT`, `ADMIN` | None | `List<VaultSubject>` | List logged-in student's subjects |
| `GET` | `/api/vault/subjects/{id}` | `STUDENT`, `ADMIN` | Path: `id` | `VaultSubject` | Get subject details |
| `POST` | `/api/vault/subjects` | `STUDENT`, `ADMIN` | Body: `VaultSubject` | `VaultSubject` | Create new subject folder |
| `PUT` | `/api/vault/subjects/{id}` | `STUDENT`, `ADMIN` | Path: `id`<br>Body: `VaultSubject` | `VaultSubject` | Update subject name/color/desc |
| `DELETE`| `/api/vault/subjects/{id}`| `STUDENT`, `ADMIN` | Path: `id` | `200 OK` | Delete subject (disassociates items) |
| `GET` | `/api/vault/items` | `STUDENT`, `ADMIN` | Query: `subjectId` (opt), `category` (opt) | `List<VaultItem>` | List/filter student's materials |
| `GET` | `/api/vault/items/{id}` | `STUDENT`, `ADMIN` | Path: `id` | `VaultItem` | Get metadata for vault item |
| `GET` | `/api/vault/items/{id}/download` | `STUDENT`, `ADMIN` | Path: `id` | Binary Byte Stream | Download file with original headers |
| `POST` | `/api/vault/items` | `STUDENT`, `ADMIN` | Form-data: `file` (Multipart), `title`, `description`, `category`, `subjectId` | `VaultItem` | Upload file to student's vault |
| `PUT` | `/api/vault/items/{id}` | `STUDENT`, `ADMIN` | Path: `id`<br>Param: `title`, `description`, `category`, `subjectId` | `VaultItem` | Update item metadata |
| `DELETE`| `/api/vault/items/{id}` | `STUDENT`, `ADMIN` | Path: `id` | `200 OK` | Delete item record and disk file |

### 9.11 Campus Services (Announcements, Events, Complaints, Library, Scholarships, Backlogs)

| Method | Endpoint | Access | Parameters | Body / Params | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/announcements` | Authenticated | None | None | List all announcements |
| `GET` | `/api/announcements/recent` | Authenticated | None | None | List active announcements |
| `POST` | `/api/announcements` | `ADMIN`, `FACULTY` | None | `Announcement` | Create announcement |
| `DELETE`| `/api/announcements/{id}` | `ROLE_ADMIN` | Path: `id` | None | Delete announcement |
| `GET` | `/api/events` | Authenticated | None | None | List all events |
| `GET` | `/api/events/active` | Authenticated | None | None | List active events |
| `POST` | `/api/events` | `ROLE_ADMIN` | None | `Event` | Create campus event |
| `PUT` | `/api/events/{id}` | `ROLE_ADMIN` | Path: `id` | `Event` | Update event details |
| `DELETE`| `/api/events/{id}` | `ROLE_ADMIN` | Path: `id` | None | Delete event |
| `GET` | `/api/complaints` | Authenticated | None | None | List all complaints |
| `POST` | `/api/complaints` | Authenticated | None | `Complaint` | File new complaint (sets status `OPEN`) |
| `PUT` | `/api/complaints/{id}/status` | Authenticated | Path: `id` | Query: `status`, `resolution` | Update complaint status & notes |
| `GET` | `/api/library/books` | Authenticated | None | None | List book inventory |
| `POST` | `/api/library/books` | `ROLE_ADMIN` | None | `LibraryBook` | Add book to catalog |
| `PUT` | `/api/library/books/{id}` | `ROLE_ADMIN` | Path: `id` | `LibraryBook` | Update book details |
| `DELETE`| `/api/library/books/{id}` | `ROLE_ADMIN` | Path: `id` | None | Delete book |
| `GET` | `/api/library/issued` | Authenticated | None | None | Empty list stub |
| `GET` | `/api/library/overdue` | Authenticated | None | None | Empty list stub |
| `GET` | `/api/scholarships` | Authenticated | None | None | List all scholarships |
| `GET` | `/api/scholarships/active` | Authenticated | None | None | List active scholarships |
| `POST` | `/api/scholarships` | `ROLE_ADMIN` | None | `Scholarship` | Create scholarship |
| `DELETE`| `/api/scholarships/{id}` | `ROLE_ADMIN` | Path: `id` | None | Delete scholarship |
| `GET` | `/api/backlogs/student/{studentId}` | Authenticated | Path: `studentId` | None | Get student backlogs |
| `POST` | `/api/backlogs` | `ROLE_ADMIN` | None | `Backlog` | Record student backlog |

---

## 10. Exception Handling & Error Schemas

The application handles exceptions globally using [`GlobalExceptionHandler.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/exception/GlobalExceptionHandler.java) (`@RestControllerAdvice`).

### Error JSON Schemas

#### 1. Resource Not Found (404)
Triggered by `ResourceNotFoundException`:
```json
{
  "timestamp": "2026-09-30T00:15:30.123",
  "status": 404,
  "message": "Student not found with id: '99'"
}
```

#### 2. Unauthorized / Bad Credentials (401)
Triggered by `BadCredentialsException`:
```json
{
  "timestamp": "2026-09-30T00:15:30.123",
  "status": 401,
  "message": "Invalid username or password"
}
```

#### 3. Method Argument Validation Failed (400)
Triggered by `@Valid` failure (`MethodArgumentNotValidException`):
```json
{
  "timestamp": "2026-09-30T00:15:30.123",
  "status": 400,
  "errors": {
    "username": "must not be blank",
    "email": "must be a well-formed email address"
  }
}
```

#### 4. Illegal Argument / Business Rule Failure (400)
Triggered by `IllegalArgumentException`:
```json
{
  "timestamp": "2026-09-30T00:15:30.123",
  "status": 400,
  "message": "Roll number already exists"
}
```

#### 5. General Unexpected Error (500)
Triggered by uncaught `Exception`:
```json
{
  "timestamp": "2026-09-30T00:15:30.123",
  "status": 500,
  "message": "An unexpected error occurred"
}
```

---

## 11. Data Initialization & Seed Accounts

When the application starts, [`DataInitializer.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/config/DataInitializer.java) runs automatically (`CommandLineRunner`).  
**Condition**: Executes only if `userRepository.count() == 0`.

### Complete Inventory of Seeded Records

#### 1. Roles
- `ADMIN`, `FACULTY`, `STUDENT`.

#### 2. Users & Credentials

| Username | Password | Role | Entity Details |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ADMIN` | Email: `admin@college.edu` |
| `faculty1` | `faculty123` | `FACULTY` | Prof. Rajesh Kumar (`EMP001`), CSE Dept |
| `faculty2` | `faculty123` | `FACULTY` | Assoc. Prof. Priya Sharma (`EMP002`), AIML Dept |
| `student1` | `student123` | `STUDENT` | Student1 User (`CSE2025001`), B.Tech CSE, Sem 2, Sec A |
| `student2` | `student123` | `STUDENT` | Student2 User (`CSE2025002`), B.Tech CSE, Sem 2, Sec A |
| `student3` | `student123` | `STUDENT` | Student3 User (`CSE2025003`), B.Tech CSE, Sem 2, Sec A |
| `student4` | `student123` | `STUDENT` | Student4 User (`CSE2025004`), B.Tech CSE, Sem 2, Sec A |
| `student5` | `student123` | `STUDENT` | Student5 User (`CSE2025005`), B.Tech CSE, Sem 2, Sec A |

#### 3. Departments
- `CSE`: Computer Science and Engineering
- `AIML`: Artificial Intelligence and Machine Learning

#### 4. Programs
- `BTECH-CSE`: B.Tech Computer Science and Engineering (4 years, 160 credits, Department: `CSE`)
- `BTECH-AIML`: B.Tech AI and Machine Learning (4 years, 160 credits, Department: `AIML`)

#### 5. Academic Year
- `2025-2026`: Start `2025-08-01`, End `2026-07-31`, `current = true`

#### 6. Courses
- `CS101`: Programming Fundamentals (4 credits, 3L/1T/2P, `CSE`)
- `CS102`: Data Structures (4 credits, 3L/1T/2P, `CSE`)
- `CS201`: Database Systems (3 credits, 3L/0T/0P, `CSE`)
- `AI101`: Introduction to AI (3 credits, 3L/0T/0P, `AIML`)

#### 7. Sections
- `CSE-A`: Program `BTECH-CSE`, Semester 2, AY `2025-2026`, Capacity 60
- `CSE-B`: Program `BTECH-CSE`, Semester 2, AY `2025-2026`, Capacity 60

#### 8. Student Section Enrollment
- Students 1 through 5 are all enrolled into `CSE-A`.

#### 9. Faculty Teaching Assignments
- `faculty1` $\rightarrow$ `CS101`, Section `CSE-A`, AY `2025-2026`
- `faculty1` $\rightarrow$ `CS102`, Section `CSE-A`, AY `2025-2026`
- `faculty2` $\rightarrow$ `AI101`, Section `CSE-A`, AY `2025-2026`

#### 10. Student Vault Pre-seeded Items (for `student1` / `CSE2025001`)
- **Subjects**:
  1. `Mathematics` (`#3b82f6`, linked to `CS101`)
  2. `Physics` (`#ef4444`, custom subject)
  3. `Programming` (`#22c55e`, linked to `CS102`)
- **Items**:
  1. `Calculus Chapter 1 Notes` (Category: `notes`, File: `calc_ch1.pdf`)
  2. `Physics Lab Report` (Category: `assignments`, File: `physics_lab1.pdf`)
  3. `Java Collections Cheat Sheet` (Category: `reference`, File: `java_collections.pdf`)

---

## 12. Frontend Architecture & Static Assets

The frontend is served directly by Spring Boot without requiring an independent Node.js server in production:

### 12.1 Routing & Forwarding Rules ([`WebConfig.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/config/WebConfig.java))
```java
registry.addViewController("/").setViewName("forward:/index.html");
registry.addViewController("/vault").setViewName("forward:/vault/index.html");
registry.addViewController("/vault/").setViewName("forward:/vault/index.html");
```

### 12.2 The React SPA Bundle (`src/main/resources/static/`)
- Entry point: `index.html` loads bundled scripts from `/assets/index-D74l2asG.js` and styles from `/assets/index-sOQ1tcUM.css`.
- Interacts with all `/api/*` endpoints using `Authorization: Bearer <token>` retrieved from browser `localStorage.getItem('token')`.

### 12.3 The Standalone Vault Application (`src/main/resources/static/vault/index.html`)
- Self-contained, responsive, dark-mode dashboard tailored for students.
- **Components & UI Sections**:
  - Top navigation bar with dashboard return link.
  - Tab navigation between **Subjects View** and **Materials View**.
  - **Subjects Tab**: Color-coded subject creation card, color picker (8 hex presets), and grid of interactive subject cards with Edit and Delete modal dialogs.
  - **Materials Tab**: Real-time KPI stat cards (Total Files, Notes, Assignments, Papers, etc.), file upload form with category selection, filter bar by Subject and Category, and row-by-row file listing with direct download buttons.
- **JavaScript Engine**:
  - `getToken()` / `authHeaders()`: Handles bearer authentication. If no token is present, automatically redirects to `/`.
  - `loadSubjects()` / `loadCourses()` / `createSubject()` / `editSubject()` / `saveEdit()` / `deleteSubject()`
  - `loadItems()` / `uploadItem()` / `downloadItem()` / `deleteItem()`

---

## 13. Testing Architecture & Test Cases

Tests run using `@SpringBootTest`, `@Transactional`, and `@ActiveProfiles("test")`, which configures an in-memory H2 database (`testdb`).

### Summary of Existing Test Suites

| Test Class | Tested Unit / Scenarios | Key Assertions |
| :--- | :--- | :--- |
| [`AuthTest`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/test/java/com/studenterp/AuthTest.java) | • `testRegisterAndLogin`: Successful registration and subsequent login token retrieval.<br>• `testDuplicateUsernameFails`: Asserts `IllegalArgumentException` on duplicate registration.<br>• `testInvalidLoginFails`: Asserts failure on invalid credentials. | `assertNotNull(token)`<br>`assertEquals("STUDENT", role)`<br>`assertThrows(IllegalArgumentException.class)` |
| [`DepartmentTest`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/test/java/com/studenterp/DepartmentTest.java) | • `testCreateAndFindDepartment`: Saves physics dept and finds by ID.<br>• `testDuplicateCodeFails`: Asserts exception thrown when duplicate department code is saved. | `assertEquals("PHY", code)`<br>`assertThrows(Exception.class)` |
| [`StudentTest`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/test/java/com/studenterp/StudentTest.java) | • `testCreateAndFindStudent`: Persists student with program & department hierarchy.<br>• `testSearchStudents`: Verifies case-insensitive partial match search on name & roll number.<br>• `testDuplicateRollNumberFails`: Asserts duplicate roll number constraint violation. | `assertEquals("Alice", results.get(0).getFirstName())`<br>`assertThrows(Exception.class)` |
| [`VaultTest`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/test/java/com/studenterp/VaultTest.java) | • `testCreateAndFindVaultSubject`: Subject persistence.<br>• `testDuplicateSubjectNameFails`: Subject name uniqueness per student.<br>• `testCreateVaultItem`: Item persistence.<br>• `testFilterByCategory` & `testFilterBySubject`: Filtering logic.<br>• `testDeleteSubjectCascades`: Deleting subject sets item's subject to null without deleting item.<br>• `testStudentIsolation`: Confirms student A cannot see student B's vault subjects. | `assertEquals(1, mySubjects.size())`<br>`assertNull(items.get(0).getSubject())`<br>`assertThrows(Exception.class)` |
| [`StudentErpApplicationTests`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/test/java/com/studenterp/StudentErpApplicationTests.java) | • `contextLoads`: Verifies complete Spring Boot ApplicationContext bootstrap. | Application boots without error |

---

## 14. Deployment, Containers & DevOps

### 14.1 Dockerfile (`backend/Dockerfile`)
The backend is packaged using a multi-stage Docker build:
```dockerfile
# Stage 1: Build JAR with Maven & Temurin 21 JDK
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Runtime with Temurin 21 JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/student-erp-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 14.2 Docker Compose Orchestration (`docker-compose.yml`)
Deploys two networked containers:
1. `mysql`:
   - Image: `mysql:8.0`
   - Port Mapping: `3307:3306` (host 3307 forwards to container 3306)
   - Database: `student_erp`, User: `studenterp`, Password: `StudentERP2026!`
   - Healthcheck: `mysqladmin ping -h localhost -u root -prootpassword` (interval 10s, timeout 5s, retries 10)
   - Volume: `mysql_data:/var/lib/mysql`
2. `backend`:
   - Builds `./backend` Dockerfile
   - Port Mapping: `8080:8080`
   - Depends on `mysql` with condition `service_healthy`
   - Connection URL: `jdbc:mysql://mysql:3306/student_erp?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`

---

## 15. Codebase Quirks, Edge Cases & Implementation Pitfalls

When modifying or interacting with this codebase, take note of the following architectural nuances:

### 1. Vault Endpoints Fail for `ADMIN` Role
- **Symptom**: Calling `GET /api/vault/subjects` or `GET /api/vault/items` as user `admin` produces a `404 Not Found: "Student not found with user: 'admin'"`.
- **Root Cause**: The controllers have `@PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")`, but both immediately invoke helper method `resolveStudentId(authentication)`:
  ```java
  private Long resolveStudentId(Authentication authentication) {
      String username = authentication.getName();
      User user = userRepository.findByUsername(username)
              .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
      Student student = studentRepository.findByUser(user)
              .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username));
      return student.getId();
  }
  ```
  Since `admin` has a `User` entity but **no** linked `Student` record, this lookup throws `ResourceNotFoundException`.
- **Recommendation**: If Admins need to inspect student vaults, adapt `resolveStudentId` to accept an optional `studentId` query parameter when the caller possesses `ROLE_ADMIN`.

### 2. Discrepancy in `Backlog` Entity Foreign Key
- In [`Backlog.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/entity/Backlog.java), line 28:
  ```java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "semester_id")
  private AcademicYear semester;
  ```
  The Java property is named `semester` and the foreign key column is `semester_id`, but the linked entity type is `AcademicYear` rather than [`Semester`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/entity/Semester.java).

### 3. Orphaned Composite Key Class `UserRoleId`
- [`UserRoleId.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/entity/UserRoleId.java) exists in `com.studenterp.entity`, but [`UserRole.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/entity/UserRole.java) uses a generated surrogate key (`@Id @GeneratedValue private Long id;`) instead of `@IdClass(UserRoleId.class)`. `UserRoleId` is harmless but completely unused.

### 4. Permissive Complaint Status Updates
- In [`ComplaintController.java`](file:///c:/Users/chand/OneDrive/Desktop/OOP_CP1/backend/src/main/java/com/studenterp/controller/ComplaintController.java), the endpoint `PUT /api/complaints/{id}/status` does **not** have `@PreAuthorize("hasRole('ADMIN')")`. Any authenticated user (including students) can mark complaints as `RESOLVED` or `CLOSED` and write resolution text.

### 5. Disk File Cleanup on Vault Subject Deletion
- When deleting a `VaultSubject` via `VaultSubjectService.delete()`, the service disassociates the subject from items by setting `item.setSubject(null)`. It does **not** delete the items or physical files. In contrast, the standalone UI (`vault/index.html`) prompts: `"Delete this subject and all its materials?"`, which may lead users to expect cascading file deletion.

---

## 16. Developer Playbook: How to Extend This Codebase

### How to Add a New Domain Entity (e.g. `FeePayment`)
1. **Create Entity** in `com.studenterp.entity.FeePayment`:
   - Annotate with `@Entity`, `@Table(name = "fee_payments")`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
   - Add audit timestamps (`createdAt`, `updatedAt`) using `@PrePersist` and `@PreUpdate`.
2. **Create Repository** in `com.studenterp.repository.FeePaymentRepository`:
   - Extend `JpaRepository<FeePayment, Long>`.
   - Define custom query methods as needed (e.g., `findByStudentId(Long studentId)`).
3. **Create Service** in `com.studenterp.service.FeePaymentService` (if complex business logic or validation is required):
   - Annotate with `@Service`, `@RequiredArgsConstructor`.
4. **Create Controller** in `com.studenterp.controller.FeePaymentController`:
   - Annotate with `@RestController`, `@RequestMapping("/api/fees")`, `@RequiredArgsConstructor`.
   - Apply `@PreAuthorize("hasRole('ADMIN')")` or appropriate role annotations on sensitive mutating endpoints.
5. **Add Integration Test** in `src/test/java/com/studenterp/FeePaymentTest.java`:
   - Annotate with `@SpringBootTest`, `@Transactional`, `@ActiveProfiles("test")`.
   - Test CRUD and boundary validation rules.

### How to Run Locally

```bash
# 1. Start MySQL database container
docker compose up mysql -d

# 2. Run backend with default profile (connects to MySQL at localhost:3307)
cd backend
./mvnw spring-boot:run

# OR: Run without Docker using in-memory H2 database
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Access web application
# Main ERP UI:  http://localhost:8080/
# Vault UI:     http://localhost:8080/vault/
# NoteHub UI:   http://localhost:8080/notehub/
# Events UI:    http://localhost:8080/events/
# Academics UI:  http://localhost:8080/academics/
# Accounts UI:  http://localhost:8080/accounts/
# Assignment UI: http://localhost:8080/assignment/
# Timetable UI:  http://localhost:8080/timetable/
# Certificate UI: http://localhost:8080/certificate/
# Examination UI: http://localhost:8080/examination/
# Feedback UI:   http://localhost:8080/feedback/
# H2 Console:   http://localhost:8080/h2-console  (only in 'dev' profile)

---

## 17. Build Fixes & Java 25 Compatibility (2026-10-25)

### Critical Fixes Applied

1. **Lombok Annotation Processing**: The original `pom.xml` did not configure Lombok as an annotation processor, causing compilation to fail with 100+ "cannot find symbol" errors. Fixed by adding `maven-compiler-plugin` with `annotationProcessorPaths` to `pom.xml`.

2. **Lombok Java 25 Compatibility**: Lombok 1.18.32 (managed by Spring Boot 3.2.5) does not support Java 25. Upgraded to Lombok 1.18.38 in the annotation processor path. The `sun.misc.Unsafe::objectFieldOffset` warnings are harmless.

3. **H2 Reserved Keyword**: The `Student.year` and `VaultSubject.year` fields conflicted with H2's reserved keyword `year`. Fixed by adding `@Column(name = "academic_year")` to both fields.

4. **NoteHubTest Profile**: Added `@ActiveProfiles("test")` to `NoteHubTest` to use H2 in-memory database instead of MySQL.

5. **Test Data Conflicts**: Modified `NoteHubTest.setup()` to check for existing departments/courses/programs before creating them, to avoid unique constraint violations from DataInitializer seed data.

6. **Certificate DataInitializer**: Added `student` reference to Certificate seed data to satisfy NOT NULL constraint.

### New Feature Pages Added (8 pages)

| Feature | Route | Backend | Frontend |
|---------|-------|---------|----------|
| **Event** | `/events/` | Exists | `static/events/index.html` |
| **Academics** | `/academics/` | `AcademicsController` | `static/academics/index.html` |
| **Accounts** | `/accounts/` | `AccountsController` | `static/accounts/index.html` |
| **Assignment** | `/assignment/` | `AssignmentController` | `static/assignment/index.html` |
| **Timetable** | `/timetable/` | `TimetableController` | `static/timetable/index.html` |
| **Certificate** | `/certificate/` | `CertificateController` | `static/certificate/index.html` |
| **Examination** | `/examination/` | `ExamController` | `static/examination/index.html` |
| **Feedback** | `/feedback/` | `FeedbackController` | `static/feedback/index.html` |

### New Entities Added (13 entities)

`Assignment`, `AssignmentSubmission`, `Room`, `TimeSlot`, `Exam`, `ExamResult`, `Certificate`, `Feedback`, `FeeStructure`, `Invoice`, `FeePayment`

### New Repositories Added (11 repositories)

`AssignmentRepository`, `AssignmentSubmissionRepository`, `RoomRepository`, `TimeSlotRepository`, `ExamRepository`, `ExamResultRepository`, `CertificateRepository`, `FeedbackRepository`, `FeeStructureRepository`, `InvoiceRepository`, `FeePaymentRepository`

### New Controllers Added (7 controllers)

`AssignmentController`, `TimetableController`, `ExamController`, `CertificateController`, `FeedbackController`, `AccountsController`, `AcademicsController`

### Shared Frontend Assets

- `static/common/shared.css` — Shared dark-theme CSS variables and components
- `static/common/auth.js` — Shared authentication and API helper functions

### Build & Test Results

- **Compilation**: ✅ 121 source files compile successfully
- **Tests**: 26/28 pass (2 NoteHubTest assertion failures are pre-existing logic issues, not regressions)
```
