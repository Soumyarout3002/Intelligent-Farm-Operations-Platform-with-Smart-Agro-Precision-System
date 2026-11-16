
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware

from app.services.groq_service import generate_structured_answer

import httpx
import logging

from app.tools.farm_tools import get_my_farms
from app.tools.crop_tools import get_my_crops
from app.tools.irrigation_tools import get_my_irrigation
from app.tools.inventory_tools import get_my_inventory
from app.tools.equipment_tools import get_my_equipment
from app.tools.weather_tools import get_my_weather


logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)


app = FastAPI(
    title="FarmVerse AI Chatbot",
    description="AI assistant for farm operations",
    version="1.0.0"
)


app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class ChatRequest(BaseModel):
    message: str


@app.get("/")
async def home():
    return {"message": "FarmVerse Chatbot API is running"}


@app.get("/health")
async def health():
    return {"status": "healthy"}


@app.post("/chat")
async def chat(
    request: ChatRequest,
    authorization: str = Header(None)
):

    # Authentication
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail="Authentication required"
        )

    token = authorization.split(" ", 1)[1].strip()
    message = request.message.lower().strip()

    try:

        # Inventory
        if any(word in message for word in [
            "inventory", "stock", "available items",
            "fertilizer", "seeds", "pesticide"
        ]):
            intent = "inventory"
            data = await get_my_inventory(token)

        # Irrigation
        elif any(word in message for word in [
            "irrigation", "watering", "water schedule",
            "irrigation schedule"
        ]):
            intent = "irrigation"
            data = await get_my_irrigation(token)

        # Equipment
        elif any(word in message for word in [
            "equipment", "machinery", "tractor",
            "machine", "equipment booking"
        ]):
            intent = "equipment"
            data = await get_my_equipment(token)

        # Weather
        elif any(word in message for word in [
            "weather", "forecast", "temperature",
            "rainfall", "rain"
        ]):
            intent = "weather"
            data = await get_my_weather(token)

        # Crops
        elif any(word in message for word in [
            "crop", "crops", "growth stage",
            "crop cycle", "harvest"
        ]):
            intent = "crop"
            data = await get_my_crops(token)

        # Farms and plots
        elif any(word in message for word in [
            "farm", "farms", "plot", "plots",
            "land"
        ]):
            intent = "farm"
            data = await get_my_farms(token)

        else:
            return {
                "reply": (
                    "I can help you manage your farms, crops, "
                    "irrigation, inventory, equipment and weather. "
                    "What would you like to know?"
                ),
                "intent": "general"
            }

        logger.info("Detected chatbot intent: %s", intent)

        # Generate natural language response using Groq
        answer = await generate_structured_answer(
            question=request.message,
            intent=intent,
            data=data
        )

        return {
            "reply": answer,
            "intent": intent
        }

    except httpx.HTTPStatusError as exc:
        logger.exception("Spring Boot API returned an HTTP error")

        raise HTTPException(
            status_code=exc.response.status_code,
            detail="The farm application API could not retrieve the requested data."
        )

    except httpx.RequestError:
        logger.exception("Spring Boot API connection failed")

        raise HTTPException(
            status_code=502,
            detail="Unable to connect to the farm application."
        )

    except Exception:
        logger.exception("Chatbot processing failed")

        raise HTTPException(
            status_code=500,
            detail="Unable to process your request."
        )

