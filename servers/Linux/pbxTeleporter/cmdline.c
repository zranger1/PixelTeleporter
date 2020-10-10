/* cmdline.c
 *
 * Serial -> UDP Bridge for Pixelblaze
 * Linux/Raspberry Pi version
 *
 * Reads data from a Pixelblaze by emulating a single (8 channel, 2048 pixel) output expander
 * board and forwards it over the network via UDP datagram on request.  The wire protocol is
 * described in Ben Hencke's Pixelblaze Output Expander board repository at:
 * https://github.com/simap/pixelblaze_output_expander
 *
 * Part of the PixelTeleporter project
 * 2020 by JEM (ZRanger1)
 * Distributed under the MIT license
*/
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <arpa/inet.h>
#include "cmdline.h"

// title and version
const char *argp_program_version = "pbxTeleporter v1.1.0 for Linux/Pi";

// --help documentation
static char doc[] = "\npbxTeleporter bridge v1.1.0\n"
		"Makes LED controller output available via UDP\n"
		"This version works exclusively with the Pixelblaze controller.\n";

// TODO -- pick an email address we can live with The Internet knowing for this
const char *argp_program_bug_address = NULL;

// argument list for --help
static char args_doc[] = "<Serial device name>";

// Availble options.
static struct argp_option options[] = {
		{"ip"          ,'i',"<IPv4 address>" , 0, "IPv4 address to use for net communication. Default: 0.0.0.0 (All available)"},
		{"listen-port" ,'l',"<portno>", 0,"TCP/IP port number on which to listen for commands. Default 8081."},
		{"send-port"   ,'s',"<portno>", 0,"TCP/IP port number on which to send data. Default 8082."},
		{0}
};

// IP address string validator -- returns 1 if valid, 0 otherwise
int isIpAddress(char *ipAddress) {
	struct sockaddr_in sa;
	int result = inet_pton(AF_INET, ipAddress, &(sa.sin_addr));
	return (result == 1);
}

// Serial device validator -- returns 1 if valid, 0 otherwise
// Checks for device name syntax only -- doesn't mess w/physical devices
int isSerialDevice(char *dev) {
	if (strncmp(dev,"/dev/",5) == 0) return 1; // linux
	if ((strncmp(dev,"COM",3) | strncmp(dev,"com",3)) == 0) return 1; // windows

	return 0;
}

// Parse command line arguments and options.
static error_t parse_opt(int key, char *arg, struct argp_state *state){

	commandline *arguments = state->input;
	switch(key){

	case 'i':  // ip address
		if (isIpAddress(arg)) {
			arguments->bind_ip = arg;
		}
		else {
			argp_error(state,"Invalid IP address. ");
		}
		break;
	case 'l':  // listen udp port
		arguments->listen_port = atoi(arg);
		break;
	case 's':  // send udp port
		arguments->send_port = atoi(arg);
		break;

	case ARGP_KEY_ARG: // process serial port argument
		if(state->arg_num > 1) {
			argp_usage(state);
		}
		if (isSerialDevice(arg)) {
			arguments->serial_port = arg;
		}
		else {
			argp_error(state,"Invalid serial device name. ");
		}
		break;

	case ARGP_KEY_END: // make sure we got our serial port
		if(state->arg_num < 1)
			argp_usage(state);
		break;

	default:
		return ARGP_ERR_UNKNOWN;
	}

	return 0;
}

// initialize the argp struct. Which will be used to parse and use the args.
struct argp argparser = {options, parse_opt, args_doc, doc};

