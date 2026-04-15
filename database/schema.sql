-- ============================================================
-- UNIV-SCHEDULER — Script de création de la base de données
-- SQLite 3 / MySQL compatible
-- ============================================================

-- Activation des clés étrangères (SQLite)
PRAGMA foreign_keys = ON;

-- ─── Table utilisateurs ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS utilisateurs (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nom          TEXT    NOT NULL,
    prenom       TEXT    NOT NULL,
    email        TEXT    UNIQUE,
    login        TEXT    UNIQUE NOT NULL,
    mot_de_passe TEXT    NOT NULL,
    role         TEXT    NOT NULL CHECK(role IN ('ADMIN','GESTIONNAIRE','ENSEIGNANT','ETUDIANT')),
    -- Enseignant
    departement  TEXT,
    specialite   TEXT,
    -- Étudiant
    groupe       TEXT,
    filiere      TEXT,
    niveau       INTEGER DEFAULT 1
);

-- ─── Table bâtiments ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS batiments (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nom            TEXT    NOT NULL,
    adresse        TEXT,
    nombre_etages  INTEGER DEFAULT 1
);

-- ─── Table salles ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS salles (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    numero       TEXT    NOT NULL,
    capacite     INTEGER NOT NULL,
    type         TEXT    NOT NULL CHECK(type IN
                     ('COURS','AMPHI','LABORATOIRE','TD','TP','CONFERENCE','SPORT')),
    batiment_id  INTEGER REFERENCES batiments(id) ON DELETE SET NULL,
    accessible   INTEGER DEFAULT 0,   -- 1 = accessibilité PMR
    -- Spécifique Laboratoire
    nb_postes    INTEGER DEFAULT 0,
    os           TEXT,
    -- Spécifique Amphithéâtre
    sonorisation    INTEGER DEFAULT 0,
    retransmission  INTEGER DEFAULT 0
);

-- ─── Table équipements ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS equipements (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nom         TEXT    NOT NULL,
    description TEXT,
    salle_id    INTEGER REFERENCES salles(id) ON DELETE CASCADE,
    fonctionnel INTEGER DEFAULT 1
);

-- ─── Table cours ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cours (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    nom           TEXT    NOT NULL,
    description   TEXT,
    enseignant_id INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    couleur       TEXT    DEFAULT '#4A90D9'
);

-- ─── Table créneaux horaires ─────────────────────────────────
CREATE TABLE IF NOT EXISTS creneaux (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    cours_id      INTEGER REFERENCES cours(id) ON DELETE CASCADE,
    salle_id      INTEGER REFERENCES salles(id) ON DELETE SET NULL,
    enseignant_id INTEGER REFERENCES utilisateurs(id),
    jour          TEXT    NOT NULL CHECK(jour IN
                     ('LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI')),
    heure_debut   TEXT    NOT NULL,  -- format HH:MM
    heure_fin     TEXT    NOT NULL,  -- format HH:MM
    groupe        TEXT    NOT NULL
);

-- Table des annulations ponctuelles de cours
CREATE TABLE IF NOT EXISTS annulations_cours (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    creneau_id       INTEGER NOT NULL REFERENCES creneaux(id) ON DELETE CASCADE,
    date_annulation  TEXT    NOT NULL,  -- format YYYY-MM-DD
    motif            TEXT    NOT NULL,
    annule_par_id    INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
    date_creation    TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(creneau_id, date_annulation)
);

-- Notifications internes visibles dans l'interface
CREATE TABLE IF NOT EXISTS notifications (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    utilisateur_id  INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    titre           TEXT    NOT NULL,
    message         TEXT    NOT NULL,
    type            TEXT    NOT NULL DEFAULT 'INFO',
    lue             INTEGER NOT NULL DEFAULT 0,
    date_creation   TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ─── Table réservations ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    salle_id        INTEGER REFERENCES salles(id) ON DELETE CASCADE,
    utilisateur_id  INTEGER REFERENCES utilisateurs(id),
    date            TEXT    NOT NULL,  -- format YYYY-MM-DD
    heure_debut     TEXT    NOT NULL,  -- format HH:MM
    heure_fin       TEXT    NOT NULL,  -- format HH:MM
    motif           TEXT,
    statut          TEXT    DEFAULT 'EN_ATTENTE'
                    CHECK(statut IN ('EN_ATTENTE','APPROUVEE','REFUSEE','ANNULEE'))
);

-- ——— Table signalements —————————————————————————————————
CREATE TABLE IF NOT EXISTS signalements (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    utilisateur_id  INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    sujet           TEXT    NOT NULL,
    description     TEXT    NOT NULL,
    statut          TEXT    NOT NULL DEFAULT 'NOUVEAU',
    date_creation   TEXT    NOT NULL
);

-- ============================================================
-- Données de démonstration
-- ============================================================

