-- =====================================================================
-- PROJET       : Gestion d'une Salle de Sport
-- FICHIER      : salle-sport.sql
-- DESCRIPTION  : Script de création de la base de données et des tables
-- SGBD         : MySQL / MariaDB (Moteur InnoDB)
-- DATE         : 2026-10-06
-- =====================================================================

-- =====================================================================
-- 1. BASE DE DONNÉES
-- =====================================================================
CREATE DATABASE IF NOT EXISTS salle_sport
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE salle_sport;

-- =====================================================================
-- 2. TABLE : COACH
-- =====================================================================
-- Représente les entraîneurs dispensant les cours dans la salle.
-- Aucune dépendance externe (table maîtresse).
CREATE TABLE IF NOT EXISTS COACH (
    id_coach INT AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    specialite VARCHAR(100) NOT NULL,
    CONSTRAINT pk_coach PRIMARY KEY (id_coach)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. TABLE : ADHERENT
-- =====================================================================
-- Représente les membres inscrits à la salle de sport.
-- L'adresse email est unique pour identifier chaque adhérent.
CREATE TABLE IF NOT EXISTS ADHERENT (
    id_adherent INT AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telephone VARCHAR(100) NOT NULL,
    CONSTRAINT pk_adherent PRIMARY KEY (id_adherent),
    CONSTRAINT uk_adherent_email UNIQUE (email)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. TABLE : COURS
-- =====================================================================
-- Représente les séances sportives planifiées.
-- Dépendance : chaque cours est obligatoirement encadré par un coach.
-- Contrainte : la capacité d'accueil doit être strictement positive (> 0).
CREATE TABLE IF NOT EXISTS COURS (
    id_cours INT AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    heure TIME NOT NULL,
    capacite_max INT NOT NULL,
    id_coach INT NOT NULL,
    CONSTRAINT pk_cours PRIMARY KEY (id_cours),
    CONSTRAINT chk_cours_capacite_max CHECK (capacite_max > 0),
    CONSTRAINT fk_cours_coach FOREIGN KEY (id_coach)
        REFERENCES COACH (id_coach)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 5. TABLE : INSCRIPTION
-- =====================================================================
-- Représente la participation d'un adhérent à un cours spécifique.
-- Dépendances : liée à ADHERENT et COURS.
-- Contrainte : un adhérent ne peut s'inscrire qu'une seule fois au même cours.
CREATE TABLE IF NOT EXISTS INSCRIPTION (
    id_inscription INT AUTO_INCREMENT,
    date_inscription DATE NOT NULL,
    statut VARCHAR(100) NOT NULL,
    id_adherent INT NOT NULL,
    id_cours INT NOT NULL,
    CONSTRAINT pk_inscription PRIMARY KEY (id_inscription),
    CONSTRAINT uk_inscription_adherent_cours UNIQUE (id_adherent, id_cours),
    CONSTRAINT fk_inscription_adherent FOREIGN KEY (id_adherent)
        REFERENCES ADHERENT (id_adherent)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_inscription_cours FOREIGN KEY (id_cours)
        REFERENCES COURS (id_cours)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 6. TABLE : ABONNEMENT
-- =====================================================================
-- Représente le contrat d'adhésion d'un membre à la salle.
-- Dépendance : lié à l'adhérent souscripteur.
-- Contrainte : la date de fin doit être postérieure ou égale à la date de début.
CREATE TABLE IF NOT EXISTS ABONNEMENT (
    id_abonnement INT AUTO_INCREMENT,
    type VARCHAR(100) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(100) NOT NULL,
    id_adherent INT NOT NULL,
    CONSTRAINT pk_abonnement PRIMARY KEY (id_abonnement),
    CONSTRAINT chk_abonnement_dates CHECK (date_fin >= date_debut),
    CONSTRAINT fk_abonnement_adherent FOREIGN KEY (id_adherent)
        REFERENCES ADHERENT (id_adherent)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;
