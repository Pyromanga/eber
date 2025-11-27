// server/CORE/server.js
const https = require('https');

const registerRoutes = require('./routes');
const serverHandler = require('./utils/serverHandler');
const logger = require('./utils/logger');

const server = https.createServer(serverHandler);
registerRoutes();

const SERVER_PORT = process.env.SERVER_PORT;
const SERVER_FULL_URL = process.env.SERVER_FULL_URL;

server.listen(SERVER_PORT, () => {
  logger.info(`Server running on ${SERVER_FULL_URL}`);
});

module.exports = server;
