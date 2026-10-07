/**
 * Country Explorer - script.js
 * Phase 4 : Recherche et filtre côté client
 *
 * Fonctionnalités implémentées :
 * - Stockage unique en mémoire des données de l'API (allCountries)
 * - Recherche en temps réel par nom de pays (insensible à la casse, espaces et accents)
 * - Normalisation Unicode des caractères accentués (normalizeText)
 * - Filtrage par région via le sélecteur (#region-filter)
 * - Combinaison logique stricte (Recherche ET Région)
 * - Mise à jour dynamique du compteur (#results-count) avec gestion du singulier/pluriel
 * - Gestion distinctive de l'état vide (#empty-state) sans provoquer d'état d'erreur
 * - Bouton et mécanisme de réinitialisation complète des filtres
 * - Aucun appel fetch() supplémentaire lors de la recherche ou du filtrage
 */

// Endpoints
const API_URL = 'https://restcountries.com/v3.1/all?fields=name,capital,region,population,flags,cca2';
const FALLBACK_API_URL = 'https://gist.githubusercontent.com/ejirocodes/f682b045d23a42f14e232d72ba4ac5e3/raw/countries.json';

// Variable d'état en mémoire : stocke l'ensemble des pays récupérés lors du chargement initial unique
let allCountries = [];

// Éléments du DOM
const searchForm = document.getElementById('search-form');
const searchInput = document.getElementById('country-search') || document.getElementById('search-input');
const regionFilter = document.getElementById('region-filter');
const resetButton = document.getElementById('reset-button');
const countriesContainer = document.getElementById('countries-container');
const loadingElement = document.getElementById('loading');
const errorMessageElement = document.getElementById('error-message');
const emptyStateElement = document.getElementById('empty-state');
const resultsCountElement = document.getElementById('results-count');

/**
 * Gère l'affichage des différents états visuels de l'interface utilisateur.
 * @param {'loading' | 'success' | 'error' | 'empty'} state
 * @param {Object} [options]
 * @param {string} [options.message]
 */
function setUIState(state, options = {}) {
  switch (state) {
    case 'loading':
      loadingElement?.classList.remove('hidden');
      errorMessageElement?.classList.add('hidden');
      emptyStateElement?.classList.add('hidden');
      if (countriesContainer) {
        countriesContainer.innerHTML = '';
      }
      if (resultsCountElement) {
        resultsCountElement.textContent = '';
      }
      break;

    case 'success':
      loadingElement?.classList.add('hidden');
      errorMessageElement?.classList.add('hidden');
      emptyStateElement?.classList.add('hidden');
      break;

    case 'error':
      loadingElement?.classList.add('hidden');
      errorMessageElement?.classList.remove('hidden');
      emptyStateElement?.classList.add('hidden');
      if (countriesContainer) {
        countriesContainer.innerHTML = '';
      }
      if (resultsCountElement) {
        resultsCountElement.textContent = '';
      }
      if (options.message && errorMessageElement) {
        const textSpan = errorMessageElement.querySelector('span:not(.error-icon)');
        if (textSpan) {
          textSpan.textContent = options.message;
        }
      }
      break;

    case 'empty':
      loadingElement?.classList.add('hidden');
      errorMessageElement?.classList.add('hidden');
      emptyStateElement?.classList.remove('hidden');
      if (countriesContainer) {
        countriesContainer.innerHTML = '';
      }
      if (resultsCountElement) {
        resultsCountElement.textContent = '0 pays trouvé';
      }
      if (emptyStateElement) {
        const textSpan = emptyStateElement.querySelector('span:not(.empty-icon)');
        if (textSpan) {
          textSpan.textContent = options.message || 'Aucun pays ne correspond à votre recherche.';
        }
      }
      break;

    default:
      console.warn(`État UI inconnu : ${state}`);
  }
}

/**
 * Crée un élément de liste représentant un détail du pays (clé/valeur).
 * Utilise textContent pour éviter toute injection HTML.
 * @param {string} label
 * @param {string} value
 * @returns {HTMLLIElement}
 */
function createDetailItem(label, value) {
  const item = document.createElement('li');
  item.className = 'country-detail-item';

  const labelSpan = document.createElement('span');
  labelSpan.className = 'detail-label';
  labelSpan.textContent = label;

  const valueSpan = document.createElement('span');
  valueSpan.className = 'detail-value';
  valueSpan.textContent = value;

  item.appendChild(labelSpan);
  item.appendChild(valueSpan);
  return item;
}

/**
 * Crée un composant de remplacement visuel quand le drapeau est indisponible.
 * @param {string} countryName
 * @returns {HTMLDivElement}
 */
