package com.mattibal.meshnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.mattibal.meshnet.devices.Led1Analog2Device;
import com.mattibal.meshnet.devices.LedLamp1Device;
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
	
	/**
	 * These are the listeners of every command (packet) sent to this Device.
	 */
	private final Set<CommandReceivedListener> listeners = new HashSet<CommandReceivedListener>();
	
	
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
		
		// If there is a command that every device must accept (EXCEPT command 0),
		// it must be catched and implemented here!
		
		// Notify the listeners 
		for(CommandReceivedListener listener: listeners){
			listener.onCommandReceived(command, uniqueDeviceId, data);
		}
		
		// I don't know how to handle the command, so I raise an exception
		throw new InexistentCommandException(command);
	}
	
	
	/**
	 * Layer 4 must use this method to create (or get if is already "known")
	 * a Device of the appropriate type
	 */
	public static Device createDeviceFromType(int deviceType, int uniqueDeviceId){
		synchronized(knownUniqueDevicesId){
			Device device = knownUniqueDevicesId.get(uniqueDeviceId);
			if(device == null){
				if(deviceType == LedTestDevice.DEVICE_TYPE){
					// TODO workaround for duplicated device type
					//device =  new LedTestDevice(uniqueDeviceId);
					device = new Led1Analog2Device(uniqueDeviceId);
				} else if(deviceType == LedLamp1Device.DEVICE_TYPE){
					device = new LedLamp1Device(uniqueDeviceId);
				} else {
					// Unknown device type
					device =  new Device(uniqueDeviceId, deviceType);
				}
			}
			return device;
		}
	}
	
	/**
	 * This method must be called by users of the MeshNet library to get Device
	 * objects of the devices they want to access to control actuators
	 * or get sensor readings.
	 * 
	 *  TODO this method should be put on Layer3Base, because if it's here as
	 *  a static method, I can't run multiple Layer3Base on the same JVM
	 *  (this is unusual, but might be useful for test purposes or if I want
	 *  to join two different MeshNet networks)
	 *
	 */
	public static Device getDeviceFromUniqueId(int uniqueDeviceId){
		synchronized(knownUniqueDevicesId){
			return knownUniqueDevicesId.get(uniqueDeviceId);
		}
	}
	
	/**
	 * Returns a copy of the currently known set of devices
	 */
	public static Set<Device> getKnownDevices(){
		synchronized(knownUniqueDevicesId){
			HashSet<Device> devices = new HashSet<Device>();
			for(Device device : knownUniqueDevicesId.values()){
				devices.add(device);
			}
			return devices;
		}
	}
	
	public synchronized void setLayer4(Layer4SimpleRpc layer4){
		this.layer4 = layer4;
	}
	
	
	public void sendCommand(int command, byte[] data) throws IOException{
		Layer4SimpleRpc l4;
		synchronized(this){
			l4 = layer4;
		}
		l4.sendCommandRequest(command, data);
	}
		
	public int getUniqueId(){
		return uniqueDeviceId;
	}
	
	
	
	/**
	 * A listener should call this method to register himself as a listener
	 * of any command directed to this Device.
	 * 
	 * @param command The command I want to listen for
	 */
	public void addCommandReceivedListener(CommandReceivedListener listener){
		listeners.add(listener);
	}
	
	/**
	 * This is the interface that somebody must implements if he want to be
	 * notified when a command is sent to this Device.
	 */
	public interface CommandReceivedListener {
		public void onCommandReceived(int command, int senderDeviceId, ByteBuffer data);
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
