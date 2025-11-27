// server.js
const express = require('express');
const app = express();

const PORT = 3000;

app.get('/', (req, res) => {
  console.log("Request received at /");
  res.send('Hello from GitHub Actions!');
});

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
