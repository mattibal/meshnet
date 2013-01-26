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
public class SerialRXTXComm implements SerialPortEventListener{
	
	public static final int TIME_OUT = 2000;
	
	protected InputStream inStream;
	protected OutputStream outStream;
	
	private byte[] readBuffer = new byte[1024];
	
	protected SerialLayer2 layer2;
	
	
	public SerialRXTXComm(String portName, BaseLayer3 layer3) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException{
		
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
		
		this.layer2 = new SerialLayer2(this, layer3);
	}

	

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		try {
			int len = 0;
			while ((len = inStream.read(readBuffer)) > -1) {
				for(byte b: readBuffer){
					layer2.onSerialByteReceived(b);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Send via serial port the byte contained in the int parameter
	 * @throws IOException 
	 */
	public void transmitByte(int dataByte) throws IOException{
		outStream.write(dataByte);
	}
	

}