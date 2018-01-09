"""This file contains camera_handler class for controlling the camera."""

from picamera import PiCamera
from datetime import datetime
import logging


class CameraHandler():
    """Class for handling RPi camera."""

    def __init__(self):
        """Init CameraHandler class."""
        self.logger = logging.getLogger('raspberry.camera.CameraHandler')
        self.logger.info('initializing CameraHandler')
        self.camera = PiCamera()
        self.camera.resolution = (160, 120)
        self.camera.color_effects = (128, 128)
        self.img_root = "img/"

    def set_start_delay(self, delay):
        """Set start delay."""
        self.start_delay = delay

    def set_img_root(self, location):
        """Set location where images are being saved."""
        self.img_root = location

    def take_image(self):
        """Take single image."""
        time_str = datetime.utcnow().strftime('%Y%m%d-%H_%M_%S_%f')[:-3]
        img_path = self.img_root + "img" + time_str + ".png"
        self.camera.capture(img_path)
        self.logger.info('saved image to path: ' + img_path)
        return img_path
