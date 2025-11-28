#!/bin/bash

if [ -f server.pid ]; then
  PID=$(cat server.pid)
  if ps -p $PID > /dev/null; then
    kill $PID
    echo "Server stopped."
  else
    echo "Server process already exited."
  fi
fi