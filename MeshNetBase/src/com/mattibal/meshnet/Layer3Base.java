package com.mattibal.meshnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.acl.LastOwnerException;
import java.util.HashSet;
import java.util.Set;

import com.mattibal.meshnet.Layer3Packet.BeaconChildResponse;
import com.mattibal.meshnet.Layer3Packet.BeaconParentResponse;
import com.mattibal.meshnet.Layer3Packet.DataToBase;
import com.mattibal.meshnet.Layer3Packet.DataToDevice;
import com.mattibal.meshnet.Layer3Packet.InvalidPacketException;
import com.mattibal.meshnet.NetworkTree.InconsistentTreeStructureException;
import com.mattibal.meshnet.NetworkTree.Node;
import com.mattibal.meshnet.NetworkTree.RootNode;
import com.mattibal.meshnet.NetworkTree.TreeAlreadyCalculatedException;


/**
 * The Layer3 of a Base of the MeshNet
 * 
 * This class must handle the frames received by the various layer2 interfaces
 * of the base, and it can send a frame to the layer2.
 *
 */
public class Layer3Base {
	
	private int networkId = 18287;
	private int networkKey = 48384;
	
	/**
	 * The network interfaces (layer2) physically connected to this base
	 */
	private final Set<ILayer2> interfaces = new HashSet<ILayer2>(); 
	
	
	// When you access these fields, you must hold the lock of BaseLayer3 object
	private NetworkTree newTree = null;
	private NetworkTree activeTree = null;
	
	
	public Layer3Base(){
		
	}
	
