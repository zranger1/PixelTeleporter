/* pbxSerial.c
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
#include <stdint.h>
#include <stdarg.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <linux/serial.h>
#include <errno.h>

#include "pbxSerial.h"

// lookup tables for serial rates.  Where was the mighty power of hardware
// abstraction when they were designing this interface?
int _serialRate[] = {50,75,110,134,150,200,300,600,1200,1800,2400,4800,9600,19200,38400,57600,115200,230400,460800,500000,
576000,921600,1000000,1152000,1500000,2000000,2500000,3000000,3500000,4000000};

speed_t _baud[]= {B50,B75,B110,B134,B150,B200,B300,B600,B1200,B1800,B2400,B4800,B9600,B19200,B38400,B57600,B115200,B230400,
B460800,B500000,B576000,B921600,B1000000,B1152000,B1500000,B2000000,B2500000,B3000000,B3500000,B4000000};

// I know why this exists, but I don't have to like it.
// returns
speed_t hideousSerialSpeedFinder(int requested) {
	speed_t speed;
	int i,n_speeds;

	speed = -1;
	n_speeds = sizeof(_serialRate) / sizeof(int);
	for (i = 0; i < n_speeds;i++) {
		if (requested == _serialRate[i]) {
			speed = _baud[i];
			break;
		}
	}
	return speed;
}

//
// Open and iniialize serial port
//
int serialOpen(const char *device,int speed)
{
  struct termios options;
  struct serial_struct serial;
  speed_t baudRate;
  int status, fd ;

// convert from desired baud rate to weird historical artifact
  baudRate = hideousSerialSpeedFinder(speed);
  if (baudRate < 0) {
	  return - 1;
  }

// attempt to open the seial port for blocking IO
//  fd = open (device, O_RDWR | O_NOCTTY | O_NDELAY | O_NONBLOCK);
  fd = open (device, O_RDWR | O_NOCTTY);
  if (fd == -1) {
	printf("%s\n",strerror(errno));
    return -1;
  }

// Set basic serial port options
// speed, parity, stop bits, etc.
  fcntl (fd, F_SETFL, O_RDWR);
  tcgetattr (fd, &options) ;

  cfmakeraw   (&options) ;
  cfsetispeed (&options, baudRate);
  cfsetospeed (&options, baudRate);

  options.c_cflag |= (CLOCAL | CREAD);
  options.c_cflag &= ~PARENB;
  options.c_cflag &= ~CSTOPB;
  options.c_cflag &= ~CSIZE;
  options.c_cflag |= CS8;
  options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
  options.c_oflag &= ~OPOST;

  options.c_cc [VMIN]  =  1;
  options.c_cc [VTIME] =  0;  // block 'till one byte is ready

  tcsetattr (fd, TCSANOW, &options) ;

// configure the port for low latency.  You may need to
// be root on some systems for this to have any effect, but
// nonetheless...
  ioctl(fd,TIOCGSERIAL,&serial);
  serial.flags |= ASYNC_LOW_LATENCY;
  ioctl(fd,TIOCSSERIAL,&serial);

// set DTR/RTS so we will receive incoming data.  Apparently,
// some UARTS are picky about this.
  ioctl (fd, TIOCMGET, &status);

  status |= TIOCM_DTR ;
  status |= TIOCM_RTS ;

  ioctl (fd, TIOCMSET, &status);

// brief delay for the hardware
  usleep (10000);

  return fd ;
}

// return bytes of data available on serial port. Ignores
// errors returned from ioctl
int serialAvailable (const int fd) {
  int size = 0;
  ioctl (fd, FIONREAD, &size);
  return size;
}

// flush tx/rx serial buffers
void serialFlush(const int fd) {
  tcflush(fd, TCIOFLUSH);
}

// close serial port
void  serialClose(const int fd) {
	close(fd);
}




