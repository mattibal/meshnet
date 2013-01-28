package com.mattibal.meshnet;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.TooManyListenersException;

/**
 * Coso di test che fa partire la base della MeshNet, e poi scambia qualche
 * messaggio con i device
 */
public class MeshNetTest {

	public static void main(String[] args) {
		
		/*SerialComm main = new SerialComm();
		main.initialize();
		Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
		System.out.println("Started");
		*/
		
		/*try {
			SerialRXTXComm comm = new SerialRXTXComm("/dev/ttyACM0");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
			
			Layer3Base base = new Layer3Base();
			SerialRXTXComm serial = new SerialRXTXComm("/dev/ttyACM0", base);
			Thread.sleep(4000);
			Layer3Base.NetworkSetupThread setup = base.new NetworkSetupThread();
			Thread setupThread = new Thread(setup);
			setupThread.start();
			Thread.sleep(3000);
			
			
		} catch (NoSuchPortException | PortInUseException
				| UnsupportedCommOperationException | IOException
				| TooManyListenersException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
