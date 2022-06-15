package pixelTeleporter.library;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.util.Arrays;

import processing.core.*;

/**
 PixelTeleporter UDP transport.
 Handles requesting a frame of pixel data from a machine on the net,
 converting the pixels to a Processing-friendly format, and moving
 them into PixelTeleporter's internal frame buffer.
<p>
 Once started, loops waiting for datagrams, yielding on UDP receive()
 timeouts.
*/
@SuppressWarnings("unused")
class PixelTeleporterThread extends Thread {  
	final int UDP_NONE=0;              // states for UDP listener
	final int UDP_REQUESTED=0x01;
	final int UDP_RECEIVED=0x03;

	//empty array to return when no packet has been received.
//	final static byte [] NO_PACKET = {};
	
	// Timeout value, in milliseconds, for "disconnected" indicator
	final int DISCONNECT_TIMEOUT = 5000;

	//command to fetch a frame from the server  
	final static byte CMD_REQUEST_FRAME = (byte) 0xF0; 	

	PixelTeleporter parent;
	DatagramSocket ds; 
	int clientPort;
	int serverPort;
	byte[] buffer;
	byte[] sendbuf;
	public int[] pixelBuffer;
	DatagramPacket datagramIn;
	DatagramPacket datagramOut;
	
	boolean running;   
	int status;
	int lastActivity;
	
	PixelTeleporterThread(PixelTeleporter parent,String ipAddr,
			              int clientPort, int serverPort, int bufsize) {
		this.parent = parent;
		this.clientPort = clientPort;
		this.serverPort = serverPort;
		buffer = new byte[bufsize];
		pixelBuffer = new int[this.parent.MAX_PIXELS];  
		sendbuf = new byte[128];  		
		sendbuf[0] = CMD_REQUEST_FRAME;  
		datagramIn = new DatagramPacket(buffer, buffer.length); 

		InetSocketAddress sourceAddress = new InetSocketAddress(ipAddr,serverPort);    
		datagramOut = new DatagramPacket(sendbuf,4,sourceAddress);

		status = UDP_NONE;
		lastActivity = parent.app.millis();
		
		try {
			boolean on = true;
			ds = new DatagramSocket(null);
			ds.setSoTimeout(0);
			ds.setReuseAddress(true);
			ds.bind(new InetSocketAddress(clientPort));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	PApplet getApplet() {
		return parent.app;
	}

	/** 
	 * Returns true if we have recieved data or a ping from the
	 * current server within the last DISCONNECT_TIMEOUT milliseconds  
	 */	
	public boolean isConnected() {
		return ((parent.app.millis() - lastActivity) < DISCONNECT_TIMEOUT);		
	}

	/** 
	 * If data isn't already waiting to be read, ask for an update.  
	 */
	public void requestData() {     
		if (available()) return;

		try {
			ds.send(datagramOut);
			status = UDP_REQUESTED; 
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean available() {
		return (status == UDP_RECEIVED);    
	}
		
	/**
	 * Flash the entire pixel display light grey at a low frequency.
	 * Used to signal that the server is disconnected.
	 */	
	int doDisconnectFlash() {
	  float bri = ((parent.app.millis() - lastActivity) % 3000f) / 3000f;
	  if (bri > 0.5) bri = 1-bri;
	  Arrays.fill(pixelBuffer,parent.app.color(128 * bri));
	  return pixelBuffer.length;				
	}

	/** 
	 * Copies pixel colors from the network buffer of bytes into the transport's
	 * internal ARGB pixel buffer, which is way faster once we get back into
	 * the Processing/OpenGL graphics API. 
	 * Returns number of pixels copied.
	 */
	int readData() {
		byte [] data;
		int i,pix,col;

		if (available()) {
			data = datagramIn.getData();
			i = pix = 0;
			while (i < datagramIn.getLength()) {
				// processing color order = 0xAARRGGBB
				col = 0xFF000000;                           //a - defaults to opaque 
				col |= Byte.toUnsignedInt(data[i++]) << 16; //r
				col |= Byte.toUnsignedInt(data[i++]) << 8;  //g
				col |= Byte.toUnsignedInt(data[i++]);       //b
				pixelBuffer[pix++] = col;
			}
			status = UDP_NONE;  
			return datagramIn.getLength() / 3;
		}
		// if we're paused, just hold the last frame
		else if (!parent.isRunning) {
			lastActivity = parent.app.millis();
			return datagramIn.getLength() / 3;			
		}
		// otherwise check to see if we've lost connection and 
		// start the grey "disconnected" flash after a few seconds.
		else if (!isConnected()) {
          return doDisconnectFlash();
		}
		
		return 0;
	}     
	
	/**
	 * Returns pointer to int array containing the last read set of pixels
	 * in Processing's normal ARGB color format.
	 */
	public int[] getPixelBuffer() {
		return pixelBuffer;
	}

	public void start() {
		System.out.println("PixelTeleporter thread starting");  
		running = true;
		super.start();
	}

	public void run() {
		while (running) {
			waitForDatagram();
			Thread.yield();
		} 
	}
	
	void quit() {
		System.out.println("PixelTeleporter thread stopping"); 
		running = false;  
		interrupt();
	}		

	void waitForDatagram() {    
		if (status == UDP_REQUESTED) {  
			try {
				ds.receive(datagramIn);
				lastActivity = parent.app.millis();
			} 
			catch (IOException e) { // catch those pesky timeouts
				status = UDP_NONE;  
				return;
			}     
			status = UDP_RECEIVED;
		}  
	}
}
