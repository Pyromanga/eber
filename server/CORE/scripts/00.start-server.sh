#!/bin/bash

node server/CORE/server.js &
echo $! > server.pid
sleep 2
