#!/usr/bin/python
"""Core module of RPi code which handles everything from camera to network."""

from logger import logger
from video_rec import CameraThread
from serial_comm import APIThread


def main():
    """Run core."""
    # Initialize camera
    logger.info('creating an instance of camera handler module')
    camera_thread = CameraThread()
    # Initialize API
    logger.info('creating an instance of api module')
    api = APIThread()
    # Start threads
    logger.info('start camera thread')
    camera_thread.start()
    logger.info('start serial thread')
    api.start()
    try:
        while True:
            continue
    except KeyboardInterrupt:
        logger.info('kill api thread')
        api.join()
        logger.info('kill camera thread')
        camera_thread.join()
        logger.info("stopped threads due to keyboard interrupt")


if __name__ == '__main__':
    main()
