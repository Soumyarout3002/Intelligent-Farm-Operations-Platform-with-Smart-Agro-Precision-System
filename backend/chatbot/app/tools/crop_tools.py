from app.services.spring_api_client import spring_get


async def get_my_crops(token: str):
    farms = await spring_get("/api/farms", token)

    if not farms:
        return []

    registered_crops = []

    for farm in farms:
        farm_id = farm["id"]

        plots = await spring_get(
            f"/api/farms/{farm_id}/plots",
            token
        )

        for plot in plots:
            plot_id = plot["id"]

            crop_cycles = await spring_get(
                f"/api/farms/{farm_id}/plots/{plot_id}/crop-cycles",
                token
            )

            for cycle in crop_cycles:
                registered_crops.append({
                    "farm": farm,
                    "plot": plot,
                    "crop_cycle": cycle
                })

    return registered_crops


async def get_crop_cycles(farm_id: int, plot_id: int, token: str):
    return await spring_get(
        f"/api/farms/{farm_id}/plots/{plot_id}/crop-cycles",
        token
    )


async def get_crop_cycle_details(
    farm_id: int,
    plot_id: int,
    cycle_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/plots/{plot_id}/crop-cycles/{cycle_id}",
        token
    )


async def get_growth_stages(
    farm_id: int,
    plot_id: int,
    cycle_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/plots/{plot_id}/crop-cycles/{cycle_id}/growth-stages",
        token
    )


async def get_crop_activities(
    farm_id: int,
    plot_id: int,
    cycle_id: int,
    token: str
):
    return await spring_get(
        f"/api/farms/{farm_id}/plots/{plot_id}/crop-cycles/{cycle_id}/activities",
        token
    )