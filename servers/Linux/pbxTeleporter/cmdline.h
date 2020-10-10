/* cmdlline.h
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
#ifndef __cmdline_h___
#define __cmdline_h___

#include <argp.h>

// struct to hold parser results
typedef struct _cmdline {
	char *serial_port;
	char *bind_ip;
	int  listen_port;
	int  send_port;
} commandline;

extern struct argp argparser;

#endif //__cmdline_h__
