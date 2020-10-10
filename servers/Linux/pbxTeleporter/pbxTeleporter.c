/* pbxTeleporter.c
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
#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>
#include <signal.h>
#include <sched.h>
#include <time.h>

#include "pbxTeleporter.h"
#include "pbxSerial.h"
#include "udpServer.h"
#include "cmdline.h"

// TODO -- per channel buffers for virtual wiring
// TODO -- support color order, again for virtual wiring
// TODO -- support extra APA brightness bits on HDR monitors?

// Global variables -- handles, buffers and pointers
int serialHandle = -1;                  // file descriptor for active serial device.
udpServer *udp;                         // network server object
uint8_t pixel_buffer[BUFFER_SIZE];      // per-pixel RGB data for current frame
uint8_t *pixel_ptr;                     // current write position in buffer
uint16_t pixelsReady;                   // number of pixels if frame is ready, 0 otherwise
int runFlag;                            // run status - 1 = keep running, 0 = shutdown

/////////////////////////////////
// Utility Functions
/////////////////////////////////

// linux approximation of Windows GetTickCount();
uint64_t getTickCount() {
	struct timespec ts;
	uint64_t ticks;

	clock_gettime(CLOCK_MONOTONIC, &ts);

	ticks = (uint64_t) ts.tv_nsec / 1000000;
	ticks += (uint64_t) ts.tv_sec * 1000;

	return ticks;
}

//
// Reads the specified number of bytes into a buffer
void readBytes(uint8_t *buf, uint16_t size) {

	for (int i = 0; i < size ;i++) {
	  serialGetbyte(serialHandle,buf++);
	}
}

// read a single byte from the serial device
uint8_t readOneByte() {
	uint8_t b;
	serialGetbyte(serialHandle,&b);
	return b;
}

// readMagicWord
// returns true if we've found the magic word "UPXL"
// false otherwise. Clunky, but fast.
bool readMagicWord() {
	if (readOneByte() != 'U') return false;
	if (readOneByte() != 'P') return false;
	if (readOneByte() != 'X') return false;
	if (readOneByte() != 'L') return false;

	return true;
}

// crcCheck()
// read and discard 32-bit CRC from data buffer 
void crcCheck() { 
	uint32_t crc;
	readBytes((uint8_t *) &crc,sizeof(crc));
}

/////////////////////////////////
// Command Handlers
/////////////////////////////////

// read pixel data in WS2812 format
// NOTE: Only handles 3 byte RGB data for now.  Discards frame
// if it's any other size.
void doSetChannelWS2812() {
	PBWS2812Channel ch;
	uint16_t data_length;

	readBytes((uint8_t *) &ch,sizeof(ch));

	// read pixel data if available
	if (ch.pixels && (ch.numElements == 3) && (ch.pixels <= MAX_PIXELS)) {
		data_length = ch.pixels * ch.numElements;
		readBytes(pixel_ptr,data_length);
		pixel_ptr += data_length;
	}

	crcCheck();
}

// read pixel data in APA 102 format
void doSetChannelAPA102() {
	PBAPA102DataChannel ch;

	readBytes((uint8_t *) &ch,sizeof(ch));

	// APA 102 data is always four bytes. The first byte
	// contains a 3 bit flag and 5 bits of "extra" brightness data.
	// We're gonna discard the "extra" APA bits and put 3-byte RGB
	// data into the output buffer.
	if (ch.frequency && (ch.pixels <= MAX_PIXELS)) {
		for (int i = 0; i < ch.pixels;i++) {
			readOneByte();
			readBytes(pixel_ptr,3);
			pixel_ptr += 3;
		}
	}

	crcCheck();
}

// draw all pixels on all channels using current data
// flags the frame as available to the network transport.
void doDrawAll() {
	pixelsReady = (pixel_ptr - pixel_buffer);
	pixel_ptr = pixel_buffer;
}

// read APA 102 clock data.  
// For now, we ignore this. Eventually, we may have to at least keep the
// desired frequency for virtual wiring
void doSetChannelAPA102Clock() {
	PBAPA102ClockChannel ch;

	readBytes((uint8_t *) &ch,sizeof(ch));
	crcCheck();
}

// Signal handling for clean shutdown
//
void pbxSignalHandler(int s){
     printf("\npbxTeleporter: %s\n",strsignal(s));
     runFlag = 0;
}

// setup
// Get configuration from command line and intialize serial and
// network communication
bool setup(int argc, char *argv[]) {
	commandline arguments;
	struct sigaction sigIntHandler;

// initialize and enable the main loop
	runFlag = 1;
	pixelsReady = 0;
	pixel_ptr = pixel_buffer;

// set defaults for parameters
	arguments.serial_port = "";
	arguments.listen_port = DEFAULT_LISTEN_PORT;
	arguments.send_port = DEFAULT_SEND_PORT;
	arguments.bind_ip = "";

// parse cli arguments.
	argp_parse(&argparser, argc, argv, 0, 0, &arguments);

	printf("pbxTeleporter v1.0.0 for Linux/Raspberry Pi\n");
	printf("    Serial Device: %s\n", arguments.serial_port);
	printf("    IP Address:    %s\n", (0 == strlen(arguments.bind_ip) ? "All" : arguments.bind_ip));
	printf("    Listen Port:   %i", arguments.listen_port);
	printf("    Send Port:     %i", arguments.send_port);
	printf("\n");

	printf("Initializing...\n");

// set up signal handler for clean termination
    sigIntHandler.sa_handler = pbxSignalHandler;
	sigemptyset(&sigIntHandler.sa_mask);
	sigIntHandler.sa_flags = 0;
	sigaction(SIGINT, &sigIntHandler, NULL);

// open and configure serial device
	printf("    Opening serial device %s\n",arguments.serial_port);

	serialHandle = serialOpen (arguments.serial_port, RCV_BITRATE);
	if (serialHandle == -1) {
		printf("   ERROR: Unable to open serial device %s\n",arguments.serial_port);
		exit(1);
	}
	printf("    %s open at %lu bps\n",arguments.serial_port,RCV_BITRATE);

// set up UDP server
	printf("    Initializing UDP transport\n");

	udp = createUdpServer(arguments.bind_ip, arguments.listen_port,arguments.send_port);

	if (udp == NULL) {
		printf("   Error: Unable to create UDP socket\n");
		exit(-1);
	}
	printf("    Network ready\n");

	printf("Initialization successful.\n");
	printf("pbxTeleporter running. <Ctrl-C> to terminate.\n");
	serialFlush(serialHandle);
	return true;
}

// main
// get pixel data from the serial device, forward packets to clients
// via UDP
int main(int argc, char *argv[]) {
	PBFrameHeader hdr;

// initialize configuration and serial and net comms.
	setup(argc,argv);

	// loop forever
	while (runFlag) {
		// read characters 'till we get the magic sequence
		if (readMagicWord()) {
			readBytes((uint8_t *) &hdr,sizeof(hdr));

			switch (hdr.command) {
			case SET_CHANNEL_WS2812:
				doSetChannelWS2812();
				break;
			case DRAW_ALL:
				doDrawAll();
				break;
			case SET_CHANNEL_APA102_DATA:
				doSetChannelAPA102();
				break;
			case SET_CHANNEL_APA102_CLOCK:
				doSetChannelAPA102Clock();
				break;
			default:
				break;
			}
		}
	}

	printf("pbxTeleporter shutting down.\n");
	destroyUdpServer(udp);
	serialClose(serialHandle);
}
