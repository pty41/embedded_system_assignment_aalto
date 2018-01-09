# Neural Network Module

## Overview

The main task of this module is to provide a neural network with 
reliable performance of predicting decisions based on the input
image from web camera of the RC car.

The module contains 3 parts: Data pre-preprocessing, Neural network
and Data delivering.


## Neural Network sub-module

This sub-module will have 3 different stages: Training, Testing and Predicting.

### Behavior of Training Stage

In this stage, the Neural Network will be able to read the data from
other module and train the neural network with a bucket of data stored
in the storage devices.


```
Read the formatted key-value pair
-> train the neural network
-> return the trained parameters

TODO: need to figure out how to return the trained parameters
```

Processes of training

1. trainer.py --load_module=[...] --load_data=[...] --save_module=[...] --train_step=[...]


Data pre-processing is designed to adjust different data format from
other modules. After this process, the data will be formatted as
following:

```
# TODO: 
#   1. try to use the same input data format of previous self-driving
#      car project.
#   2. modified to the 


```