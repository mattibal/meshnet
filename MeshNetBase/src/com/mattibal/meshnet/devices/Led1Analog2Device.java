package com.mattibal.meshnet.devices;

import java.io.IOException;

import com.mattibal.meshnet.Device;

public class Led1Analog2Device extends Device {
	
	public static final int DEVICE_TYPE = 123;
	
	private static final int SET_LED_PWM_STATE_COMMAND = 2;

	public Led1Analog2Device(int uniqueDeviceId) {
		super(uniqueDeviceId, DEVICE_TYPE);
	}
	
	public synchronized void setLedPwmState(int pwmState) throws IOException{
		byte[] data = new byte[1];
		data[0] = (byte)(pwmState & 0xFF);
		sendCommand(SET_LED_PWM_STATE_COMMAND, data);
	}

}
