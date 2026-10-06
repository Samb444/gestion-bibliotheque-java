# Exercice 4 — Country Explorer (Bases du Web)

Application web sans framework pour explorer les pays du monde via une API publique REST.

---

## 1. Objectif du projet

Cet exercice a pour but d'assimiler les fondamentaux du développement web front-end natif :
* **HTML5 sémantique et accessible** (`header`, `main`, `section`, `footer`, attributs ARIA et labels de formulaires).
* **CSS3 moderne et responsive** (Flexbox, CSS Grid, variables CSS, Media Queries, sans framework externe).
* **JavaScript Vanilla** (manipulation du DOM, fetch API, gestion asynchrone, recherche et filtres côté client).

---

## 2. Technologies utilisées

* **HTML5** : Structure sémantique et balisage accessible.
* **CSS3 natif** : Mise en page responsive sans Bootstrap ni Tailwind.
* **JavaScript Vanilla (ES6+)** : Logique applicative sans framework (React, Vue, etc.).
* **API cible (phases ultérieures)** : [REST Countries API](https://restcountries.com/).

---

## 3. Structure du projet

```text
exo-4-bases-web/
├── index.html        # Structure principale de l'application
├── css/
│   └── style.css     # Feuilles de style natives et responsive
├── js/
│   └── script.js     # Script d'initialisation et future logique API
└── README.md         # Documentation du projet
```

---

## 4. Lancement local

Le projet ne nécessite aucune dépendance Node.js ni installation de packages.

Plusieurs méthodes simples permettent de le visualiser localement :

### Méthode 1 : Avec Python (recommandée)
Dans un terminal ouvert dans le dossier `exo-4-bases-web` :
```powershell
python -m http.server 5500
```
Puis ouvrir l'adresse suivante dans un navigateur :
```text
http://127.0.0.1:5500/
```

### Méthode 2 : Avec l'extension VS Code Live Server
* Clic droit sur `index.html` > **Open with Live Server**.

### Méthode 3 : Ouverture directe
* Double-clic sur le fichier `index.html` pour l'ouvrir directement dans votre navigateur web.

---

## 5. Prochaines fonctionnalités prévues

* **Phase 2 & suivantes** :
  * Intégration de l'API REST `restcountries.com` via `fetch()`.
  * Rendu dynamique des cartes de pays (drapeau, nom, capitale, population, région).
  * Recherche interactive par nom de pays et filtrage par région.
  * Gestion dynamique des états de chargement (`#loading`) et d'erreur (`#error-message`).
