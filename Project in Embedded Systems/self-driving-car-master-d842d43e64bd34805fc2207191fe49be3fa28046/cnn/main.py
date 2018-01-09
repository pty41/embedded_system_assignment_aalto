import CNNInterface as I
import CNN_Utilities as U
import numpy as np

#I.interface_train_with_mnist()
#I.interface_test_with_mnist()


img = U.restore_np_array("./image_data.list")
label = U.restore_np_array("./label_data.list")
label = label.astype(float)
image = img.astype(float)

idx_not_9 = np.where((label == 1.) | (label == 2.) | (label ==3.))

print(idx_not_9)
label = label[idx_not_9]
image = image[idx_not_9]

train_idx, test_idx = U.split_data(image, label, 0.7) #70% train, 30% test

s_train_image, s_train_label, s_train_idx = U.over_sampling_data(image[train_idx], label[train_idx])
s_test_image, s_test_label, s_test_idx = U.over_sampling_data(image[test_idx], label[test_idx])

trainX, trainY= U.shuffle_data(s_train_image, s_train_label)
testX, testY= U.shuffle_data(s_test_image, s_test_label)

I.CNNtrain(trainX, trainY, "world1-2")




#ret,_ = I.CNNpredict(testX, "world1-2")
print ("predict result:" )

#print(ret)


print ("actual result:" )
#print(testY)

#accuracy = np.mean([ret == testY])
#print("accuracy: ",accuracy )


