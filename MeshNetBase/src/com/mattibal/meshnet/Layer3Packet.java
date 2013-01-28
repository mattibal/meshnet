package com.mattibal.meshnet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
	
	protected final ByteBuffer packet;
	
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
		
		public DataToBase(int sourceAddress, byte[] data) throws InvalidPacketException{
			super(2+data.length);
			if(sourceAddress > 255 || sourceAddress<0){
				throw new InvalidPacketException();
			}
			packet.put(DATA_TO_DEVICE_TYPE);
			this.srcAddr = sourceAddress;
			packet.put((byte)sourceAddress);
			packet.put(data);
			packet.position(2);
			this.data = packet;
		}
		
		public ByteBuffer getData(){
			return data;
		}
		
		public int getSourceAddress(){
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
		
		public DataToDevice(int destinationAddress, byte[] data) throws InvalidPacketException{
			super(2+data.length);
			if(destinationAddress > 255 || destinationAddress<0){
				throw new InvalidPacketException();
			}
			packet.put(DATA_TO_DEVICE_TYPE);
			this.destAddr = destinationAddress;
			packet.put((byte)destinationAddress);
			packet.put(data);
			packet.position(2);
			this.data = packet;
		}
		
		public ByteBuffer getData(){
			return data;
		}
		
		public int getDestinationAddress(){
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
		
		public Beacon(int networkId, int baseNonce) throws InvalidPacketException{
			super(7);
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
		
		public static final int PACKET_LEN = 9;
		
		private final long childNonce;
		
		private BeaconChildResponse(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != PACKET_LEN-1){
				throw new InvalidPacketException();
			}
			this.childNonce = bytes.getInt();
		}
		
		public BeaconChildResponse(int childNonce, int baseNonce, int networkKey) throws InvalidPacketException{
			super(PACKET_LEN);
			packet.put(BEACON_CHILD_RESPONSE_TYPE);
			this.childNonce=childNonce;
			packet.putInt(childNonce);
			packet.putInt(calculateHmac(baseNonce, networkKey));
		}
		
		public long getChildNonce(){
			return childNonce;
		}	
		
		public void verifyHmac(int baseNonce, int networkKey) throws InvalidPacketException {
			int writtenHmac = packet.getInt(PACKET_LEN-4);
			int generated = calculateHmac(baseNonce, networkKey);
			if(generated != writtenHmac){
				throw new InvalidPacketException();
			}
		}
		
		private int calculateHmac(int baseNonce, int networkKey){
			ByteBuffer key = ByteBuffer.allocate(8);
			key.order(ByteOrder.LITTLE_ENDIAN);
			key.putInt(baseNonce);
			key.putInt(networkKey);
			return generateHmac(packet.duplicate(), PACKET_LEN-4, key).getInt();
		}
	}
	
	
	
	public static class BeaconParentResponse extends Layer3Packet{
		
		public static final int PACKET_LEN = 13;
		
		private final int childNonce;
		private final int parentNonce;
		
		private BeaconParentResponse(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != PACKET_LEN-1){
				throw new InvalidPacketException();
			}
			this.childNonce = bytes.getInt();
			this.parentNonce = bytes.getInt();
		}
		
		public BeaconParentResponse(int childNonce, int parentNonce, int baseNonce, int networkKey) throws InvalidPacketException{
			super(PACKET_LEN);
			packet.put(BEACON_PARENT_RESPONSE_TYPE);
			this.childNonce=childNonce;
			this.parentNonce=parentNonce;
			packet.putInt(childNonce);
			packet.putInt(parentNonce);
			packet.putInt(calculateHmac(baseNonce, networkKey));
		}
		
		public int getChildNonce(){
			return childNonce;
		}
		
		public int getParentNonce(){
			return parentNonce;
		}
		
		public void verifyHmac(int baseNonce, int networkKey) throws InvalidPacketException {
			int writtenHmac = packet.getInt(PACKET_LEN-4);
			int generated = calculateHmac(baseNonce, networkKey);
			if(generated != writtenHmac){
				throw new InvalidPacketException();
			}
		}
		
		private int calculateHmac(int baseNonce, int networkKey){
			ByteBuffer key = ByteBuffer.allocate(8);
			key.order(ByteOrder.LITTLE_ENDIAN);
			key.putInt(baseNonce);
			key.putInt(networkKey);
			return generateHmac(packet.duplicate(), PACKET_LEN-4, key).getInt();
		}
			
	}
	
	
	
	public static class AssignAddress extends Layer3Packet{
		
		public static final int PACKET_LEN = 11;
		
		private final long childNonce;
		private final int address;
		private final int maxRoute;
		
		private AssignAddress(ByteBuffer bytes) throws InvalidPacketException {
			super(bytes);
			if(bytes.remaining() != PACKET_LEN-1){
				throw new InvalidPacketException();
			}
			this.childNonce = bytes.getInt();
			this.address = ((int)bytes.get()) & 0xff;
			this.maxRoute = ((int)bytes.get()) & 0xff;
		}
		
		public AssignAddress(int childNonce, int address, int maxRoute, int baseNonce, int networkKey) {
			super(PACKET_LEN);
			packet.put(ASSIGN_ADDRESS_TYPE);
			this.childNonce=childNonce;
			this.address=address;
			this.maxRoute=maxRoute;
			packet.putInt(childNonce);
			packet.put((byte)address);
			packet.put((byte)maxRoute);
			packet.putInt(calculateHmac(baseNonce, networkKey));
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
		
		public void verifyHmac(int baseNonce, int networkKey) throws InvalidPacketException {
			int writtenHmac = packet.getInt(PACKET_LEN-4);
			int generated = calculateHmac(baseNonce, networkKey);
			if(generated != writtenHmac){
				throw new InvalidPacketException();
			}
		}
		
		private int calculateHmac(int baseNonce, int networkKey){
			ByteBuffer key = ByteBuffer.allocate(8);
			key.order(ByteOrder.LITTLE_ENDIAN);
			key.putInt(baseNonce);
			key.putInt((int)childNonce & 0xff);
			key.putInt(networkKey);
			return generateHmac(packet.duplicate(), PACKET_LEN-4, key).getInt();
		}
	}
	
	
	private static ByteBuffer generateHmac(ByteBuffer messageBytes, int messageLen, ByteBuffer keyBytes){
		try {
			SecretKeySpec keySpec = new SecretKeySpec(keyBytes.array(), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			byte[] mess = new byte[messageLen];
			messageBytes.get(mess);
			byte[] result = mac.doFinal(mess);
			return ByteBuffer.wrap(result);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(); // this should never happen
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
