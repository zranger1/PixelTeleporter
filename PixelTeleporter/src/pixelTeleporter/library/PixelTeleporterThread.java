package pixelTeleporter.library;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
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
class PixelTeleporterThread extends Thread {  
	final int UDP_NONE=0;              // states for UDP listener
	final int UDP_REQUESTED=0x01;
	final int UDP_RECEIVED=0x03;

	//empty array to return when no packet has been received.
//	final static byte [] NO_PACKET = {};
	
	// Timeout value, in milliseconds, for "disconnected" indicator
	final int DISCONNECT_TIMEOUT = 5000;

	//command to fetch a frame from the microcontroller.  
	final static byte CMD_REQUEST_FRAME = (byte) 0xF0; 	

	PixelTeleporter parent;
	DatagramSocket ds; 
	int clientPort;
	int serverPort;
	byte[] buffer;
	byte[] sendbuf;
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
		sendbuf = new byte[128];  
		sendbuf[0] = CMD_REQUEST_FRAME;  
		datagramIn = new DatagramPacket(buffer, buffer.length); 

		InetSocketAddress sourceAddress = new InetSocketAddress(ipAddr,serverPort);    
		datagramOut = new DatagramPacket(sendbuf,4,sourceAddress);

		status = UDP_NONE;
		lastActivity = parent.app.millis();

		try {
			ds = new DatagramSocket(null);
			ds.setSoTimeout(0);
			ds.bind(new InetSocketAddress(clientPort));
		} catch (SocketException e) {
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
	int doDisconnectFlash(int [] buffer) {
	  float bri = ((parent.app.millis() - lastActivity) % 3000f) / 3000f;
	  if (bri > 0.5) bri = 1-bri;
	  Arrays.fill(buffer,(int) (128 * bri));
	  return buffer.length / 3;				
	}

	/** 
	 * Copies pixel colors from the network buffer of bytes into PixelTeleporter's
	 * internal integer pixel buffer, which work better with Processing. 
	 * Returns number of pixels copied.
	 */
	int readData(int [] buffer) {
		byte [] data;
		int i;

		if (available()) {
			data = datagramIn.getData();
			i = 0;
			while (i < datagramIn.getLength()) {
				buffer[i] = Byte.toUnsignedInt(data[i++]);  //r
				buffer[i] = Byte.toUnsignedInt(data[i++]);  //g
				buffer[i] = Byte.toUnsignedInt(data[i++]);  //b             
			}
			status = UDP_NONE;  
			return datagramIn.getLength() / 3;
		}
		else if (!isConnected()) {
          return doDisconnectFlash(buffer);
		}
		
		return 0;
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
