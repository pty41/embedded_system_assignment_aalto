#!/bin/bash

FOLDER="$(ls -1 buffer | grep csv | sed -n 1p)"
FOLDER=${FOLDER::-4}

rsync -av --exclude temp.txt img/ tensorflow:/home/group01/self-driving-car/backend/dataServer/img/$FOLDER/

rsync -av --exclude temp.txt buffer/ tensorflow:/home/group01/self-driving-car/backend/dataServer/buffer/$FOLDER/

rsync -av --exclude temp.txt logs/ tensorflow:/home/group01/self-driving-car/backend/dataServer/logs/$FOLDER/
