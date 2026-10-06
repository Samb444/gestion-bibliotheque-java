/**
 * Country Explorer - script.js
 * Phase 3 : Consommation de l'API publique et affichage dynamique
 *
 * Fonctionnalités implémentées :
 * - Connexion à l'API publique REST Countries
 * - Récupération asynchrone des données avec fetch()
 * - Gestion rigoureuse des états (chargement, succès, erreur, état vide)
 * - Création sécurisée d'éléments DOM (document.createElement, textContent)
 * - Validation des données et gestion des fallbacks (capitales, régions, drapeaux)
 * - Tri initial alphabétique côté client (localeCompare)
 * - Affichage du nombre total de pays chargés
 * - Prévention de la soumission du formulaire de recherche (sans implémenter la recherche)
 */

// Endpoints
const API_URL = 'https://restcountries.com/v3.1/all?fields=name,capital,region,population,flags,cca2';
const FALLBACK_API_URL = 'https://gist.githubusercontent.com/ejirocodes/f682b045d23a42f14e232d72ba4ac5e3/raw/countries.json';

// Éléments du DOM
const searchForm = document.getElementById('search-form');
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
        resultsCountElement.textContent = '0 pays disponible';
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

  // Mise à jour du compteur de pays chargés
  if (resultsCountElement) {
    resultsCountElement.textContent = `${validCardsCount} ${validCardsCount > 1 ? 'pays trouvés' : 'pays trouvé'}`;
  }
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
 * Récupère les pays depuis l'API et orchestre l'affichage et les états.
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

    // Gestion du tableau vide (Section 12)
    if (countries.length === 0) {
      setUIState('empty');
      return;
    }

    // Succès : affichage dynamique et mise à jour de l'UI
    renderCountries(countries);
    setUIState('success');
  } catch (error) {
    console.error('Erreur technique lors de la récupération des pays :', error);
    setUIState('error', {
      message: 'Une erreur est survenue lors du chargement des pays. Veuillez réessayer ultérieurement.'
    });
  }
}

// Initialisation au chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
  // Empêche la soumission native du formulaire sans déclencher de recherche à cette phase
  if (searchForm) {
    searchForm.addEventListener('submit', (event) => {
      event.preventDefault();
    });
  }

  // Appel initial unique de l'API
  fetchCountries();
});
