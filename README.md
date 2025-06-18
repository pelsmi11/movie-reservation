# 🎬 movie-reservation

A cloud-native, microservices-based Movie Reservation System inspired by the [roadmap.sh project specification](https://roadmap.sh/projects/movie-reservation-system). This project is designed for real-world scalability and learning, leveraging modern DevOps practices and industry-standard tools.

---

## ⚡️ **Architecture Overview**

The `movie-reservation` system is built as a Kubernetes-orchestrated set of independent microservices, each with its own data store and responsibilities. The system manages users, movies, showtimes, seat reservations, notifications, and analytics—supporting both customer and admin workflows, and integrating with external platforms for images, messaging, and email.

---

### **1. Frontend**

- **Framework:** Angular
- **Features:**
  - User and admin panels
  - Movie browsing, seat selection, booking, and history
  - Admin CRUD for movies, scheduling, and reporting
  - Service Worker for Web Push Notifications
  - Real-time in-app notifications via WebSocket
- **All API calls go through the API Gateway (Kong)**

---

### **2. API Gateway**

- **Technology:** Kong (Ingress Controller on Kubernetes)
- **Responsibilities:**
  - Single entry point for frontend traffic
  - Handles JWT authentication, authorization, rate limiting, logging, and routing
  - Ensures secure, isolated, and observable API exposure

---

### **3. Microservices**

| Microservice                | Responsibility                                       | Technology        | Neon Database | Key Feature                                 | External Services                  |
|-----------------------------|------------------------------------------------------|-------------------|--------------|----------------------------------------------|-------------------------------------|
| **user-service**            | User & role management, authentication               | Spring Boot       | user_db      | JWT issuance and validation, roles           | RabbitMQ                            |
| **movie-service**           | CRUD movies/genres, poster management                | Spring Boot       | movie_db     | Integrates Cloudinary for image upload/CDN   | Cloudinary, RabbitMQ                |
| **showtime-service**        | Scheduling, managing showtimes                       | Spring Boot       | showtime_db  | Robust showtime calendar                     | RabbitMQ                            |
| **ticket-service**          | Ticket booking, cancellation, reservation history    | Spring Boot       | ticket_db    | Transactional bookings, event publication    | RabbitMQ                            |
| **seat-reservation-service**| Seat allocation & concurrency control (anti-overbook)| Spring Boot       | seat_db      | SELECT FOR UPDATE for locking, seat holds    | RabbitMQ                            |
| **theater-service**         | Theater/screen management                            | Spring Boot       | theater_db   | CRUD for screens, locations, capacities      | RabbitMQ                            |
| **reporting-service**       | Analytics, metrics, CSV/Excel export, email reports  | Spring Boot       | reporting_db | Aggregates events, automated email via SendGrid | RabbitMQ, SendGrid                  |
| **notification-service**    | Real-time (WebSocket) and Web Push notifications     | Elixir/Phoenix OR Node.js | (user_db for tokens) | WebSocket channels, Push API, async events | RabbitMQ, Web Push, (SendGrid opt.) |

---

### **4. Messaging & Async Communication**

- **RabbitMQ (CloudAMQP):**
  - Decouples event-driven actions (e.g. "ticket_issued", "seat_reserved")
  - Powers notification-service, reporting-service, and potential audit extensions
  - Enables scalable, resilient, and distributed processing

---

### **5. External Services**

- **Neon (PostgreSQL):**  
  Each microservice has its own database, fully isolated and managed in the cloud.
- **Cloudinary:**  
  Image and media uploads for movie posters—secure URLs served via CDN.
- **SendGrid:**  
  Transactional and scheduled email delivery for reports, notifications, or alerts.

---

### **6. Notifications**

- **In-app (WebSocket):**  
  Angular receives real-time updates via notification-service (Phoenix Channel).
- **Web Push:**  
  Angular registers browser subscriptions; backend pushes native browser notifications, even when the app is closed.

---

### **7. Reporting & Exports**

- **reporting-service** exposes REST endpoints for admin analytics (top movies, revenue, occupancy, etc.).
- CSV/Excel export supported.
- Scheduled or on-demand reports delivered via SendGrid email.

---

### **8. Security & Roles**

- **JWT** authentication handled by user-service and enforced at the gateway.
- **Admin-only** endpoints for sensitive actions (CRUD, scheduling, reporting), with role-based access control at both gateway and microservice layers.

---

### **9. Infrastructure & Deployment**

- **Kubernetes** (Minikube for local, scalable for cloud):  
  Deployments, services, ingress (Kong), configmaps, and secrets defined as code.
- **Monitoring:**  
  Kubernetes Dashboard/Lens, CloudAMQP (RabbitMQ), Kong Admin API.
- **CI/CD ready:**  
  Monorepo structure for streamlined builds, testing, and deployments.

---

### **10. Monorepo Structure**
```text
/movie-reservation
│
├── services
│   ├── user-service
│   ├── movie-service
│   ├── showtime-service
│   ├── ticket-service
│   ├── seat-reservation-service
│   ├── theater-service
│   ├── reporting-service
│   └── notification-service
│
├── frontend
│   └── angular-app
│
├── infra
│   ├── k8s
│   ├── docker-compose
│   └── scripts
│
├── docs
└── README.md
```


---

## 🔄 **Typical Flow: Ticket Reservation & Notification**

1. **User** logs in via Angular (JWT issued by user-service).
2. Books a seat → **ticket-service** (validates/locks seat via seat-reservation-service).
3. ticket-service saves booking, **publishes event to RabbitMQ**.
4. **notification-service** pushes notification (WebSocket + Web Push).
5. **reporting-service** updates analytics and can email CSV reports.

---

## 🛡️ **Best Practices**

- Each microservice maintains its own schema and Flyway/Liquibase migrations.
- Automated tests (unit/integration) per service.
- Secure environment variable/config management (ConfigMap/Secret).
- Roles and JWT claims strictly enforced.

---

## **Project Inspiration**

> This implementation is inspired by the [roadmap.sh Movie Reservation System project](https://roadmap.sh/projects/movie-reservation-system).

---

*For detailed deployment, endpoints, diagrams, and contribution guidelines, see the `/docs` directory.*

---

## License

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

