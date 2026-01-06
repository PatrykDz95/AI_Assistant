#!/bin/sh
set -e

ollama serve &

echo "Waiting for Ollama"
sleep 5

echo "Pulling models"
ollama pull qwen2.5:1.5b # 600MB or qwen3:4b (2.5GB)
ollama pull nomic-embed-text

echo "Models ready"
wait
