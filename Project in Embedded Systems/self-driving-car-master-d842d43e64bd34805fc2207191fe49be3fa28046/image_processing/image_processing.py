from PIL import Image
import numpy
import matplotlib.pyplot as plt
import sys
import os
import imghdr

#Pixels higher than this will be 1. Otherwise 0.
THRESHOLD_VALUE = 205
basewidth = 150
fix_sixe = 28

def image_processing(src_file, dst_file):
    image = Image.open(src_file).convert("L")
    wpercent = (basewidth/float(image.size[0]))
    hsize = int((float(image.size[1])*float(wpercent)))
    re_image = image.resize((basewidth, hsize), Image.ANTIALIAS)
    if imghdr.what(src_file) == "jpeg":
        re_image=re_image.rotate(-90, expand=True)
    thresholdedData = re_image.point(lambda x: 0 if x<THRESHOLD_VALUE else 255, '1')
    thresholdedData.save("/Users/shan_kuan/Documents/TEST1.png")
    thresholded = numpy.asarray(thresholdedData)
    count=0
    left=0
    for j in range(len(thresholded[0])):
    	if thresholded[len(thresholded)-1][j]:
    		if not count:
    			left = j
    		count+=1
    #	print thresholded[len(thresholded)-1][j]
    left = left - count*2
    if not left:
    	left = 20
    width = (count*5)ww
    height = width
    top = len(thresholded)-height
    box = (left, top, left+width, top+height)
    area = thresholdedData.crop(box)
    resize_area = area.resize((fix_sixe, fix_sixe), Image.ANTIALIAS)
    resize_area.save(dst_file)

def usage():
    print '''usage: 
        image_processing.py <src file> <dst file>
    '''
    exit(2)
def file_exist():
    print '''src_file doesn't exist
    '''
    usage()

if __name__ == '__main__':
    if len(sys.argv) != 3:
        usage()

    src_file = sys.argv[1]
    dst_file = sys.argv[2]
    if not(os.path.isfile(src_file)):
        file_exist()
    image_processing(src_file, dst_file)
