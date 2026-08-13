# GamesUP - EDC 4

## Description
Projet e-commerce de jeux de société avec un backend `Spring Boot`, une API de recommandation `FastAPI` (KNN) et une base `PostgreSQL`.

## Lancer le backend Spring, API de recommandations et bdd
1. Démarrer les services :
```bash
docker compose up -d --build
```

## Tester
Tester le backend Spring avec coverage :
```bash
cd gamesUP
./mvnw test verify
```

## Diagrammes
- Architecture : ![docs/diagramme_architecture_.drawio.png](docs/diagramme_architecture_.drawio.png)
- Composants : [docs/diagramme_composants_.drawio.png](docs/diagramme_composants_.drawio.png)
- Classes : [docs/diagramme_classes_.drawio.png](docs/diagramme_classes_.drawio.png)
- Séquence (recommandation) : [docs/diagramme_sequence_.draxio.png](docs/diagramme_sequence_.drawio.png)

## Documentation technique
- [Voir la documentation technique](docs/gamesup_explications_travail.pdf)
