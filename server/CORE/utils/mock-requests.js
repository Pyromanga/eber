const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');
const logger = require('./logger'); // falls du schon logger hast

function request({ method = 'GET', url, body = null, headers = {} }) {
  return new Promise((resolve, reject) => {
    const parsedUrl = new URL(url);
    let lib = parsedUrl.protocol === 'https:' ? https : http;

    const options = {
      hostname: parsedUrl.hostname,
      port: parsedUrl.port || (parsedUrl.protocol === 'https:' ? 443 : 80),
      path: parsedUrl.pathname,
      method,
      headers: { ...headers }
    };

    // Logging, welche Methode / Cert-Handling gewählt wird
    if (parsedUrl.protocol === 'https:') {
      const certPath = path.resolve(__dirname, '../certs/server.crt');
      if (fs.existsSync(certPath)) {
        options.ca = fs.readFileSync(certPath);
        logger.info(`HTTPS request with self-signed certificate: ${url}`);
      } else {
        options.rejectUnauthorized = false;
        logger.info(`HTTPS request with rejectUnauthorized=false: ${url}`);
      }
    } else {
      logger.info(`HTTP request (no certificate): ${url}`);
    }

    const req = lib.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve({ status: res.statusCode, data }));
    });

    req.on('error', (err) => {
      logger.info(`Request error for ${method} ${url}: ${err.message}`);
      reject(err);
    });

    if (body) {
      const payload = typeof body === 'string' ? body : JSON.stringify(body);
      req.write(payload);
    }

    req.end();
  });
}

module.exports = { request };
