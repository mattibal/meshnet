package com.mattibal.meshnet;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.TooManyListenersException;

import com.mattibal.meshnet.devices.LedTestDevice;

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
			SerialRXTXComm serial = new SerialRXTXComm("/dev/ttyUSB0", base);
			Thread.sleep(4000);
			Layer3Base.NetworkSetupThread setup = base.new NetworkSetupThread();
			Thread setupThread = new Thread(setup);
			setupThread.start();
			setupThread.join();
			// Alè, la rete è pronta, adesso posso giocare con i device
			Device device = Device.getDeviceFromUniqueId(384932);
			if(device!=null && device instanceof LedTestDevice){
				LedTestDevice ledDevice = (LedTestDevice) device;
				for(int i=0; i<5000; i++){
					ledDevice.setLedState(true);
					Thread.sleep(20);
					ledDevice.setLedState(false);
					Thread.sleep(50);
				}
			} else {
				System.out.println("Errore get device");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
