# 🎬 movie-reservation (AWS Solutions Architect – Associate Exercise)

`movie-reservation` is a **cloud-native, microservices-based system** designed **primarily as a hands-on architectural exercise for AWS Solutions Architect – Associate (SAA)**.

It uses a realistic domain (movie reservations) to practice **AWS architectural decisions**: availability, security, scalability, and cost optimization.

> **Goal:** Design, deploy, and evolve a production-like system in AWS that demonstrates **high availability, security, scalability, and cost-awareness** — exactly what the SAA exam evaluates.

---

## 🎯 Primary Learning Objective (SAA-first)

This project is **not** about mastering Kubernetes first.
It is about being able to explain and implement:

* Why **ECS Fargate** instead of EC2 or EKS (at first)
* Why **private subnets + NAT** for outbound access
* How to isolate **DEV and PROD** safely
* How to expose APIs without exposing servers
* How to use **AWS-native messaging** (SNS/SQS/DLQ)
* How to design for AZ failure and cost optimization

---

## 🌍 Target Environment

* **AWS Region:** `us-east-1` (cost-effective, exam-friendly)
* **DNS:** Amazon Route 53 (authoritative DNS)
* **Domains (Route 53 records):**

  * PROD Web: `movie-reservation.pelsmi11.website`
  * PROD API: `api.movie-reservation.pelsmi11.website`
  * DEV Web: `movie-reservation-dev.pelsmi11.website`
  * DEV API: `api-dev.movie-reservation.pelsmi11.website`

---

## ⚡️ Architecture Overview (Phase-based)

The system evolves through **phases**, each mapped to common **SAA exam domains**.

### Phase 1 — Core AWS Architecture (FOUNDATION)

> Covers ~65–70% of SAA concepts

**Architecture Pattern**

* **Public** Application Load Balancer (ALB)
* **Private** ECS Fargate services
* **Private** RDS PostgreSQL
* **NAT Gateway** for outbound traffic to the internet (e.g., Maven repos, external APIs)
* **S3 + CloudFront** for the Angular frontend
* **IAM Task Roles** + **SSM Parameter Store / Secrets Manager**

**Key AWS Services**

* VPC (multi-AZ)
* Subnets (public/private) + route tables
* Internet Gateway (IGW)
* NAT Gateway
* ALB + Target Groups
* ECS Fargate
* RDS PostgreSQL
* S3 + CloudFront
* Route 53 + ACM
* CloudWatch
* IAM

### Phase 2 — Event-driven & Decoupling (MESSAGING)

> Covers SAA topics around async, retries, fan-out, DLQs

* **SNS** for publishing domain events
* **SQS** for service-specific consumption
* **DLQ** for failed processing
* Optional: **EventBridge** for broader event routing

### Phase 3 — Performance & Reliability (CACHING + DR)

* **ElastiCache (Redis)** for read-heavy endpoints
* **RDS Multi-AZ** and backup/restore drills
* Optional: **Read Replica** for scale and reporting
* Optional: **S3 lifecycle + replication** (CRR) to practice durability patterns

### Phase 4 — Optional Kubernetes/Kong (ADVANCED)

* EKS + Kong Ingress Controller
* GitOps (ArgoCD)

> Phase 4 is **not required** for SAA. It’s a post-certification upgrade path.

---

## 🧩 1. Shared Libraries / Packages

This repo includes shared code used across multiple microservices.

### `com.hectormartinezmoreira.jwt-core`

A shared **JWT core library** (internal package) to avoid duplicating token logic.

**Responsibilities**

* JWT generation utilities (claims, expirations)
* JWT validation utilities
* Standardized token parsing (Authorization header)
* Role/claim helpers (e.g., `ROLE_ADMIN`)

**How it’s used**

* `user-service`: issues JWTs using `jwt-core`
* Other services: validate and parse JWTs consistently via `jwt-core`

**Why (SAA perspective)**

* Keeps auth logic consistent across services (reduces implementation drift)
* Supports a clean “shared security primitive” while still keeping services independent

---

## 🖥️ 2. Frontend (Phase 1)

* **Framework:** Angular
* **Hosting:** S3 (static hosting) + CloudFront
* **Security:** HTTPS via ACM
* **Environments:** DEV/PROD via subdomains

**Why (SAA):** static hosting, CDN caching, TLS termination, DNS-based environment isolation.

---

## 🌐 3. API Entry Point (Phase 1)

### Initial Gateway Strategy

* **Application Load Balancer (ALB)**
* Path-based routing:

  * `/auth/*` → `user-service`
  * `/movies/*` → `movie-service`
  * `/tickets/*` → `ticket-service` (Phase 2)

