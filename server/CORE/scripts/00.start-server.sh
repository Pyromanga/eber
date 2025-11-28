#!/bin/bash
nohup node server/CORE/server.js > server.log 2>&1 &
echo $! > server.pid

