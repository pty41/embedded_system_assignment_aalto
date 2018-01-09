"""This file contains code for polling servo values from Arduino."""

import serial
import time
import datetime
import logging
import csv
import threading


class APIThread(threading.Thread):
    """Class for communication between arduino and RPi."""

    def __init__(self):
        """Initialize thread."""
        threading.Thread.__init__(self)
        self.ser = serial.Serial(
                       port='/dev/ttyACM0',
                       baudrate=9600,
                       timeout=1
                    )
        # create logger
        self.logger = logging.getLogger('raspberry.APIThread')
        self.logger.info('initializing API module')
        # stop request event
        self.stoprequest = threading.Event()

    def run(self):
        """Run thread."""
        self.logger.info("start API thread")
        # set csv file name
        time_str = time.strftime("%d%m%Y-%H%M%S")
        data_buffer = "buffer/labels_" + time_str + ".csv"
        self.logger.info("data buffer set")
        # open csv file
        with open(data_buffer, 'w') as buffer_file:
            self.logger.info("buffer file open")
            writer = csv.writer(buffer_file, delimiter=';')
            writer.writerow(["timestamp"] + ["servovalue"])
            self.logger.info("wrote header to csv file: " + data_buffer)
            while not self.stoprequest.isSet():
                self.ser.write(b'r')
                value = self.ser.read(1)
                if value:
                    val = str(value, 'utf-8')
                    self.ser.flush()
                    self.logger.info('read value from arduino: ' + val)
                    # get timestamp value
                    timestamp = int(datetime.datetime.now().timestamp()*1000)
                    # write data to csv
                    writer.writerow([str(timestamp)] + [str(value)])
                else:
                    self.logger.error('failed to read value from serial')
            self.logger.info("api thread was stopped")

    def join(self, timeout=None):
        """Stop polling serial."""
        self.logger.info("stopping API thread")
        self.stoprequest.set()
        threading.Thread.join(self)
