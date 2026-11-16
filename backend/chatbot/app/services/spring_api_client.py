import os
import httpx
from dotenv import load_dotenv

load_dotenv()

SPRING_API_BASE_URL = os.getenv(
    "SPRING_API_BASE_URL",
    "http://localhost:8080"
)


async def spring_get(endpoint: str, token: str):
    if not token:
        raise ValueError("Authentication token is missing")

    # Remove Bearer prefix if the caller already included it.
    token = token.strip()

    if token.lower().startswith("bearer "):
        token = token[7:].strip()

    url = (
        f"{SPRING_API_BASE_URL.rstrip('/')}/"
        f"{endpoint.lstrip('/')}"
    )

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/json"
    }

    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.get(url, headers=headers)

        if response.status_code == 401:
            raise Exception(
                f"Spring Boot authentication failed: {response.text}"
            )

        response.raise_for_status()
        return response.json()