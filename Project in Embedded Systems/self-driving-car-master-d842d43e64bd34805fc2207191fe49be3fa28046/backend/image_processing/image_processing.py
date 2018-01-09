# -*- coding: utf8 -*-
from xmlrpc.server import SimpleXMLRPCServer
from xmlrpc.server import SimpleXMLRPCRequestHandler
from socketserver import ThreadingMixIn
import threading
from PIL import Image
import numpy
#import matplotlib.pyplot as plt
import sys
from PIL import ImageEnhance
import os
import imghdr
import csv
import time
from queue import Queue
import imghdr
import pickle

#Pixels higher than this will be 1. Otherwise 0.
TRAINING_MAX = 1000
THRESHOLD_VALUE = 180
PREDICT_FOLDER = "predict"
#basewidth = 150
#fix_sixe = 28
#DEFAULT_LEFT = 20 # in case the road is not on the middel of the picture


# Create server
server = SimpleXMLRPCServer(("localhost", 1234))
class ImageProcessing(ThreadingMixIn, SimpleXMLRPCServer):
    def __init__(self):
        self.SRC_FOLDER = "/Users/dong/dong_git/self-driving-car/backend/image_processing"
        #self.DST_FLODER = "/Users/dong/dong_git/self-driving-car/backend/image_processing/backup"
        self.CSV_FOLDER = "/Users/dong/dong_git/self-driving-car/backend/image_processing/800_label.csv"
        self.IMAGE_DATA_LIST = []
        self.LABEL_DATA_LIST = []
        self.process_status = "Default"
        self.previous_status = "Default"
        self.queue_data = Queue()

    def ch_status(self, status):
        self.previous_status = self.process_status
        self.process_status = status

    def csv_write(self, dst_file, image_option=True):
        csv_list = self.LABEL_DATA_LIST
        if image_option:
            csv_list = self.IMAGE_DATA_LIST
        with open(dst_file, 'w') as myfile:
            #wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
            wr = csv.writer(myfile)
            wr.writerow(csv_list)

    def previous_read(self, fileObj):
        while True:
            data = fileObj.read()
            if not data:
                break
            yield data

    def tail_read(self, thefile):
        thefile.seek(0,2)
        count = 0
        while count < 5:
            line = thefile.readline()
            if not line:
                time.sleep(0.1)
                count = count+1
                continue
            yield line

    def lable_generate(self):
        def _label_file(data):
            list_srv = data.splitlines()
            for _data in list_srv:
                self.LABEL_DATA_LIST.append(_data.split(';')[1])

        self.LABEL_DATA_LIST = []
        logfile = open(self.CSV_FOLDER,"r")
        for _data in self.previous_read(logfile):
            _label_file(_data)
        loglines = self.tail_read(logfile)
        for _data2 in loglines:
            _label_file(_data2)


    def image_read(self):
        def _image_queue(data_list):
            list_name = data_list.splitlines()
            for _data in list_name:
                image_name = _data.split(';')[0]
                file_path = os.path.join(self.SRC_FOLDER, image_name)
                if not os.path.isfile(file_path):
                    print("the specific image is not existing")
                    return
                image = Image.open(file_path).convert("L")
                self.queue_data.put(image)
                #os.remove(file_path)

        if self.process_status not in ["Prediction", "Training"]:
            return
        if self.process_status == "Training":
            logfile = open(self.CSV_FOLDER,"r")
            thr_labling = threading.Thread(target=self.lable_generate)
            thr_labling.daemon = True
            thr_labling.start()
            for _data in self.previous_read(logfile):
                _image_queue(_data)
            loglines = self.tail_read(logfile)
            for _data2 in loglines:
                _image_queue(_data2)
        else:
            pre_count = 0
            while pre_count < 10:
                name_list = os.listdir(os.path.join(self.SRC_FOLDER, PREDICT_FOLDER))
                if not name_list:
                    time.sleep(1)
                    pre_count = pre_count+1
                    continue
                for _file_name in name_list:

                    file_path = os.path.join(self.SRC_FOLDER, PREDICT_FOLDER+"/"+_file_name)
                    if not os.path.isfile(file_path) or imghdr.what(file_path) != "png":
                        pre_count = pre_count+1
                        continue
                    pre_count = 0
                    image = Image.open(file_path).convert("L")
                    self.queue_data.put(image)
                    os.remove(file_path)

        '''       
        loglines = self.tail_read(logfile)
        for _data2 in loglines:
            _image_queue(_data2)
        '''


    def image_execute(self, status, src):
        self.SRC_FOLDER = src
        #self.DST_FLODER = dst
        self.CSV_FOLDER = os.path.join(src, "name_svr.csv")
        self.ch_status(status)
        thr_read = threading.Thread(target=self.image_read)
        thr_read.daemon = True
        thr_read.start()
        count = 0
        empty_break = 0
        self.IMAGE_DATA_LIST = []
        while self.process_status in ["Prediction", "Training"] or not(self.queue_data.empty()):
            if self.queue_data.empty():
                empty_break = empty_break+1
                time.sleep(1)
                if empty_break > 3 :
                    break
                print ("empty.....................\n")
                continue
            try:
                empty_break = 0    
                image = self.queue_data.get(block=True, timeout=1)
            except Queue.Empty:
                continue
            box = (0, 26, 160, 120)
            area = image.crop(box)
            enh_sha = ImageEnhance.Sharpness(area)
            shapeness = enh_sha.enhance(3.0).resize((80,47),Image.ANTIALIAS)
            thresholdedData = shapeness.point(lambda x: 0 if x<THRESHOLD_VALUE else 255, '1')
            # thresholdedData = image.point(lambda x: 0 if x<THRESHOLD_VALUE else 255, '1')
            # box = (0, 60, 160, 120)
            # area = thresholdedData.crop(box)
            shapeness.save(os.path.join(self.SRC_FOLDER, "../test_image/%d.png") %count)
            thresholdedData.save(os.path.join(self.SRC_FOLDER, "../test_image1/%d.png") %count)
            count = count +1
            image_list = numpy.array(thresholdedData.convert("L"))
            _list = image_list.reshape(1,image_list.shape[0]*image_list.shape[1]).astype(int)
            '''
            _list = []
            for _image in image_list:
                for __image in _image:
                    if __image:
                        _list.append(1)
                        continue
                    _list.append(0)
            if self.process_status == "Prediction":

                print ("TODO:")
                continue
                #return
                #TODO: Donmin provide the callable function name -- Tensorflow
                #yield predict_value--> the return value from tensorflow
            '''
            self.IMAGE_DATA_LIST.append(_list[0])
        #TODO: Donmin provide the callable function name -- Tensorflow
        with open("image_data.list",'wb') as f:
            pickle.dump(self.IMAGE_DATA_LIST,f)
        with open("label_data.list",'wb') as f:
            pickle.dump(self.LABEL_DATA_LIST,f)
        print("Size of self.IMAGE_DATA_LIST:", len(self.IMAGE_DATA_LIST))
        print("Size of self.LABEL_DATA_LIST:", len(self.LABEL_DATA_LIST))
        if self.previous_status == "Training" or self.process_status == "Training":
            self.csv_write(os.path.join(self.SRC_FOLDER, "training_image.csv"))
            self.csv_write(os.path.join(self.SRC_FOLDER, "training_labeling.csv"), image_option=False)
        print("Image Processing Done!!............\n")
        return True


if __name__ == '__main__':
    root = ImageProcessing()
    server.register_function(root.image_execute, "image_execute")
    server.register_function(root.ch_status, "ch_status")
    server.register_instance(root, allow_dotted_names=True)
        # Run the server's main loop
    try:
        print ("Use Control-C to exit")
        server.serve_forever()
    except KeyboardInterrupt:
        print ("Exiting")

