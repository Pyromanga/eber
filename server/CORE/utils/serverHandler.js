// server/CORE/utils/serverHandler.js
const logger = require('./logger');
const router = require('./router');

function serverHandler(req, res) {
  const { method, url } = req;

  logger.info(`Request: ${method} ${url}`);

  const handler = router.getRoute(method, url);

  if (!handler) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    return res.end('Not Found');
  }

  handler(req, res);
}

module.exports = serverHandler;
