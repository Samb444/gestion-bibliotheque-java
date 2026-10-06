# Analyse de normalisation — Salle de sport

**Projet :** Gestion d'une Salle de Sport (Exercice 3)  
**Phase :** Phase 6 — Normalisation et Contrôle du Modèle  
**SGBD cible :** MariaDB / MySQL (Moteur InnoDB)  
**Date :** 2026-10-06  

---

## Introduction et Démarche

L'objectif de cette étude est d'auditer formellement la conception de la base de données relationnelle `salle_sport` à la lumière de la théorie relationnelle énoncée par Edgar F. Codd.  
L'analyse porte sur les cinq relations du système :
- `COACH`
- `ADHERENT`
- `COURS`
- `INSCRIPTION`
- `ABONNEMENT`

Cette vérification s'appuie sur l'analyse croisée du Modèle Conceptuel de Données (MCD), du Modèle Logique de Données (MLD), du script DDL (`sql/salle-sport.sql`) et de l'état réel des tables dans le SGBD MariaDB.

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
  - `nom` et `prenom` sont découplés en deux champs scalaires distincts (absence de champ composite `nom_complet`).
  - `specialite` stocke une chaîne scalaire unique par coach (ex. `'Musculation'`, `'Fitness'`).
  - Aucun attribut ne stocke de données concaténées ni de liste.
- **Conclusion `COACH` :** **Conforme 1NF**.

#### Table `ADHERENT`
- **Schéma relationnel :** `ADHERENT (id_adherent, nom, prenom, email, telephone)`
- **Clé primaire :** `id_adherent` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - Séparation stricte du `nom` et du `prenom`.
  - `email` est une valeur scalaire unique (garantie par la contrainte `uk_adherent_email`).
  - `telephone` est stocké sous forme d'une chaîne scalaire unique au format international E.164 (pas de liste de numéros ni de colonnes multiples `telephone_fixe`/`telephone_portable`).
- **Conclusion `ADHERENT` :** **Conforme 1NF**.

#### Table `COURS`
- **Schéma relationnel :** `COURS (id_cours, nom, date, heure, capacite_max, id_coach)`
- **Clé primaire :** `id_cours` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - La dimension temporelle est décomposée de façon atomique en `date` (`DATE`) et `heure` (`TIME`), évitant toute ambiguïté sur les requêtes calendaires ou horaires.
  - `capacite_max` est un entier scalaire simple.
  - `id_coach` est une référence entière scalaire vers un seul coach encadrant.
  - La table ne contient aucune liste de participants ou d'inscriptions.
- **Conclusion `COURS` :** **Conforme 1NF**.

#### Table `INSCRIPTION`
- **Schéma relationnel :** `INSCRIPTION (id_inscription, date_inscription, statut, id_adherent, id_cours)`
- **Clé primaire :** `id_inscription` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `date_inscription` est une valeur temporelle atomique scalaire.
  - `statut` est une valeur scalaire unitaire représentant l'état du dossier (`'CONFIRMEE'`, `'EN_ATTENTE'`, `'ANNULEE'`).
  - `id_adherent` et `id_cours` sont des clés étrangères scalaires liant exactement un adhérent à un cours par tuple.
- **Conclusion `INSCRIPTION` :** **Conforme 1NF**.

#### Table `ABONNEMENT`
- **Schéma relationnel :** `ABONNEMENT (id_abonnement, type, date_debut, date_fin, statut, id_adherent)`
- **Clé primaire :** `id_abonnement` (INT, `PRIMARY KEY`, auto-incrémentée).
- **Atomicité des attributs :**
  - `type` (`VARCHAR(100)`), `date_debut` (`DATE`), `date_fin` (`DATE`) et `statut` (`VARCHAR(100)`) sont tous scalaires et indivisibles.
  - `id_adherent` est un entier scalaire identifiant l'adhérent souscripteur.
- **Conclusion `ABONNEMENT` :** **Conforme 1NF**.

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

- **`COACH` :**  
  Clé primaire : `{id_coach}` (simple).  
  Attributs non-clés : `nom`, `prenom`, `specialite`.  
  $\text{id\_coach} \rightarrow \text{nom, prenom, specialite}$.  
  $\rightarrow$ **Conforme 2NF**.

- **`ADHERENT` :**  
  Clé primaire : `{id_adherent}` (simple). Clé candidate alternative : `{email}` (simple).  
  Attributs non-clés : `nom`, `prenom`, `telephone`.  
  Les deux clés candidates étant mono-attribut, aucune dépendance partielle n'est mathématiquement possible.  
  $\rightarrow$ **Conforme 2NF**.

