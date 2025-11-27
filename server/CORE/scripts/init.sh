#!/bin/bash

THIS_SCRIPT="$(basename "$0")"
SCRIPTS_DIR="$(dirname "$0")"

echo "Running all scripts in $SCRIPTS_DIR, skipping $THIS_SCRIPT..."

for script in "$SCRIPTS_DIR"/*.sh; do
  if [[ "$(basename "$script")" != "$THIS_SCRIPT" ]]; then
    echo "Executing $script..."
    bash "$script"
  fi
done

echo "All scripts executed."
