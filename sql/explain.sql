-- =====================================================================
-- PROJET       : Gestion d'une Salle de Sport
-- FICHIER      : explain.sql
-- DESCRIPTION  : Analyse des plans d'exécution avec EXPLAIN
-- SGBD         : MySQL / MariaDB (Moteur InnoDB)
-- DATE         : 2026-10-06
-- =====================================================================

USE salle_sport;

-- =====================================================================
-- RAPPEL PÉDAGOGIQUE SUR LES PLANS D'EXÉCUTION (EXPLAIN) :
-- - id          : Identifiant séquentiel de l'étape de sélection.
-- - select_type : Type de requête (SIMPLE, PRIMARY, SUBQUERY, DEPENDENT SUBQUERY...).
-- - table       : Table concernée par l'étape.
-- - type        : Méthode d'accès (system > const > eq_ref > ref > range > index > ALL).
-- - possible_keys : Index candidats analysés par l'optimiseur.
-- - key         : Index effectivement retenu par le Cost-Based Optimizer.
-- - rows        : Estimation du nombre de lignes examinées par l'optimiseur.
-- - Extra       : Détails d'exécution (Using index, Using where, Using filesort...).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. ANALYSE REQUÊTE MÉTIER 6 : REVENUS DU MOIS (TABLE PAIEMENT)
-- ---------------------------------------------------------------------
-- Requête avec plage de dates directe et statut = 'PAYE' :
-- Observation : L'optimiseur identifie `idx_paiement_statut_date` comme clé possible.
-- Grâce à cet index composite (statut, date_paiement), la recherche s'effectue en `range`
-- ciblant exactement la tranche temporelle du mois sans lire les règlements hors période.
EXPLAIN 
SELECT 
    '2026-10' AS mois_analyse,
    COUNT(p.id_paiement) AS nombre_paiements_encaisses,
    COALESCE(SUM(p.montant), 0.00) AS revenus_totaux_fcfa
FROM PAIEMENT p
WHERE p.statut = 'PAYE'
  AND p.date_paiement >= '2026-10-01'
  AND p.date_paiement < '2026-11-01';

-- ---------------------------------------------------------------------
-- 2. ANALYSE REQUÊTE MÉTIER 3 : ADHÉRENTS SANS ABONNEMENT ACTIF (NOT EXISTS)
-- ---------------------------------------------------------------------
-- Observation :
-- 1. Table principale `a` (ADHERENT) : balayage ordonné des adhérents.
-- 2. Sous-requête corrélée (DEPENDENT SUBQUERY) sur `ab` (ABONNEMENT) :
--    - Clé retenue : `idx_abonnement_actif_lookup` (ou fk_abonnement_adherent).
--    - Méthode d'accès : `ref` sur `id_adherent`.
--    - Extra : `Using index` (Index Couvrant) : toutes les colonnes testées
--      (id_adherent, statut, date_debut, date_fin) sont résolues dans l'index B-Tree !
EXPLAIN 
SELECT 
    a.id_adherent,
    a.nom,
    a.prenom,
    a.email,
    a.telephone
FROM ADHERENT a
WHERE NOT EXISTS (
    SELECT 1
    FROM ABONNEMENT ab
    WHERE ab.id_adherent = a.id_adherent
      AND ab.statut = 'ACTIF'
      AND '2026-10-06' BETWEEN ab.date_debut AND ab.date_fin
)
ORDER BY a.nom ASC, a.prenom ASC;

-- ---------------------------------------------------------------------
-- 3. ANALYSE REQUÊTE MÉTIER 4 : TOP 3 DES ASSIDUS (PRESENCE -> INSCRIPTION -> ADHERENT)
-- ---------------------------------------------------------------------
-- Observation :
-- - Table `p` (PRESENCE) : filtrée sur statut_presence = 'PRESENT' via `idx_presence_statut` ou balayage rapide.
-- - Table `i` (INSCRIPTION) : accédée par clé primaire `PRIMARY` sur `id_inscription` (type = eq_ref, coût optimal O(1)).
-- - Table `a` (ADHERENT) : accédée par clé primaire `PRIMARY` sur `id_adherent` (type = eq_ref, coût optimal O(1)).
EXPLAIN 
SELECT 
    a.id_adherent,
    a.nom,
    a.prenom,
    a.email,
    COUNT(p.id_presence) AS nombre_presences
FROM PRESENCE p
JOIN INSCRIPTION i ON p.id_inscription = i.id_inscription
JOIN ADHERENT a ON i.id_adherent = a.id_adherent
WHERE p.statut_presence = 'PRESENT'
GROUP BY a.id_adherent, a.nom, a.prenom, a.email
ORDER BY nombre_presences DESC, a.nom ASC
LIMIT 3;

-- ---------------------------------------------------------------------
-- 4. ANALYSE REQUÊTE 1 & COUVRANCE : TRI ET RECHERCHE NOMINALE (ADHERENT)
-- ---------------------------------------------------------------------
-- 4.a) Requête couvrante (Covering Index) sur nom et prénom :
-- Observation : type = index, key = idx_adherent_nom_prenom, Extra = Using index.
-- Le tri en mémoire (filesort) est totalement éliminé grâce à l'arbre B-Tree !
EXPLAIN 
SELECT 
    nom,
    prenom
FROM ADHERENT
ORDER BY nom ASC, prenom ASC;

-- 4.b) Recherche par filtre nominal (WHERE nom = ...) :
-- Observation : type = ref, key = idx_adherent_nom_prenom, ref = const.
EXPLAIN 
SELECT 
    id_adherent,
    nom,
    prenom,
    email
FROM ADHERENT
WHERE nom = 'Ba';

-- ---------------------------------------------------------------------
-- 5. ANALYSE JOINTURE COURS -> COACH
-- ---------------------------------------------------------------------
-- Observation : Jointure sur clé étrangère id_coach résolue par `fk_cours_coach`.
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
