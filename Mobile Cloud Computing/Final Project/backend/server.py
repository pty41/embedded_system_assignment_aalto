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
    if request.method == 'GET':
        if not request.json:
            return "Failed, not json"
        if not 'id_token' in request.json:
            return "Failed, no token"
        uid = check_token(request.json['id_token'])
        if not uid:
            return "Token verification failed"

        uid = 12345
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
                    

                    list_json = json.dumps(member_list, indent=4,
                        sort_keys=True, separators=(',', ': '), ensure_ascii=False)
                else:
                    member_list = []
            except Exception as e:
                # print e
                continue
        
        return "Something went wrong"


    # Post is used for all user's actions regarding groups
    if request.method == 'POST':
        if not request.json:
            return "Failed, not json"
        if not 'id_token' in request.json:
            return "Failed, no token"
        uid = check_token(request.json['id_token'])
        if not uid:
            return "Token verification failed"

        if 'action' in request.json:
            action = request.json['action']

            if action == 'create':
                joiner_token = generate_token()
                group = {"group_name": request.json['group_name'],
                    "group_time": request.json['group_time'],
                    "joiner_token": joiner_token,
                    "member_0": uid
                }
                
                db.child("groups").child(group['group_name']).set(group)
                return joiner_token

            if action == 'join':
                group_token = request.json['group_token']
                all_groups = db.child("groups").get()
                for group in all_groups.each():
                    if group.val()['joiner_token'] == group_token:
                        if uid not in group.val():
                            db.child("groups").child(group).update({"member_"+str(len(all_groups.each())-3): uid}, user['idToken'])
                            joiner_token = generate_token()
                            db.child("groups").child(group).update({"joiner_token": joiner_token}, user['idToken'])
                

            if action == 'leave':
                all_groups = db.child("groups").get()
                for group in all_groups.each():
                    try:
                        for key in group.val():
                            if "member_0" in key:
                                db.child("groups").child(group).remove()
                                break
                            if "member" in key:
                                if group.val()[key] == uid:
                                    db.child("groups").child(group).child(key).remove()
                    except:
                        continue



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
    return decoded_token['uid']

def check_group_expiration(group_name):
    time_now = time.time()
    all_groups = db.child("groups").get()
    for group in all_groups.each():
        if time_now > group.val()['group_time']:
            db.child("groups").child(group).remove()

def getImageName(img_url):
    filename = img_url.split('/')[-1].split('.')[0]
    return filename

def getImageExt(img_url):
    file_ext = img_url.split('.')[-1]
    return file_ext

def upload_image_file(file, name, ext):
    """
    Upload the user-uploaded file to Google Cloud Storage and retrieve its
    publicly-accessible URL.
    """
    '''
    if not file:
        return None

    if hasattr(file, 'filename') == False:
        filename = name
    else:
        filename = file.filename
    
    print(file)
    print("filename", filename)
    content_type = ext
    print("content_type", content_type)
   
    output = StringIO()
    file.save(output)
    contents = output.getvalue()
    output.close()
    print("output", output)
    public_url = storage.upload_file(
        output,
        filename,
        content_type
    )

    current_app.logger.info(
        "Uploaded file %s as %s.", file.filename, public_url)
    
    return public_url
    '''
    
    
@app.route('/loadimgqlt', methods = ['POST'])
def storeimage():
    if request.method == 'POST':
        if not request.json:
            return "Failed, not json"
        if 'img_url' in request.json:
            img_url = request.json['img_url']
            # load image from firebase into memory
            r = requests.get(img_url, stream=True)
            r.raw.decode_content = True # Content-Encoding
            img = Image.open(r.raw) #NOTE: it requires pillow 2.8+
            # image parameters
            #print("Loaded image parameters>\n", "Extension:", img.format, "\n", "Mode:", img.mode,"\n", "Dimensions:",img.size)
            
            # saving image (for testing)
            #img.save("test_image", img.format)
            
            # scaling image
            # The image resolution can be low (640 by 480 pixels), 
            # high (1280 by 960 pixels), and full (original size)
        
            filename = getImageName(img_url)
            fileext = img.format
            
            scaledimageorig = img
            maxsize = (640, 480)
            scaledimagelow = img.resize(maxsize, Image.ANTIALIAS)
            maxsize = (1280, 960)
            scaledimagehigh = img.resize(maxsize, Image.ANTIALIAS)
            
            """
            Upload the user-uploaded file to Google Cloud Storage and retrieve its
            publicly-accessible URL.
            """ 
            '''
            low_url = upload_image_file(scaledimagelow, filename, fileext)
            high_url = upload_image_file(scaledimagehigh, filename, fileext)
            orig_url = img_url
            
            imgqlt = {
                    "highImageUrl": high_url,
                    "imageUrl": orig_url,
                    "lowImageUrl": low_url
                     }
            
            db.child("group_image_chain_mcc-g10").push(imgqlt)
            '''
    


if __name__ == '__main__':
     server.run()