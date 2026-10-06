# Gestion de bibliothèque — Exercice 1

Application console de gestion de bibliothèque réalisée en Java 21 dans le cadre d'un exercice pratique de programmation orientée objet (POO) et de conception logicielle.

Ce projet a pour objectif d'illustrer la modélisation métier, la mise en place d'une architecture en couches, l'utilisation de structures génériques en mémoire, l'application de règles métier strictes, ainsi que l'exploitation de l'API Stream et de l'outil de gestion de dépendances Maven.

---

## 1. Présentation

Le projet **Gestion de bibliothèque** est une application console interactive développée à des fins pédagogiques. Elle simule les opérations fondamentales de gestion des flux d'ouvrages au sein d'une bibliothèque : l'enregistrement des emprunts par les membres adhérents, le suivi des retours d'exemplaires, la consultation des emprunts individuels, la détection des retards ainsi que l'édition de statistiques d'utilisation.

Il s'agit d'un travail d'entraînement universitaire/technique et non d'une solution commerciale ou d'un logiciel de production.

---

## 2. Objectifs pédagogiques

Ce projet permet de mettre en pratique et d'approfondir les concepts clés suivants :

* **Java 21** : utilisation des fonctionnalités modernes du langage (pattern matching, switch expressions, API modernes).
* **Programmation Orientée Objet (POO)** : conception modulaire, respect des responsabilités, polymorphisme et abstractions.
* **Encapsulation & Intégrité** : protection des états internes, constructeurs avec validation des invariants et immuabilité ciblée.
* **Énumérations (`enum`)** : modélisation de domaines finis (`CategorieLivre`, `EtatLivre`) enrichis de libellés descriptifs.
* **Exceptions personnalisées** : hiérarchie d'exceptions métier (`BibliothequeException`, `QuotaEmpruntDepasseException`) pour un traitement explicite des anomalies.
* **Interfaces et généricité** : définition d'un contrat d'accès aux données réutilisable via l'interface `Repository<T>`.
* **Repositories en mémoire** : simulation de la persistance sans base de données relationnelle via des collections Java (`InMemoryRepository<T>`).
* **Services métier** : isolation de la logique applicative et de la validation des règles de gestion au sein de services dédiés.
* **Java Stream API** : manipulation déclarative des collections, filtrage, transformation et agrégation de données.
* **Comparator** : ordonnancement d'objets métier selon des critères chronologiques ou personnalisés.
* **Tests automatisés (JUnit 5)** : écriture de tests unitaires couvrant les cas nominaux, les cas limites et les levées d'exceptions.
* **Gestion de build avec Maven** : structuration standardisée du projet, cycle de vie de compilation et d'exécution des tests.

---

## 3. Fonctionnalités

L'application intègre les fonctionnalités suivantes :

* **Enregistrer un emprunt** : attribution d'un livre disponible à un membre avec définition d'une date de retour prévue.
* **Enregistrer un retour** : clôture d'un emprunt actif et remise en disponibilité immédiate de l'ouvrage.
* **Lister les emprunts d'un membre** : consultation de l'ensemble des emprunts (en cours et clôturés) associés à un adhérent donné.
* **Lister les livres en retard** : identification rapide de tous les emprunts non restitués dont la date d'échéance est dépassée.
* **Statistique des emprunts par catégorie** : calcul et affichage du nombre d'emprunts enregistrés pour chaque catégorie de livre.
* **Tri des emprunts par date de retour prévue** : tri chronologique croissant des emprunts d'un membre pour prioriser les échéances.

---

## 4. Règles métier

Le système applique scrupuleusement les règles de gestion suivantes :

### Limite d'emprunts (Quota)
Un membre ne peut pas avoir plus de **3 emprunts simultanés** (en cours).
Toute tentative d'emprunt au-delà de cette limite est rejetée par la levée de l'exception `QuotaEmpruntDepasseException`.

### Disponibilité du livre
Un livre doit obligatoirement être disponible pour faire l'objet d'un emprunt.
Les transitions d'état du livre sont automatiques :
* Lors d'un emprunt : `DISPONIBLE → EMPRUNTE`
* Lors d'un retour : `EMPRUNTE → DISPONIBLE`

Si le livre est déjà au statut `EMPRUNTE`, l'opération est refusée.

### Règle de retour
Un emprunt déjà clôturé (dont la date de retour effective est renseignée) ne peut pas être retourné une seconde fois.

