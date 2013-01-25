package com.mattibal.meshnet;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This version of the TwoWaySerialComm example makes use of the 
 * SerialPortEventListener to avoid polling.
 *
 */
public class TwoWaySerialComm
{
    public TwoWaySerialComm()
    {
        super();
    }
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                               
                (new Thread(new SerialWriter(out))).start();
                
                serialPort.addEventListener(new SerialReader(in));
                serialPort.notifyOnDataAvailable(true);

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /**
     * Handles the input coming from the serial port. A new line character
     * is treated as the end of a block in this example. 
     */
    public static class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        private byte[] buffer = new byte[1024];
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void serialEvent(SerialPortEvent arg0) {
            int data;
          
            try
            {
                int len = 0;
                while ( ( data = in.read()) > -1 )
                {
                    if ( data == '\n' ) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
                System.out.print(new String(buffer,0,len));
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }             
        }

    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }            
        }
    }
    

    
    public static void main ( String[] args )
    {
        try
        {
            (new TwoWaySerialComm()).connect("COM3");
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}