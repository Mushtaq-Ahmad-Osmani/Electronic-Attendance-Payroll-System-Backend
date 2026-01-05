# Electronic Attendance & Payroll System using QR code – Backend

## Project Description:

This backend application is part of the **Electronic Attendance and Payroll Management System** designed for Kateb University. The system automates teacher attendance using QR codes, calculates payroll based on attendance and credits, and provides secure role-based access.

The backend is developed using **Spring Boot** and follows a clean layered architecture (Controller, Service, Repository) to ensure scalability, security, and maintainability.

---

## Problem Statement:

Traditional attendance systems are time-consuming, error-prone, and lack automation. Payroll calculation based on manual attendance records often leads to inconsistencies and delays.

This system solves these problems by:

* Automating attendance using QR codes
* Automatically detecting absences and late check-ins
* Calculating payroll based on real attendance data
* Providing secure access for admins and teachers

---

## User Roles:

The system supports the following roles:

### Admin

* Manage teachers and users
* Create class schedules
* Generate and manage QR codes
* View all attendance records
* Manage payroll for all teachers
* Send notifications and emails

### Teacher

* Scan QR codes to mark attendance
* View personal attendance history
* View personal payroll details
* Receive notifications and emails

---

## Core Features:

* QR Code based attendance system
* Late check-in and early check-out detection
* Automatic absence marking
* Payroll calculation based on credits and attendance
* Role-based access control (Admin / Teacher)
* JWT-based authentication and authorization
* Automatic email sending:
  * Email confirmation
  * Sending QR codes to teachers
* Notification management system
* Export attendance and payroll reports (CSV / Excel)

---

## System Architecture:

The backend follows a **Layered Architecture**:

* **Controller Layer** – Handles HTTP requests (REST APIs)
* **Service Layer** – Business logic and validations
* **Repository Layer** – Database operations using JPA
* **Security Layer** – JWT & Spring Security

---

## Tech Stack:

* Java 17+
* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* Spring Data JPA (Hibernate)
* MySQL
* Maven

---

## Database Design (Overview):

Main entities used in the system:

* User
* Teacher
* Attendance
* Payroll
* TeacherSchedule
* Notification

Relationships are managed using JPA annotations to ensure data integrity.

---

## Security:

* JWT-based authentication
* Role-based authorization (ADMIN, TEACHER)
* Protected API endpoints
* Secure password hashing

---

## Testing:

The backend application has been tested using **JUnit** to ensure correctness and reliability of core business logic.

### Testing Tools
- JUnit 5
- Spring Boot Test
- Mockito (where applicable)

### Tested Components
- Service layer business logic
- Attendance validation logic
- Payroll calculation logic
- User authentication logic

### Test Location
All test classes are are located under:

---

## Installation & Running Backend:

### 1️⃣ Clone the repository

```bash
git clone https://github.com/Mushtaq-Ahmad-Osmani/Electronic-Attendance-Payroll-System-Backend
```

### 2️⃣ Configure Database

Update `application.properties` or `application.yml`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

### 3️⃣ Run the application

```bash
mvn spring-boot:run
```

The backend server will start on:

```
http://localhost:8080
```

---

## Related Repository:

- **Frontend Repository:**
https://github.com/Mushtaq-Ahmad-Osmani/Electronic-Attendance-and-payroll-System-Frontend

---

## Future Improvements

* Student role integration
* Mobile application support
* Advanced analytics dashboard
* Biometric attendance integration

---

## Author:

**Mushtaq Ahmad Osmani**
Computer Science Graduate
