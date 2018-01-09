import CNNInterface as I
import CNN_Utilities as U
import numpy as np

def confidence(ret,poss):
    one = poss[0][1]
    five = poss[0][5]
    return np.abs((one-five))/(one+five)




img = U.restore_np_array("./image_data.list").astype(float)
label = U.restore_np_array("./label_data.list").astype(float)


idx_3 = np.where(label==2)

ret,poss = I.CNNpredict(img[idx_3][0:50], "world1-2")

print("predict value: ", ret)
print("possibility: ", poss)


