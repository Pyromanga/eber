const colors = {
  reset: "\x1b[0m",
  blue: "\x1b[34m",
  yellow: "\x1b[33m",
  red: "\x1b[31m",
  green: "\x1b[32m"
};

const getTimestamp = () => {
  return new Date().toISOString();
};

const logger = {
  info: (msg) => {
    console.log(`${colors.blue}[INFO] ${getTimestamp()}${colors.reset} ${msg}`);
  },

  warn: (msg) => {
    console.warn(`${colors.yellow}[WARN] ${getTimestamp()}${colors.reset} ${msg}`);
  },

  error: (msg) => {
    console.error(`${colors.red}[ERROR] ${getTimestamp()}${colors.reset} ${msg}`);
  },

  success: (msg) => {
    console.log(`${colors.green}[OK] ${getTimestamp()}${colors.reset} ${msg}`);
  }
};

module.exports = logger;
