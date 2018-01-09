import xmlrpc.client
import sys

def usage():
    print ('''usage: 
        image_test.py <Training|Prediction> <src>
        src: source file path
    ''')
    exit(2)


if __name__ == '__main__':
	#file_connect = XMLServerProxy("http://%s:%d" % ("localhost", 1234), allow_none=True)
	#file_connect.image_execute("Training")
	if len(sys.argv) != 3:
		usage()
	option = sys.argv[1]
	src_file = sys.argv[2]
	if (option in ["Training", "Prediction"]):
		with xmlrpc.client.ServerProxy("http://%s:%d" % ("localhost", 1234)) as proxy:
			proxy.image_execute(option, src_file)
	else:
		usage()
