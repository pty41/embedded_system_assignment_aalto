from flask import Flask, request, jsonify
from flask_restful import Api, Resource
import firebase_admin
from firebase_admin import auth, credentials
from json import dumps
import json
import time
import uuid
import os
import pyrebase
import sys
# $ pip install requests
import requests 
import numpy
# $ pip install pillow
from PIL import Image 
import io 
from gcloud import storage

# Needed by Raine
from google.cloud import vision
import shutil
from google.cloud.vision import types

# FLASK_APP=hello.py flask run

app = Flask(__name__)
api = Api(app)


config = {
  "apiKey": "AIzaSyCwfm2u-esuj06kOJaVG3KUHzRrW-5ufZM",
  "authDomain": "mcc-fall-2017-g10.firebaseapp.com",
   "storageBucket": "mcc-fall-2017-g10.appspot.com/",
  "databaseURL": "https://mcc-fall-2017-g10.firebaseio.com/",
 # "storageBucket": "gs://mcc-fall-2017-g10.appspot.com/",
  "serviceAccount": "admin_credentials.json"
}

cred = cred = credentials.Certificate(os.path.dirname(os.path.realpath(__file__))+'/admin_credentials.json')
token_verification_app = firebase_admin.initialize_app(cred)
firebase = pyrebase.initialize_app(config)
auth = firebase.auth()
user = auth.sign_in_with_email_and_password("backend-account@mcc-fall-2017-g10.iam.gserviceaccount.com", "12345678")
db = firebase.database()


@app.route('/')
def api_root():
    return "This is root"


@app.route('/groups', methods = ['POST', 'GET'])
def api_groups():
    # Here, get method returns the members of the current gourp
    check_group_expirations()

    if request.method == 'GET':
        if not request.json:
            return "Failed, not json"
        if not 'id_token' in request.json:
            return "Failed, no token"
        decoded_token = check_token(request.json['id_token'])
        uid = decoded_token['uid']
        if not uid:
            return "Token verification failed"

        list_json = ''
        member_list = []
        is_member = False
        all_groups = db.child("groups").get()
        for group in all_groups.each():
            try:
                for key in group.val():
                    if "member" in key:
                        member_list.append(group.val()[key])
                        if group.val()[key] == uid:
                            is_member = True
                if is_member:
                    all_users = db.child("users").get()
                    for m in member_list:
                        for user in all_users.each():
                            if user.val()['uid'] == m:
                                m = user.val()['name']


                    list_json = json.dumps(member_list, indent=4,
                        sort_keys=True, separators=(',', ': '), ensure_ascii=False)
                else:
                    member_list = []
            except Exception as e:
                print e
                continue
        
        return "You are not in a group!"


    # Post is used for all user's actions regarding groups
    if request.method == 'POST':
        if not request.json:
            return "Failed, not json"
        if not 'id_token' in request.json:
            return "Failed, no token"
        try:
            decoded_token = check_token(request.json['id_token'])
        except ValueError:
            return "Token expired!"
        uid = decoded_token['uid']
        if not uid:
            return "Token verification failed"

        if 'action' in request.json:
            action = request.json['action']

            if action == 'information':
                list_json = ''
                member_list = []
                name_list = []
                is_member = False
                all_groups = db.child("groups").get()
                for group in all_groups.each():
                    try:
                        for key in group.val():
                            if "member" in key:
                                member_list.append(group.val()[key])
                                if group.val()[key] == uid:
                                    is_member = True
                        if is_member:
                            all_users = db.child("users").get()
                            for m in member_list:
                                for user in all_users.each():
                                    if user.val()['uid'] == m:
                                        name_list.append(user.val()['name'])
                            content = {"member_list":name_list, "expiration_date":group.val()['expiration_date'],"joiner_token":group.val()['joiner_token']}
                            list_json = json.dumps(content, indent=4,
                                sort_keys=True, separators=(',', ': '), ensure_ascii=False)
                            return list_json                          
                        else:
                            member_list = []
                    except Exception as e:
                        print e
                        continue
                
                return "You are not in a group!"

            if action == 'check':
                all_users = db.child("users").get()
                for user in all_users.each():
                    if user.val()['uid'] == uid:
                        try:
                            all_groups = db.child("groups").get()
                            for group in all_groups.each():
                                for key in group.val():
                                    if "member" in key:
                                        if group.val()[key] == uid:
                                            return group.val()['group_name']
                        except:
                            pass
                return "No"

            if action == 'create':
                try:
                    all_groups = db.child("groups").get()
                    for group in all_groups.each():
                        for key in group.val():
                            if "member" in key:
                                if group.val()[key] == uid:
                                    return "You are already in a group! Leave before joining another one."
                except:
                    pass

                joiner_token = generate_token()
                group = {"group_name": request.json['group_name'],
                    "group_time": request.json['group_time'],
                    "expiration_date": time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(request.json['group_time'])),
                    "joiner_token": joiner_token,
                    "member_0": uid
                }
                add_user(decoded_token)
                db.child("groups").child(group['group_name']).set(group)
                return joiner_token

            if action == 'join':
                print "joiner tulee"
                group_token = request.json['group_token']
                all_groups = db.child("groups").get()
                for group in all_groups.each():
                    try:
                        for key in group.val():
                            if "member" in key:
                                if group.val()[key] == uid:
                                    return "You are already in a group! Leave before joining another one."
                    except:
                        continue
                for group in all_groups.each():
                    if group.val()['joiner_token'] == group_token:
                        db.child("groups").child(group.val()['group_name']).update({"member_"+str(len(all_groups.each())-3): uid})
                        joiner_token = generate_token()
                        db.child("groups").child(group.val()['group_name']).update({"joiner_token": joiner_token})
                        add_user(decoded_token)
                        return "You were successful in your attempt to join a group!"
                return "Token does not match to any group, try again."

            if action == 'leave':
                found = False
                output = "Leaving group failed, no matching group found"
                all_groups = db.child("groups").get()
                for group in all_groups.each():
                    if not not found:
                        break
                    #try:
                    for key in group.val():
                        if "member_0" in key:
                            if group.val()[key] == uid:
                                db.child("groups").child(group.val()['group_name']).remove()
                                delete_user(uid)
                                output = "You have deleted the group!"
                                found = True
                        elif "member" in key:
                            if group.val()[key] == uid:
                                db.child("groups").child(group.val()['group_name']).child(key).remove()
                                delete_user(uid)
                                output = "You were successful in your attempt to leave the group!"
                                found = True
                    #except:
                    #    continue
                return output


