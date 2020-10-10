/* pbxSerial.h
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
#ifndef __serial_h__
#define __serial_h__

extern int serialOpen(const char *device, const int baud);
extern int serialAvailable(const int fd);
extern void serialFlush(const int fd);
extern void serialClose(const int fd);

// Read a single byte from the serial device
// read() will block for the time set in serialOpen.
// BUGBUG - ignores timeout errors, so
// check w/serialAvailable to be sure data is ready if
// that is of concern.
#define serialGetbyte(fd,b) read(fd,b,1)
#define serialGetbytes(fd,b,n) read(fd,b,n)

#endif

