// server/CORE/server.js
const http = require('http');
const logger = require('./utils/logger');

const port = 3000;

const server = http.createServer((req, res) => {
  logger.info(`Request received: ${req.method} ${req.url}`);

  if (req.method === 'GET' && req.url === '/') {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('Hello from Node HTTP server!');
  } else if (req.method === 'POST' && req.url === '/echo') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ received: JSON.parse(body) }));
      logger.info(`POST /echo with body: ${body}`);
    });
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Not found');
  }
});

server.listen(port, () => {
  logger.info(`Server running at http://localhost:${port}`);
});
