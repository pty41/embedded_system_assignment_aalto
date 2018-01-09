import CNNInterface as I
from DataLoader import DataLoader
import numpy as np

data = DataLoader()
train_X, train_Y = data.getMINSTTrainData()
test_X, test_Y = data.getMINSTTestData()


print("train shape", train_X.shape, train_Y.shape)

'''
#test case: is there any problem with less label?
#for example, what happens if we manually set all 
#the label of '3' as '5', will it decrease the accuracy?


# we will treat 0,1,2,3,4,5 as 1

t_new_idx = np.where(train_Y <= 5)
train_Y[t_new_idx] = 1.

#same as test

t_new_test_idx = np.where(test_Y <=5)
test_Y[t_new_test_idx] = 1.
'''

#test 2: make some overlap with data, we gonna to mark 1/4 of
# image "4" as 5, so foth and so on

idx_all = []
for i in range(10):
    idx_all.append( np.where(train_Y == i)[0])

for i in range(10):
    idx = idx_all[i]
    print(idx)
    print(idx.shape)
    l = idx.shape[0]
    _idx = idx[0:l//8]
    print("length of ",i," is ",l, ", 1/8 = ", _idx.shape[0])
    print(i, " <> ",(i+1) % 10)
    print("origin: ",train_Y[_idx])
    train_Y[idx] = (i+1) % 10.
    print("new: ",train_Y[_idx])




model_name = "interface_test_with_mnist"

I.CNNtrain(train_X, train_Y, "world1-1")

ret  = I.CNNpredict(test_X,"world1-1")



accuracy = np.mean([ret == test_Y])


print(accuracy)
