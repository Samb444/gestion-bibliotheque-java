# Analyse de normalisation — Salle de sport

**Projet :** Gestion d'une Salle de Sport (Exercice 3)  
**Phase :** Phase 6 — Normalisation et Contrôle du Modèle  
**SGBD cible :** MariaDB / MySQL (Moteur InnoDB)  
**Date :** 2026-10-06  

---

## Introduction et Démarche

L'objectif de cette étude est d'auditer formellement la conception de la base de données relationnelle `salle_sport` à la lumière de la théorie relationnelle énoncée par Edgar F. Codd.  
L'analyse porte sur l'ensemble des sept relations du système :
- `COACH`
- `ADHERENT`
- `COURS`
- `INSCRIPTION`
- `ABONNEMENT`
- `PAIEMENT`
- `PRESENCE`

Cette vérification s'appuie sur l'analyse croisée du Modèle Conceptuel de Données (MCD), du Modèle Logique de Données (MLD), du script DDL (`sql/salle-sport.sql`) et de l'état réel des tables dans le SGBD MariaDB sous XAMPP.

---

## 1. Première forme normale — 1NF

### 1.1 Définition formelle de la 1NF
Une relation $R$ est en **Première Forme Normale (1NF)** si et seulement si :
1. Chaque colonne contient des valeurs scalaires rigoureusement **atomiques** (indivisibles dans le domaine métier).
2. Aucune colonne ne contient une collection, une liste, un tableau ou un ensemble de valeurs.
3. Aucune colonne ne concatène plusieurs informations hétérogènes.
4. Il n'existe aucun groupe répétitif d'attributs (ex. `tel_1`, `tel_2`, etc.).
5. Chaque tuple (ligne) est identifiable de manière unique par une **clé primaire** définie et non-nulle.

---

### 1.2 Analyse détaillée par table

#### Table `COACH`
- **Schéma relationnel :** `COACH (id_coach, nom, prenom, specialite)`
- **Clé primaire :** `id_coach` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `nom` et `prenom` sont découplés en deux champs scalaires distincts.
  - `specialite` stocke une chaîne scalaire unique par coach (ex. `'Musculation'`, `'Fitness'`).
  - Aucun attribut ne stocke de données concaténées ni de liste.
- **Conclusion `COACH` :** **Conforme 1NF**.

#### Table `ADHERENT`
- **Schéma relationnel :** `ADHERENT (id_adherent, nom, prenom, email, telephone)`
- **Clé primaire :** `id_adherent` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - Séparation stricte du `nom` et du `prenom`.
  - `email` est une valeur scalaire unique (garantie par la contrainte `uk_adherent_email`).
  - `telephone` est stocké sous forme d'une chaîne scalaire unique au format international E.164.
- **Conclusion `ADHERENT` :** **Conforme 1NF**.

#### Table `COURS`
- **Schéma relationnel :** `COURS (id_cours, nom, date, heure, capacite_max, id_coach)`
- **Clé primaire :** `id_cours` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - La dimension temporelle est décomposée de façon atomique en `date` (`DATE`) et `heure` (`TIME`).
  - `capacite_max` est un entier scalaire simple.
  - `id_coach` est une référence entière scalaire vers un seul coach encadrant.
- **Conclusion `COURS` :** **Conforme 1NF**.

#### Table `INSCRIPTION`
- **Schéma relationnel :** `INSCRIPTION (id_inscription, date_inscription, statut, id_adherent, id_cours)`
- **Clé primaire :** `id_inscription` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `date_inscription` est une valeur temporelle atomique scalaire.
  - `statut` est une valeur scalaire unitaire (`'CONFIRMEE'`, `'EN_ATTENTE'`, `'ANNULEE'`).
  - `id_adherent` et `id_cours` sont des clés étrangères scalaires.
- **Conclusion `INSCRIPTION` :** **Conforme 1NF**.

#### Table `ABONNEMENT`
- **Schéma relationnel :** `ABONNEMENT (id_abonnement, type, date_debut, date_fin, statut, id_adherent)`
- **Clé primaire :** `id_abonnement` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `type`, `date_debut`, `date_fin` et `statut` sont tous scalaires et indivisibles.
  - `id_adherent` est un entier scalaire identifiant l'adhérent souscripteur.
- **Conclusion `ABONNEMENT` :** **Conforme 1NF**.

