import tensorflow as tf
import numpy as np

import Training

from DataLoader import DataLoader

PREDICTION_THRESHOLD = 0.10
LABEL_CLASS = 3
def CNNtrain(train_x, train_y, model_name):

    import modelCNN as NN

    with tf.Session() as sess:
        init = tf.global_variables_initializer()
        sess.run(init)


        Training.train(sess,NN.optimizer, 
                        NN.x, NN.y_, train_x, train_y, 
                        loss_funciton=NN.cost, 
                        )

        Training.save(sess,model_name)


def CNNpredict_init(model_name):
    #print("\n\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!! CNN init!!!!!!!!!!!!!!!!!!\n\n\n\n")
    path = "./model/" + model_name
    meta_file = path + ".meta"
    new_saver = tf.train.import_meta_graph(meta_file)

    sess =  tf.Session() 
    new_saver.restore(sess, tf.train.latest_checkpoint('./model'))
    graph = tf.get_default_graph()
    #for n in graph.as_graph_def().node:
        #print(n.name)

    return sess, graph



def CNNpredict(test_x, sess, graph):

    make_logits = graph.get_tensor_by_name("logits:0")
    new_x = graph.get_tensor_by_name("featureX:0")
    ret = sess.run(make_logits,
            feed_dict={new_x:test_x})

    pred = sess.run(tf.argmax(ret,1))


    top_n = tf.reduce_sum(tf.nn.top_k(ret, LABEL_CLASS)[0],1)
    top_1 = tf.reduce_sum(tf.nn.top_k(ret, 1)[0],1)

    poss = sess.run(top_1/top_n)

    
    return pred, poss


def confidence(ret,poss):
    one = poss[0][1]
    five = poss[0][5]
    return np.abs((one-five))/(one+five)

def interface_train_with_mnist():
    data = DataLoader()
    train_X, train_Y = data.getMINSTTrainData(one_hot=True)
    train_X_, train_Y_ = data.getMINSTTrainData()
    
    model_name = "interface_test_with_mnist"

    CNNtrain(train_X, train_Y, model_name)

def interface_test_with_mnist():
    data = DataLoader()
    test_X, test_Y = data.getMINSTTestData()
    
    model_name = "interface_test_with_mnist"

    pred_input = test_X[0:100]
    target = test_Y[0:100]
    pred_output = CNNpredict(pred_input, model_name)

    accuracy = np.mean([target == pred_output])
    print(accuracy)

