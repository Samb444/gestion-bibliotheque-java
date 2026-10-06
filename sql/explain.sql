-- =====================================================================
-- PROJET       : Gestion d'une Salle de Sport
-- FICHIER      : explain.sql
-- DESCRIPTION  : Analyse des plans d'exécution avec EXPLAIN (Phase 5)
-- SGBD         : MySQL / MariaDB (Moteur InnoDB)
-- DATE         : 2026-10-06
-- =====================================================================

USE salle_sport;

-- =====================================================================
-- RAPPEL PÉDAGOGIQUE SUR LES PLANS D'EXÉCUTION (EXPLAIN) :
-- - id          : Identifiant de l'étape de sélection.
-- - select_type : Nature de la requête (SIMPLE, PRIMARY, SUBQUERY...).
-- - table       : Table concernée par l'étape.
-- - type        : Méthode d'accès (system > const > eq_ref > ref > range > index > ALL).
-- - possible_keys : Index candidats que l'optimiseur pourrait employer.
-- - key         : Index effectivement choisi par l'optimiseur.
-- - rows        : Estimation du nombre de lignes examinées.
-- - Extra       : Informations complémentaires (Using index, Using where, Using filesort...).
--
-- NOTE SUR LE VOLUME DE DONNÉES :
-- Dans une petite base de données (ex: 5 à 18 lignes), l'optimiseur à base de coût
-- (Cost-Based Optimizer) préfère souvent un parcours séquentiel (type = ALL)
-- plutôt que d'effectuer des allers-retours aléatoires entre l'index secondaire
-- et la table de données (bookmark lookup). Cela est tout à fait normal.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. REQUÊTE REPRÉSENTATIVE 1 : RECHERCHE & TRI DES ADHÉRENTS
-- ---------------------------------------------------------------------
-- 1.a) Requête d'origine (queries.sql - Requête 1) avec toutes les colonnes :
-- Observation : Sur 12 lignes, l'optimiseur fait un parcours ALL + filesort,
-- car lire l'index puis faire des lookups pour email et telephone coûte plus cher.
EXPLAIN 
SELECT 
    id_adherent,
    nom,
    prenom,
    email,
    telephone
FROM ADHERENT
ORDER BY nom ASC, prenom ASC;

-- 1.b) Requête couvrante (Covering Index) sur nom et prénom :
-- Observation : type = index, key = idx_adherent_nom_prenom, Extra = Using index.
-- Le tri en mémoire (filesort) est totalement éliminé grâce à l'arbre B-Tree !
EXPLAIN 
SELECT 
    nom,
    prenom
FROM ADHERENT
ORDER BY nom ASC, prenom ASC;

-- 1.c) Recherche par filtre nominal (WHERE nom = ...) :
-- Observation : type = ref, key = idx_adherent_nom_prenom, ref = const.
-- Accès direct par recherche dichotomique dans l'index.
EXPLAIN 
SELECT 
    id_adherent,
    nom,
    prenom,
    email
FROM ADHERENT
WHERE nom = 'Diop';

-- ---------------------------------------------------------------------
-- 2. REQUÊTE REPRÉSENTATIVE 2 : JOINTURE COURS -> COACH
-- ---------------------------------------------------------------------
-- 2.a) Jointure globale (queries.sql - Requête 2) :
-- Observation : Table coach parcourue (5 lignes), puis table cours accédée
-- via l'index `fk_cours_coach` (type = ref, ref = salle_sport.co.id_coach).
EXPLAIN 
SELECT 
    c.nom AS nom_cours,
    c.date AS date_cours,
    c.heure AS heure_cours,
    co.nom AS nom_coach,
    co.prenom AS prenom_coach
FROM COURS c
JOIN COACH co ON c.id_coach = co.id_coach
ORDER BY c.date ASC, c.heure ASC;

-- 2.b) Cours d'un coach identifié (queries.sql - Requête 7) :
-- Observation : Table coach accédée par const (PRIMARY), et table cours
-- accédée par ref sur `fk_cours_coach` avec ref = const (1 ligne estimée).
EXPLAIN 
SELECT 
    c.id_cours,
    c.nom AS nom_cours,
    c.date AS date_cours,
    c.heure AS heure_cours,
    c.capacite_max,
    co.nom AS nom_coach,
    co.prenom AS prenom_coach
FROM COURS c
JOIN COACH co ON c.id_coach = co.id_coach
WHERE co.id_coach = 1
ORDER BY c.date ASC, c.heure ASC;

-- ---------------------------------------------------------------------
-- 3. REQUÊTE REPRÉSENTATIVE 3 : JOINTURE INSCRIPTION -> ADHERENT -> COURS
-- ---------------------------------------------------------------------
-- Observation :
-- - Table c (COURS) parcourue (10 lignes).
-- - Table i (INSCRIPTION) accédée via l'index `fk_inscription_cours` (type = ref).
-- - Table a (ADHERENT) accédée via sa clé primaire `PRIMARY` (type = eq_ref, coût minimal).
-- Aucune redondance n'est nécessaire car les index FK et PK suffisent amplement.
EXPLAIN 
SELECT 
    a.nom AS nom_adherent,
    a.prenom AS prenom_adherent,
    c.nom AS nom_cours,
    c.date AS date_cours,
    i.statut AS statut_inscription
FROM INSCRIPTION i
JOIN ADHERENT a ON i.id_adherent = a.id_adherent
JOIN COURS c ON i.id_cours = c.id_cours
ORDER BY c.date ASC, a.nom ASC;

-- ---------------------------------------------------------------------
-- 4. REQUÊTE REPRÉSENTATIVE 4 : RECHERCHE DES ABONNEMENTS D'UN ADHÉRENT
-- ---------------------------------------------------------------------
-- 4.a) Abonnements actifs (queries.sql - Requête 6) :
-- Observation : Table ab (ABONNEMENT) filtrée sur statut = 'ACTIF',
-- puis jointure vers a (ADHERENT) via PRIMARY (type = eq_ref).
EXPLAIN 
SELECT DISTINCT
    a.id_adherent,
    a.nom,
    a.prenom,
    a.email,
    ab.type AS type_abonnement,
    ab.date_debut,
    ab.date_fin,
    ab.statut AS statut_abonnement
FROM ADHERENT a
JOIN ABONNEMENT ab ON a.id_adherent = ab.id_adherent
WHERE ab.statut = 'ACTIF'
ORDER BY a.nom ASC, a.prenom ASC;

-- 4.b) Recherche directe des abonnements pour un adhérent spécifique :
-- Observation : Accès à la table ABONNEMENT via l'index `fk_abonnement_adherent`
-- (type = ref, ref = const, rows = 1).
-- Preuve que la colonne id_adherent dans ABONNEMENT est déjà parfaitement indexée.
EXPLAIN 
SELECT 
    ab.id_abonnement,
    ab.type,
    ab.date_debut,
    ab.date_fin,
    ab.statut
FROM ABONNEMENT ab
WHERE ab.id_adherent = 1;
