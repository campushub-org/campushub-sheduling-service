# 🗓️ CampusHub - Scheduling Service

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

> Le **Scheduling Service** est le cerveau organisationnel de CampusHub. Il orchestre la planification des enseignements, gère les assignations des professeurs et assure la cohérence temporelle en détectant les conflits de ressources.

---

## 🚀 Fonctionnalités Clés

- **Moteur de Planification** : Création, modification et suppression d'événements de calendrier (Cours, TD, TP, Examens).
- **Résolution Dynamique** : Système intelligent d'assignation des enseignants (`TeacherAssignment`) liant les identités du User Service aux codes matières.
- **Détection de Conflits** : Algorithme de vérification en temps réel pour éviter les doubles réservations de salles ou de professeurs.
- **Gestion du Référentiel Pédagogique** : Catalogue complet des Unités d'Enseignement (UE) avec gestion des crédits, niveaux et semestres.
- **Sauvegarde en Rafale (Batch)** : Possibilité d'enregistrer des séries d'événements pour une planification rapide.

---

## 🛠️ Stack Technique

- **Core :** Spring Boot 3.2.5
- **Persistence :** Spring Data JPA + Hibernate
- **Communication :** OpenFeign (pour interagir avec User et Salle Service)
- **Base de données :** MySQL 8.0
- **Discovery :** Eureka Client

---

## 📡 API Endpoints Principaux

### 📅 Événements (Events)
| Méthode | Path | Description |
| :--- | :--- | :--- |
| `GET` | `/api/scheduling/events` | Liste filtrée des événements (par prof, salle, etc.) |
| `POST` | `/api/scheduling/events` | Planification d'une nouvelle séance |
| `POST` | `/api/scheduling/batch-save` | Enregistrement groupé (récurrence) |
| `POST` | `/api/scheduling/check-conflicts` | Vérification de disponibilité d'un créneau |

### 🔗 Assignations & Matières
| Méthode | Path | Description |
| :--- | :--- | :--- |
| `GET` | `/api/scheduling/assignments/subject/:code` | Liste des profs habilités pour une UE |
| `GET` | `/api/subjects` | Catalogue complet des matières |
| `POST` | `/api/subjects` | Ajout d'une nouvelle Unité d'Enseignement |
| `PUT` | `/api/subjects/:code` | Mise à jour des crédits ou de l'intitulé d'une UE |

---

## ⚙️ Configuration & Installation

### Build du package
```bash
./mvnw clean package -DskipTests
```

### Lancement Local
```bash
./mvnw spring-boot:run
```

### Déploiement Docker
```bash
docker build -t campushub-scheduling-service .
```

---
<p align="center">L'intelligence temporelle au cœur du campus</p>
