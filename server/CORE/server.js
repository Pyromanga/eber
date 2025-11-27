// server/CORE/server.js
const https = require('https');

const registerRoutes = require('./routes');
const serverHandler = require('./utils/serverHandler');
const logger = require('./utils/logger');

const PORT = 3000;

const server = https.createServer(serverHandler);

registerRoutes();

server.listen(PORT, () => {
  logger.info(`Server running on https://localhost:${PORT}`);
});

module.exports = server;
