"""File containing logger."""

import logging
import time

# Set log file, example: 17102017-152501
log_file = "logs/" + time.strftime("%d%m%Y-%H%M%S") + ".log"

try:
    file = open(log_file, 'r')
except IOError:
    file = open(log_file, 'w')

file.close()

# Create logger instance
logger = logging.getLogger("raspberry")
logger.setLevel(logging.DEBUG)
# Create file handle
fh = logging.FileHandler(log_file)
fh.setLevel(logging.DEBUG)
# Create formatter
formatter = logging.Formatter(
    '%(asctime)s - %(name)s - %(levelname)s - %(message)s')
fh.setFormatter(formatter)
# Attach file handler to logger
logger.addHandler(fh)
