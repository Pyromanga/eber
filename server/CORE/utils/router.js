// server/CORE/utils/router.js

const logger = require('./logger');

const routes = {
  GET: {},
  POST: {},
  PUT: {},
  DELETE: {}
};

function register(method, path, handler) {
  method = method.toUpperCase();
  if (!routes[method]) routes[method] = {};
  routes[method][path] = handler;
  logger.info(`Route registered: ${method} ${path}`);
}

function getRoute(method, path) {
  method = method.toUpperCase();
  return routes[method]?.[path] || null;
}

module.exports = { register, getRoute };
