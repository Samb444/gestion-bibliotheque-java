-- =====================================================================
-- PROJET       : Gestion d'une Salle de Sport
-- FICHIER      : indexes.sql
-- DESCRIPTION  : Script de création et d'optimisation des index SQL
-- SGBD         : MySQL / MariaDB (Moteur InnoDB)
-- DATE         : 2026-10-06
-- =====================================================================

USE salle_sport;

-- =====================================================================
-- 1. ANALYSE PRÉALABLE DES INDEX EXISTANTS (RÈGLE ANTI-REDONDANCE)
-- =====================================================================
-- Lors de la création des tables InnoDB dans salle-sport.sql :
-- 1. COACH       : PRIMARY KEY (id_coach) -> Index clustered B-Tree.
-- 2. ADHERENT    : PRIMARY KEY (id_adherent) -> Index clustered B-Tree.
--                  uk_adherent_email (email) -> Index B-Tree UNIQUE.
-- 3. COURS       : PRIMARY KEY (id_cours) -> Index clustered B-Tree.
--                  fk_cours_coach (id_coach) -> Index B-Tree automatique InnoDB.
-- 4. INSCRIPTION : PRIMARY KEY (id_inscription) -> Index clustered B-Tree.
--                  uk_inscription_adherent_cours (id_adherent, id_cours) -> Index composite UNIQUE.
--                  fk_inscription_cours (id_cours) -> Index B-Tree automatique InnoDB.
-- 5. ABONNEMENT  : PRIMARY KEY (id_abonnement) -> Index clustered B-Tree.
--                  fk_abonnement_adherent (id_adherent) -> Index B-Tree automatique InnoDB.
-- 6. PAIEMENT    : PRIMARY KEY (id_paiement) -> Index clustered B-Tree.
--                  fk_paiement_abonnement (id_abonnement) -> Index B-Tree automatique InnoDB.
-- 7. PRESENCE    : PRIMARY KEY (id_presence) -> Index clustered B-Tree.
--                  uk_presence_inscription (id_inscription) -> Index B-Tree UNIQUE.

-- =====================================================================
-- 2. ÉTUDE ET CRÉATION DES INDEX MÉTIER PERTINENTS
-- =====================================================================

-- ---------------------------------------------------------------------
-- TABLE : ADHERENT
-- Colonnes étudiées : nom, prenom
-- Justification :
--   - Utilisé fréquemment dans les clauses ORDER BY nom ASC, prenom ASC
--     (Requêtes 1, 3, 4, 7, 12) ainsi que pour les recherches nominales.
--   - Un index composite (nom, prenom) élimine le filesort en mémoire.
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_adherent_nom_prenom
    ON ADHERENT (nom, prenom);

-- ---------------------------------------------------------------------
-- TABLE : PAIEMENT
-- Colonnes étudiées : statut, date_paiement
-- Justification :
--   - Crucial pour la Requête 6 (Revenus du mois) qui filtre simultanément
--     sur statut = 'PAYE' et sur une période mensuelle (date_paiement).
--   - Permet un balayage ciblé de l'index sans parcourir les paiements en attente ou échoués.
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_paiement_statut_date
    ON PAIEMENT (statut, date_paiement);

-- ---------------------------------------------------------------------
-- TABLE : ABONNEMENT
-- Colonnes étudiées : id_adherent, statut, date_debut, date_fin
-- Justification :
--   - Optimise la sous-requête de la Requête 3 (Adhérents sans abonnement actif).
--   - Index composite couvrant (Covering Index) : permet de valider l'activité
--     d'un abonnement directement dans l'index B-Tree sans accès table (Using index).
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_abonnement_actif_lookup
    ON ABONNEMENT (id_adherent, statut, date_debut, date_fin);

-- ---------------------------------------------------------------------
-- TABLE : PRESENCE
-- Colonne étudiée : statut_presence
-- Justification :
--   - Utilisé par la Requête 4 (Top 3 des assidus) pour filtrer statut_presence = 'PRESENT'.
--   - Accélère l'isolation des présences effectives en évitant le traitement des absences.
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_presence_statut
    ON PRESENCE (statut_presence);

-- =====================================================================
-- 3. VÉRIFICATION DES INDEX ACTIFS
-- =====================================================================
SHOW INDEX FROM ADHERENT;
SHOW INDEX FROM PAIEMENT;
SHOW INDEX FROM ABONNEMENT;
SHOW INDEX FROM PRESENCE;
