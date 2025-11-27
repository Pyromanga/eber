const express = require('express');
const logger = require('./logger');

const app = express();
const port = 3000;

app.get('/', (req, res) => {
  logger.info("GET / was requested");
  res.send("Hello from GitHub Actions!");
});

app.listen(port, () => {
  logger.info(`Server running on http://localhost:${port}`);
});