### Gestion du retard
Un emprunt est considéré en retard si :
* Il est toujours actif (non retourné) et la date du jour est strictement postérieure à la date de retour prévue ;
* Ou bien s'il a été retourné tardivement (date de retour effective postérieure à la date de retour prévue).

---

## 5. Architecture du projet

Le projet adopte une architecture en couches favorisant la séparation des responsabilités.

```text
src/
├── main/
│   └── java/
│       └── sn/
│           └── codesamb/
│               ├── App.java
│               ├── exception/
│               │   ├── BibliothequeException.java
│               │   └── QuotaEmpruntDepasseException.java
│               ├── model/
│               │   ├── CategorieLivre.java
│               │   ├── Emprunt.java
│               │   ├── EtatLivre.java
│               │   ├── Livre.java
│               │   └── Membre.java
│               ├── repository/
│               │   ├── EmpruntRepository.java
│               │   ├── InMemoryRepository.java
│               │   ├── LivreRepository.java
│               │   ├── MembreRepository.java
│               │   └── Repository.java
│               └── service/
│                   ├── GestionEmpruntService.java
│                   └── StatistiqueBibliothequeService.java
└── test/
    └── java/
        └── sn/
            └── codesamb/
                ├── AppTest.java
                ├── model/
                │   └── ModelTest.java
                ├── repository/
                │   ├── EmpruntRepositoryTest.java
                │   ├── InMemoryRepositoryTest.java
                │   ├── LivreRepositoryTest.java
                │   └── MembreRepositoryTest.java
                └── service/
                    ├── GestionEmpruntServiceTest.java
                    └── StatistiqueBibliothequeServiceTest.java
```

### Rôle des composants

* **`model`** : contient les entités métier fondamentales (`Livre`, `Membre`, `Emprunt`) et les énumérations (`CategorieLivre`, `EtatLivre`). Ces classes encapsulent leurs attributs et garantissent la cohérence des données élémentaires.
* **`exception`** : regroupe les exceptions fonctionnelles personnalisées de l'application.
* **`repository`** : fournit la couche d'accès aux données. Elle s'articule autour de l'interface générique `Repository<T>` et d'une implémentation en mémoire `InMemoryRepository<T>`, spécialisée pour chaque type d'entité.
* **`service`** : orchestre les cas d'utilisation et applique les règles de gestion (validation des quotas, vérification des états, agrégations statistiques et ordonnancement).
* **`App`** : point d'entrée de l'application console. Elle gère les interactions textuelles avec l'utilisateur (affichage des menus, lecture et validation des saisies) et délègue l'exécution des opérations aux services.

---

## 6. Modèle métier

Le cœur du domaine se compose de trois entités et de deux énumérations :

### `Livre`
Représente un ouvrage physique référencé dans la bibliothèque :
* `id` (`Long`) : identifiant unique ;
* `titre` (`String`) : titre du livre ;
* `auteur` (`String`) : nom de l'auteur ;
* `categorie` (`CategorieLivre`) : thème de l'ouvrage ;
* `etat` (`EtatLivre`) : statut de disponibilité (`DISPONIBLE` ou `EMPRUNTE`).

Méthode notable : `estDisponible()` retourne `true` si le livre est à l'état `DISPONIBLE`.

### `Membre`
Représente un adhérent inscrit :
* `id` (`Long`) : identifiant unique ;
* `nom` (`String`) : nom de famille ;
* `prenom` (`String`) : prénom ;
* `email` (`String`) : adresse électronique de contact.

