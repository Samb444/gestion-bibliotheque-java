-- =====================================================================
-- PROJET       : Gestion d'une Salle de Sport
-- FICHIER      : indexes.sql
-- DESCRIPTION  : Script de création et d'optimisation des index SQL (Phase 5)
-- SGBD         : MySQL / MariaDB (Moteur InnoDB)
-- DATE         : 2026-10-06
-- =====================================================================

USE salle_sport;

-- =====================================================================
-- 1. ANALYSE PRÉALABLE DES INDEX EXISTANTS (RÈGLE ANTI-REDONDANCE)
-- =====================================================================
-- Lors de la création des tables InnoDB dans salle-sport.sql :
-- 1. COACH       : PRIMARY KEY (id_coach) -> Index clustered B-Tree déjà présent.
-- 2. ADHERENT    : PRIMARY KEY (id_adherent) -> Index clustered B-Tree déjà présent.
--                  uk_adherent_email (email) -> Index B-Tree UNIQUE déjà présent.
-- 3. COURS       : PRIMARY KEY (id_cours) -> Index clustered B-Tree déjà présent.
--                  fk_cours_coach (id_coach) -> Index B-Tree automatique créé par InnoDB.
-- 4. INSCRIPTION : PRIMARY KEY (id_inscription) -> Index clustered B-Tree déjà présent.
--                  uk_inscription_adherent_cours (id_adherent, id_cours) -> Index composite UNIQUE.
--                  fk_inscription_cours (id_cours) -> Index B-Tree automatique créé par InnoDB.
-- 5. ABONNEMENT  : PRIMARY KEY (id_abonnement) -> Index clustered B-Tree déjà présent.
--                  fk_abonnement_adherent (id_adherent) -> Index B-Tree automatique créé par InnoDB.

-- =====================================================================
-- 2. ÉTUDE ET CRÉATION DES INDEX PERTINENTS
-- =====================================================================

-- ---------------------------------------------------------------------
-- TABLE : ADHERENT
-- Colonnes étudiées : nom, prenom
-- Justification :
--   - Utilisé fréquemment dans les clauses ORDER BY nom ASC, prenom ASC
--     (Requêtes 1, 6, 9) ainsi que pour les recherches nominales par filtre.
--   - Aucun index existant ne couvrait cette combinaison.
--   - Un index composite (nom, prenom) permet d'accélérer le tri et les recherches
--     selon le principe du préfixe le plus à gauche (leftmost prefix).
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_adherent_nom_prenom
    ON ADHERENT (nom, prenom);

-- ---------------------------------------------------------------------
-- TABLE : COURS
-- Colonne étudiée : id_coach
-- Analyse DBA :
--   - La colonne id_coach est une clé étrangère référençant COACH(id_coach).
--   - Le moteur InnoDB a déjà automatiquement créé l'index B-Tree `fk_cours_coach`.
--   - Décision : AUCUN index supplémentaire n'est créé afin d'éviter
--     une redondance stricte qui dégraderait les opérations DML (INSERT/UPDATE/DELETE).
-- ---------------------------------------------------------------------

-- ---------------------------------------------------------------------
-- TABLE : INSCRIPTION
-- Colonnes étudiées : id_adherent, id_cours
-- Analyse DBA :
--   - La contrainte `uk_inscription_adherent_cours` a déjà créé un index UNIQUE
--     composite sur (id_adherent, id_cours). Grâce à la règle du leftmost prefix,
--     toute recherche ou jointure sur `id_adherent` utilise déjà cet index.
--   - La clé étrangère `fk_inscription_cours` a également généré un index B-Tree
--     sur `id_cours`.
--   - Décision : AUCUN index supplémentaire n'est créé, les deux colonnes
--     étant déjà couvertes de manière optimale.
-- ---------------------------------------------------------------------

-- ---------------------------------------------------------------------
-- TABLE : ABONNEMENT
-- Colonne étudiée : id_adherent
-- Analyse DBA :
--   - La clé étrangère `fk_abonnement_adherent` a déjà généré automatiquement
--     un index B-Tree sur (id_adherent) lors de la création de la table.
--   - Décision : AUCUN index supplémentaire n'est créé pour éviter un doublon
--     inutile gaspillant de la mémoire et des I/O.
-- ---------------------------------------------------------------------

-- =====================================================================
-- 3. VÉRIFICATION DES INDEX ACTIFS
-- =====================================================================
SHOW INDEX FROM ADHERENT;