**Why not Kong yet?**
ALB is cheaper/simpler and directly aligned to SAA topics (LBs, routing, health checks, HA).

---

## 🧩 4. Microservices (Phase-based Growth)

### Phase 1 — Minimal but Complete (MVP)

| Service           | Responsibility                   | Technology  | Storage          |
| ----------------- | -------------------------------- | ----------- | ---------------- |
| **user-service**  | Auth, users, roles, JWT issuance | Spring Boot | RDS (schema)     |
| **movie-service** | Movies/genres, poster metadata   | Spring Boot | RDS (schema), S3 |

### Phase 2 — Business Expansion + Messaging

| Service                  | Responsibility                      | Messaging                 |
| ------------------------ | ----------------------------------- | ------------------------- |
| **ticket-service**       | Bookings, cancellation, history     | publishes `ticket_issued` |
| **reporting-service**    | Aggregates events, exports, reports | consumes events (SQS)     |
| **notification-service** | Email notifications                 | consumes events (SQS)     |

### Phase 3 — Stronger Consistency

| Service                      | Responsibility                         |
| ---------------------------- | -------------------------------------- |
| **seat-reservation-service** | Concurrency control / anti-overbooking |
| **theater-service**          | Screens, locations, capacities         |

---

## 🗄️ 5. Data Layer

### Phase 1 (cost-aware)

* Single **RDS PostgreSQL** in private subnets
* Separate schemas per service (e.g., `user_schema`, `movie_schema`)
* Automated backups enabled

### Phase 2 (scalability)

* Separate RDS per service **or** Aurora
* Multi-AZ for production
* Optional read replica for reporting

**SAA Topics:** RDS vs Aurora, Multi-AZ vs Read Replica, backups, restore, RTO/RPO.

---

## 📦 6. Media Storage (Phase 1)

* **Amazon S3** for posters
* Upload via **presigned URLs**
* Delivery via **CloudFront**

**Phase 3 optional:** Lambda on S3 upload → generate thumbnails.

---

## 🔄 7. Messaging & Async Communication (Phase 2)

RabbitMQ is replaced to align with SAA.

### AWS-native messaging

* **SNS** topic for domain events (e.g., `ticket_issued`)
* **SQS** queues per consumer service
* **DLQ** for retries/failures

**SAA Topics:** decoupling, fan-out, retries, DLQs.

---

## 🔔 8. Notifications (Phase 2)

* Email notifications driven by events:

  * SNS → SQS → notification-service
* Optional: SES migration later (to learn AWS email)

---

## 🔐 9. Security Model (Phase 1)

* JWT issued by `user-service` (using `jwt-core`)
* Services validate tokens consistently using `jwt-core`
* IAM Task Roles (no long-lived credentials)
* Secrets stored in **SSM Parameter Store** or **Secrets Manager**
* Compute in **private subnets**

---

## 🧠 10. Caching (Phase 3)

* **ElastiCache (Redis)** for read-heavy endpoints (catalog, top movies)
* TTL + invalidation strategy

---

## 🚀 11. Infrastructure & Deployment

### Primary Path (SAA-focused)

* ECS Fargate
* ALB
* Route 53
* RDS
* S3/CloudFront
* CloudWatch

### Optional / Advanced

* EKS + Kong (Phase 4)
* GitOps (ArgoCD)

---

## 📁 Monorepo Structure

```text
/movie-reservation
│
├── services
│   ├── user-service
│   ├── movie-service
│   ├── ticket-service
│   ├── reporting-service
│   ├── notification-service
│   ├── seat-reservation-service
│   └── theater-service
│
├── packages
│   └── jwt-core                # com.hectormartinezmoreira.jwt-core
│
├── frontend
│   └── angular-app
│
├── infra
│   ├── aws
│   ├── docker
│   └── scripts
│
├── docs
│   ├── architecture
│   ├── aws-decisions
│   └── diagrams
│
└── README.md
```

---

## 🧭 Why This Project Prepares You for SAA

If you can explain:

* Why ALB + ECS Fargate
* Why private subnets + NAT
* Why SNS/SQS/DLQ over RabbitMQ
* How DEV/PROD are isolated with Route 53
* Where secrets live and how IAM roles access them
* What happens when an AZ fails

…then you’re thinking exactly like the SAA exam expects.

---

## 📌 Project Inspiration

Inspired by the roadmap.sh Movie Reservation System project, restructured as an **AWS Solutions Architect learning exercise**.

---

## License

MIT License — see [LICENSE](LICENSE).