### `Emprunt`
Matérialise l'opération de prêt d'un livre à un membre :
* `id` (`Long`) : identifiant de la transaction ;
* `livre` (`Livre`) : livre emprunté ;
* `membre` (`Membre`) : adhérent bénéficiaire ;
* `dateEmprunt` (`LocalDate`) : date de début du prêt ;
* `dateRetourPrevue` (`LocalDate`) : date limite de restitution convenue ;
* `dateRetourEffective` (`LocalDate`) : date réelle de retour (nulle tant que le livre n'est pas rendu).

Méthodes notables :
* `estEnCours()` : vrai si le livre n'a pas encore été restitué (`dateRetourEffective == null`).
* `estEnRetard()` : vrai si la date limite de restitution est dépassée.

### Énumérations
* **`CategorieLivre`** : catégories thématiques (`ROMAN`, `SCIENCE`, `HISTOIRE`, `INFORMATIQUE`, `DROIT`).
* **`EtatLivre`** : états possibles d'un exemplaire (`DISPONIBLE`, `EMPRUNTE`).

---

## 7. Repository générique

La persistance des données repose sur l'interface générique suivante :

```java
public interface Repository<T> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    boolean deleteById(Long id);
    boolean existsById(Long id);
    long count();
}
```

### Fonctionnement en mémoire

L'implémentation de référence `InMemoryRepository<T>` stocke les entités dans une `LinkedHashMap<Long, T>`, ce qui permet d'assurer un accès rapide en temps constant $O(1)$ tout en conservant l'ordre d'insertion des enregistrements. Elle reçoit lors de sa construction une fonction d'extraction d'identifiant (`Function<T, Long>`), par exemple `Livre::getId`.

Les classes `LivreRepository`, `MembreRepository` et `EmpruntRepository` héritent de cette base générique et y ajoutent si nécessaire des méthodes de recherche spécialisées (par exemple `findByMembreId`, `findEnCoursByMembreId` ou `findEnRetard` dans `EmpruntRepository`).

### Intérêt de l'abstraction générique

* **Réutilisation du code** : factorisation complète des opérations CRUD communes (création, recherche, comptage, suppression).
* **Découplage** : séparation nette entre la logique métier et le mécanisme de stockage.
* **Évolutivité pédagogique** : possibilité de remplacer le stockage en mémoire par un stockage persistant (fichiers, base de données) sans modifier les services métier.

*Note : Les données sont stockées exclusivement en mémoire vive et ne sont pas persistées sur disque.*

---

## 8. Services métier

La logique applicative est répartie sur deux services :

### `GestionEmpruntService`
Centralise l'ensemble des règles relatives au cycle de vie des emprunts :
* validation de l'existence des membres et des livres ;
* contrôle strict du quota maximal de 3 emprunts simultanés par adhérent ;
* vérification de la disponibilité du livre et mise à jour de son statut (`DISPONIBLE ↔ EMPRUNTE`) ;
* enregistrement de la restitution et interdiction des retours multiples ;
* recherche des emprunts d'un membre et identification des prêts en retard.

### `StatistiqueBibliothequeService`
Prend en charge l'analyse et la production d'indicateurs chiffrés sur l'activité de la bibliothèque en s'appuyant sur l'API Stream de Java.

---

## 9. Exploitation de Java Stream

Le calcul des statistiques illustre la puissance expressive de l'API Stream. Par exemple, pour déterminer le **nombre total d'emprunts par catégorie de livre**, le service exécute un regroupement avec comptage :

```java
emprunts.stream()
    .collect(Collectors.groupingBy(
        emprunt -> emprunt.getLivre().getCategorie(),
        Collectors.counting()
    ));
```

### Mécanisme
* `stream()` : transforme la liste des emprunts en flux continu de données.
* `Collectors.groupingBy(...)` : partitionne le flux en regroupant les éléments selon la clé extraite (`emprunt.getLivre().getCategorie()`).
* `Collectors.counting()` : applique un réducteur en aval (*downstream collector*) pour dénombrer les emprunts de chaque catégorie.

Le résultat produit est une table d'association `Map<CategorieLivre, Long>` associant chaque catégorie au volume d'emprunts correspondant.

---

## 10. Utilisation de Comparator

Pour faciliter le suivi des échéances, les emprunts d'un membre peuvent être classés par ordre chronologique de date de retour prévue :

```java
public static final Comparator<Emprunt> COMPARATEUR_DATE_RETOUR_PREVUE =
    Comparator.comparing(Emprunt::getDateRetourPrevue);
```

Ce comparateur est combiné avec l'opération `sorted(...)` des Streams :

```java
empruntRepository.findByMembreId(membreId).stream()
    .sorted(comparator)
    .toList();
```

Cette approche permet un tri déclaratif, lisible et facilement substituable selon les besoins d'affichage.

---

## 11. Tests automatisés

La fiabilité de l'application est garantie par une suite de tests unitaires et d'intégration développée avec **JUnit 5**.

### Périmètre testé
* **Entités du modèle** : validation des constructeurs, getters/setters, règles d'invariants, méthodes `equals`, `hashCode` et `toString`.
* **Repositories** : opérations CRUD, gestion des cas limites (identifiants inexistants, valeurs nulles), requêtes spécialisées.
* **Services métier** :
  * validation des emprunts nominaux et des retours ;
  * vérification du blocage au-delà du quota de 3 emprunts ;
  * gestion des livres indisponibles ou inexistants ;
  * interdiction des retours déjà effectués ;
  * détection exacte des emprunts en retard ;
  * calculs statistiques et tris par comparateurs.
* **Application console (`App`)** : validation des parcours utilisateur, affichage des menus, contrôle des saisies invalides et messages de confirmation.

### Bilan d'exécution

```text
72 tests
0 échec
0 erreur
BUILD SUCCESS
```

---

## 12. Prérequis

Pour exécuter et compiler ce projet, votre environnement de travail doit disposer des éléments suivants :

* **Java Development Kit (JDK) 21** ou version supérieure ;
* **Apache Maven 3.10** (ou version compatible) ;
* Un terminal compatible (PowerShell, Bash, Zsh, etc.).

---

## 13. Installation et exécution

### 1. Cloner le dépôt

```powershell
git clone https://github.com/Samb444/gestion-bibliotheque-java.git
cd gestion-bibliotheque
```

### 2. Exécuter la suite de tests

```powershell
mvn clean test
```

### 3. Compiler et packager l'application

```powershell
mvn clean package
```

### 4. Lancer l'application console

Le projet ne contenant pas de plugin d'exécution prédéfini dans le `pom.xml`, vous pouvez lancer l'application console de deux manières :

#### Option A : Depuis votre IDE (Recommandé)
Ouvrez le projet dans votre environnement de développement (IntelliJ IDEA, Eclipse, VS Code) et exécutez la méthode principale `main` de la classe :
`sn.codesamb.App`

#### Option B : En ligne de commande avec Java
Après avoir exécuté la compilation (`mvn clean test` ou `mvn clean package`), lancez l'application via :

```powershell
java -cp target/classes sn.codesamb.App
```

Ou, si le fichier JAR a été généré via `mvn clean package` :

```powershell
java -cp target/gestion-bibliotheque-1.0-SNAPSHOT.jar sn.codesamb.App
```

---

## 14. Choix de conception

Les décisions d'architecture retenues privilégient la clarté et la maintenabilité :

* **Architecture en couches découplées** : la vue (`App`), la logique métier (`service`), l'accès aux données (`repository`) et la structure des données (`model`) sont rigoureusement isolés.
* **Repository générique paramétré** : l'interface `Repository<T>` et la classe `InMemoryRepository<T>` évitent la duplication de code tout en garantissant un typage fort.
* **Stockage en mémoire avec `LinkedHashMap`** : concilie l'accès instantané par clé avec la conservation de l'ordre d'insertion des éléments.
* **Exceptions fonctionnelles dédiées** : utilisation d'exceptions dérivées de `RuntimeException` pour signaler clairement les violations de règles métier (`QuotaEmpruntDepasseException`) sans encombrer la signature des méthodes.
* **API `java.time.LocalDate`** : gestion robuste des dates d'emprunt et d'échéance, avec calculs précis des retards sans recourir aux anciennes classes `Date` / `Calendar`.
* **Identité basée sur la clé primaire** : les méthodes `equals` et `hashCode` s'appuient sur l'identifiant unique `id` des entités.
* **Programmation fonctionnelle avec Stream & Comparator** : traitement lisible et fluide des listes, des calculs de statistiques et des ordonnancements.
* **Sécurisation par les tests** : chaque règle de gestion est couverte par un ensemble de tests unitaires reproductibles.

---

## 15. Limites actuelles

En tant qu'exercice pratique centré sur la programmation orientée objet en console, le projet comporte des limites assumées inhérentes à son périmètre :

* **Stockage en mémoire volatile** : aucune base de données relationnelle (PostgreSQL, MySQL) ni fichier de persistance externe n'est utilisé. Toutes les données sont réinitialisées à la fermeture de l'application.
* **Interface exclusivement textuelle** : l'interaction se fait uniquement via la console en ligne de commande ; aucune interface web ni graphique (GUI) n'est fournie.
* **Absence d'authentification** : aucun module de gestion des comptes utilisateurs, de mots de passe ou de contrôle d'accès par rôle (administrateur, bibliothécaire, membre) n'est implémenté.
* **Monothreading** : l'application est conçue pour un usage séquentiel mono-utilisateur en ligne de commande.
