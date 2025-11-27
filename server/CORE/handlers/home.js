// server/CORE/handlers/home.js
const logger = require('../utils/logger');

module.exports = function home(req, res) {
  logger.info("Handling GET /");
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end('Hello from the server!');
};