- **`COURS` :**  
  Clé primaire : `{id_cours}` (simple).  
  Attributs non-clés : `nom`, `date`, `heure`, `capacite_max`, `id_coach`.  
  Tous dépendent de l'identifiant du cours dans son intégralité.  
  $\rightarrow$ **Conforme 2NF**.

- **`ABONNEMENT` :**  
  Clé primaire : `{id_abonnement}` (simple).  
  Attributs non-clés : `type`, `date_debut`, `date_fin`, `statut`, `id_adherent`.  
  Tous dépendent de l'identifiant de l'abonnement.  
  $\rightarrow$ **Conforme 2NF**.

---

### 2.3 Analyse spécifique de la table `INSCRIPTION`

La table `INSCRIPTION` possède une structure remarquable :
1. **Clé primaire physique (Surrogate Key) :** `id_inscription` (clé simple).
2. **Clé candidate naturelle métier :** le couple composé `(id_adherent, id_cours)`, garanti unique par la contrainte SQL `CONSTRAINT uk_inscription_adherent_cours UNIQUE (id_adherent, id_cours)`.

Attributs non-clés : `date_inscription`, `statut`.

#### Étude des dépendances fonctionnelles vis-à-vis de la clé candidate composée :
- **Pour `date_inscription` :**
  - Est-ce que $\text{id\_adherent} \rightarrow \text{date\_inscription}$ ? **Non.** Un adhérent s'inscrit à des cours différents à des dates différentes.
  - Est-ce que $\text{id\_cours} \rightarrow \text{date\_inscription}$ ? **Non.** Les inscriptions à un cours donné s'échelonnent sur plusieurs dates distinctes.
  - Dépendance réelle : $\{\text{id\_adherent}, \text{id\_cours}\} \rightarrow \text{date\_inscription}$. La dépendance est **totale et élémentaire**.

- **Pour `statut` :**
  - Est-ce que $\text{id\_adherent} \rightarrow \text{statut}$ ? **Non.** Le statut de l'inscription est spécifique à un cours donné (un adhérent peut être `'CONFIRMEE'` pour un cours et `'EN_ATTENTE'` pour un autre).
  - Est-ce que $\text{id\_cours} \rightarrow \text{statut}$ ? **Non.** Un cours accueille des participants dont certains sont confirmés et d'autres en attente.
  - Dépendance réelle : $\{\text{id\_adherent}, \text{id\_cours}\} \rightarrow \text{statut}$. La dépendance est **totale et élémentaire**.

#### Conclusion sur `id_inscription` :
L'utilisation d'un identifiant artificiel auto-incrémenté `id_inscription` comme clé primaire technique n'altère en rien la normalisation. Même en évaluant la relation sous le prisme de sa clé candidate composite naturelle `{id_adherent, id_cours}`, **aucun attribut non-clé ne dépend d'une fraction de cette clé**.  
La table `INSCRIPTION` est donc **rigoureusement conforme à la 2NF**.

---

## 3. Troisième forme normale — 3NF

### 3.1 Définition formelle de la 3NF
Une relation $R$ est en **Troisième Forme Normale (3NF)** si et seulement si :
1. Elle est en **2NF**.
2. Il n'existe **aucune dépendance fonctionnelle transitive** entre attributs non-clés.  
   Formellement : pour toute dépendance fonctionnelle non triviale $X \rightarrow Y$, soit $X$ est une superclé de $R$, soit $Y$ est un attribut premier (appartenant à une clé candidate).

---

### 3.2 Audit des dépendances par table

#### Table `ADHERENT`
- Dépendances fonctionnelles :
  $$\text{id\_adherent} \rightarrow \{\text{nom, prenom, email, telephone}\}$$
  $$\text{email} \rightarrow \{\text{id\_adherent, nom, prenom, telephone}\}$$
- Recherche de transitivité non-clé $\rightarrow$ non-clé :
  - Le `nom` détermine-t-il le `prenom` ou le `telephone` ? **Non.**
  - Le `telephone` détermine-t-il le `nom` ? **Non.**
  - Aucune colonne non-clé ne détermine une autre colonne non-clé.
- **Conclusion `ADHERENT` :** **Conforme 3NF**.

#### Table `COACH`
- Dépendance fonctionnelle :
  $$\text{id\_coach} \rightarrow \{\text{nom, prenom, specialite}\}$$