function createFlagPlaceholder(countryName) {
  const placeholder = document.createElement('div');
  placeholder.className = 'flag-placeholder';
  placeholder.setAttribute('role', 'img');
  placeholder.setAttribute('aria-label', `Drapeau indisponible pour ${countryName}`);

  const emblem = document.createElement('span');
  emblem.className = 'flag-emblem';
  emblem.setAttribute('aria-hidden', 'true');
  emblem.textContent = '🌍';

  const badge = document.createElement('span');
  badge.className = 'flag-badge';
  badge.textContent = 'Sans drapeau';

  placeholder.appendChild(emblem);
  placeholder.appendChild(badge);
  return placeholder;
}

/**
 * Construit de manière sécurisée la carte DOM d'un pays.
 * @param {Object} country
 * @returns {HTMLElement | null}
 */
function createCountryCard(country) {
  // 1. Validation obligatoire du nom commun
  const commonName = country?.name?.common?.trim();
  if (!commonName) {
    return null;
  }

  const card = document.createElement('article');
  card.className = 'country-card';

  // 2. Zone du drapeau
  const flagContainer = document.createElement('div');
  flagContainer.className = 'card-flag-container';

  const flagUrl = country.flags?.svg || country.flags?.png || '';
  if (flagUrl) {
    const flagImg = document.createElement('img');
    flagImg.className = 'country-flag';
    flagImg.src = flagUrl;
    flagImg.alt = `Drapeau de ${commonName}`;
    flagImg.loading = 'lazy';

    // Remplacement par placeholder si l'image distante échoue
    flagImg.addEventListener('error', () => {
      flagContainer.innerHTML = '';
      flagContainer.appendChild(createFlagPlaceholder(commonName));
    });

    flagContainer.appendChild(flagImg);
  } else {
    flagContainer.appendChild(createFlagPlaceholder(commonName));
  }

  // 3. Corps de la carte
  const cardBody = document.createElement('div');
  cardBody.className = 'card-body';

  const title = document.createElement('h3');
  title.className = 'country-name';
  title.textContent = commonName;

  const detailsList = document.createElement('ul');
  detailsList.className = 'country-details';
  detailsList.setAttribute('aria-label', `Détails pour ${commonName}`);

  // Capitale (avec fallback robuste)
  let capitalText = 'Capitale non renseignée';
  if (Array.isArray(country.capital) && country.capital.length > 0 && country.capital[0]?.trim()) {
    capitalText = country.capital[0].trim();
  } else if (typeof country.capital === 'string' && country.capital.trim()) {
    capitalText = country.capital.trim();
  }
  detailsList.appendChild(createDetailItem('Capitale :', capitalText));

  // Région (avec fallback robuste)
  const regionText = (typeof country.region === 'string' && country.region.trim())
    ? country.region.trim()
    : 'Région non renseignée';
  detailsList.appendChild(createDetailItem('Région :', regionText));

  // Population (avec formatage en français)
  let populationText = 'Population non renseignée';
  if (typeof country.population === 'number' && !Number.isNaN(country.population)) {
    populationText = `${country.population.toLocaleString('fr-FR')} habitants`;
  }
  detailsList.appendChild(createDetailItem('Population :', populationText));

  cardBody.appendChild(title);
  cardBody.appendChild(detailsList);

  card.appendChild(flagContainer);
  card.appendChild(cardBody);

  return card;
}

/**
 * Trie et affiche la collection de pays dans le conteneur principal.
 * Met également à jour le compteur de résultats avec gestion singulier/pluriel.
 * @param {Array<Object>} countries
 */
function renderCountries(countries) {
  if (!countriesContainer) {
    return;
  }
  countriesContainer.innerHTML = '';

  // Tri alphabétique côté client sans muter le tableau d'origine
  const sortedCountries = [...countries].sort((a, b) => {
    const nameA = a?.name?.common || '';
    const nameB = b?.name?.common || '';
    return nameA.localeCompare(nameB, 'fr', { sensitivity: 'base' });
  });

  const fragment = document.createDocumentFragment();
  let validCardsCount = 0;

  for (const country of sortedCountries) {
    const card = createCountryCard(country);
    if (card) {
      fragment.appendChild(card);
      validCardsCount++;
    }
  }

  countriesContainer.appendChild(fragment);

  // Mise à jour du compteur de pays trouvés (gestion singulier/pluriel)
  if (resultsCountElement) {
    resultsCountElement.textContent = `${validCardsCount} ${validCardsCount > 1 ? 'pays trouvés' : 'pays trouvé'}`;
  }
}

/**
 * Normalise une chaîne de caractères :
 * - supprime les accents et signes diacritiques (décomposition Unicode NFD)
 * - convertit en minuscules
 * - supprime les espaces superflus aux extrémités
 * @param {string} value
 * @returns {string}
 */
function normalizeText(value) {
  if (typeof value !== 'string') {
    return '';
  }
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('fr-FR')
    .trim();
}

/**
 * Filtre le tableau en mémoire allCountries en combinant la recherche par nom et le filtre régional.
 * Ne modifie jamais le tableau source.
 * Logique stricte : Recherche ET Région.
 * @returns {Array<Object>} Tableau des pays correspondant aux critères
 */
