import timeit
import requests
import random
import shutil
import os

URL = "http://tensorflow.logxar.space/api/imageUpload"
URL = "http://192.168.1.110:5000/api/imageUpload"
TESTFILE = "img/160x120.png"
TESTTARGET = "img/{}_160x120.png"
ITERATIONS = 100


def main():
    for i in range(ITERATIONS):
        shutil.copyfile(TESTFILE, TESTTARGET.format(i))

    start_time = timeit.default_timer()
    for i in range(ITERATIONS):
        with open(TESTTARGET.format(i), 'rb') as f:
            files = {'file': f}
            # data = {"servovalue": random.randint(0, 7)}
            requests.post(URL + "/" + str(random.randint(0, 7)), files=files)
    elapsed = timeit.default_timer() - start_time
    speed = ITERATIONS/elapsed
    msg = "Sending and saving {:d} images took {:f} Seconds ({:f} pics/s)"
    for i in range(ITERATIONS):
        os.remove(TESTTARGET.format(i))
    print(msg.format(ITERATIONS, elapsed, speed))


if __name__ == '__main__':
    main()
