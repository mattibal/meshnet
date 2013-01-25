package com.mattibal.meshnet;

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
		
		TwoWaySerialComm comm = new TwoWaySerialComm();
		try {
			comm.connect("/dev/ttyACM0");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
