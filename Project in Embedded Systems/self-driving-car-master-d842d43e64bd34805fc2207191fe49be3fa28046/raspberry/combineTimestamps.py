import os
import csv


def getClosestIndex(timestamp, labelIndex, labelList):
    difference = abs(labelList[labelIndex]["timestamp"] - timestamp)
    while True:
        try:
            nextDifference = abs(labelList[labelIndex + 1]["timestamp"] -
                                 timestamp)
        except IndexError:
            print(timestamp)
            return False, difference
        if nextDifference > difference:
            return labelIndex, labelList[labelIndex]["timestamp"] - timestamp
        else:
            difference = nextDifference
            labelIndex += 1


def main():
    imageList = []
    for file in os.listdir("img"):
        if file.endswith(".jpg"):
            filename = str(file)
            fileTime = filename.split(".")[0][5:]
            # print(filename)
            # print(fileTime)
            imageList.append({"filename": filename, "filetime": int(fileTime)})
    orderedImages = sorted(imageList, key=lambda k: k['filetime'])

    for i in orderedImages[:10]:
        print(i)

    for file in os.listdir("buffer"):
        if str(file).endswith(".csv"):
            labelList = []
            with open("buffer/" + str(file), "r") as f:
                reader = csv.DictReader(f, delimiter=";")
                for row in reader:
                    # print(row)
                    timestamp = int(row['timestamp'])
                    servovalue = int(row['servovalue'][2])
                    labelList.append({"timestamp": timestamp,
                                      "servovalue": servovalue})
            # fileTime = filename.split(".")[0][5:]
            # print(filename)
            # print(fileTime)
            orderedLabels = sorted(labelList, key=lambda k: k['timestamp'])
            for i in orderedLabels[:10]:
                print(i)

            labelIndex = 0
            with open("buffer/parsed_" + str(file), "w") as f:
                fieldnames = ["filename", "servovalue", "millisDiff"]
                writer = csv.DictWriter(f, fieldnames=fieldnames,
                                        delimiter=";")
                writer.writeheader()
                for img in orderedImages:
                    labelIndex, difference = getClosestIndex(img["filetime"],
                                                             labelIndex,
                                                             orderedLabels)
                    if labelIndex:
                        servovalue = orderedLabels[labelIndex]["servovalue"]
                        writer.writerow({'filename': img["filename"],
                                         'servovalue': servovalue,
                                         'millisDiff': difference})
                    else:
                        print("First over: ", str(img["filename"]))
                        break


if __name__ == '__main__':
    main()
