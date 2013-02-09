MeshNet
===================

MeshNet is a stack of network protocols designed for home automation and wireless sensor networks. 

It lets you make an extremely low cost network of 8-bit microcontrollers with sensors and actuators spread around your home, connected with many different wired and wireless technologies, all controlled by a software written in an high-level language like Java that runs in one or more high-power central nodes (for example a Rasperry Pi).
MeshNet is similar to ZigBee networks, but it's much more low cost: you can use a Nordic nRF24l01 2,4 GHz wireless module shipped from China for only 1€, instead of an XBee radio that cost more than 25€!!

This implementation of MeshNet is composed by two parts: an Arduino library that must runs in Atmega328 microcontrollers that are part of the network, and a Java code that runs in the "Base" (a Raspberry Pi) that is needed to make the network working.


Protocols stack
--------------------

The MeshNet protocols involve many layers of the ISO/OSI stack:
* Layer3: MeshNet uses a "tree-routing" network, where the root of the tree is the "Base" (a Rasp Pi or similar) where all the complex logic runs, and all the other nodes are the microcontrollers with sensors and actuators. Like the Internet Protocol (IP), the MeshNet Layer3 can interconnect many different Layer2 networks, for example a wired RS-485 serial or CAN bus with a ZigBee or nRF24l01 wireless network. Every node (microcontroller) is a Layer3 router that can extend the range of the network, and the routes are dynamically created by the various Bases, making MeshNet a fault tolerant mesh network. Every time you add, move or remove a device from the network (including Bases), the tree is rebuild and everything start working again in a matter of seconds.
* Layer2: in this implementation I have written one layer 2 for serial connections and one for the Nordic nRF24l01 radios using a slightly modified version of the RF24 Arduino library from maniacbug ( https://github.com/maniacbug/RF24 ). The serial layer 2 uses an HDLC-like protocol with CRC16 error detection and the Arduino Serial library (so the Atmega328 UART). It can be simply used with the USB from your computer to an Arduino board, or maybe for something more complex like a multi-drop RS-485 or CAN bus.
* Layer4 and Layer7: this is actually just a very simple RPC protocol on the top of a UDP-like layer4 protocol, but can be extended with functionalities similar to TCP.

Why you didn't just use the classic TCP/IP stack instead of making a new one?? Because MeshNet must be extremely efficient: the Atmega 328 has just 2 KB of RAM, so the routing tables must be very small, and the nRF24l01 radio frames have a 32 byte max payload size, instead of the typical Internet MTU of about 1500 bytes.


Security
--------------------

MeshNet is designed with security in mind: every message can be protected with an HMAC-SHA1, and the key contains some random nonces to protect from replay attacks. However I'm not a cryptography expert and this will certainly have huge flaws, so don't rely on it.


Infos about the code
--------------------

The code is licensed as GPLv3.

The code is divided in these parts:
* "arduino lib" - the Arduino library
* "arduino sketches" - these are some example Arduino sketches to test the library
* "MeshNetBase" - this is an Eclipse Java project of the code that runs on the Base of the network (an always on Raspberry Pi for example), that uses RXTX to access the serial port, so you have to pass a parameter to the JVM to make it working (search Java RXTX for more informations).


Development status
--------------------

The code and the protocols described of MeshNet are in a very early stage of development.

This is a list of things that need to be done:
* All devices must detect a MAC address collision (this happens frequently since it's 8 bit long, so 256 available addresses) and change their address quickly
* Serial tx collision detection and retransmission, something like CSMA/CA
* Maybe a better layer4 protocol that has some functionalities of TCP like retransmission
* A good documentation

Currently I have tested MeshNet only with this network configuration:
* An Arduino Uno with an nRF24l01+ module and connected via USB to a computer where runs the Java software
* Another Arduino Uno with an nRF24l01+ module and with a LED on pin 4

In both Arduinos must run the "MeshNet_Serial_RF24" sketch, with a different "deviceUniqueId" constant for each one.
When you launch "MeshNetTest.java" in the computer, it will setup the network in some seconds, and then the LED on the 2nd Arduino will start blink!

Every time the LED is switched on or off, a packet has been sent by the base (the Java program) in the 1st Arduino via the USB (serial) connection, and then routed by them to the 2nd Arduino, where it's executed the RPC handler function that switch on and off the LED.
