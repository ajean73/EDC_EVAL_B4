import pandas as pd
from pathlib import Path

from models import GameCatalogItem, TrainInteraction, TrainRequest


def load_training_data(file_path: str):
    return pd.read_csv(file_path)


def build_training_data_from_csv_or_fallback(file_path: str, n_neighbors: int = 5) -> TrainRequest:
    csv_path = Path(file_path)
    if not csv_path.exists():
        return build_fallback_training_data(n_neighbors=n_neighbors)

    try:
        df = load_training_data(str(csv_path))
    except Exception:
        return build_fallback_training_data(n_neighbors=n_neighbors)

    if df.empty:
        return build_fallback_training_data(n_neighbors=n_neighbors)

    catalog = _build_catalog_from_dataframe(df)
    interactions = _build_interactions_from_dataframe(df)

    if not catalog or not interactions:
        return build_fallback_training_data(n_neighbors=n_neighbors)

    return TrainRequest(
        catalog=catalog,
        interactions=interactions,
        n_neighbors=max(2, int(n_neighbors)),
    )


def build_seed_training_data() -> TrainRequest:
    catalog = [
        GameCatalogItem(
            game_id="11111111-1111-1111-1111-111111111111",
            title="Dixit",
            publisher="Libellud",
            categories=["Ambiance", "Cartes"],
            authors=["Jean-Louis Roubira"],
            price=29.99,
            average_rating=4.5,
        ),
        GameCatalogItem(
            game_id="22222222-2222-2222-2222-222222222222",
            title="Les Aventuriers du Rail - Europe",
            publisher="Days of Wonder",
            categories=["Familial", "Gestion"],
            authors=["Alan R. Moon"],
            price=42.99,
            average_rating=4.4,
        ),
        GameCatalogItem(
            game_id="33333333-3333-3333-3333-333333333333",
            title="7 Wonders",
            publisher="Repos Production",
            categories=["Strategie", "Cartes"],
            authors=["Antoine Bauza"],
            price=44.99,
            average_rating=4.6,
        ),
        GameCatalogItem(
            game_id="44444444-4444-4444-4444-444444444444",
            title="Takenoko",
            publisher="Bombyx",
            categories=["Familial", "Placement"],
            authors=["Antoine Bauza"],
            price=36.99,
            average_rating=4.3,
        ),
    ]

    interactions = [
        TrainInteraction(
            user_id="u-1",
            game_id="11111111-1111-1111-1111-111111111111",
            rating=4.7,
        ),
        TrainInteraction(
            user_id="u-1",
            game_id="22222222-2222-2222-2222-222222222222",
            rating=4.4,
        ),
        TrainInteraction(
            user_id="u-2",
            game_id="33333333-3333-3333-3333-333333333333",
            rating=4.6,
        ),
        TrainInteraction(
            user_id="u-2",
            game_id="44444444-4444-4444-4444-444444444444",
            rating=4.3,
        ),
    ]

    return TrainRequest(catalog=catalog, interactions=interactions, n_neighbors=3)


def build_fallback_training_data(n_neighbors: int = 5) -> TrainRequest:
    seed_data = build_seed_training_data()
    return TrainRequest(
        catalog=seed_data.catalog,
        interactions=seed_data.interactions,
        n_neighbors=max(2, int(n_neighbors)),
    )


def _build_catalog_from_dataframe(df: pd.DataFrame) -> list[GameCatalogItem]:
    required = {"game_id", "title"}
    if not required.issubset(df.columns):
        return []

    rows = df.drop_duplicates(subset=["game_id"]).to_dict(orient="records")
    catalog: list[GameCatalogItem] = []

    for row in rows:
        catalog.append(
            GameCatalogItem(
                game_id=str(row.get("game_id")),
                title=str(row.get("title")),
                publisher=str(row.get("publisher", "unknown")),
                categories=_parse_list_field(row.get("categories")),
                authors=_parse_list_field(row.get("authors")),
                price=float(row.get("price", 0.0) or 0.0),
                average_rating=float(row.get("average_rating", 0.0) or 0.0),
            )
        )

    return catalog


def _build_interactions_from_dataframe(df: pd.DataFrame) -> list[TrainInteraction]:
    required = {"user_id", "game_id", "rating"}
    if not required.issubset(df.columns):
        return []

    rows = df.to_dict(orient="records")
    interactions: list[TrainInteraction] = []

    for row in rows:
        rating = float(row.get("rating", 0.0) or 0.0)
        interactions.append(
            TrainInteraction(
                user_id=str(row.get("user_id")),
                game_id=str(row.get("game_id")),
                rating=rating,
            )
        )

    return interactions


def _parse_list_field(value) -> list[str]:
    if value is None:
        return []

    text = str(value).strip()
    if not text or text.lower() == "nan":
        return []

    separator = "|" if "|" in text else ","
    return [item.strip() for item in text.split(separator) if item.strip()]
