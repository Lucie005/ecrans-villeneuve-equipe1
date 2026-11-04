# Hexagone « Vente de places » — TP du J4

Le cœur du core domain des Écrans de Villeneuve : les agrégats `Seance` et `Commande`, et trois cas d'usage.
**Du Java pur** au J4 : ni Spring, ni base de données. Les adapters sont en mémoire, l'horloge est réglable.
Spring Boot arrive après le J4, à l'étape 7 (voir le sujet).

## Lancer les tests

Dans IntelliJ :

- **tout** : fenêtre **Maven → vente → Lifecycle → test** ;
- **une seule classe** : clic droit sur la classe de test (par exemple `SeanceTest`) → **Run**.

Au départ, presque tout est **rouge** : c'est normal. Votre travail est de les passer au **vert**, étape par étape.
Cherchez `TODO` dans le code : chaque méthode à écrire contient des **indices**.

## Les étapes

| Étape | À écrire | Tests | Règles |
|---|---|---|---|
| 1 | `domaine/seance/Seance` : `bloquer`, `vendre`, `libererBlocagesExpires` | `etape1/SeanceTest` | B1, B2, B4 |
| 2 | `domaine/commande/Commande` : `creer`, `total`, `payer`, `expirer` | `etape2/CommandeTest` | B3, B6 |
| 3 | `application/service/ReserverDesPlacesService` | `etape3/ReserverDesPlacesTest` | |
| 4 | `application/service/ConfirmerPaiementService` | `etape4/ConfirmerPaiementTest` | |
| 5 | `application/service/ExpirerBlocagesService` | `etape5/ExpirerBlocagesTest` | B2 |
| 6 | trouver et corriger la dépendance dans le mauvais sens cachée dans le code fourni (voir ci-dessous) | tous, toujours verts | |
| ✔ | rien : tout est branché par `config/ApplicationVente` | `ScenarioSoireeDuJeudiTest` | |
| 7 | après le J4 : brancher Spring Boot (pom, contrôleurs REST, webhook, planificateur) — section « Étape 7 » du sujet | `etape7/ApiVenteTest` (dans `fournis/`) | |
| 8 | après le J4 : les ventes partenaires SortiesFlash (cas d'usage + ACL) — section « Étape 8 » du sujet | `etape8/VentePartenaireApiTest` (dans `fournis/`) | B11 |
| bonus | après l'étape 7 : une page web, la consultation du plan de salle, une base H2, l'annulation d'une séance (section 9 du sujet) | | |

**Organisation** : les étapes 1 et 2 sont indépendantes. À trois, l'étape 1 (la plus longue) en binôme et l'étape 2 seul·e, en parallèle ; à deux, une étape chacun.
Ensuite, répartissez-vous les services (étapes 3, 4 et 5), puis faites l'étape 6 ensemble.

## Étape 6 : vérifier l'architecture à la main

**Edit → Find → Find in Files…** (**Ctrl+Maj+F**), onglet **Directory**, sur le dossier indiqué :

| Dans le dossier… | Cherchez | Résultat attendu |
|---|---|---|
| `domaine` | `.application.`, puis `.adapter.`, puis `.config.` | aucun : le domaine ne dépend que de Java |
| `domaine` et `application` | `import org.springframework` | aucun |
| `application` | `.adapter.`, puis `.config.` | aucun : l'application ne connaît pas les adapters |
| `domaine`, `application` et `adapter` | `.now()` | aucun : l'heure vient de la `Clock` injectée |

**Le code fourni contient une dépendance dans le mauvais sens**, et les tests sont verts malgré elle. Chaque résultat est une dépendance dans le mauvais sens : corrigez-la en passant par un port, puis expliquez-la dans la section 10 du DAT.

## Ce qui est déjà écrit (lisez-le !)

- les value objects (`PlaceId`, `Montant`, `Tarif`, `EtatPlace`…), les exceptions métier, les événements ;
- les **ports** (`application/port/in` et `application/port/out`) ;
- les **adapters** : en mémoire (avec verrou optimiste sur les séances), grille tarifaire fixe, paiement simulé,
  et l'ACL `WebhookPayVia` qui traduit les notifications de PayVia ;
- la **composition root** `config/ApplicationVente`.

## Bonus

Les tests de `bonus/` sont désactivés (`@Disabled`). Supprimez l'annotation pour les activer :

- `PaiementEnDoubleTest` : PayVia envoie deux fois la même notification (B14) ;
- `PaiementTardifTest` : le paiement arrive après la fin du blocage (B13) ;
- `ConcurrenceTest` : deux caisses vendent dans la même séance au même instant (ADR-0002).

## Organisation du code

```
com.ecrans.vente
├── domaine/          ← agrégats, VO, événements : aucune dépendance
├── application/
│   ├── port/in/      ← cas d'usage (ports primaires)
│   ├── port/out/     ← ce dont l'application a besoin (ports secondaires)
│   └── service/      ← implémentation des cas d'usage
├── adapter/
│   ├── in/payvia/    ← webhook PayVia (ACL entrante)
│   ├── in/web/       ← étape 7 : API REST et gestion des erreurs
│   ├── in/planification/ ← étape 7 : le planificateur de B2
│   ├── in/sortiesflash/  ← étape 8 : l'ACL des ventes SortiesFlash
│   └── out/…         ← mémoire, tarification, paiement, événements
└── config/           ← composition root (et, à l'étape 7, la configuration Spring)
```

À côté de `src/` : `fournis/`, les tests des étapes 7 et 8 à copier une fois Spring installé.
