#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   AI Assistant - Shutdown Script${NC}"
echo -e "${BLUE}========================================${NC}"

# Change to project root directory
cd "$(dirname "$0")"

# Stop MCP Weather Server
echo -e "\n${YELLOW} Stopping MCP Weather Server...${NC}"
if [ -f mcp-server.pid ]; then
    MCP_PID=$(cat mcp-server.pid)
    if ps -p $MCP_PID > /dev/null 2>&1; then
        kill $MCP_PID
        echo -e "${GREEN} MCP Weather Server stopped${NC}"
    else
        echo -e "${YELLOW} MCP Weather Server was not running${NC}"
    fi
    rm mcp-server.pid
else
    # Try to kill by process name as fallback
    pkill -f "uvicorn mcp_server:app" && echo -e "${GREEN} MCP Weather Server stopped${NC}" || echo -e "${YELLOW}  MCP Weather Server was not running${NC}"
fi

# Stop Docker services
echo -e "\n${YELLOW} Stopping Docker services...${NC}"
if docker compose -f docker/stack.yml ps | grep -q "Up"; then
    docker compose -f docker/stack.yml down
    echo -e "${GREEN} Docker services stopped${NC}"
else
    echo -e "${YELLOW} Docker services were not running${NC}"
fi

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN} All services stopped!${NC}"
echo -e "${BLUE}========================================${NC}"

