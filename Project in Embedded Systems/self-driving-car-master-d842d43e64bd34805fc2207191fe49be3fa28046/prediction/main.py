#!flask/bin/python
from flask import Flask, request, jsonify
import os
# import shutil
from werkzeug.utils import secure_filename
from image_processing import ImageProcessing
import csv
import CNNInterface as I
import time
UPLOAD_FOLDER = "img"
# shutil.rmtree(UPLOAD_FOLDER)
if (not os.path.exists(UPLOAD_FOLDER)):
    os.mkdir(UPLOAD_FOLDER)

#CSVFILE = "buffer/labels.csv"
image_execute = ImageProcessing()
app = Flask("self-driving-car")
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
sess = None
graph = None
'''
@app.route('/api/imageUpload/<int:servovalue>', methods=["POST"])
def imageUpload(servovalue):
    if not servovalue:
        return jsonify({
            "Status": "Error",
            "msg": "No servovalue"
            })
    # print(servovalue)
    # print("\n\n\n\n")
    # print(request.form)
    # while True:
    #     pass
    mode = "w"
    if os.path.exists(CSVFILE):
        mode = "a"

    with open(CSVFILE, mode) as buffer_file:
        # initialize csv writer and write header to file
        writer = csv.writer(buffer_file, delimiter=';')
        if request.method == 'POST':
            # check if the post request has the file part
            if 'file' not in request.files:
                return jsonify({
                    "Status": "Error",
                    "msg": "No uploaded files"
                    })
            file = request.files['file']
            # if user does not select file, browser also
            # submit a empty part without filename
            if file.filename == '':
                return jsonify({
                    "Status": "Error",
                    "msg": "Empty filename"
                    })
            if file:
                filename = secure_filename(file.filename)
                file.save(os.path.join(app.config['UPLOAD_FOLDER'],
                                       filename))
                writer.writerow([os.path.join(app.config['UPLOAD_FOLDER'],
                                              filename)] + [servovalue])
            return jsonify({
                "Status": "Success",
                "msg": "Image saved successfully"
                })

'''
@app.route('/api/getPrediction', methods=["POST"])
def getPrediction():
    if request.method == 'POST':
        #print ("post starting......",time.time())
        # check if the post request has the file part
        if 'file' not in request.files:
            return jsonify({
                "Status": "Error",
                "msg": "No uploaded files"
                })
        file = request.files['file']
        if not file:
            return jsonify({
                "Status": "Error",
                "msg": "file upload failed"
                })
        # if user does not select file, browser also
        # submit a empty part without filename
        if file.filename == '':
            return jsonify({
                "Status": "Error",
                "msg": "Empty filename"
                })
        filename = secure_filename(file.filename)
        file_path = os.path.join(app.config['UPLOAD_FOLDER'],
                               filename)
        file.save(file_path)
        #print("save finish.......",time.time())
        predictedValue = image_execute.image_execute(file_path,sess, graph)
       # print ("file: %s: %s" %(filename, predictedValue), time.time())
        return jsonify({
            "Status": "Success",
            "msg": "Prediction successfull",
            "value": str(predictedValue)
            })
    else:
        return jsonify({
            "Status": "Error",
            "msg": "Wrong method"
            })


@app.route('/')
def index():
    return "Hello, World!"


if __name__ == '__main__':
    #print("init call start>>>>>>>>>>>>>>")
    sess, graph = I.CNNpredict_init("world1-1")
    #print("init call end<<<<<<<<<<<<<<")
    app.run(debug=False, host="0.0.0.0")