	protected synchronized void addLayer2Interface(ILayer2 interf){
		interfaces.add(interf);
	}
	
	
	/**
	 * This is the method called by a layer2 when it receives a frame of data
	 */
	public synchronized void onFrameReceived(ByteBuffer frame, ILayer2 srcInterface, int srcMacAddress){ 

		try {
			Layer3Packet packet = Layer3Packet.buildFromByteArray(frame);
			if(packet instanceof DataToBase){
				DataToBase dataToBase = (DataToBase) packet;
				// verify hmac? (now it doesn't have hmac)
				onDataToBase(dataToBase);
			} else if(packet instanceof BeaconChildResponse){
				BeaconChildResponse beaconChildResponse = (BeaconChildResponse) packet;
				// TODO verify hmac with different tree baseNonces
				beaconChildResponse.verifyHmac(newTree.baseNonce, srcMacAddress);
				onBeaconChildResponse(beaconChildResponse, srcInterface, srcMacAddress);
			} else if(packet instanceof BeaconParentResponse){
				BeaconParentResponse beaconParentResponse = (BeaconParentResponse) packet;
				// TODO verify hmac
				onBeaconParentResponse(beaconParentResponse);
			} else {
				System.out.println("Inuseful packet arrived");
			}
				
		} catch (Layer3Packet.InvalidPacketException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This is the interface that a Layer2 must implements to work with this
	 * base layer3.
	 */
	public static interface ILayer2 {
		public void sendLayer3Packet(byte[] bytesToSend, int destMacAddress) throws IOException;
	}
	
	/**
	 * The interface a Layer4 must implement to receive packets from this
	 * base Layer3
	 */
	public static interface ILayer4 {
		public void onPacketReceived(Layer3Packet.DataToBase packet);
	}
	
	
	// Methods to send packets to devices
	
	/**
	 * Send a beacon to all devices, by broadcasting them to all physical interfaces
	 * 
	 * @param tree Needed to ensure that the baseNonce I'm sending is the one of this tree
	 * @throws IOException If it was impossible to send a beacon to an interface
	 */
	private void sendBeacon(NetworkTree tree) throws IOException{
		try {
			Layer3Packet.Beacon beacon = new Layer3Packet.Beacon(networkId, tree.baseNonce);
			for(ILayer2 interf: interfaces){
				interf.sendLayer3Packet(beacon.getRawBytes().array(), 0);
			}
		} catch (InvalidPacketException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Send an assignAddress packet to all devices on the provided network tree
	 * that (we think) are still not been assigned ("connected" to the network)
	 * 
	 * @return false if I haven't sent any message because all devices has been assigned
	 * @throws IOException 
	 */
	private boolean sendAssignAddressToAllUnassignedDevices(NetworkTree tree) throws IOException{
		boolean isSomebodyUnassigned = false;
		NetworkTree.Node unassigned;
		while( (unassigned = tree.getNextUnassignedNode()) != null){
			isSomebodyUnassigned = true;
			// Send assignAddress packet
			Layer3Packet.AssignAddress packet = new Layer3Packet.AssignAddress(
					unassigned.getChildNonce(),
					(short)unassigned.getAddress(), (short)unassigned.getMaxRoute(),
					tree.baseNonce, networkKey);
			RootNode rootNode;
			if(unassigned instanceof RootNode){
				rootNode = (RootNode) unassigned;
			} else {
				rootNode = tree.getRouteToNode(unassigned.getAddress());
			}
			rootNode.getNetInterface().sendLayer3Packet(packet.getRawBytes().array(), rootNode.getMacAddress());
		}
		return isSomebodyUnassigned;
	}
	
	
	public void sendDataToDevice(byte[] dataPayload, NetworkTree.Node destNode) throws IOException{
		int destinationAddress = destNode.getAddress();
		DataToDevice packet = new DataToDevice(destinationAddress, dataPayload);
		byte[] packetBytes = packet.getRawBytes().array();
		RootNode firstHop = destNode.getRouteToMyself();
		firstHop.getNetInterface().sendLayer3Packet(packetBytes, firstHop.getMacAddress());
	}
	
	
	// Incoming packet handlers
	
	private void onDataToBase(DataToBase data){
		Node node = null;
		if(newTree != null){
			node = newTree.getNodeFromAddress(data.getSourceAddress());
		}
		if(node==null && activeTree != null){
			node = activeTree.getNodeFromAddress(data.getSourceAddress());
		}
		if(node!=null){
			ILayer4 layer4 = node.getLayer4();
			if(layer4 == null){
				layer4 = new Layer4SimpleRpc(node, this);
				node.setLayer4AndAssigned(layer4);
			}
			layer4.onPacketReceived(data);
		}
	}
	
	private void onBeaconChildResponse(BeaconChildResponse beaconResp, ILayer2 srcInterface, int srcMacAddress){
		if(newTree!=null){
			try {
				newTree.setRootNode((int)beaconResp.getChildNonce(), srcInterface, srcMacAddress);
			} catch (TreeAlreadyCalculatedException
					| InconsistentTreeStructureException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void onBeaconParentResponse(BeaconParentResponse beaconResp){
		if(newTree!=null){
			try {
				newTree.setRelationship((int)beaconResp.getParentNonce(), (int)beaconResp.getChildNonce());
			} catch (TreeAlreadyCalculatedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Setup a network tree by sending (and retransmitting if necessary)
	 * beacons and assignAddress packets.
	 */
	public class NetworkSetupThread implements Runnable {
		
		public static final int MAX_NUM_ASSIGN_ADDRESS_RETRIES = 10;
		
		@Override
		public void run() {
			try {				
				// Generate a random baseNonce for the new network tree
				SecureRandom rand;
				try {
					rand = SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
				int baseNonce = rand.nextInt();
				// Create a new NetworkTree, and assign it as lastTree
				synchronized(this){
					newTree = new NetworkTree(baseNonce);
				}
				// Send beacons and wait so every device can answer with beacon responses
				sendBeacon(newTree);
				Thread.sleep(3000);
				// Hopefully I have received all beacon responses, now I assign addresses
				int retries = MAX_NUM_ASSIGN_ADDRESS_RETRIES;
				boolean isSomebodyUnassigned = true;
				while(retries>0 && isSomebodyUnassigned){
					isSomebodyUnassigned = sendAssignAddressToAllUnassignedDevices(newTree);
					// now I wait a bit while devices are sending me command 0 layer4 packets
					Thread.sleep(2000);
					retries--;
				}
				// Wow, now we should have the network working!!
				// I set the tree we have generated as the activeTree
				activeTree = newTree;
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}