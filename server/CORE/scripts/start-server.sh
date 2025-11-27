#!/bin/bash

node server.js &
echo $! > server.pid
sleep 2
