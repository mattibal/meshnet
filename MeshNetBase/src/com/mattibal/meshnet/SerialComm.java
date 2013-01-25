package com.mattibal.meshnet;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**
 * This version of the TwoWaySerialComm example makes use of the
 * SerialPortEventListener to avoid polling.
 * 
 */
public class SerialComm implements SerialPortEventListener{
	
	public static final int TIME_OUT = 2000;
	
	protected InputStream inStream;
	protected OutputStream outStream;
	
	private byte[] readBuffer = new byte[1024];
	
	protected SerialLayer2 layer2;
	
	
	public SerialComm(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException{
		
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new IOException("Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					TIME_OUT);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				inStream = serialPort.getInputStream();
				outStream = serialPort.getOutputStream();

				//(new Thread(new SerialWriter(out))).start();

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);

			} else {
				throw new IOException("This is not a serial port!.");
			}
		}
		
		this.layer2 = new SerialLayer2(this);
	}

	

	@Override
	public void serialEvent(SerialPortEvent arg0) {
				
		int data;

		try {
			int len = 0;
			while ((data = inStream.read()) > -1) {
				layer2.onSerialByteReceived(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Send via serial port the byte contained in the int parameter
	 */
	public void transmitByte(int dataByte){
		// TODO
	}
	

	
	/*public static class SerialWriter implements Runnable {
		OutputStream out;

		public SerialWriter(OutputStream out) {
			this.out = out;
		}

		public void run() {
			try {
				int c = 0;
				while ((c = System.in.read()) > -1) {
					this.out.write(c);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}*/

}