package com.mattibal.meshnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class represent a layer3 packet
 */
public class Layer3Packet {
	
	/**
	 * This build a Layer3Packet of the proper type from a byte array
	 */
	public static Layer3Packet buildFromByteArray(ByteBuffer bytes) throws InvalidPacketException {
		
		if(bytes.capacity() == 0){
			throw new InvalidPacketException("Packet with zero lenght");
		}
		
		bytes.order(ByteOrder.LITTLE_ENDIAN);
		int firstByte = bytes.get();
		
		if(firstByte == DATA_TO_BASE_TYPE){
			return new DataToBase(bytes);
		} else if (firstByte == DATA_TO_DEVICE_TYPE){
			return new DataToDevice(bytes);
		} else if (firstByte == BEACON_TYPE){
			return new Beacon(bytes);
		} else if (firstByte == BEACON_CHILD_RESPONSE_TYPE){
			return new BeaconChildResponse(bytes);
		} else if (firstByte == BEACON_PARENT_RESPONSE_TYPE){
			return new BeaconParentResponse(bytes);
		} else if (firstByte == ASSIGN_ADDRESS_TYPE){
			return new AssignAddress(bytes);
		} else {
			return new Layer3Packet(bytes); // Unknown packet type
		}
	}
	
	
	private static final byte DATA_TO_BASE_TYPE = 0x00;
	private static final byte DATA_TO_DEVICE_TYPE = 0x01;
	private static final byte BEACON_TYPE = 0x02;
	private static final byte BEACON_CHILD_RESPONSE_TYPE = 0x03;
	private static final byte BEACON_PARENT_RESPONSE_TYPE = 0x04;
	private static final byte ASSIGN_ADDRESS_TYPE = 0x05;
	
	final protected ByteBuffer packet;
	
	private Layer3Packet(int capacity){
		packet = ByteBuffer.allocate(capacity);
		packet.order(ByteOrder.LITTLE_ENDIAN);
	}
	private Layer3Packet(ByteBuffer src){
		this.packet=src;
	}
	
	public ByteBuffer getRawBytes(){
		return packet.duplicate();
	}
	
	
	public static class DataToBase extends Layer3Packet{
		
		private final int srcAddr; 
		private final ByteBuffer data;
		
		private DataToBase(ByteBuffer bytes) throws InvalidPacketException{
			super(bytes);
			if(bytes.remaining()==0){
				throw new InvalidPacketException();
			}
			this.srcAddr = ((int)bytes.get()) & 0xff;
			this.data=bytes; // i assume that the rest of the buffer is all data
		}
		
		public DataToBase(int sourceMacAddress, byte[] data) throws InvalidPacketException{
			super(2+data.length);
			if(sourceMacAddress > 255 || sourceMacAddress<0){
				throw new InvalidPacketException();
			}
			packet.put(DATA_TO_DEVICE_TYPE);
			this.srcAddr = sourceMacAddress;
			packet.put((byte)sourceMacAddress);
			packet.put(data);
			packet.position(2);
			this.data = packet;
		}
		
		public ByteBuffer getData(){
			return data;
		}
		
		public int getSourceMacAddress(){
			return srcAddr;
		}
	}

	
	public static class DataToDevice extends Layer3Packet{

		private final int destAddr; 
		private final ByteBuffer data;

		private DataToDevice(ByteBuffer bytes) throws InvalidPacketException{
			super(bytes);
			if(bytes.remaining()==0){
				throw new InvalidPacketException();
			}
			this.destAddr = ((int)bytes.get()) & 0xff;
			this.data=bytes; // i assume that the rest of the buffer is all data
		}
		
		public DataToDevice(int destinationMacAddress, byte[] data) throws InvalidPacketException{
			super(2+data.length);
			if(destinationMacAddress > 255 || destinationMacAddress<0){
				throw new InvalidPacketException();
			}
			packet.put(DATA_TO_DEVICE_TYPE);
			this.destAddr = destinationMacAddress;
			packet.put((byte)destinationMacAddress);
			packet.put(data);
			packet.position(2);
			this.data = packet;
		}
		
		public ByteBuffer getData(){
			return data;
		}
		
		public int getDestinationMacAddress(){
			return destAddr;
		}
	}
	
	
	
	public static class Beacon extends Layer3Packet{
		
		private final int networkId;
		private final long baseNonce;
		
		private Beacon(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != 6){
				throw new InvalidPacketException();
			}
			this.networkId = ((int)bytes.getShort()) & 0xffff;
			this.baseNonce = ((long)bytes.getInt()) & 0xffffffff;
		}
		
