from flask import Flask, request
import numpy as np
import pickle
import pandas as pd
import sklearn

app=Flask(__name__)

pickle_in = open("classifier.pkl","rb")
classifier=pickle.load(pickle_in)

@app.route('/')
def welcome():
    return "Welcome All"

@app.route('/predict_single',methods=["Get"])
def predict_single_data_point():
    AccX = request.args.get("Acc X")
    AccY = request.args.get("Acc Y")
    AccZ = request.args.get("Acc Z")
    GyroX = request.args.get("gyro_x")
    GyroY = request.args.get("gyro_y")
    GyroZ = request.args.get("gyro_z")
    prediction = classifier.predict([[AccX,AccY,AccZ,GyroX,GyroY,GyroZ]])
    print(prediction)
    return "Label = "+str(prediction)

@app.route('/predict_file',methods=["POST"])
def predict_multiple_data_points():
    df_test = pd.read_csv(request.files.get("file"))
    prediction = classifier.predict(df_test)
    classlist = list(prediction)
    percentone = (len([ele for ele in classlist if ele == 1]) / len(classlist)) * 100
    safetyscore = 10
    if percentone > 50:
        safetyscore = 4
    elif percentone > 40:
        safetyscore = 5
    elif percentone > 35:
        safetyscore = 6
    elif percentone > 30:
        safetyscore = 7
    elif percentone > 25:
        safetyscore = 8
    elif percentone > 20:
        safetyscore = 9
    else:
        safetyscore = 10

    #return str(list(prediction))
    #return str(percentone)
    return str(safetyscore)

if __name__=='__main__':
    app.run(host='0.0.0.0')