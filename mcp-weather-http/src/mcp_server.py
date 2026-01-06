# Run:
#   add OPENWEATHER_API_KEY to .env file
#   python3 -m venv .venv
#   source .venv/bin/activate
#   pip install fastapi uvicorn httpx python-dotenv
#   python -m uvicorn mcp_server:app --port 3333
#
# Test:
#   curl -s http://localhost:3333/mcp -H 'content-type: application/json' \
#     -d '{"jsonrpc":"2.0","id":"3","method":"tools/call","params":{"name":"get_current_weather","arguments":{"city":"Warsaw","units":"metric"}}}'

from __future__ import annotations

import httpx
import os
import time
from dotenv import load_dotenv
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from typing import Any, Dict, Optional, Tuple

load_dotenv()

# ----------------------------
# Config
# ----------------------------
OPENWEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY")

if not OPENWEATHER_API_KEY:
    raise RuntimeError("OPENWEATHER_API_KEY is not set")

OPENWEATHER_BASE = "https://api.openweathermap.org/data/2.5"

# Basic, in-memory TTL cache to reduce API usage (optional, but helpful)
CACHE_TTL_SECONDS = int(os.getenv("WEATHER_CACHE_TTL_SECONDS", "120"))
_cache: Dict[Tuple[str, str], Tuple[float, Dict[str, Any]]] = {}  # (city, units) -> (expires_at, data)

MCP_PROTOCOL_VERSION = "2026-01-01"

app = FastAPI(title="OpenWeather MCP Server", version="1.0.0")


# ----------------------------
# JSON-RPC helpers
# ----------------------------
def rpc_result(req_id: Any, result: Any) -> Dict[str, Any]:
    return {"jsonrpc": "2.0", "id": req_id, "result": result}


def rpc_error(req_id: Any, code: int, message: str, data: Any = None) -> Dict[str, Any]:
    err: Dict[str, Any] = {"code": code, "message": message}
    if data is not None:
        err["data"] = data
    return {"jsonrpc": "2.0", "id": req_id, "error": err}


def require_object(value: Any, name: str) -> Dict[str, Any]:
    if not isinstance(value, dict):
        raise ValueError(f"{name} must be an object")
    return value


def require_str(value: Any, name: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{name} must be a non-empty string")
    return value.strip()


def normalize_units(units: Any) -> str:
    if units is None:
        return "metric"
    if units not in ("metric", "imperial"):
        raise ValueError("units must be 'metric' or 'imperial'")
    return units


# ----------------------------
# OpenWeather client
# ----------------------------
async def fetch_current_weather(city: str, units: str) -> Dict[str, Any]:
    key = (city.lower(), units)
    now = time.time()

    # Cache hit
    hit = _cache.get(key)
    if hit and hit[0] > now:
        return hit[1]

    url = f"{OPENWEATHER_BASE}/weather"
    params = {"q": city, "units": units, "appid": OPENWEATHER_API_KEY}

    async with httpx.AsyncClient(timeout=10.0) as client:
        r = await client.get(url, params=params)

    # OpenWeather error handling
    if r.status_code != 200:
        # OpenWeather usually returns a JSON body with message
        try:
            payload = r.json()
            msg = payload.get("message") or r.text
        except Exception:
            msg = r.text
        raise RuntimeError(f"OpenWeather error {r.status_code}: {msg}")

    data = r.json()

    # Normalize output for your MCP clients
    result = {
        "city": data.get("name"),
        "country": data.get("sys", {}).get("country"),
        "temperature": data.get("main", {}).get("temp"),
        "feels_like": data.get("main", {}).get("feels_like"),
        "humidity": data.get("main", {}).get("humidity"),
        "pressure": data.get("main", {}).get("pressure"),
        "weather": (data.get("weather") or [{}])[0].get("description"),
        "wind_speed": data.get("wind", {}).get("speed"),
        "units": units,
        "source": "openweathermap",
    }

    # Cache store
    _cache[key] = (now + CACHE_TTL_SECONDS, result)
    return result


# ----------------------------
# MCP tools metadata
# ----------------------------
TOOLS = [
    {
        "name": "get_current_weather",
        "description": "Get current weather by city name using OpenWeatherMap",
        "inputSchema": {
            "type": "object",
            "properties": {
                "city": {"type": "string", "description": "City name, e.g. Warsaw"},
                "units": {
                    "type": "string",
                    "enum": ["metric", "imperial"],
                    "default": "metric",
                    "description": "Units for temperature/wind",
                },
            },
            "required": ["city"],
            "additionalProperties": False,
        },
    }
]


# ----------------------------
# MCP endpoint
# ----------------------------
@app.post("/mcp")
async def mcp_endpoint(request: Request):
    try:
        body = await request.json()
        body = require_object(body, "request")

        req_id = body.get("id")
        method = body.get("method")
        params = body.get("params", {})

        if method is None:
            return JSONResponse(content=rpc_error(req_id, -32600, "Invalid Request: missing method"))

        # MCP: initialize
        if method == "initialize":
            # params may include clientInfo, capabilities; we ignore safely
            return rpc_result(
                req_id,
                {
                    "protocolVersion": MCP_PROTOCOL_VERSION,
                    "serverInfo": {"name": "openweathermap-mcp", "version": "1.0.0"},
                    "capabilities": {"tools": {}},
                },
            )

        # MCP: tools/list
        if method == "tools/list":
            return rpc_result(req_id, {"tools": TOOLS})

        # MCP: tools/call
        if method == "tools/call":
            params = require_object(params, "params")
            tool_name = params.get("name")
            args = params.get("arguments", {})

            if tool_name != "get_current_weather":
                return JSONResponse(content=rpc_error(req_id, -32601, f"Unknown tool: {tool_name}"))

            args = require_object(args, "arguments")
            city = require_str(args.get("city"), "city")
            units = normalize_units(args.get("units"))

            data = await fetch_current_weather(city=city, units=units)
            return rpc_result(req_id, data)

        # Unknown method
        return JSONResponse(content=rpc_error(req_id, -32601, f"Method not found: {method}"))

    except ValueError as ve:
        # bad params / validation
        return JSONResponse(content=rpc_error(None, -32602, "Invalid params", str(ve)))
    except Exception as e:
        # internal error
        return JSONResponse(content=rpc_error(None, -32603, "Internal error", str(e)))


# Optional: simple health endpoint
@app.get("/health")
async def health():
    return {"status": "ok"}
