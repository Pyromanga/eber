#!/bin/bash
# init.sh (oder beliebiger Name): Dispatcher für alle Scripts

# Name dieses Scripts
THIS_SCRIPT="$(basename "$0")"

# Verzeichnis, in dem das Script liegt
SCRIPTS_DIR="$(dirname "$0")"

echo "Running all scripts in $SCRIPTS_DIR, skipping $THIS_SCRIPT..."

# Schleife über alle .sh Dateien im Ordner
for script in "$SCRIPTS_DIR"/*.sh; do
  # Script überspringen, falls es das aktuelle ist
  if [[ "$(basename "$script")" != "$THIS_SCRIPT" ]]; then
    echo "Executing $script..."
    bash "$script"
  fi
done

echo "All scripts executed."
