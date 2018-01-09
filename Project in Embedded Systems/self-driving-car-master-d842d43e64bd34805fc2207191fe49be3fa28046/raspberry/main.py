#!/usr/bin/python
"""Core module of RPi code which handles everything from camera to network."""

from apiThread import APIThread
from cameraThread import CameraThread
from logger import logger
import time


def main():
    """Run core."""
    logger.info('create an instance of camera thread')
    try:
        camera_thread = CameraThread()
        logger.info('create an instance of api thread')
        api_thread = APIThread()

        print("starting sleep 1 min.............")
        time.sleep(60)
        print("start")
        logger.info('start threads')
        camera_thread.start()
        api_thread.start()

        while True:
            continue

    except KeyboardInterrupt:
        logger.info('kill threads')
        api_thread.join()
        camera_thread.join()
        logger.info('stopped threads due to keyboard interrupt')

    except Exception as e:
        logger.error('error occured: ' + str(e))


if __name__ == '__main__':
    main()
