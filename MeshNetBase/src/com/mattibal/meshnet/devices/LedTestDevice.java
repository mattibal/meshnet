package com.mattibal.meshnet.devices;

import java.io.IOException;

import com.mattibal.meshnet.Device;

/**
 * This is a simple device that has a LED, like an Arduino board.
 * The LED can be switched on and off.
 */
public class LedTestDevice extends Device {
	
	public static final int DEVICE_TYPE = 123;
	
	public LedTestDevice(int uniqueDeviceId){
		super(uniqueDeviceId, DEVICE_TYPE);
	}
	
	/** TODO this method should block until I receive a response from the
	 * device that he has received the message and the LED has been actually
	 * turned on.
	 * @throws IOException when I MIGHT not have set the LED state
	 */
	public void setLedState(boolean on) throws IOException{
		byte[] data = new byte[1];
		if(on){
			data[0] = 1;
		} else {
			data[0] = 0;
		}
		this.getLayer4().sendCommandRequest(1, data);
	}

}
