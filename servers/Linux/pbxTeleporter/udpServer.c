/* udpServer.c
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
#include "udpServer.h"
#include "pbxTeleporter.h"


#define UDP_INBUFSIZE 256 // size of buffer for incoming UPD data.

void _debugPrintAddress(struct sockaddr_in *addr) {
  struct hostent *hostp;
  char *hostaddrp;
  
  hostp = gethostbyaddr((const char *)&addr->sin_addr.s_addr, 
      	  sizeof(addr->sin_addr.s_addr), AF_INET);
          
  if (hostp == NULL) {
    printf("gethostbyaddr failed.\n");
    return;
  }  
      
  hostaddrp = inet_ntoa(addr->sin_addr);
  
  if (hostaddrp == NULL) {
      printf("inet_ntoa failed.\n");
  }   
   
  printf("%s (%s)\n",  hostp->h_name, hostaddrp);
}

udpServer *createUdpServer(char *bind_addr, int listen_port, int send_port) {
  udpServer *udp;
  int options;
	
  udp = (udpServer *) malloc(sizeof(udpServer)); 
  udp->clientlen = sizeof(struct sockaddr_in);
  udp->listen_port = listen_port;
  udp->send_port = send_port;
	
// open socket	
  udp->fd = socket(AF_INET, SOCK_DGRAM, 0);
  
  if (udp->fd < 0) {
    printf("pbxTeleporter: ERROR opening socket\n");
    return NULL;
  }
	
// set socket options
// Allow address reuse to get rid of the "Address already in use" and
// delay when we need to restart the server
  options = 1;
  setsockopt(udp->fd, SOL_SOCKET, SO_REUSEADDR, 
	     (const void *)&options , sizeof(int));	
	     
// configure listening address
//
  memset((char *) &udp->server, 0, sizeof(udp->server));
  if (strlen(bind_addr) == 0) {
      udp->server.sin_addr.s_addr = htonl(INADDR_ANY);
  }
  else {
	  inet_aton(bind_addr,&udp->server.sin_addr);
  }
  udp->server.sin_family = AF_INET;
  udp->server.sin_port = htons((unsigned short) listen_port);
  
// bind socket to address  
  if (bind(udp->fd,(struct sockaddr *) &udp->server, sizeof(struct sockaddr_in)) < 0) {
    printf("pbxTeleporter: Bind socket failed.\n");
    return NULL;
  }

// create listener thead
  pthread_create(&udp->pt, NULL, &udpThread, (void*) udp);

  return udp;   	
}

// send/receive fns
int udpServerListen(udpServer *udp,uint8_t *rcvbuf,size_t bufsize) {
  udp->clientlen = sizeof(struct sockaddr);

  return recvfrom(udp->fd,rcvbuf ,bufsize , 0,
		 (struct sockaddr *) &udp->client, (socklen_t *) &udp->clientlen);
}

int udpServerSend(udpServer *udp, uint8_t *sendbuf,size_t bufsize) {
	udp->client.sin_port = htons(udp->send_port);
	return sendto(udp->fd,  sendbuf, (int) bufsize, 0,
		(struct sockaddr*)&udp->client, udp->clientlen);
}

void destroyUdpServer(udpServer *udp) {
  pthread_join(udp->pt, NULL);
  if (udp != NULL) {
    free(udp);
  }
}

// UDP Server thread function. Once data becomes available, does a blocking
// listen for requests, and forwards the pixel data when it gets one. Net
// data rate is decoupled from Pixelblaze frame rate, and multiple clients
// are supported, although you're gonna need a sturdy router for that...
// TODO - implement protocol for virtual wiring
void *udpThread(void *arg) {
	uint8_t incoming_buffer[UDP_INBUFSIZE];
	udpServer *udp = (udpServer *) arg;
	int res;

	pthread_detach(pthread_self());

	while(runFlag) {
		if (pixelsReady > 0) {
			res = udpServerListen(udp,incoming_buffer,UDP_INBUFSIZE);

			if (res > 0) {
				udpServerSend(udp,pixel_buffer,pixelsReady);
			}
		}
	}

	pthread_exit(NULL);
}