		public Beacon(int networkId, long baseNonce) throws InvalidPacketException{
			super(7);
			if(networkId > 65535 || networkId<0
					|| baseNonce > 4294967295L || baseNonce <0){
				throw new InvalidPacketException();
			}
			packet.put(BEACON_TYPE);
			this.networkId=networkId;
			this.baseNonce=baseNonce;
			packet.putShort((short)networkId);
			packet.putInt((int)baseNonce);
		}
		
		public int getNetworkId(){
			return networkId;
		}
		
		public long getBaseNonce(){
			return baseNonce;
		}	
	}
	
	
	
	public static class BeaconChildResponse extends Layer3Packet{
		
		private final long childNonce;
		private final long hmac;
		
		private BeaconChildResponse(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != 8){
				throw new InvalidPacketException();
			}
			this.childNonce = ((long)bytes.getInt()) & 0xffffffff;
			this.hmac = ((long)bytes.getInt()) & 0xffffffff;
		}
		
		public BeaconChildResponse(long childNonce, long hmac) throws InvalidPacketException{
			super(9);
			if(childNonce > 4294967295L || childNonce <0
					|| hmac > 4294967295L || hmac <0){
				throw new InvalidPacketException();
			}
			packet.put(BEACON_CHILD_RESPONSE_TYPE);
			this.childNonce=childNonce;
			this.hmac=hmac;
			packet.putInt((int)childNonce);
			packet.putInt((int)hmac);
		}
		
		public long getChildNonce(){
			return childNonce;
		}
		
		public long getHmac(){
			return hmac;
		}	
	}
	
	
	
	public static class BeaconParentResponse extends Layer3Packet{
		
		private final long childNonce;
		private final long parentNonce;
		private final long hmac;
		
		private BeaconParentResponse(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != 12){
				throw new InvalidPacketException();
			}
			this.childNonce = ((long)bytes.getInt()) & 0xffffffff;
			this.parentNonce = ((long)bytes.getInt()) & 0xffffffff;
			this.hmac = ((long)bytes.getInt()) & 0xffffffff;
		}
		
		public BeaconParentResponse(long childNonce, long parentNonce, long hmac) throws InvalidPacketException{
			super(13);
			if(childNonce > 4294967295L || childNonce <0
					|| parentNonce > 4294967295L || parentNonce <0
					|| hmac > 4294967295L || hmac <0){
				throw new InvalidPacketException();
			}
			packet.put(BEACON_PARENT_RESPONSE_TYPE);
			this.childNonce=childNonce;
			this.parentNonce=parentNonce;
			this.hmac=hmac;
			packet.putInt((int)childNonce);
			packet.putInt((int)parentNonce);
			packet.putInt((int)hmac);
		}
		
		public long getChildNonce(){
			return childNonce;
		}
		
		public long getParentNonce(){
			return parentNonce;
		}
		
		public long getHmac(){
			return hmac;
		}	
	}
	
	
	
	public static class AssignAddress extends Layer3Packet{
		
		private final long childNonce;
		private final int address;
		private final int maxRoute;
		private final long hmac;
		
		private AssignAddress(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != 10){
				throw new InvalidPacketException();
			}
			this.childNonce = ((long)bytes.getInt()) & 0xffffffff;
			this.address = ((int)bytes.getShort()) & 0xffff;
			this.maxRoute = ((int)bytes.getShort()) & 0xffff;
			this.hmac = ((long)bytes.getInt()) & 0xffffffff;
		}
		
		public AssignAddress(long childNonce, int address, int maxRoute, long hmac) throws InvalidPacketException{
			super(11);
			if(address > 65535 || address<0
					|| maxRoute > 65535 || maxRoute<0
					|| childNonce > 4294967295L || childNonce <0
					|| hmac > 4294967295L || hmac <0){
				throw new InvalidPacketException();
			}
			packet.put(ASSIGN_ADDRESS_TYPE);
			this.childNonce=childNonce;
			this.address=address;
			this.maxRoute=maxRoute;
			this.hmac=hmac;
			packet.putInt((int)childNonce);
			packet.putShort((short)address);
			packet.putShort((short)maxRoute);
			packet.putInt((int)hmac);
		}
		
		public long getChildNonce(){
			return childNonce;
		}
		public int getAddress(){
			return address;
		}
		public int getMaxRoute(){
			return maxRoute;
		}
		public long getHmac(){
			return hmac;
		}	
	}
	
	
	
	public static class InvalidPacketException extends IOException {
		public InvalidPacketException(){
		}
		public InvalidPacketException(String string) {
			super(string);
		}
	}

}
