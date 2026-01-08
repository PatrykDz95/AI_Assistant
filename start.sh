#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   AI Assistant - Startup Script${NC}"
echo -e "${BLUE}========================================${NC}"

# Change to project root directory
cd "$(dirname "$0")"

# Function to check if a port is in use
check_port() {
    lsof -i :$1 > /dev/null 2>&1
    return $?
}

# Function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local check_command=$2
    local max_wait=$3
    local wait_time=0

    echo -e "${YELLOW} Waiting for $service_name to be ready...${NC}"

    while ! eval $check_command > /dev/null 2>&1; do
        if [ $wait_time -ge $max_wait ]; then
            echo -e "${RED} $service_name failed to start within ${max_wait}s${NC}"
            return 1
        fi
        sleep 2
        wait_time=$((wait_time + 2))
        echo -e "${YELLOW}   ... still waiting (${wait_time}s)${NC}"
    done

    echo -e "${GREEN} $service_name is ready!${NC}"
    return 0
}

# Step 1: Start Docker services
echo -e "\n${BLUE} Step 1: Starting Docker services...${NC}"

if docker compose -f docker/stack.yml ps | grep -q "Up"; then
    echo -e "${YELLOW} Docker services already running. Restarting...${NC}"
    docker compose -f docker/stack.yml down
fi

docker compose -f docker/stack.yml up -d

# Step 2: Wait for PostgreSQL
wait_for_service "PostgreSQL" "docker exec aiassistant-postgres pg_isready -U postgres" 60 || exit 1

# Step 3: Wait for Ollama
wait_for_service "Ollama" "curl -s http://localhost:11434/api/tags" 30 || exit 1

# Step 4: Pull Ollama models if not present
echo -e "\n${BLUE} Step 2: Checking Ollama models...${NC}"

echo -e "${YELLOW} Checking qwen2.5:1.5b model...${NC}"
if docker exec aiassistant-ollama ollama list | grep -q "qwen2.5:1.5b"; then
    echo -e "${GREEN} qwen2.5:1.5b already available${NC}"
else
    echo -e "${YELLOW} Pulling qwen2.5:1.5b (this may take a few minutes)...${NC}"
    docker exec aiassistant-ollama ollama pull qwen2.5:1.5b
fi

echo -e "${YELLOW} Checking nomic-embed-text model...${NC}"
if docker exec aiassistant-ollama ollama list | grep -q "nomic-embed-text"; then
    echo -e "${GREEN} nomic-embed-text already available${NC}"
else
    echo -e "${YELLOW} Pulling nomic-embed-text (this may take a few minutes)...${NC}"
    docker exec aiassistant-ollama ollama pull nomic-embed-text
fi

# Step 5: Setup MCP Weather Server
echo -e "\n${BLUE}  Step 3: Setting up MCP Weather Server...${NC}"
cd mcp-weather-http

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}  No .env file found. Creating template...${NC}"
    echo "OPENWEATHER_API_KEY=your_api_key_here" > .env
    echo -e "${RED} Please add your OpenWeather API key to mcp-weather-http/.env${NC}"
    echo -e "${YELLOW}   Get your free API key from: https://openweathermap.org/api${NC}"
    echo -e "${YELLOW}   Then run this script again.${NC}"
    exit 1
fi

# Check if API key is set
if grep -q "your_api_key_here" .env; then
    echo -e "${RED} Please set your OpenWeather API key in mcp-weather-http/.env${NC}"
    echo -e "${YELLOW}   Get your free API key from: https://openweathermap.org/api${NC}"
    exit 1
fi

# Create virtual environment if it doesn't exist
if [ ! -d ".venv" ]; then
    echo -e "${YELLOW} Creating Python virtual environment...${NC}"

    # Find a usable python executable (try python3, python, then py -3)
    PYTHON_CMD=""
    if command -v python3 >/dev/null 2>&1; then
        PYTHON_CMD=python3
    elif command -v python >/dev/null 2>&1; then
        PYTHON_CMD=python
    elif command -v py >/dev/null 2>&1; then
        # Use the Windows Python launcher with -3 to prefer Python 3
        PYTHON_CMD="py -3"
    fi

    if [ -z "$PYTHON_CMD" ]; then
        echo -e "${RED} Python 3 is not installed or not found in PATH.${NC}"
        echo -e "${YELLOW} Please install Python 3 and re-run this script. On Windows, check 'Add Python to PATH' during installation.${NC}"
        exit 1
    fi

    # Create venv using the discovered python command
    eval "$PYTHON_CMD -m venv .venv"
