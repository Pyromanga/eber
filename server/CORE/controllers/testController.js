// server/CORE/controllers/testController.js
const logger = require('../utils/logger');
const { request } = require('../utils/httpClient');

const SERVER_URL = 'http://localhost:3000';

async function runTests() {
  logger.info('Starting tests...');

  const tests = [
    {
      name: 'GET /',
      fn: async () => {
        const res = await request({ method: 'GET', url: `${SERVER_URL}/` });
        return res.data === 'Hello from the server!';
      }
    },
    {
      name: 'POST /echo',
      fn: async () => {
        const res = await request({
          method: 'POST',
          url: `${SERVER_URL}/echo`,
          body: { msg: 'test' },
          headers: { 'Content-Type': 'application/json' }
        });
        return JSON.parse(res.data).received.msg === 'test';
      }
    }
  ];

  for (const test of tests) {
    try {
      const result = await test.fn();
      if (result) {
        logger.info(`${test.name}: PASSED`);
      } else {
        logger.info(`${test.name}: FAILED`);
      }
    } catch (err) {
      logger.info(`${test.name}: ERROR - ${err.message}`);
    }
  }

  logger.info('All tests finished.');
}

if (require.main === module) runTests();

module.exports = { runTests };
