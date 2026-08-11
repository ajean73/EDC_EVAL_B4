from fastapi import FastAPI, HTTPException

from data_loader import build_training_data_from_csv_or_fallback
from models import PredictRequest, PredictResponse, TrainRequest, TrainResponse, UserData
from recommendation import KNNRecommender

app = FastAPI()
recommender = KNNRecommender()


@app.get("/")
async def root():
    return {
        "message": "API de recommandation KNN en ligne",
        "model_trained": recommender.model is not None,
    }


@app.post("/recommendations/train", response_model=TrainResponse)
async def train_recommendation_model(request: TrainRequest):
    try:
        return recommender.train(request)
    except ValueError as ex:
        raise HTTPException(status_code=400, detail=str(ex)) from ex
    except Exception as ex:
        raise HTTPException(status_code=500, detail=str(ex)) from ex


@app.post("/recommendations/train/csv", response_model=TrainResponse)
async def train_csv_model(csv_path: str = "training_data.csv", n_neighbors: int = 5):
    try:
        request = build_training_data_from_csv_or_fallback(csv_path, n_neighbors=n_neighbors)
        return recommender.train(request)
    except Exception as ex:
        raise HTTPException(status_code=500, detail=str(ex)) from ex


@app.post("/recommendations/predict", response_model=PredictResponse)
async def predict_recommendations(request: PredictRequest):
    try:
        return recommender.predict(request)
    except ValueError as ex:
        raise HTTPException(status_code=400, detail=str(ex)) from ex
    except Exception as ex:
        raise HTTPException(status_code=500, detail=str(ex)) from ex