
import numpy as np

from tensorflow.examples.tutorials.mnist import input_data

class DataLoader:
    def __init__(self):
        print("init DataLoader")
    def getLinearTrainData(self):
        train_X = np.asarray([3.3,4.4,5.5,6.71,6.93,4.168,9.779,6.182,7.59,2.167,
                         7.042,10.791,5.313,7.997,5.654,9.27,3.1])
        train_Y = np.asarray([1.7,2.76,2.09,3.19,1.694,1.573,3.366,2.596,2.53,1.221,
                         2.827,3.465,1.65,2.904,2.42,2.94,1.3])
        return train_X, train_Y
    def getLinearTestData(self):
        test_X = np.asarray([6.83, 4.668, 8.9, 7.91, 5.7, 8.7, 3.1, 2.1])
        test_Y = np.asarray([1.84, 2.273, 3.2, 2.831, 2.92, 3.24, 1.35, 1.03])
        return test_X, test_Y
    def getMINSTTrainData(self,one_hot=False):
        mnist = input_data.read_data_sets("./MNIST_data/",one_hot=one_hot)
        batch = mnist.train.next_batch(1000)
        X = batch[0]
        Y = np.copy(batch[1])
        return X,Y

    def getMINSTTestData(self,one_hot=False):
        mnist = input_data.read_data_sets("./MNIST_data/",one_hot=one_hot)
        X = mnist.test.images
        Y = np.copy(mnist.test.labels)
        return X, Y
