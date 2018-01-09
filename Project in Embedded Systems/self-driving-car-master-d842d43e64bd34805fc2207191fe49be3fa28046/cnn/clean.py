import pickle
import CNN_Utilities as U
import numpy as np

test = U.restore_np_array("label_data.list").astype(int)

print(np.sum(test))

for i in range(1,len(test)):
    if (test[i-1] == 1 and test[i] == 9):
        test[i]=1

print(np.sum(test))
U.save_np_array(test, "label_data_washed.list")

