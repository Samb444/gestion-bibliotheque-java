# Exercice 5 — API REST de Gestion d'une Salle de Sport (`gestion-salle-sport-api`)

Projet pédagogique d'initiation à **Spring Boot 3** et aux architectures d'API REST en **Java 21**.

---

## 📌 État d'avancement : Phase 1 (Initialisation Technique)

Cette première étape pose les fondations techniques du projet :
- Structure Maven standard et déclarations de dépendances ;
- Configuration de l'environnement d'exécution et de la connexion MySQL sécurisée ;
- Découpage modulaire de l'architecture en packages ;
- Mise en place d'un endpoint technique de vérification (`/api/health`).

> **Important** : Le domaine métier (entités JPA, repositories, services, contrôleurs CRUD, DTOs, validations et sécurité JWT) sera développé dans les phases suivantes.

---

## 🛠️ Stack Technique & Prérequis

| Composant | Version minimale requise | Rôle |
| :--- | :--- | :--- |
| **Java** | 21 (LTS) | Langage d'exécution |
| **Spring Boot** | 3.3.4 | Framework applicatif backend |
| **Maven** | 3.9+ | Outil de build et gestion des dépendances |
| **MySQL / MariaDB** | 8.x / 10.4+ | Système de gestion de base de données relationnelle |
| **Spring Data JPA** | Inclus dans starter | Couche ORM et persistance (Hibernate) |
| **Spring Validation** | Inclus dans starter | Validation des données (Bean Validation) |

---

## 🗄️ Configuration de la Base de Données (MySQL)

Le fichier de configuration est situé dans [`src/main/resources/application.properties`](src/main/resources/application.properties).

### Principes de sécurité appliqués :
- **Aucun mot de passe ni identifiant secret n'est écrit en dur dans le code ou suivi par Git.**
- Des variables d'environnement sont utilisées avec des valeurs par défaut adaptées au développement local (XAMPP / Wamp / MariaDB).

### Variables configurables :
| Variable | Description | Valeur par défaut locale |
| :--- | :--- | :--- |
| `DB_HOST` | Hôte du serveur MySQL | `localhost` |
| `DB_PORT` | Port d'écoute MySQL | `3306` |
| `DB_NAME` | Nom de la base de données | `salle_sport` |
| `DB_USER` | Utilisateur de connexion | `root` |
| `DB_PASSWORD` | Mot de passe associé | *(vide en local XAMPP)* |

### Démarrage préalable de MySQL :
Avant de lancer l'application, assurez-vous que votre serveur MySQL est démarré et que la base de données existe :
```sql
CREATE DATABASE IF NOT EXISTS salle_sport CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📂 Architecture des Packages

Le projet adopte une séparation nette des responsabilités :

```text
gestion-salle-sport-api/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/sn/codesamb/gestionsallesport/
    │   │   ├── controller/      # Contrôleurs REST (HealthController en Phase 1)
    │   │   ├── service/         # Logique métier et règles de gestion (Phase 3+)
    │   │   ├── repository/      # Interfaces Spring Data JPA d'accès aux données (Phase 2+)
    │   │   ├── dto/             # Data Transfer Objects requêtes/réponses (Phase 3+)
    │   │   ├── entity/          # Entités persistantes JPA (Phase 2)
    │   │   ├── exception/       # Exceptions métier et gestionnaire global (Phase 3+)
    │   │   └── GestionSalleSportApplication.java  # Classe principale (main)
    │   └── resources/
    │       └── application.properties             # Configuration Spring Boot & MySQL
    └── test/
        └── java/sn/codesamb/gestionsallesport/
            ├── GestionSalleSportApplicationTests.java  # Test de chargement de contexte
            └── controller/
                └── HealthControllerTest.java           # Test unitaire de l'endpoint /api/health
```

---

## 🚀 Compilation et Lancement

### 1. Cloner ou se placer dans le répertoire du projet
```powershell
cd gestion-salle-sport-api
```

### 2. Exécuter les tests automatisés
```powershell
mvn clean test
```

### 3. Démarrer l'application Spring Boot
```powershell
mvn spring-boot:run
```

L'application démarre sur le port **8080** : `http://localhost:8080`.

---

## 🩺 Endpoint de Vérification Technique (Health Check)

Un endpoint de test a été créé pour valider que l'API est opérationnelle :

- **Méthode** : `GET`
- **URL** : `http://localhost:8080/api/health`
- **En-tête Accept** : `application/json`
- **Réponse HTTP attendue** : `200 OK`
- **Corps de la réponse** :
```json
{
  "status": "UP"
}
```

### Test rapide via cURL ou PowerShell :
```powershell
curl http://localhost:8080/api/health
# ou via PowerShell :
Invoke-RestMethod -Uri http://localhost:8080/api/health
```

---

## 🔮 Éléments réservés aux prochaines phases

Les fonctionnalités suivantes feront l'objet des phases ultérieures :
1. **Phase 2 — Modélisation du Domaine (Entités JPA & Repositories)** :
   - `Adherent`, `Coach`, `Cours`, `Abonnement`, `Inscription`, `Paiement` ;
   - Relations JPA (`@OneToMany`, `@ManyToOne`, etc.) ;
2. **Phase 3 — DTOs, Mappings & Services Métiers** :
   - Règles de gestion (inscriptions valides, abonnements actifs) ;
   - DTOs d'entrée et de sortie ;
3. **Phase 4 — Contrôleurs REST & Validations** :
   - CRUD complets avec validation (`@Valid`, Bean Validation) ;
   - Gestion globale des erreurs avec `@RestControllerAdvice` ;
4. **Phase 5 — Pagination, Filtres & Documentation** :
   - Pagination et tri Spring Data ;
   - Documentation OpenAPI / Swagger ;
5. **Phase 6 — Sécurisation** :
   - Authentification et autorisation avec Spring Security & JWT.
