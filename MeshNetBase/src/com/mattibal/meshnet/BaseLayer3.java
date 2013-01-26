package com.mattibal.meshnet;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The Layer3 of a Base of the MeshNet
 * 
 * This class must handle the frames received by the various layer2 interfaces
 * of the base, and it can send a frame to the layer2.
 *
 */
public class BaseLayer3 {
	
	
	
	/**
	 * This is the method called by a layer2 when it receives a frame of data
	 */
	public synchronized void onFrameReceived(ByteBuffer frame){
		
	}
	
	
	/**
	 * This is the interface that a Layer2 must implements to work with this
	 * base layer3.
	 */
	static interface ILayer2{
		
		public void sendFrame(byte[] bytesToSend) throws IOException;
		
	}

}
