# 🚢 Complete Kubernetes Guide for Movie Reservation

## 📚 Table of Contents
1. [What is Kubernetes?](#what-is-kubernetes)
2. [Fundamental Concepts](#fundamental-concepts)
3. [Installation and Configuration](#installation-and-configuration)
4. [Basic Commands](#basic-commands)
5. [YAML Manifests](#yaml-manifests)
6. [Applying K8s to Your Project](#applying-k8s-to-your-project)
7. [Networking and Services](#networking-and-services)
8. [Configuration and Secrets](#configuration-and-secrets)
9. [Monitoring and Debugging](#monitoring-and-debugging)
10. [Best Practices](#best-practices)

---

## 🎯 What is Kubernetes?

**Kubernetes (K8s)** is a container orchestration platform that automates the deployment, scaling, and management of containerized applications.

### Why use Kubernetes in your project?
- **Automatic scalability**: Scale your microservices based on demand
- **High availability**: Automatically restart failed containers
- **Resource management**: Efficiently distribute CPU and memory
- **Zero-downtime deployments**: Automatic rolling updates
- **Service discovery**: Services find each other automatically

---

## 🧱 Fundamental Concepts

### 1. **Pod**
The smallest unit in K8s. Contains one or more containers that share network and storage.

### 2. **Deployment**
Manages the lifecycle of Pods, including updates and rollbacks.

### 3. **Service**
Provides a stable IP and DNS to access a set of Pods.

### 4. **ConfigMap**
Stores non-sensitive configuration (environment variables, config files).

### 5. **Secret**
Stores sensitive information (passwords, tokens, keys).

### 6. **Namespace**
Provides logical isolation of resources within the cluster.

### 7. **Ingress**
Manages external HTTP/HTTPS access to services.

---

## 🛠️ Installation and Configuration

### Option 1: Minikube (Local Development)
```bash
# Install Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Start local cluster
minikube start --driver=docker --memory=4096 --cpus=2

# Check status
minikube status
```

### Option 2: Docker Desktop
```bash
# Enable Kubernetes in Docker Desktop
# Settings > Kubernetes > Enable Kubernetes
```

### Install kubectl
```bash
# Linux
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Verify installation
kubectl version --client
```

---

## 🎮 Basic Commands

### Cluster Information
```bash
# View cluster information
kubectl cluster-info

# View nodes
kubectl get nodes

# View all resources
kubectl get all

# View resources in real-time
kubectl get pods --watch
```

### Pod Management
```bash
# List pods
kubectl get pods

# Describe a pod
kubectl describe pod <pod-name>

# View logs
kubectl logs <pod-name>

# Execute command in pod
kubectl exec -it <pod-name> -- /bin/bash

# Delete pod
kubectl delete pod <pod-name>
```

### Apply Manifests
```bash
# Apply a file
kubectl apply -f deployment.yaml

# Apply all files from a directory
kubectl apply -f k8s/

# Delete resources
kubectl delete -f deployment.yaml
```

---

## 📄 YAML Manifests

### Basic Structure
All K8s manifests have this structure:
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
  labels:
    app: my-app
spec:
  # Resource specification
```

### Deployment Example
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  labels:
    app: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:latest
        ports:
        - containerPort: 60201
        env:
        - name: DB_HOST
          value: "postgres-service"
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

---

## 🎬 Applying K8s to Your Movie Reservation Project

### 1. Directory Structure
```
movie-reservation/
├── infra/
│   └── k8s/
│       ├── namespaces/
│       ├── databases/
│       ├── services/
│       ├── configmaps/
│       ├── secrets/
│       └── ingress/
```

### 2. Namespace for the Project
```yaml
# k8s/namespaces/movie-reservation-ns.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: movie-reservation
  labels:
    name: movie-reservation
```

### 3. ConfigMap for User Service
```yaml
# k8s/configmaps/user-service-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-service-config
  namespace: movie-reservation
data:
  DB_HOST: "postgres-service"
  DB_NAME_USER_SERVICE: "user_db"
  SERVER_PORT: "60201"
  SPRING_PROFILES_ACTIVE: "kubernetes"
```

### 4. Secret for Credentials
```yaml
# k8s/secrets/database-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: database-secret
  namespace: movie-reservation
type: Opaque
data:
  DB_USER: cG9zdGdyZXM=      # postgres (base64)
  DB_PASSWORD: cGFzc3dvcmQ=  # password (base64)
```

### 5. Deployment for User Service
```yaml
# k8s/services/user-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: movie-reservation
  labels:
    app: user-service
    version: v1
spec:
  replicas: 2
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
        version: v1
    spec:
      containers:
      - name: user-service
        image: hectormartinez/user-service:latest
        ports:
        - containerPort: 60201
        envFrom:
        - configMapRef:
            name: user-service-config
        - secretRef:
            name: database-secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 60201
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 60201
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: movie-reservation
spec:
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 60201
  type: ClusterIP
```

### 6. PostgreSQL Deployment
```yaml
# k8s/databases/postgres-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: movie-reservation
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: "user_db"
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: DB_USER
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: DB_PASSWORD
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: movie-reservation
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
  type: ClusterIP
```

---

## 🌐 Networking and Services

### Service Types

#### 1. ClusterIP (Internal)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  type: ClusterIP  # Default
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 60201
```

#### 2. NodePort (External - Development)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service-nodeport
spec:
  type: NodePort
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 60201
    nodePort: 30201
```

#### 3. LoadBalancer (External - Production)
```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service-lb
spec:
  type: LoadBalancer
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 60201
```

### Ingress Controller
```yaml
# k8s/ingress/movie-reservation-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: movie-reservation-ingress
  namespace: movie-reservation
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: movie-reservation.local
    http:
      paths:
      - path: /api/users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 80
      - path: /api/movies
        pathType: Prefix
        backend:
          service:
            name: movie-service
            port:
              number: 80
```

---

## 🔧 Configuration and Secrets

### Create Secrets from Command Line
```bash
# Create generic secret
kubectl create secret generic database-secret \
  --from-literal=DB_USER=postgres \
  --from-literal=DB_PASSWORD=mypassword \
  -n movie-reservation

# Create secret from file
kubectl create secret generic app-secret \
  --from-file=.env \
  -n movie-reservation

# View secrets
kubectl get secrets -n movie-reservation
kubectl describe secret database-secret -n movie-reservation
```

### ConfigMap from File
```bash
# Create ConfigMap from file
kubectl create configmap user-service-config \
  --from-file=application.yml \
  -n movie-reservation

# Create ConfigMap from variables
kubectl create configmap user-service-config \
  --from-literal=DB_HOST=postgres-service \
  --from-literal=SERVER_PORT=60201 \
  -n movie-reservation
```

---

## 🔍 Monitoring and Debugging

### Debugging Commands
```bash
# View pod logs
kubectl logs user-service-7d4f8c8b4-abc12 -n movie-reservation

# Real-time logs
kubectl logs -f user-service-7d4f8c8b4-abc12 -n movie-reservation

# Logs from all pods of a deployment
kubectl logs -l app=user-service -n movie-reservation

# Describe resources for debugging
kubectl describe pod user-service-7d4f8c8b4-abc12 -n movie-reservation
kubectl describe service user-service -n movie-reservation

# Cluster events
kubectl get events -n movie-reservation --sort-by=.metadata.creationTimestamp

# Resource metrics
kubectl top nodes
kubectl top pods -n movie-reservation
```

### Port Forwarding for Testing
```bash
# Local port forwarding to pod
kubectl port-forward pod/user-service-7d4f8c8b4-abc12 8080:60201 -n movie-reservation

# Forwarding to service
kubectl port-forward service/user-service 8080:80 -n movie-reservation
```

---

## 🎯 Best Practices

### 1. File Organization
```
k8s/
├── base/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   └── secret.yaml
├── services/
│   ├── user-service/
│   ├── movie-service/
│   └── ticket-service/
└── databases/
    ├── postgres/
    └── redis/
```

### 2. Consistent Labels and Selectors
```yaml
metadata:
  labels:
    app: user-service
    component: backend
    version: v1.0.0
    environment: production
```

### 3. Resource Limits and Requests
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

### 4. Health Checks
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 60201
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 60201
  initialDelaySeconds: 5
  periodSeconds: 5
```

### 5. Security Context
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 2000
```

---

## 🚀 Deployment Commands

### Deploy Your Application
```bash
# Create namespace
kubectl apply -f k8s/namespaces/

# Create secrets and configmaps
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/configmaps/

# Deploy database
kubectl apply -f k8s/databases/

# Wait for DB to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n movie-reservation --timeout=60s

# Deploy services
kubectl apply -f k8s/services/

# Verify deployment
kubectl get all -n movie-reservation

# View pod status
kubectl get pods -n movie-reservation -w
```

### Update Application
```bash
# Update image
kubectl set image deployment/user-service user-service=hectormartinez/user-service:v2.0.0 -n movie-reservation

# Check rollout
kubectl rollout status deployment/user-service -n movie-reservation

# Rollback if there are issues
kubectl rollout undo deployment/user-service -n movie-reservation
```

### Automation Scripts
```bash
#!/bin/bash
# deploy.sh

set -e

NAMESPACE="movie-reservation"
IMAGE_TAG=${1:-latest}

echo "🚀 Deploying Movie Reservation System..."

# Create namespace
kubectl apply -f k8s/namespaces/

# Apply secrets and configs
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/configmaps/

# Deploy databases
kubectl apply -f k8s/databases/
kubectl wait --for=condition=ready pod -l app=postgres -n $NAMESPACE --timeout=120s

# Deploy services
kubectl apply -f k8s/services/

# Wait for deployments
kubectl wait --for=condition=available deployment --all -n $NAMESPACE --timeout=300s

echo "✅ Deployment completed successfully!"
echo "📊 Cluster status:"
kubectl get all -n $NAMESPACE
```

---

## 🎓 Next Steps

1. **Hands-on Practice**: Implement the manifests for your project
2. **Helm Charts**: Learn to use Helm for package management
3. **Monitoring**: Implement Prometheus and Grafana
4. **CI/CD**: Integrate with GitHub Actions or GitLab CI
5. **Service Mesh**: Explore Istio for service-to-service communication
6. **Scaling**: Implement Horizontal Pod Autoscaler (HPA)

Would you like me to dive deeper into any specific topic or help you implement any practical part?
