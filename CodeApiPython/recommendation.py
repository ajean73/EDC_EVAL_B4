from __future__ import annotations

from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, Tuple

import joblib
import numpy as np
import pandas as pd
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import MultiLabelBinarizer, StandardScaler

from models import (
    GameCatalogItem,
    PredictRequest,
    PredictResponse,
    RecommendationItem,
    TrainRequest,
    TrainResponse,
)


class KNNRecommender:
    def __init__(self, artifacts_dir: str = "artifacts"):
        self.artifacts_dir = Path(artifacts_dir)
        self.artifacts_dir.mkdir(parents=True, exist_ok=True)
        self.artifacts_file = self.artifacts_dir / "knn_model.joblib"

        self.model: NearestNeighbors | None = None
        self.scaler: StandardScaler | None = None
        self.category_encoder: MultiLabelBinarizer | None = None
        self.author_encoder: MultiLabelBinarizer | None = None
        self.publisher_columns: List[str] = []
        self.feature_matrix: np.ndarray | None = None
        self.game_ids: List[str] = []
        self.catalog_by_id: Dict[str, GameCatalogItem] = {}
        self.trained_at: str = ""

        self._load_if_exists()

    def train(self, request: TrainRequest) -> TrainResponse:
        if len(request.catalog) < 2:
            raise ValueError("Training requires at least 2 games in catalog")

        self.game_ids = [game.game_id for game in request.catalog]
        self.catalog_by_id = {game.game_id: game for game in request.catalog}

        raw_matrix, self.category_encoder, self.author_encoder, self.publisher_columns = self._build_feature_matrix(
            request.catalog
        )

        self.scaler = StandardScaler()
        self.feature_matrix = self.scaler.fit_transform(raw_matrix)

        neighbors = max(2, min(request.n_neighbors, len(request.catalog)))
        self.model = NearestNeighbors(n_neighbors=neighbors, metric="cosine")
        self.model.fit(self.feature_matrix)
        self.trained_at = datetime.now(timezone.utc).isoformat()

        self._save()

        return TrainResponse(
            trained=True,
            games_count=len(request.catalog),
            interactions_count=len(request.interactions),
            trained_at=self.trained_at,
        )

    def predict(self, request: PredictRequest) -> PredictResponse:
        if self.model is None or self.feature_matrix is None:
            return PredictResponse(
                user_id=request.user_data.user_id,
                model_trained=False,
                recommendations=[],
            )

        purchased = request.user_data.purchases
        if not purchased:
            recommendations = self._fallback_top_rated(request.top_k)
            return PredictResponse(
                user_id=request.user_data.user_id,
                model_trained=True,
                recommendations=recommendations,
            )

        purchased_ids = {purchase.game_id for purchase in purchased}
        scores: Dict[str, float] = {}

        for purchase in purchased:
            if purchase.game_id not in self.catalog_by_id:
                continue

            game_index = self.game_ids.index(purchase.game_id)
            game_vector = self.feature_matrix[game_index].reshape(1, -1)
            distances, indices = self.model.kneighbors(game_vector)

            for distance, neighbor_index in zip(distances[0], indices[0]):
                candidate_game_id = self.game_ids[neighbor_index]
                if candidate_game_id in purchased_ids:
                    continue

                raw_similarity = 1.0 - float(distance)
                similarity = max(0.0, raw_similarity)
                rating_weight = max(0.5, purchase.rating)
                scores[candidate_game_id] = scores.get(candidate_game_id, 0.0) + similarity * rating_weight

        if not scores:
            recommendations = self._fallback_top_rated(request.top_k, excluded_ids=purchased_ids)
            return PredictResponse(
                user_id=request.user_data.user_id,
                model_trained=True,
                recommendations=recommendations,
            )

        ranked = sorted(scores.items(), key=lambda item: item[1], reverse=True)[: request.top_k]
        recommendations = [
            RecommendationItem(
                game_id=game_id,
                score=round(score, 4),
                reason="similar_to_purchase_history",
            )
            for game_id, score in ranked
        ]

        return PredictResponse(
            user_id=request.user_data.user_id,
            model_trained=True,
            recommendations=recommendations,
        )

    def _fallback_top_rated(self, top_k: int, excluded_ids: set[str] | None = None) -> List[RecommendationItem]:
        excluded_ids = excluded_ids or set()
        ranked_catalog = sorted(
            self.catalog_by_id.values(),
            key=lambda game: (game.average_rating, game.price),
            reverse=True,
        )

        items: List[RecommendationItem] = []
        for game in ranked_catalog:
            if game.game_id in excluded_ids:
                continue
            items.append(
                RecommendationItem(
                    game_id=game.game_id,
                    score=round(game.average_rating, 4),
                    reason="top_rated_fallback",
                )
            )
            if len(items) >= top_k:
                break

        return items

    def _build_feature_matrix(
        self, catalog: List[GameCatalogItem]
    ) -> Tuple[np.ndarray, MultiLabelBinarizer, MultiLabelBinarizer, List[str]]:
        categories = [item.categories for item in catalog]
        authors = [item.authors for item in catalog]

        category_encoder = MultiLabelBinarizer()
        category_matrix = category_encoder.fit_transform(categories)

        author_encoder = MultiLabelBinarizer()
        author_matrix = author_encoder.fit_transform(authors)

        publisher_frame = pd.get_dummies(pd.Series([item.publisher for item in catalog]), dtype=float)
        publisher_columns = list(publisher_frame.columns)

        numeric_matrix = np.array(
            [[float(item.price), float(item.average_rating)] for item in catalog], dtype=float
        )

        feature_matrix = np.hstack(
            [
                numeric_matrix,
                publisher_frame.to_numpy(dtype=float),
                category_matrix.astype(float),
                author_matrix.astype(float),
            ]
        )

        return feature_matrix, category_encoder, author_encoder, publisher_columns

    def _save(self) -> None:
        payload = {
            "model": self.model,
            "scaler": self.scaler,
            "category_encoder": self.category_encoder,
            "author_encoder": self.author_encoder,
            "publisher_columns": self.publisher_columns,
            "feature_matrix": self.feature_matrix,
            "game_ids": self.game_ids,
            "catalog_by_id": self.catalog_by_id,
            "trained_at": self.trained_at,
        }
        joblib.dump(payload, self.artifacts_file)

    def _load_if_exists(self) -> None:
        if not self.artifacts_file.exists():
            return

        payload = joblib.load(self.artifacts_file)
        self.model = payload.get("model")
        self.scaler = payload.get("scaler")
        self.category_encoder = payload.get("category_encoder")
        self.author_encoder = payload.get("author_encoder")
        self.publisher_columns = payload.get("publisher_columns", [])
        self.feature_matrix = payload.get("feature_matrix")
        self.game_ids = payload.get("game_ids", [])
        self.catalog_by_id = payload.get("catalog_by_id", {})
        self.trained_at = payload.get("trained_at", "")
