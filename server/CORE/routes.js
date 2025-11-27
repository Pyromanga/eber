// server/CORE/routes.js
const router = require('./utils/router');
const homeHandler = require('./handlers/home');
const echoHandler = require('./handlers/echo');

module.exports = function registerRoutes() {
  router.register("GET", "/", homeHandler);
  router.register("POST", "/echo", echoHandler);
};
