/* udpServer.h
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
#ifndef __UDPSERVER_H
#define __UDPSERVER_H
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

typedef struct _udpServer {
  int listen_port;
  int send_port;
  int fd;
  struct sockaddr_in server; 
  struct sockaddr_in client;   
  int clientlen;  
  pthread_t pt;
} udpServer;

void _debugPrintAddress(struct sockaddr_in *addr);
udpServer *createUdpServer(char *bind_addr, int listen_port, int send_port);
int udpServerListen(udpServer *udp,uint8_t *rcvbuf,size_t bufsize);
int udpServerSend(udpServer *udp, uint8_t *sendbuf,size_t bufsize);
void destroyUdpServer(udpServer *udp);
void *udpThread(void *arg);

#endif