-- Comptes utilisateurs (mots de passe en clair pour la démo)
INSERT INTO utilisateurs (nom, prenom, email, login, mot_de_passe, role) VALUES
    ('Kali',   'François',  'papalyndiaye0@gmail.com' ,  'papalyndiaye0@gmail.com', 'admin123',  'ADMIN'),
    ('Konaté',   'Madame', 'p.martin@univ.fr',     'gestionnaire',  'gest123',   'GESTIONNAIRE'),
    ('Diallo',  'Abdoulaye', 's.bernard@univ.fr',    'sbernard',      'ens123',    'ENSEIGNANT'),
    ('Lefebvre', 'Thomas', 't.lefebvre@univ.fr',   'tlefebvre',     'ens123',    'ENSEIGNANT'),
    ('Moreau',   'Julie',  'j.moreau@univ.fr',     'jmoreau',       'ens123',    'ENSEIGNANT'),
    ('Simon',    'Léa',    'lea.simon@etud.fr',    'lsimon',        'etu123',    'ETUDIANT'),
    ('Laurent',  'Hugo',   'hugo.laurent@etud.fr', 'hlaurent',      'etu123',    'ETUDIANT');

UPDATE utilisateurs SET departement='Informatique', specialite='Algorithmique' WHERE login='sbernard';
UPDATE utilisateurs SET departement='Mathématiques', specialite='Algèbre' WHERE login='tlefebvre';
UPDATE utilisateurs SET departement='Informatique', specialite='Réseaux' WHERE login='jmoreau';
UPDATE utilisateurs SET groupe='L2-INFO-A', filiere='Informatique', niveau=2 WHERE login='lsimon';
UPDATE utilisateurs SET groupe='L2-INFO-A', filiere='Informatique', niveau=2 WHERE login='hlaurent';
UPDATE Utilisateurs SET nom='Kali', prenom='François', email='papalyndiaye0@gmail.com', login='papalyndiaye0@gmail.com' WHERE mot_de_passe='admin123';
UPDATE Utilisateurs SET nom='Konaté', prenom='Madame' where login='gestionnaire';


-- Bâtiments
INSERT INTO batiments (nom, adresse, nombre_etages) VALUES
    ('Bâtiment A - Sciences',  '10 rue de la Paix', 4),
    ('Bâtiment B - Lettres',   '12 rue de la Paix', 3),
    ('Bâtiment C - Amphithéâtres', '14 rue de la Paix', 2);

-- Salles
INSERT INTO salles (numero, capacite, type, batiment_id, accessible) VALUES
    ('A101', 30,  'COURS',       1, 1),
    ('A102', 30,  'TD',          1, 1),
    ('A201', 20,  'LABORATOIRE', 1, 0),
    ('A202', 25,  'TP',          1, 0),
    ('B101', 40,  'COURS',       2, 1),
    ('B102', 35,  'TD',          2, 1),
    ('C001', 200, 'AMPHI',       3, 1),
    ('C002', 150, 'AMPHI',       3, 1);

UPDATE salles SET nb_postes=20, os='Linux/Windows' WHERE numero='A201';
UPDATE salles SET sonorisation=1, retransmission=1 WHERE type='AMPHI';

-- Équipements
INSERT INTO equipements (nom, description, salle_id, fonctionnel) VALUES
    ('Projecteur',     'Vidéoprojecteur HD',        1, 1),
    ('Tableau blanc',  'Grand tableau blanc',       1, 1),
    ('Climatisation',  'Climatisation réversible',  1, 1),
    ('Projecteur',     'Vidéoprojecteur HD',        2, 1),
    ('Tableau blanc',  'Tableau blanc',             2, 1),
    ('PC Enseignant',  'Poste enseignant Linux',    3, 1),
    ('Imprimante',     'Imprimante réseau',         3, 1),
    ('Micro-casques',  'Casques audio',             3, 0),
    ('Vidéoprojecteur','Projecteur 4K',             7, 1),
    ('Système son',    'Amplificateur + micros',    7, 1);

-- Cours
INSERT INTO cours (nom, description, enseignant_id, couleur) VALUES
    ('Algorithmique',      'Introduction aux algorithmes', 3, '#E74C3C'),
    ('Bases de données',   'Modélisation et SQL',          3, '#3498DB'),
    ('Algèbre linéaire',   'Espaces vectoriels',           4, '#2ECC71'),
    ('Réseaux',            'Protocoles et architectures',  5, '#9B59B6'),
    ('POO Java',           'Programmation orientée objet', 3, '#F39C12');

-- Créneaux
INSERT INTO creneaux (cours_id, salle_id, enseignant_id, jour, heure_debut, heure_fin, groupe) VALUES
    (1, 1, 3, 'LUNDI',    '08:00', '10:00', 'L2-INFO-A'),
    (1, 1, 3, 'LUNDI',    '08:00', '10:00', 'L2-INFO-B'),
    (2, 2, 3, 'MARDI',    '10:00', '12:00', 'L2-INFO-A'),
    (3, 5, 4, 'MERCREDI', '14:00', '16:00', 'L2-INFO-A'),
    (4, 3, 5, 'JEUDI',    '08:00', '10:00', 'L2-INFO-A'),
    (5, 2, 3, 'VENDREDI', '10:00', '12:00', 'L2-INFO-A'),
    (1, 7, 3, 'LUNDI',    '14:00', '16:00', 'L3-INFO-A'),
    (3, 5, 4, 'MARDI',    '08:00', '10:00', 'L3-MATH-A');
