/* pbxTeleporter.h
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
#ifndef __pbxteleporter_h__
#define __pbxteleporter_h__

#define MAX_PIXELS     4096
#define RCV_BITRATE    2000000L           // bits/sec coming from pixelblaze
#define BUFFER_SIZE    (256+(MAX_PIXELS * 3))
#define DEFAULT_LISTEN_PORT 8081          // default UDP ports
#define DEFAULT_SEND_PORT   8082

///////////////////////////////////////////////////////////////////////////////////////////
// structures imported from pixelblaze expander source
// https://github.com/simap/pixelblaze_output_expander
//////////////////////////////////////////////////////////////////////////////////////////
enum RecordType {
  SET_CHANNEL_WS2812 = 1, DRAW_ALL, SET_CHANNEL_APA102_DATA, SET_CHANNEL_APA102_CLOCK
};

typedef struct {
//    int8_t magic[4];
    uint8_t channel;
    uint8_t command;
}  __attribute__((packed)) PBFrameHeader;

typedef struct {
    uint8_t numElements; //0 to disable channel, usually 3 (RGB) or 4 (RGBW)
    union {
        struct {
            uint8_t redi :2, greeni :2, bluei :2, whitei :2; //color orders, data on the line assumed to be RGB or RGBW
        };
        uint8_t colorOrders;
    };
    uint16_t pixels;
}  __attribute__((packed)) PBWS2812Channel;

typedef struct {
    uint32_t frequency;
    union {
        struct {
            uint8_t redi :2, greeni :2, bluei :2; //color orders, data on the line assumed to be RGB
        };
        uint8_t colorOrders;
    };
    uint16_t pixels;
}  __attribute__((packed)) PBAPA102DataChannel;

typedef struct {
    uint32_t frequency;
}  __attribute__((packed)) PBAPA102ClockChannel;

// global variables
extern int runFlag;
extern uint16_t pixelsReady;
extern uint8_t pixel_buffer[];

#endif /* __pbxteleporter_h__ */