- Recherche de transitivité :
  - La `specialite` détermine-t-elle un coach ? **Non.** Plusieurs coachs partagent la même spécialité (ex. Yoga, Musculation).
  - Il n'existe pas d'attribut dérivé de la spécialité (ex. description de spécialité, matériel requis).
- **Conclusion `COACH` :** **Conforme 3NF**.

#### Table `COURS`
- Dépendance fonctionnelle :
  $$\text{id\_cours} \rightarrow \{\text{nom, date, heure, capacite_max, id_coach}\}$$
- Recherche de transitivité :
  - Le `nom` du cours détermine-t-il l'horaire ou la capacité ? **Non.** Deux sessions de "Yoga" peuvent avoir des créneaux et des capacités distincts.
  - Y a-t-il des informations propres au coach répliquées dans `COURS` (ex. `nom_coach`, `specialite_coach`) ? **Non.** Seul l'identifiant technique `id_coach` est présent. Pour connaître le nom du coach, une jointure relationnelle sur `COACH` est nécessaire. Il n'y a donc pas de transitivité $\text{id\_cours} \rightarrow \text{id\_coach} \rightarrow \text{nom\_coach}$ au sein de la table `COURS`.
- **Conclusion `COURS` :** **Conforme 3NF**.

#### Table `INSCRIPTION`
- Dépendance fonctionnelle :
  $$\text{id\_inscription} \rightarrow \{\text{date_inscription, statut, id_adherent, id_cours}\}$$
- Recherche de transitivité :
  - La `date_inscription` détermine-t-elle le `statut` ? **Non.**
  - Y a-t-il des informations relatives à l'adhérent (`nom`, `email`) ou au cours (`nom_cours`, `date_cours`) dupliquées dans `INSCRIPTION` ? **Non.** Seules les clés étrangères `id_adherent` et `id_cours` y figurent.
- **Conclusion `INSCRIPTION` :** **Conforme 3NF**.

#### Table `ABONNEMENT`
- Dépendance fonctionnelle :
  $$\text{id\_abonnement} \rightarrow \{\text{type, date_debut, date_fin, statut, id_adherent}\}$$
- Recherche de transitivité :
  - Le `type` ('Mensuel', 'Annuel', 'Trimestriel') détermine-t-il la `date_fin` ? **Non.** La `date_fin` dépend de la `date_debut` spécifique à chaque souscription individuelle.
  - Y a-t-il un catalogue de prix ou de conditions dénormalisé dans la table ? **Non.**
  - Le `statut` dépend de la période de validité et de l'historique du contrat particulier, pas uniquement du type.
  - Aucune information descriptive de l'adhérent n'y est répétée.
- **Conclusion `ABONNEMENT` :** **Conforme 3NF**.

---

## 4. Redondances

### 4.1 Redondances évitées dans la conception
Le modèle exclut formellement les redondances de données :
1. **Informations du coach dans `COURS` :** Aucun doublon (`nom`, `prenom`, `specialite` sont exclus de `COURS`). Seule la clé étrangère `id_coach` relie les entités.
2. **Informations de l'adhérent dans `INSCRIPTION` :** Aucun doublon (`nom`, `prenom`, `email`, `telephone` sont absents d'`INSCRIPTION`).
3. **Informations du cours dans `INSCRIPTION` :** Aucun doublon (`nom`, `date`, `heure`, `capacite_max` restent confinés dans `COURS`).
4. **Informations de l'adhérent dans `ABONNEMENT` :** Aucun doublon d'état civil ou de contact.
5. **Absence d'attributs calculés stockés :**
   - Le nombre de places occupées à un cours n'est **pas** stocké sous forme de colonne `nb_inscrits` dans `COURS`. Il est obtenu dynamiquement via `COUNT(INSCRIPTION.id_inscription)`.
   - Le nombre de places disponibles n'est **pas** stocké ; il résulte de la formule dynamique `COURS.capacite_max - COUNT(...)`.
   - La durée restante d'un abonnement n'est pas stockée ; elle est calculée à la volée par différence de dates (`DATEDIFF(date_fin, CURRENT_DATE)`).

### 4.2 Rôle légitime des clés étrangères
Les clés étrangères (`COURS.id_coach`, `INSCRIPTION.id_adherent`, `INSCRIPTION.id_cours`, `ABONNEMENT.id_adherent`) ne constituent pas des redondances négatives. En théorie relationnelle, elles représentent les **vecteurs d'association indispensables** (pointeurs logiques non ambigus) assurant l'intégrité référentielle sans dupliquer la moindre information sémantique.

