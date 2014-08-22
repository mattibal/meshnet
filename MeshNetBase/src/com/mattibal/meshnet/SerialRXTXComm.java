package com.mattibal.meshnet;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
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
public class SerialRXTXComm{
	
	public static final int TIME_OUT = 2000;
	
	protected InputStream inStream;
	protected OutputStream outStream;
	
	private byte[] readBuffer = new byte[1024];
	
	protected Layer2Serial layer2;
	
	
	public SerialRXTXComm(CommPortIdentifier portIdentifier, Layer3Base layer3) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException{

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

				new SerialReceiver().start();

				/*serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);*/

			} else {
				throw new IOException("This is not a serial port!.");
			}
		}
		
		this.layer2 = new Layer2Serial(this, layer3);
	}

	
	
	public class SerialReceiver extends Thread {
		@Override
		public void run() {
			super.run();
			int len = -1;
			try{
				while((len=inStream.read(readBuffer))>-1){
					//System.out.println("rx: "+(new String(readBuffer, 0, len)));
					for(int i=0; i<len; i++){
						layer2.onSerialByteReceived(readBuffer[i]);
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Send via serial port the byte contained in the int parameter
	 * @throws IOException 
	 */
	public void transmitByte(byte data) throws IOException{
		outStream.write(data);
		//System.out.print(" tx:"+(int)data);
	}
	

}