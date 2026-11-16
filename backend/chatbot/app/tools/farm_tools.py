from app.services.spring_api_client import spring_get


async def get_my_farms(token: str):
    return await spring_get("/api/farms", token)


async def get_farm_details(farm_id: int, token: str):
    return await spring_get(f"/api/farms/{farm_id}", token)


async def get_farm_plots(farm_id: int, token: str):
    return await spring_get(f"/api/farms/{farm_id}/plots", token)


async def get_plot_details(farm_id: int, plot_id: int, token: str):
    return await spring_get(
        f"/api/farms/{farm_id}/plots/{plot_id}",
        token
    )