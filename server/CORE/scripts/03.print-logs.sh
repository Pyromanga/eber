echo "🔹 Server Logs 🔹"
          if [ -f server/CORE/server.log ]; then
            cat server/CORE/server.log
          else
            echo "No server.log found."
          fi