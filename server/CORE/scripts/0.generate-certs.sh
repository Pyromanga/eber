#!/bin/bash

CERT_DIR="server/CORE/certs"

mkdir -p "$CERT_DIR"

# Falls existieren: überspringen
if [[ -f "$CERT_DIR/server.key" && -f "$CERT_DIR/server.crt" ]]; then
  echo "Certificates already exist. Skipping generation."
  exit 0
fi

echo "Generating self-signed certificates..."

openssl req -x509 \
  -newkey rsa:2048 \
  -nodes \
  -keyout "$CERT_DIR/server.key" \
  -out "$CERT_DIR/server.crt" \
  -days 365 \
  -subj "/CN=localhost"

echo "Certificates generated."