#### Table `PAIEMENT`
- **Schéma relationnel :** `PAIEMENT (id_paiement, montant, date_paiement, mode_paiement, statut, id_abonnement)`
- **Clé primaire :** `id_paiement` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `montant` est un scalaire monétaire exact (`DECIMAL(10,2)`).
  - `date_paiement` est une date scalaire pure (`DATE`).
  - `mode_paiement` et `statut` sont des codes d'état normalisés simples (`VARCHAR(50)`).
  - `id_abonnement` est une clé étrangère scalaire référençant le contrat.
- **Conclusion `PAIEMENT` :** **Conforme 1NF**.

#### Table `PRESENCE`
- **Schéma relationnel :** `PRESENCE (id_presence, date_presence, statut_presence, id_inscription)`
- **Clé primaire :** `id_presence` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `date_presence` est une date scalaire atomique.
  - `statut_presence` est une valeur scalaire unitaire (`'PRESENT'`, `'ABSENT'`).
  - `id_inscription` est une clé étrangère scalaire unique (`uk_presence_inscription`).
- **Conclusion `PRESENCE` :** **Conforme 1NF**.

---

## 2. Deuxième forme normale — 2NF

### 2.1 Définition formelle de la 2NF
Une relation $R$ est en **Deuxième Forme Normale (2NF)** si et seulement si :
1. Elle est en **1NF**.
2. Tout attribut non-clé dépend **pleinement et entièrement** de chaque clé candidate de $R$ (absence de dépendance fonctionnelle partielle).

> **Propriété fondamentale :**  
> Si la clé primaire d'une relation est **simple** (constituée d'un attribut unique), la relation est **automatiquement en 2NF**, car un attribut unique ne possède aucun sous-ensemble strict non-vide pouvant déterminer un attribut non-clé.

---

### 2.2 Analyse des tables à clé primaire simple

- **`COACH` :** Clé `{id_coach}` simple $\rightarrow$ **Conforme 2NF**.
- **`ADHERENT` :** Clé `{id_adherent}` simple (et clé candidate alternative `{email}` simple) $\rightarrow$ **Conforme 2NF**.
- **`COURS` :** Clé `{id_cours}` simple $\rightarrow$ **Conforme 2NF**.
- **`ABONNEMENT` :** Clé `{id_abonnement}` simple $\rightarrow$ **Conforme 2NF**.
- **`PAIEMENT` :** Clé `{id_paiement}` simple $\rightarrow$ **Conforme 2NF**.
- **`PRESENCE` :** Clé `{id_presence}` simple $\rightarrow$ **Conforme 2NF**.

---

### 2.3 Analyse spécifique de la table `INSCRIPTION`

La table `INSCRIPTION` possède :
1. **Clé primaire physique (Surrogate Key) :** `id_inscription` (simple).
2. **Clé candidate naturelle métier :** le couple composé `(id_adherent, id_cours)`, garanti unique par `uk_inscription_adherent_cours`.

Attributs non-clés : `date_inscription`, `statut`.

#### Dépendances fonctionnelles :
- Pour `date_inscription` : $\{\text{id\_adherent}, \text{id\_cours}\} \rightarrow \text{date\_inscription}$. La dépendance est totale et élémentaire.
- Pour `statut` : $\{\text{id\_adherent}, \text{id\_cours}\} \rightarrow \text{statut}$. Le statut s'applique à l'inscription précise de cet adhérent à ce cours.
- **Conclusion `INSCRIPTION` :** **Conforme 2NF**.

---

## 3. Troisième forme normale — 3NF

### 3.1 Définition formelle de la 3NF
Une relation $R$ est en **Troisième Forme Normale (3NF)** si et seulement si :
1. Elle est en **2NF**.
2. Aucun attribut non-clé ne dépend d'un autre attribut non-clé (absence de dépendance fonctionnelle transitive).

---

### 3.2 Audit des dépendances transitives

