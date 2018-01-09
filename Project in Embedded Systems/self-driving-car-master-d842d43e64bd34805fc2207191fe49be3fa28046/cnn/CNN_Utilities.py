from __future__ import print_function
import pickle
import numpy as np
import random

#import matplotlib.pyplot as plt



def save_np_array(np_list, filename):
    with open(filename, 'wb') as f:
        pickle.dump(list(np_list),f)

def restore_np_array(filename):
    with open(filename,'rb') as f:
        ret = pickle.load(f)
    return np.asarray(ret)

def split_data(image, label, p):
    #
    # p is the proportion of the train_data, should be <=1
    #
    # return two list:
    #   train_data: the index of train data
    #   test_data: the index of test data

    if p>1: 
        #train_data, test_data
        return np.asarray(range(image.shape[0])), np.array([])

    train_data =np.array([])
    test_data =np.array([])

    unique, counts = np.unique(label, return_counts=True)

    for u,c in zip(unique, counts):
        idx = np.where(label == u)[0]
        beacon = int(np.around(c * p))
        train_data = np.append( train_data, idx[0:beacon])
        test_data = np.append( test_data, idx[beacon::])
    
    return train_data.astype(int), test_data.astype(int)
        


def over_sampling_data(image,label):

    IMAGE_SIZE = image.shape[1]

    unique, counts = np.unique(label, return_counts=True)

    max_idx = np.argmax(counts)

    print("The maximum counts is: ",counts[max_idx], " | servo_value: ", unique[max_idx])

    NEW_ROW_SIZE = counts[max_idx] * unique.shape[0]


    sampling_label = np.zeros((NEW_ROW_SIZE,))
    sampling_image = np.zeros((NEW_ROW_SIZE, IMAGE_SIZE))

    sampling_label = np.array([])
    sampling_image = np.zeros([1,IMAGE_SIZE]) #append() should have the same dimension in specific axis
    sampling_idx = np.array([])

    for ser_val in unique:
        print("over sampling label", ser_val, "...")

        idx = np.where(label == ser_val)
        image_s = image[idx]
        label_s = label[idx]
        index_s = idx[0]

        amount = idx[0].shape[0]

        copy_times = counts[max_idx] // amount
        copy_tail = counts[max_idx] % amount

        for t in range(copy_times):
            sampling_label = np.append(sampling_label, label_s)
            sampling_image = np.append(sampling_image, image_s,axis=0)
            sampling_idx = np.append(sampling_idx, index_s)

        sampling_label = np.append(sampling_label, label_s[0:copy_tail])
        sampling_image = np.append(sampling_image, image_s[0:copy_tail],axis=0)
        sampling_idx = np.append(sampling_idx, index_s[0:copy_tail])

    sampling_image = sampling_image[1::,:] # clean the first row, which is the place holder
    return sampling_image, sampling_label, sampling_idx

def uniform_sampling_data(image, label):
    IMAGE_SIZE = image.shape[1]

    unique, counts = np.unique(label, return_counts=True)

    min_idx = np.argmin(counts)

    print("The minimum counts is: ",counts[min_idx], " | servo_value: ", unique[min_idx])

    NEW_ROW_SIZE = counts[min_idx] * unique.shape[0]


    sampling_label = np.zeros((NEW_ROW_SIZE,))
    sampling_image = np.zeros((NEW_ROW_SIZE, IMAGE_SIZE))

    sampling_label = np.array([])
    sampling_image = np.zeros([1,IMAGE_SIZE]) #append() should have the same dimension in specific axis
    sampling_idx = np.array([])

    for ser_val in unique:
        print("sampling label", ser_val, "...")
        idx_list = random.sample(list(np.where(label==ser_val)[0]), counts[min_idx])
        idx_list = np.asarray(idx_list)
        sampling_label = np.append(sampling_label, label[idx_list])
        sampling_image = np.append(sampling_image, image[idx_list],axis=0)
        sampling_idx = np.append(sampling_idx, idx_list)

    sampling_image = sampling_image[1::,:] # clean the first row, which is the place holder
    return sampling_image, sampling_label, sampling_idx

def shuffle_data(image, label):
    tmp = np.append(image, label[:,None], axis=1)
    np.random.shuffle(tmp)
    r_img = tmp[:,0:-1]
    r_label = tmp[:,-1]
    return r_img, r_label

#def validate_data(sampling_image, sampling_label, sampling_idx):
    #print("validating the sampling image..")

    #image_plot = random.randint(0, sampling_image.shape[0])
    #pixels = np.array(sampling_image[image_plot] * 255, dtype='uint8')
    #pixels = pixels.reshape((45, 160))
    #label = sampling_label[image_plot]
    #plt.title('Label: {label} - Index: {index}'.format(label=label,index = sampling_idx[image_plot]))
    #plt.imshow(pixels, cmap='gray', interpolation='none')
    #plt.show()


if __name__ == '__main__':
    IMAGE_DATA = "./original_data/image_data.list"
    LABEL_DATA = "./original_data/label_data.list"
    image = restore_np_array(IMAGE_DATA).astype(float)
    label = restore_np_array(LABEL_DATA).astype(int)

    sampling_image, sampling_label, sampling_idx = uniform_sampling_data(image,label)

    print("new sampled shape = label: ", sampling_label.shape, " image: ", sampling_image.shape)

    #validate_data(sampling_image,sampling_label,sampling_idx)