function filterCountries() {
  const searchTerm = searchInput ? searchInput.value : '';
  const selectedRegion = regionFilter ? regionFilter.value.trim() : '';

  const normalizedSearch = normalizeText(searchTerm);

  return allCountries.filter((country) => {
    // 1. Recherche par nom (sur name.common, complété par name.official)
    const commonName = country?.name?.common || '';
    const officialName = country?.name?.official || '';

    const normalizedCommon = normalizeText(commonName);
    const normalizedOfficial = normalizeText(officialName);

    const matchesSearch = !normalizedSearch
      || normalizedCommon.includes(normalizedSearch)
      || normalizedOfficial.includes(normalizedSearch);

    // 2. Filtre par région (comparaison insensible à la casse)
    const countryRegion = country?.region ? country.region.trim() : '';
    const matchesRegion = !selectedRegion
      || (countryRegion.toLowerCase() === selectedRegion.toLowerCase());

    // Combinaison logique ET
    return matchesSearch && matchesRegion;
  });
}

/**
 * Applique les filtres en cours et orchestre la mise à jour de l'UI.
 * Bascule vers l'état 'empty' si aucun pays ne correspond aux critères.
 */
function applyFilters() {
  const filtered = filterCountries();

  if (filtered.length === 0) {
    setUIState('empty', {
      message: 'Aucun pays ne correspond à votre recherche.'
    });
  } else {
    setUIState('success');
    renderCountries(filtered);
  }
}

/**
 * Réinitialise la recherche et le filtre régional à leur valeur par défaut,
 * puis réaffiche l'intégralité des pays chargés en mémoire.
 */
function resetFilters() {
  if (searchInput) {
    searchInput.value = '';
  }
  if (regionFilter) {
    regionFilter.value = '';
  }
  applyFilters();
  searchInput?.focus();
}

/**
 * Récupère les données de secours en cas d'indisponibilité ou dépréciation de l'API officielle.
 * @returns {Promise<Array<Object> | null>}
 */
async function fetchFallbackCountries() {
  try {
    const fallbackResponse = await fetch(FALLBACK_API_URL);
    if (!fallbackResponse.ok) {
      throw new Error(`Erreur HTTP miroir : ${fallbackResponse.status}`);
    }
    const rawText = await fallbackResponse.text();
    const parsedData = new Function('return ' + rawText)();
    if (Array.isArray(parsedData)) {
      console.info(`Données REST Countries v3.1 de secours chargées (${parsedData.length} pays).`);
      return parsedData;
    }
  } catch (fallbackError) {
    console.error('Échec de la récupération des données de secours :', fallbackError);
  }
  return null;
}

/**
 * Récupère les pays depuis l'API (une seule fois au chargement) et initialise l'application.
 */
async function fetchCountries() {
  setUIState('loading');

  try {
    const response = await fetch(API_URL);

    // fetch ne rejette pas automatiquement les erreurs HTTP 4xx/5xx
    if (!response.ok) {
      throw new Error(`Réponse HTTP incorrecte de l'API : ${response.status} ${response.statusText}`);
    }

    let countries = await response.json();

    // Validation des données : l'API officielle v3.1 renvoie un objet d'erreur de dépréciation
    if (!Array.isArray(countries)) {
      console.warn("L'API REST Countries officielle a renvoyé un payload inattendu (dépréciation v5 ou indisponibilité) :", countries);

      // Bascule automatique vers le miroir REST Countries v3.1 pour garantir le fonctionnement visuel
      countries = await fetchFallbackCountries();
    }

    // Si les données ne sont toujours pas un tableau valide
    if (!Array.isArray(countries)) {
      throw new Error("Les données reçues ne sont pas au format attendu (tableau de pays).");
    }

    // Stockage en mémoire pour le filtrage côté client ultérieur
    allCountries = countries;

    // Gestion du tableau vide (Section 12 Phase 3)
    if (allCountries.length === 0) {
      setUIState('empty', {
        message: 'Aucun pays disponible.'
      });
      return;
    }

    // Affichage initial en appliquant les filtres (par défaut : 250 pays)
    applyFilters();
  } catch (error) {
    console.error('Erreur technique lors de la récupération des pays :', error);
    setUIState('error', {
      message: 'Une erreur est survenue lors du chargement des pays. Veuillez réessayer ultérieurement.'
    });
  }
}

// Initialisation au chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
  // Événements de recherche en temps réel et de sélection de région
  if (searchInput) {
    searchInput.addEventListener('input', applyFilters);
    searchInput.addEventListener('search', applyFilters);
  }

  if (regionFilter) {
    regionFilter.addEventListener('change', applyFilters);
  }

  if (resetButton) {
    resetButton.addEventListener('click', resetFilters);
  }

  // Empêche la soumission native du formulaire et applique le filtrage en mémoire
  if (searchForm) {
    searchForm.addEventListener('submit', (event) => {
      event.preventDefault();
      applyFilters();
    });
  }

  // Appel initial unique de l'API
  fetchCountries();
});
