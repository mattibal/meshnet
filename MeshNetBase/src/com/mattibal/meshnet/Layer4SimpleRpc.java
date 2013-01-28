package com.mattibal.meshnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mattibal.meshnet.Device.InexistentCommandException;
import com.mattibal.meshnet.Layer3Packet.DataToBase;

/**
 * This is a Layer4 that provide a very simple RPC.
 * 
 * This layer4 has the responsibility of determine if the requested
 * layer7 "procedures" (of RPC, here they are implemented as Java methods)
 * are available in the base code and in the device code.
 * 
 * Every node on the network has one instance of this Layer4SimpleRpc object.
 * This object must be able to understand what is the type and ID of the device,
 * and associate them (and create if necessary) an instance of Device object
 * (or a subclass of Device).
 */
public class Layer4SimpleRpc implements Layer3Base.ILayer4 { 
	
	// When the NetworkTree is rebuilded, Node and this Layer4 must be rebuilded!
	private final NetworkTree.Node node;
	
	/** Used to send packets */
	private final Layer3Base layer3;
	
	/** 
	 * The specific Device object that this layer4 communicates with.
	 * At the beginning when this Layer4 is created, the Device may be unknown! 
	 */
	private Device device = null;
	
	
	protected Layer4SimpleRpc(NetworkTree.Node node, Layer3Base layer3){
		this.node = node;
		this.layer3 = layer3;
	}

	/**
	 * Method called by Layer3Base when the base receives a DataToBase packet
	 * from the node corresponding with this Layer4SimpleRpc instance
	 */
	@Override
	public void onPacketReceived(DataToBase packet) {
		// Note: the ByteBuffer position must be at the start of the Layer4 packet
		ByteBuffer data = packet.getData();
		int command = data.get() & 0xff;
		if(command == 0){
			onDeviceInfoCommand(data);
		} else {
			if(device != null){
				try {
					device.onCommandRequestArrived(command, data);
				} catch (InexistentCommandException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("command called to unknown device");
			}
		}
	}
	
	
	/**
	 * This method should be called by Device implementations to send a command
	 * to their corresponding device
	 * @throws IOException when for some reasons the packet MIGHT not been arrived
	 */
	public void sendCommandRequest(int command, byte[] data) throws IOException{
		ByteBuffer buf = ByteBuffer.allocate(data.length+1);
		buf.order(ByteOrder.LITTLE_ENDIAN); // it's not necessary, but who knows..
		buf.put((byte) command);
		buf.put(data);
		layer3.sendDataToDevice(buf.array(), node);
	}
	
	
	/**
	 * This is the command 0, a special command that every device must have,
	 * and it tells the uniqueDeviceID and the deviceType.
	 * 
	 * I implement here the command procedure because I need them to create
	 * (or associate) with the correspondent Device object.
	 */
	private void onDeviceInfoCommand(ByteBuffer data){
		byte echoReply = data.get();
		if(echoReply != 45){
			System.out.println("wrong echo: "+echoReply);
		}
		int deviceType = data.getInt();
		int deviceUniqueId = data.getInt();
		device = Device.createDeviceFromType(deviceType, deviceUniqueId);
	}
	
	public void sendDeviceInfoCommand() throws IOException{
		byte[] sendData = new byte[1];
		sendData[0] = 45; // data to echo
		sendCommandRequest(0, sendData);
	}
	

}
