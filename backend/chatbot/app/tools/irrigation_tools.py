
from app.services.spring_api_client import spring_get


async def get_my_irrigation(token: str):
    farms = await spring_get("/api/farms", token)

    results = []

    for farm in farms:
        farm_id = farm["id"]

        schedules = await spring_get(
            f"/api/farms/{farm_id}/irrigation/schedules",
            token
        )

        suggestions = await spring_get(
            f"/api/farms/{farm_id}/irrigation/suggestions",
            token
        )

        results.append({
            "farm": farm,
            "schedules": schedules,
            "suggestions": suggestions
        })

    return results


async def get_irrigation_schedule(
    farm_id: int,
    schedule_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/irrigation/schedules/{schedule_id}",
        token
    )


async def get_irrigation_history(
    farm_id: int,
    schedule_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/irrigation/schedules/{schedule_id}/records",
        token
    )