- **`COACH` :** $\text{id\_coach} \rightarrow \text{nom, prenom, specialite}$. Aucune relation entre non-clés $\rightarrow$ **Conforme 3NF**.
- **`ADHERENT` :** $\text{id\_adherent} \rightarrow \text{nom, prenom, telephone}$. Aucune relation entre non-clés $\rightarrow$ **Conforme 3NF**.
- **`COURS` :** $\text{id\_cours} \rightarrow \text{nom, date, heure, capacite\_max, id\_coach}$. Les attributs du coach ne figurent pas dans COURS $\rightarrow$ **Conforme 3NF**.
- **`INSCRIPTION` :** $\text{id\_inscription} \rightarrow \text{date\_inscription, statut, id\_adherent, id\_cours}$. Ni les détails de l'adhérent ni ceux du cours ne sont dupliqués $\rightarrow$ **Conforme 3NF**.
- **`ABONNEMENT` :** $\text{id\_abonnement} \rightarrow \text{type, date\_debut, date\_fin, statut, id\_adherent}$. Aucun catalogue de prix dénormalisé $\rightarrow$ **Conforme 3NF**.
- **`PAIEMENT` :** $\text{id\_paiement} \rightarrow \text{montant, date\_paiement, mode\_paiement, statut, id\_abonnement}$. La table référence uniquement le contrat d'abonnement ; aucune information redondante de l'adhérent n'y est dupliquée $\rightarrow$ **Conforme 3NF**.
- **`PRESENCE` :** $\text{id\_presence} \rightarrow \text{date\_presence, statut\_presence, id\_inscription}$. La présence référence exclusivement l'inscription ; aucun attribut de l'adhérent ou du cours n'est copié $\rightarrow$ **Conforme 3NF**.

---

## 4. Redondances évitées dans la conception

Le modèle élimine méthodiquement toute redondance négative :
1. **Informations financières isolées :** `PAIEMENT` contient les transactions financières réelles sans polluer le contrat `ABONNEMENT`.
2. **Émargements séparés des réservations :** `PRESENCE` matérialise le pointage réel du jour J sans altérer l'état initial d'`INSCRIPTION`.
3. **Absence de duplication de clés :** Ni `PAIEMENT` ni `PRESENCE` ne dupliquent `id_adherent` ou `id_cours`. Les jointures naturelles par clés étrangères permettent de reconstituer l'information de façon rigoureusement normalisée.
4. **Calculs d'agrégation non stockés :** Les revenus mensuels, le nombre d'inscrits, les places restantes et le nombre de présences sont tous calculés par requêtes SQL (`SUM`, `COUNT`) et non stockés sous forme de colonnes dénormalisées.

---

## 5. Anomalies éliminées

- **Anomalie d'insertion :** Un adhérent peut exister sans abonnement ni inscription ; un cours peut exister sans inscrits ; un abonnement peut exister avant d'être soldé par un paiement.
- **Anomalie de mise à jour :** Les informations d'un adhérent ou d'un tarif d'abonnement sont modifiées en un point unique.
- **Anomalie de suppression :** Les règles `ON DELETE RESTRICT` protègent les données historiques et empêchent la création d'enregistrements orphelins.

---

## 6. Vérifications SQL réelles (MariaDB / MySQL sous XAMPP)

Le schéma a été déployé et validé sur MariaDB 10.4 (base `salle_sport`) :
- **Tables créées (7) :** `COACH`, `ADHERENT`, `COURS`, `INSCRIPTION`, `ABONNEMENT`, `PAIEMENT`, `PRESENCE`.
- **Clés primaires :** Toutes typées en `INT AUTO_INCREMENT` clustered B-Tree.
- **Clés étrangères :** 6 contraintes `FOREIGN KEY` actives avec `ON DELETE RESTRICT ON UPDATE CASCADE`.
- **Index B-Tree métier créés :**
  - `idx_adherent_nom_prenom` sur `ADHERENT (nom, prenom)`
  - `idx_paiement_statut_date` sur `PAIEMENT (statut, date_paiement)`
  - `idx_abonnement_actif_lookup` sur `ABONNEMENT (id_adherent, statut, date_debut, date_fin)`
  - `idx_presence_statut` sur `PRESENCE (statut_presence)`

---

## 7. Analyse de la table volontairement mal conçue

> **Statut : En attente de la table fournie par l'enseignant**

Conformément aux directives du cahier des charges, aucune fausse table mal conçue n'a été inventée ou injectée artificiellement dans le projet. Dès que la table d'exercice sera communiquée par l'enseignant, la méthodologie suivante sera appliquée :
1. Identification de la relation non normalisée (0NF / violation 1NF).
2. Détection des dépendances fonctionnelles partielles (violation 2NF).
3. Détection des dépendances transitives (violation 3NF).
4. Décomposition sans perte d'information ni de dépendance fonctionnelle vers des tables en 3NF / BCNF.

---

## 8. Conclusion

L'audit démontre que le modèle relationnel de la salle de sport à 7 tables respecte intégralement les principes de normalisation en **3NF**. Il garantit une cohérence transactionnelle irréprochable et supporte avec performance toutes les requêtes métier d'exploitation.
