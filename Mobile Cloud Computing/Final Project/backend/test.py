from flask import Flask, request, current_app
from flask_restful import Api, Resource
import firebase_admin
from firebase_admin import auth, credentials
from json import dumps
import uuid
import pyrebase
import os
import IPython
# $ pip install requests
import requests 
import numpy
import io 
import os
import StringIO
# $ pip install pillow
from PIL import Image 
from gcloud import storage


# FLASK_APP=test.py flask run

config = {
  "apiKey": "AIzaSyCwfm2u-esuj06kOJaVG3KUHzRrW-5ufZM",
  "authDomain": "mcc-fall-2017-g10.firebaseapp.com",
  "databaseURL": "https://mcc-fall-2017-g10.firebaseio.com/",
  "storageBucket": "mcc-fall-2017-g10.appspot.com/",
 # "storageBucket": "gs://mcc-fall-2017-g10.appspot.com/",
  "serviceAccount": os.path.dirname(os.path.realpath(__file__))+"/admin_credentials.json"
}

firebase = pyrebase.initialize_app(config)


cred = cred = credentials.Certificate(os.path.dirname(os.path.realpath(__file__))+'/admin_credentials.json')
default_app = firebase_admin.initialize_app(cred)
auth = firebase.auth()
user = auth.sign_in_with_email_and_password("backend-account@mcc-fall-2017-g10.iam.gserviceaccount.com", "12345678")
db = firebase.database()

'''
data = db.child("group_image_chain_mcc-g10").get()

for datum in data.each():
    print datum.key()
'''

def getImageExt(img_url):
    file_ext = img_url.split('.')[-1]
    return file_ext

# resizing image according to given dimensions
def rescale(img, maxsize):
    scaledimg = img.resize(maxsize, Image.ANTIALIAS)
    scaledimg.format = img.format
    scaledimg.mode = img.mode
    return scaledimg

def upload_image_file(img, filename, choice, group_id, message_id):
    client = storage.Client(project = 'mcc-fall-2017-g10', credentials = firebase.credentials)
    bucket = client.get_bucket('mcc-fall-2017-g10.appspot.com')
    blob = bucket.blob(group_id+"/"+message_id+"/"+filename+choice+"."+img.format)
    outfile = filename+choice+"."+img.format
    img.save(outfile)
    
    blob.upload_from_filename(filename = outfile,)
    
    # download url in storage 
    #print(storage.child(outfile).get_url(outfile))
    '''
     Currently working on download url
    '''
    os.remove(outfile)
    stor = firebase.storage()
    '''
    print blob.public_url
    print '\n'
    print blob.generate_signed_url(method='GET')
    print '\n'
    print blob.metadata
    print '\n'
    print blob.url
    '''
    
    
    return stor.ref(group_id+"/"+message_id+"/"+filename+choice+"."+img.format).getDownloadURL()
   
@app.route('/loadimgqlt', methods = ['POST'])
def storeimage(img_url, group_id, message_id):
    # load image from firebase into memory
    r = requests.get(img_url, stream=True)
    r.raw.decode_content = True # Content-Encoding
    img = Image.open(r.raw) #NOTE: it requires pillow 2.8+
                
    filename  =  getImageName(img_url)
    fileext =  getImageExt(img_url)

    scaledimageorig = img
    maxsize = (640, 480)
    scaledimagelow = rescale(img, maxsize)
    maxsize = (1280, 960)
    scaledimagehigh = rescale(img, maxsize)
    low_url = upload_image_file(scaledimagelow, filename, "low", group_id, message_id)
    high_url = upload_image_file(scaledimagehigh, filename, "high", group_id, message_id)
    orig_url = img_url
                   
    imgqlt = {
            "highImageUrl": high_url,
            "lowImageUrl": low_url
            }
                    
    db.child("group_image_chain_mcc-g10").child(group_id).child(message_id).update(imgqlt)


IPython.embed()