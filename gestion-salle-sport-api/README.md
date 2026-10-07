# Exercice 5 — API REST de Gestion d'une Salle de Sport (`gestion-salle-sport-api`)

Projet pédagogique d'initiation à **Spring Boot 3** et aux architectures d'API REST en **Java 21**.

---

## 📌 État d'avancement : Phase 3 (Repositories Spring Data JPA & Services Métiers)

Cette étape met en place la couche d'accès aux données (Spring Data JPA) et la couche de services avec injection par constructeur :
- **Repositories Spring Data JPA** : interfaces `AdherentRepository`, `CoursRepository` et `InscriptionRepository` étendant `JpaRepository` ;
- **Services Métiers** : classes `AdherentService`, `CoursService` et `InscriptionService` assurant la délégation propre et l'isolation ;
- **Injection de Dépendances** : injection par constructeur sans annotation `@Autowired` sur les attributs ;
- **Tests Unitaires Mockito** : validation de la délégation des méthodes de service vers les repositories en isolation totale de la base de données ;
- Endpoint technique de vérification (`/api/health`) maintenu opérationnel.

> **Important (Périmètre Phase 3)** : Conformément aux consignes pédagogiques de découpage, les Contrôleurs REST CRUD, DTOs, Mappers, validations `@Valid` et sécurité JWT seront intégrés dans les phases suivantes. Aucun script DDL automatique n'altère la base de données (`spring.jpa.hibernate.ddl-auto=none`).

---

## 🛠️ Stack Technique & Prérequis

| Composant | Version minimale requise | Rôle |
| :--- | :--- | :--- |
| **Java** | 21 (LTS) | Langage d'exécution |
| **Spring Boot** | 3.3.4 | Framework applicatif backend |
| **Maven** | 3.9+ | Outil de build et gestion des dépendances |
| **MySQL / MariaDB** | 8.x / 10.4+ | Système de gestion de base de données relationnelle |
| **Spring Data JPA** | Inclus dans starter | Couche ORM et persistance (Hibernate 6) |
| **Spring Validation** | Inclus dans starter | Validation des données (Bean Validation) |

---

## 🏛️ Modélisation du Domaine (Entités & Relations JPA)

### 1. Entité `Adherent` (Table `adherents`)
Représente un membre de la salle de sport.
- **Attributs & Contraintes JPA** :
  - `id` (`Long`) : Identifiant technique primaire (`@GeneratedValue(strategy = GenerationType.IDENTITY)`) ;
  - `nom` (`String`) : Non null (`@Column(nullable = false, length = 100)`) ;
  - `prenom` (`String`) : Non null (`@Column(nullable = false, length = 100)`) ;
  - `email` (`String`) : Non null et unique (`@Column(nullable = false, unique = true, length = 150)`) ;
  - `telephone` (`String`) : Non null (`@Column(nullable = false, length = 20)`) ;
  - `dateNaissance` (`LocalDate`) : Date de naissance via l'API `java.time` (`@Column(name = "date_naissance")`).
- **Association** :
  - `inscriptions` (`List<Inscription>`) : `@OneToMany(mappedBy = "adherent")` avec cascade et gestion d'orphelins.

### 2. Entité `Cours` (Table `cours`)
Représente une séance d'entraînement collective.
- **Attributs & Contraintes JPA** :
  - `id` (`Long`) : Clé primaire auto-générée (`IDENTITY`) ;
  - `nom` (`String`) : Intitulé du cours, non null (`@Column(nullable = false, length = 100)`) ;
  - `type` (`TypeCours`) : Enum stocké au format chaîne (`@Enumerated(EnumType.STRING)`) ;
  - `capacite` (`Integer`) : Capacité d'accueil positive (`@Column(nullable = false)` avec validation logicielle `capacite > 0`) ;
  - `dateHeure` (`LocalDateTime`) : Date et heure de début (`@Column(name = "date_heure", nullable = false)`) ;
  - `salle` (`String`) : Salle de pratique, non null (`@Column(nullable = false, length = 50)`).
- **Association** :
  - `inscriptions` (`List<Inscription>`) : `@OneToMany(mappedBy = "cours")` avec cascade et gestion d'orphelins.

### 3. Entité `Inscription` (Table `inscriptions`)
Représente la participation d'un adhérent à un cours spécifique.
- **Attributs & Contraintes JPA** :
  - `id` (`Long`) : Clé primaire auto-générée (`IDENTITY`) ;
  - `dateInscription` (`LocalDateTime`) : Horodatage d'enregistrement (`@Column(name = "date_inscription", nullable = false)`) ;
  - `presence` (`StatutPresence`) : Statut de présence stocké en texte (`@Enumerated(EnumType.STRING)`), valeur par défaut `ABSENT`.
- **Associations obligatoires** :
  - `adherent` (`Adherent`) : `@ManyToOne(fetch = FetchType.LAZY, optional = false)` avec `@JoinColumn(name = "adherent_id", nullable = false)` ;
  - `cours` (`Cours`) : `@ManyToOne(fetch = FetchType.LAZY, optional = false)` avec `@JoinColumn(name = "cours_id", nullable = false)`.

### 4. Énumérations
- **`TypeCours`** : `YOGA`, `CARDIO`, `MUSCULATION`, `HIIT`, `CIRCUIT`.
- **`StatutPresence`** : `PRESENT`, `ABSENT`.

---

## 🗄️ Repositories Spring Data JPA (Phase 3)

Situés dans le package `sn.codesamb.gestionsallesport.repository` :

