#!/usr/bin/python
"""Core module of RPi code which handles everything from camera to network."""

from logger import logger
from camera_handler import CameraHandler
from network_handler import NetworkHandler
from api import API
import time
import csv

# Interval to take images (for debugging purposes)
INTERVAL = 1
URL = "http://192.168.1.110:5000/"


def main():
    """Run core."""
    # Initialize camera
    logger.info('creating an instance of camera handler module')
    camera = CameraHandler()
    network = NetworkHandler(URL)
    # Initialize API
    logger.info('creating an instance of api module')
    api = API()
    # Initialize network

    # open csv file
    time_str = time.strftime("%d%m%Y-%H%M%S")
    data_buffer = "buffer/labels_" + time_str + ".csv"
    print("starting sleep 1 min.............")
    time.sleep(60)
    print("starting take picture............")
    with open(data_buffer, 'w') as buffer_file:
        # initialize csv writer and write header to file
        writer = csv.writer(buffer_file, delimiter=';')
        writer.writerow(["image"] + ["servovalue"])

        loop_start = time.time()
        while True:
            loop_end = time.time()
            if (loop_end - loop_start) > INTERVAL:
                loop_start = time.time()
                api.request_servo_value()
                img_path = camera.take_image()
                servalue = api.read_servo_value()
                # Write img path and servo value to csv file
                writer.writerow([img_path] + [servalue])
                print("Img saved to " + img_path)
                print("Got servo value: " + servalue)
                network.post_image(img_path, servalue)


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        logger.info("Stopping system due to keyboard interrupt")
