package com.mattibal.meshnet.utils;

/**
 * Calcola il CRC a 16 bit
 */
public class CrcCcittCalculator {

    int crc;

    public void reset(){
        crc = 0xffff;
    }

    public int get(){
        return crc;
    }

    public void update(byte d){
        int data = d&0xff;
        data ^= crc&0xff;
        data ^= (data << 4)&0xff;
        crc = (((data << 8) | ((crc>>8)&0xff)) ^ (data >> 4)
               ^ (data << 3));
    }
}
