from typing import List
from pydantic import BaseModel, Field


class GameCatalogItem(BaseModel):
    game_id: str
    title: str
    publisher: str = "unknown"
    categories: List[str] = Field(default_factory=list)
    authors: List[str] = Field(default_factory=list)
    price: float = 0.0
    average_rating: float = 0.0


class TrainInteraction(BaseModel):
    user_id: str
    game_id: str
    rating: float = 0.0


class TrainRequest(BaseModel):
    catalog: List[GameCatalogItem]
    interactions: List[TrainInteraction] = Field(default_factory=list)
    n_neighbors: int = 5


class UserPurchase(BaseModel):
    game_id: str
    rating: float = 0.0


class UserData(BaseModel):
    user_id: str
    purchases: List[UserPurchase] = Field(default_factory=list)


class PredictRequest(BaseModel):
    user_data: UserData
    top_k: int = 5


class RecommendationItem(BaseModel):
    game_id: str
    score: float
    reason: str


class PredictResponse(BaseModel):
    user_id: str
    model_trained: bool
    recommendations: List[RecommendationItem]


class TrainResponse(BaseModel):
    trained: bool
    games_count: int
    interactions_count: int
    trained_at: str
