"""File containing camera thread."""

import time
import datetime
import threading
import logging
from picamera import PiCamera


class CameraThread(threading.Thread):
    """Thread for handling RPi camera."""

    def __init__(self):
        """Init CameraHandler class."""
        threading.Thread.__init__(self)
        # create logger
        self.logger = logging.getLogger('raspberry.cameraThread')
        self.logger.info("initializing Camera thread")
        self.stoprequest = threading.Event()

    def filenames(self):
        """Generate filename."""
        while not self.stoprequest.isSet():
            timestamp = int(datetime.datetime.now().timestamp()*1000)
            yield 'img/image%d.jpg' % timestamp

    def run(self):
        """Start recording images."""
        self.logger.info("start Camera thread")
        with PiCamera() as camera:
            camera.resolution = (160, 120)
            camera.color_effects = (128, 128)
            camera.framerate = 30
            self.logger.info("start preview")
            camera.start_preview()
            time.sleep(2)
            self.logger.info("start capture sequence")
            camera.capture_sequence(self.filenames(), use_video_port=True)

    def join(self, timeout=None):
        """Stop recording images."""
        self.logger.info("stopping camera thread")
        self.stoprequest.set()
        threading.Thread.join(self)
