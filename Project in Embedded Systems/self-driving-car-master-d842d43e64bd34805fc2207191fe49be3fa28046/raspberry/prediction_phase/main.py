"""File containing main control loop for prediction phase."""

from logger import logger
from picamera import PiCamera
from network_handler import NetworkHandler
import time
import datetime
import serial
import os

stopped = False

ser = serial.Serial(
    port='/dev/ttyACM0',
    baudrate=9600,
    timeout=1
)
network = NetworkHandler("http://192.168.1.110:5000")


def filenames():
    """Generate filename."""
    global stopped
    while not stopped:
        timestamp = int(datetime.datetime.now().timestamp() * 1000)
        # yield 'img/image.jpg' % timestamp
        yield 'img/image' + str(timestamp) + '.jpg'
        img_path = 'img/image' + str(timestamp) + '.jpg'
        logger.info('image saved to path: ' + img_path)
        # send image over network and wait for response
        # response = servo position
        resp = network.post_image(img_path)
        if resp:
            # parse response into byte value pos and write servo val to serial
            if resp == 1:
                pos = b'1'
                ser.write(pos)
                logger.info('wrote servo value: ' + str(resp))
            elif resp == 2:
                pos = b'2'
                ser.write(pos)
                logger.info('wrote servo value: ' + str(resp))
            elif resp == 3:
                pos = b'3'
                ser.write(pos)
                logger.info('wrote servo value: ' + str(resp))
            elif resp == 9:
                pos = b'9'
                ser.write(pos)
                logger.info('wrote servo value: ' + str(resp))
            else:
                logger.error('no proper servo value was given')
        else:
            logger.error('no response received from server')
            # write motor stop command
            ser.write(b'0')
        # remove image file to preserve memory
        # this can be removed if it takes too much time!!
        img_file = os.path.expanduser(
            '~') + '/self-driving-car/raspberry/prediction_phase/' + img_path
        try:
            os.remove(img_file)
        except OSError:
            logger.error('failed to remove file: ' + img_file)
            continue


def main():
    """Run main loop."""
    global stopped
    try:
        with PiCamera() as camera:
            camera.resolution = (160, 120)
            camera.color_effects = (128, 128)
            camera.framerate = 30
            logger.info("start preview")
            camera.start_preview(fullscreen=False, window=(100, 20, 320, 240))
            time.sleep(2)
            logger.info("start capture sequence")
            camera.capture_sequence(filenames(), use_video_port=True)

    except KeyboardInterrupt:
        stopped = True


if __name__ == '__main__':
    main()
