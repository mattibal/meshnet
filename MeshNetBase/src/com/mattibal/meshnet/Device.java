package com.mattibal.meshnet;

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
	
	

}
