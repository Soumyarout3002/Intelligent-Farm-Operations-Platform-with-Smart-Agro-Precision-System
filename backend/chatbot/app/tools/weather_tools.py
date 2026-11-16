
from app.services.spring_api_client import spring_get


async def get_my_weather(token: str):
    farms = await spring_get("/api/farms", token)

    results = []

    for farm in farms:
        farm_id = farm["id"]

        current_weather = await spring_get(
            f"/api/farms/{farm_id}/weather/current",
            token
        )

        forecast = await spring_get(
            f"/api/farms/{farm_id}/weather/forecast",
            token
        )

        results.append({
            "farm": farm,
            "current_weather": current_weather,
            "forecast": forecast
        })

    return results
