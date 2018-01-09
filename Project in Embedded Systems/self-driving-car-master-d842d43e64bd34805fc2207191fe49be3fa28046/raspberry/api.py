"""This file contains API for controlling the car."""

import serial
import logging

# create logger
api_logger = logging.getLogger('raspberry.api')


class API():
    """Class for API between RPi and Arduino."""

    def __init__(self):
        """Initialize API class."""
        self.ser = serial.Serial(
                       port='/dev/ttyACM0',
                       baudrate=9600,
                       timeout=1
                    )
        # create logger
        self.logger = logging.getLogger('raspberry.api.API')
        self.logger.info('initializing API module')

    def request_servo_value(self):
        """Send servo request to arduino."""
        self.ser.write(b'r')
        return

    def read_servo_value(self):
        """Read servo value from serial."""
        value = self.ser.read(1)
        if value:
            val = str(value, 'utf-8')
            self.ser.flush()
            self.logger.info('read value from arduino: ' + val)
            print("Read value from arduino: " + val)
            return val
        else:
            self.logger.error('failed to read value from serial')
            return 'empty'

    def get_servo_value(self):
        """Get servo value from the car."""
        try:
            self.logger.info('requesting servo value')
            # write command to arduino to get servo value
            self.ser.write(b'r')
            # read value from serial sent by arduino
            value = self.ser.read()
            if value:
                val = str(value, 'utf-8')
                self.ser.flush()
                self.logger.info('read value from arduino: ' + val)
                print("Read value from arduino: " + val)
                return val
            else:
                self.logger.error('failed to read value from serial')
                return 'empty'
        except serial.SerialTimeoutException:
            self.logger.error('serial timeout exceeded')
