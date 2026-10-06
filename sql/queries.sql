USE salle_sport;

-- =====================================================================
-- EXERCICE 3 : GESTION DE SALLE DE SPORT
-- FICHIER    : queries.sql
-- DESCRIPTION: Requêtes SQL d'exploitation et d'analyse des données
--              Couverture intégrale des exigences du cahier des charges
-- =====================================================================

-- =====================================================================
-- SECTION 1 : REQUÊTES MÉTIER DU CAHIER DES CHARGES (EXIGENCES 1 À 6)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Requête 1 — Liste des cours avec le nombre d'inscrits
-- Objectif  : Afficher la liste de tous les cours et dénombrer leurs inscrits.
-- Règle     : Inclut obligatoirement les cours sans inscription (zéro inscrit).
-- Technique : LEFT JOIN entre COURS et INSCRIPTION, GROUP BY et COUNT(i.id_inscription).
-- ---------------------------------------------------------------------
SELECT 
    c.id_cours,
    c.nom AS nom_cours,
    c.date AS date_cours,
    c.heure AS heure_cours,
    c.capacite_max,
    COUNT(i.id_inscription) AS nombre_inscrits
FROM COURS c
LEFT JOIN INSCRIPTION i ON c.id_cours = i.id_cours
GROUP BY c.id_cours, c.nom, c.date, c.heure, c.capacite_max
ORDER BY c.id_cours ASC;

-- ---------------------------------------------------------------------
-- Requête 2 — Cours ayant atteint leur capacité maximale
-- Objectif  : Détecter les cours complets ou en surcapacité.
-- Règle     : Comparer strictement nombre_inscrits >= capacite_max.
-- Technique : INNER JOIN, GROUP BY et filtrage post-agrégation avec HAVING.
-- ---------------------------------------------------------------------
SELECT 
    c.id_cours,
    c.nom AS nom_cours,
    c.date AS date_cours,
    c.capacite_max,
    COUNT(i.id_inscription) AS nombre_inscrits
FROM COURS c
JOIN INSCRIPTION i ON c.id_cours = i.id_cours
GROUP BY c.id_cours, c.nom, c.date, c.capacite_max
HAVING COUNT(i.id_inscription) >= c.capacite_max
ORDER BY c.id_cours ASC;

-- ---------------------------------------------------------------------
-- Requête 3 — Adhérents sans abonnement actif
-- Objectif  : Cibler les membres dont l'adhésion est expirée, suspendue ou inexistante.
-- Règle     : Un abonnement actif doit avoir statut = 'ACTIF' et englober la date courante.
-- Technique : Sous-requête corrélée avec NOT EXISTS (évite les doublons et faux résultats JOIN).
-- ---------------------------------------------------------------------
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
-- Requête 4 — Top 3 des adhérents les plus assidus
-- Objectif  : Identifier les trois membres les plus réguliers aux entraînements.
-- Règle     : Filtrer impérativement sur statut_presence = 'PRESENT' (exclut les absences).
-- Technique : Jointures PRESENCE -> INSCRIPTION -> ADHERENT, GROUP BY, tri décroissant et LIMIT 3.
-- ---------------------------------------------------------------------
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
-- Requête 5 — Nombre d'adhérents par type d'abonnement
-- Objectif  : Répartir les adhérents par formule souscrite (Annuel, Trimestriel, Mensuel).
-- Règle     : Éviter de compter plusieurs fois un adhérent ayant des renouvellements historiques.
-- Logique   : Utilisation de COUNT(DISTINCT ab.id_adherent) pour dénombrer les personnes uniques,
--             mis en regard de COUNT(ab.id_abonnement) pour le volume de contrats souscrits.
-- ---------------------------------------------------------------------
SELECT 
    ab.type AS type_abonnement,
    COUNT(DISTINCT ab.id_adherent) AS nombre_adherents_distincts,
    COUNT(ab.id_abonnement) AS total_contrats_souscrits
FROM ABONNEMENT ab
GROUP BY ab.type
ORDER BY nombre_adherents_distincts DESC;

-- ---------------------------------------------------------------------
-- Requête 6 — Revenus du mois (Chiffre d'affaires encaissé)
-- Objectif  : Calculer le montant total perçu pour un mois calendaire donné.
-- Règle     : Sommer exclusivement les paiements validés (statut = 'PAYE').
--             Exclut les règlements en attente ('EN_ATTENTE') ou échoués ('ECHOUE').
-- Mois réf. : Octobre 2026 (du 2026-10-01 inclus au 2026-11-01 exclu).
-- Pédagogie : L'usage d'une plage de dates permet d'exploiter efficacement
--             l'index composite (statut, date_paiement), contrairement à l'application
--             directe de fonctions YEAR() / MONTH() sur la colonne.
-- ---------------------------------------------------------------------
SELECT 
    '2026-10' AS mois_analyse,
    COUNT(p.id_paiement) AS nombre_paiements_encaisses,
    COALESCE(SUM(p.montant), 0.00) AS revenus_totaux_fcfa
FROM PAIEMENT p
WHERE p.statut = 'PAYE'
  AND p.date_paiement >= '2026-10-01'
  AND p.date_paiement < '2026-11-01';

-- =====================================================================
-- SECTION 2 : REQUÊTES D'EXPLOITATION COMPLÉMENTAIRES CONSERVÉES
-- =====================================================================

-- ---------------------------------------------------------------------
-- Requête 7 — Tous les adhérents
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
-- Requête 8 — Cours et coachs encadrants
-- Objectif  : Planning complet des séances avec le coach responsable.
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
-- Requête 9 — Inscriptions détaillées
-- Objectif  : Suivi individuel des réservations par cours et statut.
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
-- Requête 10 — Charge d'enseignement d'un coach (ex: Moussa Diop)
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
-- Requête 11 — Nombre de cours par coach
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
-- Requête 12 — Adhérents sans aucune inscription
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
-- Requête 13 — Tableau de bord des volumes globaux
-- ---------------------------------------------------------------------
SELECT 
    (SELECT COUNT(*) FROM ADHERENT) AS total_adherents,
    (SELECT COUNT(*) FROM COACH) AS total_coachs,
    (SELECT COUNT(*) FROM COURS) AS total_cours,
    (SELECT COUNT(*) FROM INSCRIPTION) AS total_inscriptions,
    (SELECT COUNT(*) FROM ABONNEMENT) AS total_abonnements,
    (SELECT COUNT(*) FROM PAIEMENT) AS total_paiements,
    (SELECT COUNT(*) FROM PRESENCE) AS total_presences;