---

## 5. Anomalies

L'analyse démontre que l'architecture normalisée en 3NF immunise la base de données contre les trois classes d'anomalies relationnelles classiques :

### 5.1 Anomalies d'insertion
- **Création d'un coach sans cours :** **Possible et immédiate.** Un nouveau coach peut être inséré dans `COACH` sans qu'aucun cours ne lui soit encore attribué (cardinalité `0,N`).
- **Création d'un adhérent sans inscription :** **Possible et immédiate.** Un adhérent peut être créé dans `ADHERENT` sans participer à aucun cours.
- **Création d'un cours sans inscription :** **Possible et immédiate.** Un cours peut être planifié dans `COURS` avec une capacité d'accueil sans compter le moindre inscrit initial.

### 5.2 Anomalies de mise à jour
- **Modification des informations d'un coach :** Si un coach change de spécialité ou corrige l'orthographe de son nom, la mise à jour s'effectue sur **un seul enregistrement** dans la table `COACH`. Aucune ligne de la table `COURS` n'a besoin d'être modifiée.
- **Modification des coordonnées d'un adhérent :** Si un adhérent change de numéro de téléphone ou d'adresse email, l'opération touche une seule ligne dans `ADHERENT`. Toutes les inscriptions et abonnements associés restent intacts et cohérents.

### 5.3 Anomalies de suppression
- **Suppression d'une inscription :** Supprimer une ligne dans `INSCRIPTION` annule la participation sans détruire l'adhérent concerné dans `ADHERENT` ni le cours concerné dans `COURS`.
- **Protection par contraintes d'intégrité référentielle :** Grâce aux clauses `ON DELETE RESTRICT` définies sur les clés étrangères, le SGBD bloque la suppression accidentelle d'un adhérent ou d'un cours tant qu'il existe des inscriptions associées, garantissant une cohérence absolue des données.

---

## 6. Vérifications SQL réelles (MariaDB / MySQL)

Un audit direct sur l'instance MariaDB active dans la base `salle_sport` a permis de valider la conformité physique du schéma :

### 6.1 Structures de tables (`SHOW CREATE TABLE`)
- **`COACH` :** Moteur InnoDB, clé primaire `id_coach` INT auto-incrémentée.
- **`ADHERENT` :** Moteur InnoDB, clé primaire `id_adherent`, contrainte d'unicité `uk_adherent_email`.
- **`COURS` :** Moteur InnoDB, clé primaire `id_cours`, clé étrangère `fk_cours_coach` vers `COACH(id_coach)` avec `ON UPDATE CASCADE`, contrainte de validation `CHECK (capacite_max > 0)`.
- **`INSCRIPTION` :** Moteur InnoDB, clé primaire `id_inscription`, contrainte d'unicité composite `uk_inscription_adherent_cours (id_adherent, id_cours)`, deux clés étrangères vers `ADHERENT` et `COURS`.
- **`ABONNEMENT` :** Moteur InnoDB, clé primaire `id_abonnement`, contrainte de validation `CHECK (date_fin >= date_debut)`, clé étrangère vers `ADHERENT`.

### 6.2 Indexation active (`SHOW INDEX`)
- Clés primaires sous forme d'index clustered B-Tree sur les 5 tables.
- Index B-Tree uniques : `email` sur `ADHERENT`, `(id_adherent, id_cours)` sur `INSCRIPTION`.
- Index B-Tree non-uniques : `(nom, prenom)` sur `ADHERENT`, `id_coach` sur `COURS`, `id_cours` sur `INSCRIPTION`, `id_adherent` sur `ABONNEMENT`.

---

## 7. Conclusion

L'audit complet des modèles conceptuel, logique et physique permet d'affirmer avec certitude que :
- Le schéma relationnel de la salle de sport satisfait rigoureusement les exigences de la **Première Forme Normale (1NF)**.
- Le schéma relationnel satisfait rigoureusement les exigences de la **Deuxième Forme Normale (2NF)**.
- Le schéma relationnel satisfait rigoureusement les exigences de la **Troisième Forme Normale (3NF)**.

Aucune dénormalisation sauvage, aucune dépendance partielle, aucune dépendance transitive et aucune redondance injustifiée n'ont été détectées. Le modèle est optimal, robuste et prêt pour la production.
