package com.mattibal.meshnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class represent the tree of the devices "connected" with a base.
 * 
 * The relationships of an instance of this tree can't be modified or deleted,
 * they can only be added. If you need to modify a relationship, you have to
 * create a new instance of this class.
 */
public class NetworkTree {

	private HashSet<RootNode> rootNodes = new HashSet<RootNode>();
	
	/** When this will be set to true, it will not be possible anymore to add nodes */
	private boolean addrCalculationDone = false;
	
	protected final int baseNonce;
	
	public NetworkTree(int baseNonce){
		this.baseNonce = baseNonce;
	}

	
	/**
	 * Get the root node that is the first route for the given address.
	 * @return null if the given address is not of my network
	 */
	public synchronized RootNode getRouteToNode(int nodeAddress){
		for(RootNode node: rootNodes){
			if(node.getAddress() <= nodeAddress && node.getMaxRoute() >= nodeAddress){
				return node;
			}
		}
		return null;
	}
	
	public synchronized void setRootNode(int childNonce, Layer3Base.ILayer2 interf, int macAddress) throws TreeAlreadyCalculatedException, InconsistentTreeStructureException{
		Node n = childNonceToNodes.get(childNonce);
		if(n == null){
			if(addrCalculationDone){
				throw new TreeAlreadyCalculatedException();
			}
			RootNode rootNode = new RootNode(childNonce, interf, macAddress);
			rootNodes.add(rootNode);
		} else {
			// Check if this setRootNode request is consistent with the tree structure
			if(!(n instanceof RootNode)){
				throw new InconsistentTreeStructureException();
			}
			RootNode rootNode = (RootNode) n;
			if(!rootNode.getNetInterface().equals(interf)){
				throw new InconsistentTreeStructureException();
			}
		}
	}
	
	public synchronized void setRelationship(int parentNonce, int childNonce) throws TreeAlreadyCalculatedException{
		Node parent = getNodeFromNonce(parentNonce);
		Node child = getNodeFromNonce(childNonce);
		if(addrCalculationDone && !parent.children.contains(child)){
			throw new TreeAlreadyCalculatedException();
		}
		parent.addChild(child);
	}
	
	/**
	 * Gets the next node that need to receive the "assignAddress" packet
	 * @return null if all nodes has been assigned
	 */
	public synchronized Node getNextUnassignedNode(){
		if(!addrCalculationDone){
			int endAddr = 0;
			for(Node node : rootNodes){
				endAddr = node.calculateAddrMaxRoute(endAddr+1);
			}
			addrCalculationDone = true;
		}
		Iterator<RootNode> it = rootNodes.iterator();
		Node unassigned = null;
		while(unassigned==null && it.hasNext()){
			unassigned = it.next().getNextUnassigned();
		}
		return unassigned;
	}
	
	
	/** The Integer is childNonce of the Node */
	private HashMap<Integer,Node> childNonceToNodes = new HashMap<Integer,Node>();
	
	private Node getNodeFromNonce(int nonce) throws TreeAlreadyCalculatedException{
		Node n = childNonceToNodes.get(nonce);
		if(n==null){
			if(addrCalculationDone){
				throw new TreeAlreadyCalculatedException();
			}
			n = new Node(nonce);
			childNonceToNodes.put(nonce, n);
		}
		return n;
	}
	
	/** The Integer is the address of the node */
	private HashMap<Integer,Node> addressToNode = new HashMap<Integer,Node>();
	
	public Node getNodeFromAddress(int address){
		return addressToNode.get(address);
	}
	
	
	/**
	 * This class represent a node (a device) of the tree
	 */
	public class Node {
		
		private int childNonce;
		private HashSet<Node> children = new HashSet<Node>();
		
		private int address = -1;
		private int maxRoute = -1;
		
		private Layer3Base.ILayer4 layer4 = null;
		
		private Node(int childNonce){
			this.childNonce = childNonce;
		}
		
		private void addChild(Node child){
			children.add(child);
		}
		
		/**
		 * Recursively calculate the address and maxRoute fields of this
		 * node and all his descendants.
		 * @param startAddr The start of the address range allocated to this node and his descendants
		 * @return The last (stop) address allocated to a descendant of this node
		 */
		private int calculateAddrMaxRoute(int startAddr){
			this.address = startAddr;
			addressToNode.put(startAddr, this);
			int stopAddr = startAddr;
			for(Node child: children){
				stopAddr = child.calculateAddrMaxRoute(stopAddr+1);
			}
			maxRoute = stopAddr;
			return stopAddr;
		}
		
		/**
		 * Recursively search for an unassigned node, starting from myself,
		 * and recursively with my descendants.
		 * @return The next unassigned node, or null if are all assigned
		 */
		protected synchronized Node getNextUnassigned(){
			if(layer4 == null){
				return this;
			}
			Iterator<Node> it = children.iterator();
			Node unassigned = null;
			while(unassigned==null && it.hasNext()){
				unassigned = it.next().getNextUnassigned();
			}
			return unassigned;
		}
		
		/**
		 * Layer3 needs this childNonce to calculate HMAC of messages for
		 * this node of this network tree instance.
		 */
		public int getChildNonce(){
			return childNonce;
		}
		
		// Needed by Layer3 to send an assignAddress to this node
		public int getAddress(){
			return address;
		}
		public int getMaxRoute(){
			return maxRoute;
		}
		
		/**
		 * @return null if the device is not "assigned" and doesn't yet have a layer4
		 */
		public synchronized Layer3Base.ILayer4 getLayer4(){
			return layer4;
		}
		
		/**
		 * This also implicitly set the device as "assigned" (has received assignAddress)
		 */
		protected synchronized void setLayer4AndAssigned(Layer3Base.ILayer4 layer4){
			this.layer4 = layer4;
		}
		
		public RootNode getRouteToMyself(){
			return NetworkTree.this.getRouteToNode(this.getAddress());
		}
	}
	
	
	/**
	 * This is a root node. Since it's directly connected to me, I also
	 * know what is my layer2 interface that is connected to him.
	 */
	public class RootNode extends Node {
		
		private final Layer3Base.ILayer2 interf;
		private final int macAddress;
		
		public RootNode(int childNonce, Layer3Base.ILayer2 interf, int macAddress){
			super(childNonce);
			this.interf = interf;
			this.macAddress = macAddress;
		}
		
		public Layer3Base.ILayer2 getNetInterface(){
			return interf;
		}
		
		public int getMacAddress(){
			return macAddress;
		}
	}
	
	
	
	@SuppressWarnings("serial")
	public static class TreeAlreadyCalculatedException extends Exception {
	}
	
	@SuppressWarnings("serial")
	public static class InconsistentTreeStructureException extends Exception {
	}
}
