// server/CORE/handlers/echo.js
const logger = require('../utils/logger');

module.exports = function echo(req, res) {
  let body = '';
  req.on('data', chunk => body += chunk);
  req.on('end', () => {
    logger.info(`Handling POST /echo with body: ${body}`);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ received: JSON.parse(body) }));
  });
};
