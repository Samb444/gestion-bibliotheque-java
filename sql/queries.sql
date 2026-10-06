USE salle_sport;

-- =====================================================================
-- EXERCICE 3 : GESTION DE SALLE DE SPORT
-- FICHIER    : queries.sql
-- DESCRIPTION: Requêtes SQL d'exploitation et d'analyse des données
-- =====================================================================

-- ---------------------------------------------------------------------
-- Requête 1 — Tous les adhérents
-- Objectif  : Afficher tous les adhérents triés par nom de famille.
-- ---------------------------------------------------------------------
SELECT 
    id_adherent,
    nom,
    prenom,
    email,
    telephone
FROM ADHERENT
ORDER BY nom ASC, prenom ASC;

-- ---------------------------------------------------------------------
-- Requête 2 — Cours et coachs
-- Objectif  : Afficher le nom du cours, la date, l'heure et l'encadrant.
-- Technique : Jointure interne (INNER JOIN) entre COURS et COACH.
-- ---------------------------------------------------------------------
SELECT 
    c.nom AS nom_cours,
    c.date AS date_cours,
    c.heure AS heure_cours,
    co.nom AS nom_coach,
    co.prenom AS prenom_coach
FROM COURS c
JOIN COACH co ON c.id_coach = co.id_coach
ORDER BY c.date ASC, c.heure ASC;

-- ---------------------------------------------------------------------
-- Requête 3 — Inscriptions détaillées
-- Objectif  : Afficher l'adhérent, le cours associé, sa date et le statut.
-- Technique : Multiples jointures (INSCRIPTION -> ADHERENT et COURS).
-- ---------------------------------------------------------------------
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
-- Requête 4 — Nombre d'inscrits par cours
-- Objectif  : Afficher pour chaque cours sa capacité et son nombre d'inscrits.
-- Technique : LEFT JOIN pour inclure les cours sans inscrits, GROUP BY et COUNT.
-- ---------------------------------------------------------------------
SELECT 
    c.id_cours,
    c.nom AS nom_cours,
    c.capacite_max,
    COUNT(i.id_inscription) AS nombre_inscrits
FROM COURS c
LEFT JOIN INSCRIPTION i ON c.id_cours = i.id_cours
GROUP BY c.id_cours, c.nom, c.capacite_max
ORDER BY c.id_cours ASC;

-- ---------------------------------------------------------------------
-- Requête 5 — Cours complets
-- Objectif  : Identifier les cours dont les inscriptions atteignent ou dépassent la capacité.
-- Technique : Jointure, GROUP BY et filtrage agrégé avec HAVING.
-- ---------------------------------------------------------------------
SELECT 
    c.id_cours,
    c.nom AS nom_cours,
    c.capacite_max,
    COUNT(i.id_inscription) AS nombre_inscrits
FROM COURS c
JOIN INSCRIPTION i ON c.id_cours = i.id_cours
GROUP BY c.id_cours, c.nom, c.capacite_max
HAVING COUNT(i.id_inscription) >= c.capacite_max;

-- ---------------------------------------------------------------------
-- Requête 6 — Adhérents avec abonnement actif
-- Objectif  : Lister les adhérents titulaires d'un abonnement en cours de validité.
-- Technique : JOIN avec filtrage sur statut = 'ACTIF'.
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- Requête 7 — Cours d'un coach donné
-- Objectif  : Afficher le planning des cours pris en charge par un coach spécifique.
-- Paramètre : id_coach = 1 (ex: Moussa Diop) — modifiable selon besoin.
-- ---------------------------------------------------------------------
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
-- Requête 8 — Nombre de cours par coach
-- Objectif  : Récapituler la charge d'enseignement de chaque entraîneur.
-- Technique : LEFT JOIN pour afficher tous les coachs, COUNT(c.id_cours) et GROUP BY.
-- ---------------------------------------------------------------------
SELECT 
    co.id_coach,
    co.nom AS nom_coach,
    co.prenom AS prenom_coach,
    co.specialite,
    COUNT(c.id_cours) AS nombre_cours
FROM COACH co
LEFT JOIN COURS c ON co.id_coach = c.id_coach
GROUP BY co.id_coach, co.nom, co.prenom, co.specialite
ORDER BY nombre_cours DESC, co.nom ASC;

-- ---------------------------------------------------------------------
-- Requête 9 — Adhérents sans inscription
-- Objectif  : Cibler les adhérents inscrits à la salle mais à aucune séance.
-- Technique : LEFT JOIN avec sélection des clés nulles (i.id_inscription IS NULL).
-- ---------------------------------------------------------------------
SELECT 
    a.id_adherent,
    a.nom,
    a.prenom,
    a.email,
    a.telephone
FROM ADHERENT a
LEFT JOIN INSCRIPTION i ON a.id_adherent = i.id_adherent
WHERE i.id_inscription IS NULL
ORDER BY a.nom ASC, a.prenom ASC;

-- ---------------------------------------------------------------------
-- Requête 10 — Statistiques globales
-- Objectif  : Tableau de bord synthétique du volume d'activité de la salle.
-- Technique : Sous-requêtes scalaires indépendantes.
-- ---------------------------------------------------------------------
SELECT 
    (SELECT COUNT(*) FROM ADHERENT) AS total_adherents,
    (SELECT COUNT(*) FROM COACH) AS total_coachs,
    (SELECT COUNT(*) FROM COURS) AS total_cours,
    (SELECT COUNT(*) FROM INSCRIPTION) AS total_inscriptions,
    (SELECT COUNT(*) FROM ABONNEMENT) AS total_abonnements;
