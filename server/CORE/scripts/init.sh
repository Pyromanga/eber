#!/bin/bash

THIS_SCRIPT="$(basename "$0")"
SCRIPTS_DIR="$(dirname "$0")"
CONFIG_FILE="$(dirname "$0")/../.config/env"

if [ ! -f "$CONFIG_FILE" ]; then
  echo "Config file not found at $CONFIG_FILE"
  exit 1
fi

# Load config
set -o allexport
source "$CONFIG_FILE"
set +o allexport

# Build full URL
SERVER_FULL_URL="${SERVER_PROTOCOL}://${SERVER_URL}:${SERVER_PORT}"

# Export for all child processes
export SERVER_FULL_URL

echo "🔧 Loaded configuration:"
echo "  PROTOCOL: $SERVER_PROTOCOL"
echo "  URL:      $SERVER_URL"
echo "  PORT:     $SERVER_PORT"
echo "  FULL URL: $SERVER_FULL_URL"

echo "Running all scripts in $SCRIPTS_DIR, skipping $THIS_SCRIPT..."

for script in "$SCRIPTS_DIR"/*.sh; do
  if [[ "$(basename "$script")" != "$THIS_SCRIPT" ]]; then
    echo "Executing $script..."
    bash "$script"
  fi
done

echo "All scripts executed."
