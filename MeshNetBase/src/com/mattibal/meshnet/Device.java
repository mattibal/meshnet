package com.mattibal.meshnet;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;

import com.mattibal.meshnet.devices.LedTestDevice;

/**
 * This class represent a real physical device that I might be able to contact
 * with my network.
 * It should not be tied with a particular base, this object can be shared across
 * multiple bases, so it must contain some information about to what base is
 * now connected the device.
 * 
 * This class should contain high-level methods to access the sensors or actuators
 * of the device. So this class should be extended with other classes that
 * represent the capabilities of any specific kind of device
 * (for example RgbLightDevices with a method setRgb(float r, float g, float b))
 * 
 * When the layer3 connects to a device, it should ask with a layer7 thing the
 * capabilities of the device and the universal unique id of the device, so
 * an appropriate Device object can be created and linked to the layer3 node.
 */
public class Device {
	
	/** A code that is guaranteed to be different among all devices
	 * on a network. */
	private final int uniqueDeviceId;
	
	private final int deviceType;
	
	/** This is not final because it can change during the life of this object! */
	private Layer4SimpleRpc layer4 = null;
	
	/**
	 * This set must contain all the devices of this network that at least this
	 * base known that they exist (that they are associated with this network,
	 * also if now they are offline). 
	 */
	private static HashMap<Integer,Device> knownUniqueDevicesId = new HashMap<Integer,Device>();
	
	/** This constructor also add the Device to the knownDevices */
	protected Device(int uniqueDeviceId, int deviceType){
		this.uniqueDeviceId=uniqueDeviceId;
		this.deviceType=deviceType;
		knownUniqueDevicesId.put(uniqueDeviceId,this);
	}
	
	
	/**
	 * This method should be extended by the subclass. 
	 * They must check here if they can handle the command type requested:
	 * if they can they handle and executes them, if not (because it's a general
	 * command implemented here) they call this implementation with super(...)
	 *  
	 * @throws InexistentCommandException if the command requested doesn't
	 * exist in this Device implementation 
	 */
	public void onCommandRequestArrived(int command, ByteBuffer data) throws InexistentCommandException{
		// If there is a command that every device must accept (except command 0),
		// it must catch and implemented here!
		throw new InexistentCommandException(command);
	}
	
	
	/**
	 * Layer 4 must use this method to create (or get if is already "known")
	 * a Device of the appropriate type
	 */
	public static Device createDeviceFromType(int deviceType, int uniqueDeviceId){
		Device device = knownUniqueDevicesId.get(uniqueDeviceId);
		if(device == null){
			if(deviceType == LedTestDevice.DEVICE_TYPE){
				device =  new LedTestDevice(uniqueDeviceId);
			} else {
				device =  new Device(uniqueDeviceId, deviceType);
			}
		}
		return device;
	}
	
	
	
	public synchronized void setLayer4(Layer4SimpleRpc layer4){
		this.layer4 = layer4;
	}
	
	
	protected synchronized Layer4SimpleRpc getLayer4(){
		return layer4;
	}
	
	
	
	/**
	 * Thrown when it's requested the execution of a command that doesn't exist
	 * for this kind of device
	 */
	public static class InexistentCommandException extends Exception {
		
		public final int command;
		
		public InexistentCommandException(int command){
			this.command = command;
		}
	}
}
