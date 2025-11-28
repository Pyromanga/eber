const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');

function request({ method = 'GET', url, body = null, headers = {} }) {
  return new Promise((resolve, reject) => {
    const parsedUrl = new URL(url);
    const lib = parsedUrl.protocol === 'https:' ? https : http;

    const options = {
      hostname: parsedUrl.hostname,
      port: parsedUrl.port || (parsedUrl.protocol === 'https:' ? 443 : 80),
      path: parsedUrl.pathname,
      method,
      headers: {
        ...headers
      }
    };

    // Automatisch Self-Signed Cert anhängen, falls vorhanden
    if (parsedUrl.protocol === 'https:') {
      const certPath = path.resolve(__dirname, '../certs/server.crt');
      if (fs.existsSync(certPath)) {
        options.ca = fs.readFileSync(certPath);
      } else {
        // fallback, falls kein Cert vorhanden, z.B. -k
        options.rejectUnauthorized = false;
      }
    }

    const req = lib.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve({ status: res.statusCode, data }));
    });

    req.on('error', reject);

    if (body) {
      const payload = typeof body === 'string' ? body : JSON.stringify(body);
      req.write(payload);
    }

    req.end();
  });
}

module.exports = { request };
