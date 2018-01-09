# This is the Operator, who takes responsibility of operations of Neural Network models
# There are three basic operation:
#       1. training: The operation will train the network and save it
#           TODO:   - customized saving point
#                   - auto check point
#       2. testing: testing the data
#           TODO:   - accuracy estimate
#       3. predicting: predict the data
#       4. saving: save the trained session




import numpy as np
import tensorflow as tf

MINI_BATCH_SIZE = 50

TRAINING_EPOCH = 20
DISPLAY_STEP = 50


def train(sess,optimizer, X, Y , X_data_batch, Y_data_batch, loss_funciton=None):
    # sess: session
    # X: placeholder of X
    # X_data_batch: train data set, shape=(amount of data, pixels of an image)
    print("Start to train data, shape=(amount of data, pixels of an image): ", X_data_batch.shape)
    batch_count = X_data_batch.shape[0] // MINI_BATCH_SIZE
    rest_batch = X_data_batch.shape[0] % MINI_BATCH_SIZE


    print("mini_batch_size = ", MINI_BATCH_SIZE, " number of epoches: ", 
            TRAINING_EPOCH, ". In total: ", TRAINING_EPOCH*(batch_count+1)," times traning" )
    
    for epoch in range(TRAINING_EPOCH):
        for i_ in range(batch_count):
            idx = i_ * MINI_BATCH_SIZE
            minibatch_data_x = X_data_batch[idx:idx + MINI_BATCH_SIZE]
            minibatch_data_y = Y_data_batch[idx:idx + MINI_BATCH_SIZE]
            sess.run(optimizer, feed_dict={X: minibatch_data_x, 
                                            Y: minibatch_data_y})

            if (epoch* i_ +1) % DISPLAY_STEP == 0:
                print("- training step:", '%04d' % (epoch*i_+1))
                print("-- in epoch:", '%04d' % (epoch+1))
                if loss_funciton is not None:
                    training_cost = sess.run(loss_funciton,feed_dict={X: X_data_batch, Y: Y_data_batch})
                    print(" --> cost=", training_cost, '\n')
        
        if not (rest_batch == 0):
            sess.run(optimizer, feed_dict={X: X_data_batch[-rest_batch::], 
                                            Y: Y_data_batch[-rest_batch::]})




    print("Optimization Finished!")
    if loss_funciton is not None:
        training_cost = sess.run(loss_funciton,feed_dict={X: X_data_batch, Y: Y_data_batch})
        print("Training cost=", training_cost, '\n')

def save(sess, model_name):
    saver = tf.train.Saver()
    path = "./model/" + model_name  
    print("Saving model to: " ,path)
    ret = saver.save(sess, path )
    print("saver ret = ", ret)

def restore(sess,model_name):
    path = "./model/" + model_name
    meta_file = path + ".meta"
    new_saver = tf.train.import_meta_graph(meta_file)
    print("Restroe model from: " ,path)
    new_saver.restore(sess, path)



def test(sess,X, Y , X_data_batch, Y_data_batch, loss_funciton):
    init = tf.global_variables_initializer()
    sess.run(init)
    testing_cost = sess.run(loss_funciton,
        feed_dict={X: X_data_batch, Y: Y_data_batch})  # same function as cost above
    print("Testing cost=", testing_cost)

def predict(sess,X,  X_data, predict_funciton):
    # Run the initializer
    feed_dict={X: X_data}  # same function as cost above
    ret = sess.run( predict_funciton ,
        feed_dict=feed_dict)  # same function as cost above
    pred_value = tf.nn.top_k(ret)
    print_value =sess.run(pred_value)[1]
    print("predict=",print_value[:,0])
    print("another approach")
    correct = tf.argmax(predict_funciton, 1)
    ret = sess.run(correct, 
        feed_dict=feed_dict)  # same function as cost above
    print ("argmax=", ret)
