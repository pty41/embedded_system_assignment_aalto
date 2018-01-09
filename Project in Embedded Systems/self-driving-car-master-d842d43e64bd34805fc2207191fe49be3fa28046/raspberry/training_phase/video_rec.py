"""This file contains camera_handler class for controlling the camera."""

from picamera import PiCamera
import time
import logging
import threading

# create logger
api_logger = logging.getLogger('raspberry.camera')


class CameraThread(threading.Thread):
    """Class for handling RPi camera."""

    def __init__(self):
        """Init CameraHandler class."""
        threading.Thread.__init__(self)
        # create logger
        self.logger = logging.getLogger('raspberry.camera.CameraHandler')
        self.logger.info("initializing CameraHandler thread")
        # init camera
        self.camera = None
        self.start_delay = 5
        self.interval = 1
        self.vid_root = "../img/"
        self.video_path = self.vid_root + "video.h264"
        self.start_time = 0
        self.end_time = 0
        self.stoprequest = threading.Event()

    def run(self):
        """Start recording video."""
        self.logger.info("setup camera")
        self.camera = PiCamera()
        self.camera.rotation = 180
        self.camera.resolution = (320, 240)
        self.camera.framerate = 10
        self.camera.color_effects = (128, 128)
        self.logger.info("starting camera")
        self.camera.start_preview()
        time.sleep(self.start_delay)
        self.logger.info("camera started")
        time_str = time.strftime("%d%m%Y-%H%M%S")
        self.video_path = self.vid_root + "video_" + time_str + ".h264"
        self.camera.start_recording(self.video_path)
        self.logger.info("recording started")
        self.start_time = time.time()
        while not self.stoprequest.isSet():
            # record time
            self.end_time = time.time()
            self.logger("time elapsed for recording: " +
                        str(self.end_time - self.start_time))

    def join(self, timeout=None):
        """Stop recording video."""
        self.logger.info("stopping recording due to keyboard interrupt")
        self.camera.stop_recording()
        self.camera.stop_preview()
        self.logger("time elapsed for recording: " +
                    str(self.end_time - self.start_time))
        self.stoprequest.set()
        threading.Thread.join(self)