fi

# Activate virtual environment and install dependencies
echo -e "${YELLOW} Installing Python dependencies...${NC}"
# Support both Unix-style and Windows-style venv layouts
if [ -f ".venv/bin/activate" ]; then
    # Unix-like (Linux, macOS)
    source .venv/bin/activate
elif [ -f ".venv/Scripts/activate" ]; then
    # Git Bash / MSYS on Windows provides a POSIX-style shell and the venv includes a Scripts/activate
    source .venv/Scripts/activate
else
    echo -e "${YELLOW} Virtual environment activation script not found (.venv/bin/activate or .venv/Scripts/activate).${NC}"
    echo -e "${YELLOW} Attempting to proceed using python from the detected environment...${NC}"
    # Try to use the venv python directly if it exists
    if [ -f ".venv/Scripts/python.exe" ]; then
        VENV_PY=".venv/Scripts/python.exe"
    elif [ -f ".venv/bin/python" ]; then
        VENV_PY=".venv/bin/python"
    else
        VENV_PY=""
    fi

    if [ -z "$VENV_PY" ]; then
        echo -e "${RED} Could not find a Python interpreter inside .venv. You may need to create the venv manually or run this script inside WSL/Git Bash.${NC}"
        exit 1
    fi
    # Use the venv's python to upgrade pip and install deps
    "$VENV_PY" -m pip install -q --upgrade pip
    "$VENV_PY" -m pip install -q fastapi uvicorn httpx python-dotenv
fi

# If we activated via source, use pip directly
if command -v pip >/dev/null 2>&1; then
    pip install -q --upgrade pip
    pip install -q fastapi uvicorn httpx python-dotenv
fi

# Check if MCP server is already running
if check_port 3333; then
    echo -e "${YELLOW}  Port 3333 is already in use. Stopping existing MCP server...${NC}"
    pkill -f "uvicorn mcp_server:app" || true
    sleep 2
fi

# Start MCP server in background
echo -e "${YELLOW} Starting MCP Weather Server on port 3333...${NC}"
cd src
nohup python -m uvicorn mcp_server:app --port 3333 > ../../mcp-server.log 2>&1 &
MCP_PID=$!
echo $MCP_PID > ../../mcp-server.pid
cd ..

cd ..

# Wait for MCP server to be ready
wait_for_service "MCP Weather Server" "curl -s http://localhost:3333/mcp" 15 || {
    echo -e "${RED} MCP server failed to start. Check mcp-server.log for details.${NC}"
    exit 1
}

# Step 6: Display status
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN} All services are running!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN} Service Status:${NC}"
echo -e "  PostgreSQL:    ${GREEN}http://localhost:5432${NC}"
echo -e " ️  PgAdmin:       ${GREEN}http://localhost:5050${NC} (admin@admin.com / admin)"
echo -e "  Ollama:        ${GREEN}http://localhost:11434${NC}"
echo -e " ️  MCP Weather:   ${GREEN}http://localhost:3333/mcp${NC}"
echo ""
echo -e "${YELLOW} Logs:${NC}"
echo -e "  Docker services:  ${YELLOW}docker compose -f docker/stack.yml logs -f${NC}"
echo -e "  MCP server:       ${YELLOW}tail -f mcp-server.log${NC}"
echo ""
echo -e "${YELLOW} To stop all services:${NC}"
echo -e "  ${YELLOW}./stop.sh${NC}"
echo ""
echo -e "${GREEN} You can now run your Spring Boot application!${NC}"
echo -e "  ${YELLOW}./gradlew bootRun${NC}"
echo -e "  or run from your IDE"
echo ""

