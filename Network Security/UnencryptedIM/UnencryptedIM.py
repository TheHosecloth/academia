"""
	UnencryptedIM.py
		Creates a peer-to-peer instant messaging session between a host server
		and a connecting client. Users can terminate the session by pressing
		CTRL + C

	command line options:
		-s        executes server instance of instant messaging app,
				  does not require a defined argument

		-c        executes client instance of instant messsaging app,
		  		  requires hostname argument
"""

import argparse, IMserver, IMclient

# instantiate parser
parser = argparse.ArgumentParser()

# option -s can be called without delaring an argument
parser.add_argument('-s', '--server', nargs = '?', help = 'executes instant messaging \
	application and creates a server socket for a client to connect to')
# option -c, however, must take a hostname to form a client connection
parser.add_argument('-c', '--client', help = 'takes hostname as argument and connects to a \
	server instance of the messaging application, allowing message exchange')

# parse arguments and check which option was picked
args = parser.parse_args()

if args.client:
	IMclient.IM_client(args.client)

else:
	IMserver.IM_server()