USE salle_sport;

-- ============================================
-- 1. DONNÉES DES COACHS
-- ============================================
-- Insertion des entraîneurs de la salle de sport (minimum 4 coachs)
INSERT INTO COACH (id_coach, nom, prenom, specialite) VALUES
(1, 'Diop', 'Moussa', 'Musculation'),
(2, 'Ndiaye', 'Awa', 'Fitness'),
(3, 'Sow', 'Cheikh', 'Cardio Training'),
(4, 'Diallo', 'Fatou', 'Yoga'),
(5, 'Fall', 'Ibrahima', 'Cross Training');

-- ============================================
-- 2. DONNÉES DES ADHÉRENTS
-- ============================================
-- Insertion des membres du club (minimum 10 adhérents, emails uniques)
INSERT INTO ADHERENT (id_adherent, nom, prenom, email, telephone) VALUES
(1, 'Ba', 'Amadou', 'amadou.ba@gmail.com', '+221771234567'),
(2, 'Faye', 'Mariama', 'mariama.faye@yahoo.fr', '+221782345678'),
(3, 'Cisse', 'Ousmane', 'ousmane.cisse@gmail.com', '+221763456789'),
(4, 'Sarr', 'Aminata', 'aminata.sarr@orange.sn', '+221704567890'),
(5, 'Gueye', 'Abdoulaye', 'abdoulaye.gueye@gmail.com', '+221775678901'),
(6, 'Kane', 'Ndeye Fatou', 'ndeye.kane@hotmail.com', '+221786789012'),
(7, 'Sy', 'Mamadou', 'mamadou.sy@gmail.com', '+221767890123'),
(8, 'Ndao', 'Khady', 'khady.ndao@yahoo.sn', '+221708901234'),
(9, 'Diouf', 'Babacar', 'babacar.diouf@gmail.com', '+221779012345'),
(10, 'Seck', 'Astou', 'astou.seck@orange.sn', '+221780123456'),
(11, 'Camara', 'Modou', 'modou.camara@gmail.com', '+221761234567'),
(12, 'Traore', 'Rama', 'rama.traore@gmail.com', '+221772345678');

-- ============================================
-- 3. DONNÉES DES COURS
-- ============================================
-- Insertion des cours dispensés par les coachs (minimum 8 cours, capacités réalistes)
INSERT INTO COURS (id_cours, nom, date, heure, capacite_max, id_coach) VALUES
(1, 'Musculation Débutant', '2026-10-12', '09:00:00', 10, 1),
(2, 'Cardio Training', '2026-10-12', '10:30:00', 15, 3),
(3, 'Yoga Vinyasa', '2026-10-12', '17:00:00', 12, 4),
(4, 'Fitness Dynamique', '2026-10-13', '08:30:00', 15, 2),
(5, 'Renforcement Musculaire', '2026-10-13', '10:00:00', 12, 1),
(6, 'HIIT Express', '2026-10-13', '18:00:00', 3, 3),
(7, 'Stretching & Mobilité', '2026-10-14', '09:00:00', 10, 4),
(8, 'Circuit Training', '2026-10-14', '18:30:00', 4, 5),
(9, 'Boxe Fitness', '2026-10-15', '17:30:00', 10, 5),
(10, 'Pilates Matwork', '2026-10-15', '11:00:00', 12, 4);

-- ============================================
-- 4. DONNÉES DES INSCRIPTIONS
-- ============================================
-- Inscriptions des adhérents aux cours (minimum 15 inscriptions, sans doublon de couple adherent/cours)
INSERT INTO INSCRIPTION (id_inscription, date_inscription, statut, id_adherent, id_cours) VALUES
(1, '2026-10-01', 'CONFIRMEE', 1, 6),
(2, '2026-10-01', 'CONFIRMEE', 2, 6),
(3, '2026-10-02', 'CONFIRMEE', 3, 6),
(4, '2026-10-02', 'CONFIRMEE', 1, 8),
(5, '2026-10-02', 'CONFIRMEE', 4, 8),
(6, '2026-10-03', 'CONFIRMEE', 5, 8),
(7, '2026-10-03', 'CONFIRMEE', 6, 8),
(8, '2026-10-03', 'CONFIRMEE', 1, 1),
(9, '2026-10-04', 'EN_ATTENTE', 7, 1),
(10, '2026-10-04', 'CONFIRMEE', 2, 2),
(11, '2026-10-04', 'ANNULEE', 8, 2),
(12, '2026-10-05', 'CONFIRMEE', 3, 3),
(13, '2026-10-05', 'CONFIRMEE', 9, 3),
(14, '2026-10-05', 'CONFIRMEE', 10, 3),
(15, '2026-10-06', 'CONFIRMEE', 4, 4),
(16, '2026-10-06', 'EN_ATTENTE', 5, 4),
(17, '2026-10-06', 'CONFIRMEE', 7, 5),
(18, '2026-10-06', 'CONFIRMEE', 8, 7);

-- ============================================
-- 5. DONNÉES DES ABONNEMENTS
-- ============================================
-- Abonnements souscrits par les adhérents (minimum 10 abonnements, date_fin >= date_debut)
INSERT INTO ABONNEMENT (id_abonnement, type, date_debut, date_fin, statut, id_adherent) VALUES
(1, 'Annuel', '2026-01-01', '2026-12-31', 'ACTIF', 1),
(2, 'Trimestriel', '2026-08-01', '2026-10-31', 'ACTIF', 2),
(3, 'Mensuel', '2026-10-01', '2026-10-31', 'ACTIF', 3),
(4, 'Annuel', '2026-02-01', '2027-01-31', 'ACTIF', 4),
(5, 'Trimestriel', '2026-09-01', '2026-11-30', 'ACTIF', 5),
(6, 'Mensuel', '2026-07-01', '2026-07-31', 'EXPIRE', 6),
(7, 'Trimestriel', '2026-05-01', '2026-07-31', 'EXPIRE', 7),
(8, 'Annuel', '2025-01-01', '2025-12-31', 'EXPIRE', 8),
(9, 'Mensuel', '2026-09-15', '2026-10-15', 'SUSPENDU', 9),
(10, 'Annuel', '2026-03-01', '2027-02-28', 'ACTIF', 10),
(11, 'Trimestriel', '2026-09-01', '2026-11-30', 'ACTIF', 11),
(12, 'Mensuel', '2026-06-01', '2026-06-30', 'EXPIRE', 12);
