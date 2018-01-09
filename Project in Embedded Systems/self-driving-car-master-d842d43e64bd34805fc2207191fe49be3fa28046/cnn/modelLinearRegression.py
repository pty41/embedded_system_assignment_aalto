#This is the Model of LinearRegression
# A model will consist : 
#   {model_fn: return an optimizer }


import tensorflow as tf
import numpy
import matplotlib.pyplot as plt
rng = numpy.random

# Parameters
learning_rate = 0.01

# tf Graph Input
X = tf.placeholder("float")
Y = tf.placeholder("float")

def logits(X,W,b):
    pred = tf.add(tf.multiply(X, W), b)
    return pred

def model_fn(X_data_batch,Y_data_batch):

    # Training Data

    n_samples = X_data_batch.shape[0]


    # Set model weights
    W = tf.Variable(rng.randn(), name="weight")
    b = tf.Variable(rng.randn(), name="bias")

    # Construct a linear model
    pred = logits(X,W,b)
    # Mean squared error
    cost = tf.reduce_sum(tf.pow(pred-Y, 2))/(2*n_samples)

    optimizer = tf.train.GradientDescentOptimizer(learning_rate).minimize(cost)

    # Initialize the variables (i.e. assign their default value)
    init = tf.global_variables_initializer()
    return optimizer,cost,X,Y

def pred_fn (X_data):
    pred = logits(X,W,b)
    return pred, X