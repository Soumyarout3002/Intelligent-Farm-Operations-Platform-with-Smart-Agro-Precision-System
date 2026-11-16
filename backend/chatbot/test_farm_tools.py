import asyncio
from app.tools.farm_tools import get_my_farms

async def main():
    token = input("Enter your login Bearer token: ").strip()

    try:
        result = await get_my_farms(token)
        print("\nFarm tools working!")
        print("Response:", result)

    except Exception as e:
        print("\nFarm tools test failed!")
        print("Error:", str(e))

asyncio.run(main())