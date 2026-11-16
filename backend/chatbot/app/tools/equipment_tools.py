
from app.services.spring_api_client import spring_get


async def get_my_equipment(token: str):
    farms = await spring_get("/api/farms", token)

    results = []

    for farm in farms:
        farm_id = farm["id"]

        equipment = await spring_get(
            f"/api/farms/{farm_id}/equipment",
            token
        )

        results.append({
            "farm": farm,
            "equipment": equipment
        })

    return results


async def get_equipment_details(
    farm_id: int,
    equipment_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/equipment/{equipment_id}",
        token
    )


async def get_equipment_bookings(
    farm_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/equipment/bookings",
        token
    )


async def get_equipment_usage(
    farm_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/equipment/usage",
        token
    )


async def get_individual_equipment_usage(
    farm_id: int,
    equipment_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/equipment/{equipment_id}/usage",
        token
    )
