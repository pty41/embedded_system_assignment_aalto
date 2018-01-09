
import tensorflow as tf
import math

FLAGS = None

IMAGE_HEIGHT = 47  
IMAGE_WIDTH = 80

IMAGE_SIZE = IMAGE_HEIGHT * IMAGE_WIDTH
LABEL_CLASS = 3


def deepnn(x):
    """deepnn builds the graph for a deep net for classifying digits.

    Args:
        x: an input tensor with the dimensions (N_examples, IMAGE_SIZE), where IMAGE_SIZE is the
        number of pixels in a standard MNIST image.

    Returns:
        A tuple (y, keep_prob). y is a tensor of shape (N_examples, 10), with values
        equal to the logits of classifying the digit into one of 10 classes (the
        digits 0-9). keep_prob is a scalar placeholder for the probability of
        dropout.
    """
    # Reshape to use within a convolutional neural net.
    # Last dimension is for "features" - there is only one here, since images are
    # grayscale -- it would be 3 for an RGB image, 4 for RGBA, etc.
    with tf.name_scope('reshape'):
        x_image = tf.reshape(x, [-1, IMAGE_HEIGHT, IMAGE_WIDTH, 1])

    # First convolutional layer - maps one grayscale image to 32 feature maps.
    with tf.name_scope('conv1'):
        W_conv1 = weight_variable([5, 5, 1, 32])
        b_conv1 = bias_variable([32])
        h_conv1 = tf.nn.relu(conv2d(x_image, W_conv1) + b_conv1)

    # size of h_conv1 = (45*160 * 32)
    # Pooling layer - downsamples by 2X.
    with tf.name_scope('pool1'):
        h_pool1 = max_pool_2x2(h_conv1)

    # size of h_conv1 = (23*80 * 32)

    # Second convolutional layer -- maps 32 feature maps to 64.
    with tf.name_scope('conv2'):
        W_conv2 = weight_variable([5, 5, 32, 64])
        b_conv2 = bias_variable([64])
        h_conv2 = tf.nn.relu(conv2d(h_pool1, W_conv2) + b_conv2)
    # size of h_conv2 = (23*80 * 64)

    # Second pooling layer.
    with tf.name_scope('pool2'):
        h_pool2 = max_pool_2x2(h_conv2)
    # size of h_pool2 = (12*40 * 64)


    # Fully connected layer 1 -- after 2 round of downsampling, our 28x28 image
    # is down to 7x7x64 feature maps -- maps this to 1024 features.
    with tf.name_scope('fc1'):
        #TODO: may lead to fault
        W_fc1 = weight_variable([round(IMAGE_HEIGHT/4) * round(IMAGE_WIDTH/4) * 64, 1024])
        b_fc1 = bias_variable([1024])

        h_pool2_flat = tf.reshape(h_pool2, [-1, round(IMAGE_HEIGHT/4) * round(IMAGE_WIDTH/4) * 64])
        h_fc1 = tf.nn.relu(tf.matmul(h_pool2_flat, W_fc1) + b_fc1)

    # Dropout - controls the complexity of the model, prevents co-adaptation of
    # features.
    with tf.name_scope('dropout'):
        keep_prob=tf.Variable(tf.constant(1.0))
        h_fc1_drop = tf.nn.dropout(h_fc1, keep_prob)

    # Map the 1024 features to 10 classes, one for each digit
    with tf.name_scope('fc2'):
        W_fc2 = weight_variable([1024, 10])
        b_fc2 = bias_variable([10])

    return tf.add(tf.matmul(h_fc1_drop, W_fc2) , b_fc2, name = "logits")


def conv2d(x, W):
    """conv2d returns a 2d convolution layer with full stride."""
    #NOTE: "SAME" means the output size = in_height/strides,
    #       https://www.tensorflow.org/api_guides/python/nn#convolution
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding='SAME')


def max_pool_2x2(x):
    """max_pool_2x2 downsamples a feature map by 2X."""
    return tf.nn.max_pool(x, ksize=[1, 2, 2, 1],
   strides=[1, 2, 2, 1], padding='SAME')


def weight_variable(shape):
    """weight_variable generates a weight variable of a given shape."""
    initial = tf.truncated_normal(shape, stddev=0.1)
    return tf.Variable(initial)


def bias_variable(shape):
    """bias_variable generates a bias variable of a given shape."""
    initial = tf.constant(0.1, shape=shape)
    return tf.Variable(initial)


def my_deepnn(x):
    with tf.name_scope('hidden1'):
        weights = tf.Variable(
                tf.truncated_normal([IMAGE_SIZE, 18],
           stddev=1.0 / math.sqrt(float(IMAGE_SIZE))),
                name='weights')
        biases = tf.Variable(tf.zeros([18]),
    name='biases')
        hidden1 = tf.nn.relu(tf.matmul(x, weights) + biases)
    # Hidden 2
    with tf.name_scope('hidden2'):
        weights = tf.Variable(
                tf.truncated_normal([18, 10],
           stddev=1.0 / math.sqrt(float(18))),
                name='weights')
        biases = tf.Variable(tf.zeros([10]),
    name='biases')
        hidden2 = tf.nn.relu(tf.matmul(hidden1, weights) + biases)
    # Linear
    with tf.name_scope('softmax_linear'):
        weights = tf.Variable(
                tf.truncated_normal([10, 10],
           stddev=1.0 / math.sqrt(float(10))),
                name='weights')
        biases = tf.Variable(tf.zeros([10]),
    name='biases')
    logits = tf.add(tf.matmul(hidden2, weights) , biases, name='logits')
    return logits


def loss(logits, labels):
    labels = tf.to_int64(labels)
    cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(
            labels=labels, logits=logits, name='xentropy')
    return tf.reduce_mean(cross_entropy, name='cost')


def training(loss, learning_rate):
        with tf.name_scope("training_func"):
            # Add a scalar summary for the snapshot loss.
            tf.summary.scalar('loss', loss)
            # Create the gradient descent optimizer with the given learning rate.
            s_optimizer = tf.train.GradientDescentOptimizer(learning_rate)
            # Create a variable to track the global step.
            #global_step = tf.Variable(0, name='global_step', trainable=False)
            # Use the optimizer to apply the gradients that minimize the loss
            # (and also increment the global step counter) as a single training step.
            #train_op = optimizer.minimize(loss, global_step=global_step, name="optimizer")
            train_op = s_optimizer.minimize(loss,   name="optimizer")
            return train_op
 



    # Create the model
x = tf.placeholder(tf.float32, [None, IMAGE_SIZE] ,name="featureX")

    # Define loss and optimizer
y_ = tf.placeholder(tf.int32, [None])

    # Build the graph for the deep net

logits = deepnn(x)

with tf.name_scope('loss'):
    cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(labels=y_,
                      logits=logits)
cost = tf.reduce_mean(cross_entropy)


optimizer = tf.train.AdamOptimizer(1e-4).minimize(cost)
