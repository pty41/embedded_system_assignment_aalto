"""Simple example for raspberry pi serial."""

import serial
import time

ser = serial.Serial('/dev/ttyACM0', 9600, timeout=.1)
time.sleep(1)  # give the connection a second to settle
while True:
    cmd = input()
    if cmd == 'get':
        ser.write(b'r')
        data = ser.read()
        if data:
            print(data)
            ser.flush()
