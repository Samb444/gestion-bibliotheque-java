# Exercice 5 — API REST de Gestion d'une Salle de Sport (`gestion-salle-sport-api`)

Projet pédagogique d'initiation à **Spring Boot 3** et aux architectures d'API REST en **Java 21**.

---

## 📌 État d'avancement : Phase 2 (Modélisation du Domaine & Entités JPA)

Cette étape met en place les entités JPA fondamentales, leurs contraintes, leurs énumérations et leurs associations relationnelles :
- Création des entités persistantes : `Adherent`, `Cours`, `Inscription` ;
- Énumérations associées : `TypeCours` et `StatutPresence` (mappées en `EnumType.STRING`) ;
- Relations JPA bidirectionnelles avec méthodes d'assistance et protection contre les boucles infinies ;
- Tests unitaires des entités et tests d'intégration du métamodèle JPA (`Metamodel`) ;
- Endpoint technique de vérification (`/api/health`) maintenu opérationnel.

> **Important (Périmètre Phase 2)** : Conformément aux consignes pédagogiques, les Repositories Spring Data, DTOs, Mappers, Services métiers, Contrôleurs CRUD, validations `@Valid` et sécurité JWT seront intégrés dans les phases ultérieures. Aucun script DDL automatique n'altère la base de données (`spring.jpa.hibernate.ddl-auto=none`).

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

### 5. Bonnes pratiques appliquées
- **Pas de Lombok (`@Data`)** : Contrôle explicite des constructeurs, getters, setters et méthodes d'assistance bidirectionnelles (`addInscription`, `removeInscription`).
- **Prévention des boucles infinies** : Exclusion stricte des collections (`inscriptions`) et associations inverses des méthodes `toString()`, `equals()` et `hashCode()`.
- **Typage moderne `java.time`** : Utilisation exclusive de `LocalDate` et `LocalDateTime`.

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
    │   │   ├── entity/          # Entités JPA : Adherent, Cours, Inscription, Enums (Phase 2)
    │   │   ├── service/         # Logique métier et règles de gestion (Phase 3+)
    │   │   ├── repository/      # Interfaces Spring Data JPA d'accès aux données (Phase 3+)
    │   │   ├── dto/             # Data Transfer Objects requêtes/réponses (Phase 3+)
    │   │   ├── exception/       # Exceptions métier et gestionnaire global (Phase 3+)
    │   │   └── GestionSalleSportApplication.java  # Point d'entrée principal
    │   └── resources/
    │       └── application.properties             # Configuration Spring Boot & MySQL
    └── test/
        └── java/sn/codesamb/gestionsallesport/
            ├── GestionSalleSportApplicationTests.java  # Test de chargement de contexte
            ├── controller/
            │   └── HealthControllerTest.java           # Test unitaire de l'endpoint /api/health
            └── entity/
                ├── EntityUnitTest.java                 # Tests unitaires des entités & associations
                └── JpaEntityMappingTest.java           # Test de conformité du métamodèle JPA
```

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

1. **Phase 3 — Repositories & Services Métiers** :
   - Interfaces `JpaRepository` pour `Adherent`, `Cours`, `Inscription` ;
   - Règles de gestion (capacité maximale, conflits d'horaires, inscriptions valides) ;
   - DTOs d'entrée et de sortie avec mappers.
2. **Phase 4 — Contrôleurs REST & Validations** :
   - Endpoints CRUD complets avec validation (`@Valid`, Bean Validation) ;
   - Gestion globale des erreurs avec `@RestControllerAdvice`.
3. **Phase 5 — Pagination, Filtres & Documentation** :
   - Pagination et tri Spring Data ;
   - Documentation OpenAPI / Swagger.
4. **Phase 6 — Sécurisation** :
   - Authentification et autorisation avec Spring Security & JWT.