@app.route('/token')
def generate_token():
    # Generate a single use token.(UUID)
    return str(uuid.uuid4())


global posted_info
posted_info = "No info posted"

@app.route('/test', methods = ['POST', 'GET'])
def request_test():
    if request.method == 'POST':
        if not request.json:
            return "Failed, not json"
        if not 'title' in request.json:
            return "Failed, no title"
        global posted_info
        posted_info = request.json['key1']
        print posted_info
        return "info saved!"
    elif request.method == 'GET':
        global posted_info
        return posted_info
    else:
        return "Failed - User GET or POST"


def check_token(token):
    decoded_token = False
    decoded_token = firebase_admin.auth.verify_id_token(token)
    if decoded_token == False:
        return False
    return decoded_token

def check_group_expirations():
    time_now = time.time()
    all_groups = db.child("groups").get()
    try: 
        for group in all_groups.each():
            if isinstance(group.val()['group_time'], int) or isinstance(group.val()['group_time'], float):
                if time_now > group.val()['group_time']:
                    db.child("groups").child(group.val()['group_name']).remove()
            else:
                db.child("groups").child(group.val()['group_name']).remove()
    except:
        pass


def add_user(token):
    #This function add the user into "users" table in db, if not already there
    uid = token['uid']
    all_users = db.child("users").get()
    for user in all_users.each():
        try:
            if user.val()['uid'] == uid:
                return 0
        except:
            continue
    user_template = {"name": token['name'],
        "email": token['email'],
        "uid": uid
    }
                
    db.child("users").child(uid).set(user_template)

def delete_user(uid):
    #This function deletes the user from "users" table in db, if it is there
    all_users = db.child("users").get()
    for user in all_users.each():
        try:
            if user.val()['uid'] == uid:
                db.child("users").child(uid).remove()
        except:
            continue



def getImageName(img_url):
    filename = img_url.split('/')[-1].split('.')[0]
    return filename

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
    of = open(outfile, 'rb')
    
    blob.upload_from_filename(filename=outfile)
    stor = firebase.storage()
    # download url in storage 
    #print(storage.child(outfile).get_url(outfile))
    os.remove(outfile)
    return stor.child(outfile).get_url(1)



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


@app.route('/loadimgqlt', methods = ['POST'])
def storeimage():
    if request.method == 'POST':
        if not request.json:
            return "Failed, not json"
        if 'group_id' in request.json:
            group_id = request.json['project_id']
            if 'message_id' in request.json:
                message_id = request.json['message_id']
                if 'img_url' in request.json:
                    img_url = request.json['img_url']
                    # load image from firebase into memory
                    r = requests.get(img_url, stream=True)
                    r.raw.decode_content = True # Content-Encoding
                    img = Image.open(r.raw) #NOTE: it requires pillow 2.8+
                    
                    filename  =  getImageName(img_url)
                    fileext =  getImageExt(img_url)
                    # image parameters
                    #print("Loaded image parameters>\n", "Extension:", img.format, "\n", "Mode:", img.mode,"\n", "Dimensions:",img.size)
                    
                    # saving image (for testing)
                    #img.save("test_image", img.format)
                    
                    # scaling image
                    # The image resolution can be low (640 by 480 pixels), 
                    # high (1280 by 960 pixels), and full (original size)
                    scaledimageorig = img
                    maxsize = (640, 480)
                    scaledimagelow = rescale(img, maxsize)
                    maxsize = (1280, 960)
                    scaledimagehigh = rescale(img, maxsize)
        
                    # get download url from storage
                    low_url = upload_image_file(scaledimagelow, filename, "low", group_id, message_id)
                    high_url = upload_image_file(scaledimagehigh, filename, "high", group_id, message_id)
                    orig_url = img_url
                   
                    imgqlt = {
                            "highImageUrl": high_url,
                            "lowImageUrl": low_url
                             }
                    
                    db.child("group_image_chain_mcc-g10").child(group_id).child(message_id).update(imgqlt)


<<<<<<< Updated upstream
=======
@app.route('/detectFaces', methods = ['POST'])
def detect_faces():
    if request.method == 'POST':
        if not request.json:
            return "Failed, not json"
        if 'img_url' in request.json:


            img_url = request.json['img_url']
            # load image from firebase into memory
            r = requests.get(img_url, stream=True)

            path = "tmpImage"
            if r.status_code == 200:
               with open(path, 'wb') as f:
                   r.raw.decode_content = True
                   shutil.copyfileobj(r.raw, f)

            with open(path, 'rb') as image:
                faces = detect_face(image)

            return faces


>>>>>>> Stashed changes

def detect_face(image_file):

    client = vision.ImageAnnotatorClient()

    content = image_file.read()
    image = types.Image(content=content)

    return client.face_detection(image=image).face_annotations



if __name__ == '__main__':
     app.run(host='127.0.0.1', port=8080, debug=True)