| Repository | Entité | Méthodes spécifiques |
| :--- | :--- | :--- |
| **`AdherentRepository`** | `Adherent` | `Optional<Adherent> findByEmail(String email)` |
| **`CoursRepository`** | `Cours` | Méthodes CRUD standard `JpaRepository` |
| **`InscriptionRepository`** | `Inscription` | `List<Inscription> findByAdherentId(Long adherentId)`<br>`List<Inscription> findByCoursId(Long coursId)`<br>`boolean existsByAdherentIdAndCoursId(Long adherentId, Long coursId)` |

---

## ⚙️ Services Métiers (Phase 3)

Situés dans le package `sn.codesamb.gestionsallesport.service` avec injection obligatoire par constructeur (`final` fields) :

### 1. `AdherentService`
- `List<Adherent> findAll()`
- `Optional<Adherent> findById(Long id)`
- `Optional<Adherent> findByEmail(String email)`
- `Adherent save(Adherent adherent)`
- `void deleteById(Long id)`

### 2. `CoursService`
- `List<Cours> findAll()`
- `Optional<Cours> findById(Long id)`
- `Cours save(Cours cours)`
- `void deleteById(Long id)`

### 3. `InscriptionService`
- `List<Inscription> findAll()`
- `Optional<Inscription> findById(Long id)`
- `List<Inscription> findByAdherentId(Long adherentId)`
- `List<Inscription> findByCoursId(Long coursId)`
- `boolean existsByAdherentIdAndCoursId(Long adherentId, Long coursId)`
- `Inscription save(Inscription inscription)`
- `void deleteById(Long id)`

---

## 🗄️ Configuration de la Base de Données (MySQL)

Le fichier de configuration est situé dans [`src/main/resources/application.properties`](src/main/resources/application.properties).

### Principes appliqués :
- **Sécurité** : Aucun mot de passe ni identifiant secret n'est écrit en dur dans le code ou suivi par Git.
- **Variables configurables** : `DB_HOST` (défaut : `localhost`), `DB_PORT` (`3306`), `DB_NAME` (`salle_sport`), `DB_USER` (`root`), `DB_PASSWORD` (`""`).
- **Intégrité BDD** : `spring.jpa.hibernate.ddl-auto=none` pour interdire toute modification automatique du schéma existant par Hibernate.

```sql
-- Création préalable de la base si nécessaire :
CREATE DATABASE IF NOT EXISTS salle_sport CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📂 Architecture des Packages

```text
gestion-salle-sport-api/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/sn/codesamb/gestionsallesport/
    │   │   ├── controller/      # Contrôleurs REST (HealthController)
    │   │   ├── entity/          # Entités JPA : Adherent, Cours, Inscription, Enums
    │   │   ├── repository/      # Spring Data JPA : AdherentRepository, CoursRepository, InscriptionRepository
    │   │   ├── service/         # Services : AdherentService, CoursService, InscriptionService
    │   │   ├── dto/             # Data Transfer Objects (Phase 4+)
    │   │   ├── exception/       # Exceptions métier et gestionnaire d'erreurs (Phase 4+)
    │   │   └── GestionSalleSportApplication.java  # Point d'entrée principal
    │   └── resources/
    │       └── application.properties             # Configuration Spring Boot & MySQL
    └── test/
        └── java/sn/codesamb/gestionsallesport/
            ├── GestionSalleSportApplicationTests.java  # Test de chargement de contexte Spring
            ├── controller/
            │   └── HealthControllerTest.java           # Test unitaire de l'endpoint /api/health
            ├── entity/
            │   ├── EntityUnitTest.java                 # Tests unitaires des entités & associations
            │   └── JpaEntityMappingTest.java           # Test de conformité du métamodèle JPA
            └── service/
                ├── AdherentServiceTest.java            # Tests unitaires Mockito AdherentService
                ├── CoursServiceTest.java               # Tests unitaires Mockito CoursService
                └── InscriptionServiceTest.java         # Tests unitaires Mockito InscriptionService
```

---

## 🧪 Tests Automatisés Disponibles

Les tests sont exécutables via Maven et couvrent l'ensemble des couches implémentées :

- **`AdherentServiceTest`** : vérifie la délégation de `findAll()`, `findById()`, `findByEmail()`, `save()`, `deleteById()` vers `AdherentRepository` avec Mockito ;
- **`CoursServiceTest`** : vérifie la délégation de `findAll()`, `findById()`, `save()`, `deleteById()` vers `CoursRepository` avec Mockito ;
- **`InscriptionServiceTest`** : vérifie la délégation de `findAll()`, `findById()`, `findByAdherentId()`, `findByCoursId()`, `existsByAdherentIdAndCoursId()`, `save()`, `deleteById()` vers `InscriptionRepository` avec Mockito ;
- **`EntityUnitTest`** : intégrité des entités, constructeurs, validations logicielles et relations bidirectionnelles ;
- **`JpaEntityMappingTest`** : validation du métamodèle JPA et des types de colonnes/tables relationnelles ;
- **`HealthControllerTest`** : test du contrôleur REST `/api/health` via `MockMvc` ;
- **`GestionSalleSportApplicationTests`** : test de chargement complet du contexte Spring Boot.

---

## 🚀 Compilation et Lancement

### 1. Se placer dans le répertoire du projet
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

---

## 🔮 Éléments réservés aux prochaines phases

1. **Phase 4 — Contrôleurs REST & DTOs** :
   - DTOs d'entrée et de sortie avec mappers ;
   - Endpoints CRUD complets avec validation (`@Valid`, Bean Validation) ;
   - Gestion globale des erreurs avec `@RestControllerAdvice`.
2. **Phase 5 — Pagination, Filtres & Documentation** :
   - Pagination et tri Spring Data ;
   - Documentation OpenAPI / Swagger.
3. **Phase 6 — Sécurisation** :
   - Authentification et autorisation avec Spring Security & JWT.
