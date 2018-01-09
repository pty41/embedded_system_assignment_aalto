"""This file contains code for polling servo values from Arduino."""

import serial
import time
import logging
import csv
import threading

# create logger
api_logger = logging.getLogger('raspberry.api')


class APIThread(threading.Thread):
    """Class for communication between arduino and RPi."""

    def __init__(self):
        """Init the API."""
        threading.Thread.__init__(self)
        # init serial
        self.ser = serial.Serial(
            port='/dev/ttyACM0',
            baudrate=9600,
            parity=serial.PARITY_NONE,
            stopbits=serial.STOPBITS_ONE,
            bytesize=serial.EIGHTBITS,
            timeout=0.05
        )
        # create logger
        self.logger = logging.getLogger('raspberry.api.API')
        self.logger.info('initializing API module')
        # set csv file name
        time_str = time.strftime("%d%m%Y-%H%M%S")
        self.data_buffer = "../buffer/labels_" + time_str + ".csv"
        # open csv file
        try:
            self.buffer_file = open(self.data_buffer, 'w')
        except IOError:
            self.logger.warning(
                'the data buffer file already existed so opening it')
            self.buffer_file = open(self.data_buffer, 'o')
        # init csv writer
        self.writer = csv.writer(self.buffer_file, delimiter=';')
        self.writer.writerow(["timestamp"] + ["servovalue"])
        self.logger.info("wrote header to csv file: " + self.data_buffer)
        # stop request event
        self.stoprequest = threading.Event()

    def read_servo_value(self):
        """Get servo value from the car and map it into csv with timestamp."""
        cmd = "1"
        self.logger.info("writing to serial, command: " + cmd)
        self.ser.write(cmd.encode())
        self.logger.info("serial is open")
        while not self.stoprequest.isSet():
            try:
                # flush serial before reading
                self.ser.flush()
                # read value from serial
                value = str(self.ser.readline().decode().strip('\r\n'))
                self.logger.info("read value " + value + " from arduino")

                # check if value from serial contains too many numbers
                if len(value) > 1:
                    value = value[0]
                self.logger.info("value was stripped to " + value)
                # get timestamp value
                timestamp = int(round(time.time() * 1000))
                # write data to csv
                self.writer.writerow([str(timestamp)] + [str(value)])

            except serial.SerialTimeoutException:
                self.logger.error('serial timeout exceeded')

            except IOError:
                self.logger.error('error when writing to csv file')

            except Exception as e:
                self.logger.error('error occured: ' + str(e))

    def run(self):
        """Run thread."""
        self.logger.info("started reading serial")
        self.read_servo_value()

    def join(self, timeout=None):
        """End thread."""
        self.buffer_file.close()
        self.logger.info("stopped serial due to keyboard interrupt")
        self.stoprequest.set()
        threading.Thread.join(self)
