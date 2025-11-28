#!/bin/bash

# Wait until server is ready
#!/bin/bash


echo "Waiting for server to be ready..."
echo "$SERVER_FULL_URL <- server url"

for i in {1..10}; do
  if curl --silent --output /dev/null "$SERVER_FULL_URL"; then
    echo "Server is up!"
    break
  fi

  echo "Server not ready yet, retrying..."
  sleep 1
done

# Tests starten
node server/CORE/controllers/testController.js
