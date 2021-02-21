/* pbxTeleporter.ino
 *  
 * Serial -> UDP Bridge for Pixelblaze
 * ESP8266 version
 *
 * Reads data from a Pixelblaze by emulating a single (8 channel, 2048 pixel) output expander
 * board and forwards it over the network via UDP datagram on request.  The wire protocol is
 * described in Ben Hencke's Pixelblaze Output Expander board repository at:
 * https://github.com/simap/pixelblaze_output_expander
 * 
 * Requires installation of ESP8266 board support for Arduino IDE* 
 *
 * Part of the PixelTeleporter project
 * 2020 by JEM (ZRanger1)
 * Distributed under the MIT license
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <SoftwareSerial.h>

// Serial setup
#define SSBAUD          115200            // log to console for debugging
#define BAUD            2000000           // bits/sec coming from pixelblaze
#define BUFFER_SIZE     6150              // 2048 pixels plus a little
Stream* logger;

// Network setup
//#define _SSID "SSID"              // Your WiFi SSID goes here.  
//#define _PASS "WOWGREATPASSWORD"  // Your WiFi password goes here.
#define LISTEN_PORT 8081          // UDP port on which pbxTeleporter listens for commands
#define DATA_OUT_PORT 8082        // UDP port on client to which we send data

WiFiUDP Udp;
IPAddress targetIP;

// Datagram rate limiting - not currently used.  May be necessary
// eventually to support multple clients. For now, everything behaves well at 60+ fps,
// so the outgoing packet rate is determined by the client.
//
#define MIN_SEND_INTERVAL 17   // milliseconds - (16.6667ms == 60 fps)
uint64_t sendTimer = 0;

// TODO - per channel buffers for virtual wiring
// For now, we just concatenate pixel data and forward it all to the client
// on request.
// TODO -- in V2 support color order.
// For now, the user must set the  strand to be (ideally) a WS2812 w/RGB pixels 
// or (slightly less ideally) an APA102, which requires us to discard data to
// get RGB pixels. Someday, we may want the extra 5 bits of APA data, but for
// now it doesn't make any visisble difference on monitors.
// (Maybe eventually HDR if the client does some extra color calculation??)
//
#define MAX_PIXELS     2048                    // 8 channels x 256 pixels/channel
uint8_t pixel_buffer[BUFFER_SIZE];             // per-pixel RGB data for current frame
uint8_t incoming_buffer[256];                  // incoming requests for pixels
uint8_t *pixel_ptr;                            // current write position in buffer

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
} __attribute__((packed)) PBFrameHeader;

typedef struct {
    uint8_t numElements; //0 to disable channel, usually 3 (RGB) or 4 (RGBW)
    union {
        struct {
            uint8_t redi :2, greeni :2, bluei :2, whitei :2; //color orders, data on the line assumed to be RGB or RGBW
        };
        uint8_t colorOrders ;
    };
    uint16_t pixels;
} __attribute__((packed)) PBWS2812Channel ;

typedef struct {
    uint32_t frequency;
    union {
        struct {
            uint8_t redi :2, greeni :2, bluei :2; //color orders, data on the line assumed to be RGB
        };
        uint8_t colorOrders;
    };
    uint16_t pixels;
} __attribute__((packed)) PBAPA102DataChannel ;

typedef struct {
    uint32_t frequency;
}  __attribute__((packed)) PBAPA102ClockChannel;

/////////////////////////////////
// Utility Functions
/////////////////////////////////

// readBytes()
// reads the specified number of bytes into a buffer
// yields if bytes are not available
void readBytes(uint8_t *buf, uint16_t size) {
  int i = 0;
  while (i < size) {
    if (Serial.available()) {
      *buf++ = Serial.read();
      i++;
    }
    else {
      delay(0);
    }
  }  
}

// readOneByte()
// Read a single byte, yielding if the buffer is empty
uint8_t readOneByte() {
  while (!Serial.available()) {
    delay(0);
  }
  return Serial.read();
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
// TODO -- for virtual wiring, we need to check this before passing
// channel data on to the clients.  For use with Processing, crc
// errors have been nonexistent, so it really isn't needed.
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
void doDrawAll() {
  int packetSize = Udp.parsePacket();
  if (packetSize) {   
    uint16_t data_size = pixel_ptr - pixel_buffer;    
    if (data_size) {
      Udp.beginPacket(Udp.remoteIP(),DATA_OUT_PORT);
      Udp.write(pixel_buffer,data_size);  
      Udp.endPacket(); 
    } 
  }  
  pixel_ptr = pixel_buffer;      
}

// read APA 102 clock data.  
// TODO - For now, we ignore this. For full-on Pixel Telporter, we'll
// eventually have to do something with it...
void doSetChannelAPA102Clock() {
  PBAPA102ClockChannel ch;

  readBytes((uint8_t *) &ch,sizeof(ch));
  crcCheck();  
}

/////////////////////////////////
// Main loop & InitializationUtility Functions
/////////////////////////////////

// setup()
// Create software serial port for low speed logging and swap UART so it 
// speaks over GPIO.  Pins are: RX=GPIO13 TX=GPIO15
void setup() {
  pinMode(LED_BUILTIN, OUTPUT);

  Serial.begin(BAUD);  
  Serial.swap(); 
  Serial.setRxBufferSize(1024);  
  
// use HardwareSerial0 pins so we can still log to the
// regular usbserial chips
  SoftwareSerial* ss = new SoftwareSerial(3, 1);
  ss->begin(SSBAUD);
  ss->enableIntTx(false);
  logger = ss;
  logger->println();

// Configure and connect WiFi
  WiFi.mode(WIFI_STA);
  WiFi.begin(_SSID,_PASS);

  logger->print("\n\n\n");
  logger->println("pbxTeleporter - Pixel Teleporter Bridge v1.1.2 for ESP8266");
  logger->println("Connecting to wifi...");
  while(WiFi.status() != WL_CONNECTED) {
    logger->print('.');
    delay(500);
  }

  logger->println("Connected!");
  logger->print("IP address: ");
  logger->println(WiFi.localIP());  
  logger->printf("pbxTeleporter listening on %d, responding on %d\n",
                 LISTEN_PORT,DATA_OUT_PORT);
  
  Udp.begin(LISTEN_PORT);    

  pixel_ptr = pixel_buffer;

  logger->println("Setup: Success");
}

// main loop
void loop() {
  PBFrameHeader hdr;
  
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
