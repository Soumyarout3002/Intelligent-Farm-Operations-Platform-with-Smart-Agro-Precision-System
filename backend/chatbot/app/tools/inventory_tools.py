
from app.services.spring_api_client import spring_get


async def get_my_inventory(token: str):
    farms = await spring_get("/api/farms", token)

    results = []

    for farm in farms:
        farm_id = farm["id"]

        items = await spring_get(
            f"/api/farms/{farm_id}/inventory/items",
            token
        )

        results.append({
            "farm": farm,
            "inventory": items
        })

    return results


async def get_inventory_item(
    farm_id: int,
    item_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/inventory/items/{item_id}",
        token
    )


async def get_stock_history(
    farm_id: int,
    item_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/inventory/items/{item_id}/stock/history",
        token
    )
