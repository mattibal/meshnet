/*
    This file is part of the MeshNet Arduino library.
    Copyright (C) 2013  Mattia Baldani

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
   The Layer2 of MeshNet that uses a serial (optionally multidrop like RS-485) connection using the Arduino Serial library
*/


#include "SerialLayer2.h"

/*
--- Specifiche layer2 per connessioni seriali: ---
Come layer1 viene usata una UART, cioè una cosa seriale che trasmette e riceve contemporaneamente un flusso di bit.
Subito sopra il layer1 UART c'è l'HDLC, che si occupa del framing dei pacchetti e del controllo CRC.
Come payload del pacchetto HDLC, userò questi tipi di pacchetti:

1 byte: indirizzo MAC sorgente
1 byte: indirizzo MAC destinazione

Se devo inviare un pacchetto in broadcast, l'indirizzo MAC destinazione sarà 0.

Dato che ho solo 256 indirizzi disponibili, possono esserci dei conflitti. Posso fare che quando mi sveglio scelgo come mio indirizzo MAC un numero a caso, e provo ad andare avanti e usare il layer3 con quell'indirizzo MAC. Se poi vedo che il layer3 non riesce a collegarsi alla base, cioè gli HMAC dei messaggi di risposta della base non sono stati fatti con il mio ChildNonce, potrebbe voler dire che c'è stato un conflitto di indirizzi MAC, e quindi scelgo un nuovo mio indirizzo MAC casuale e riprovo a instaurare la connessione con il layer3.
Il sistema deve tollerare un conflitto di indirizzi MAC anche per motivi di sicurezza, un attaccante può inviare un pacchetto con lo stesso indirizzo MAC di un mio device dato che non c'è nessuna autenticazione, quindi idealmente il mio sistema non deve risentire per niente di un conflitto del genere.

Se uso la RS-485 con più di 2 device, potrebbero esserci collisioni, e in quel caso l'HDLC vede che il pacchetto ricevuto è corrotto e lo scarta, quindi quei 2 o più pacchetti entrati in collisione vengono persi. Se il traffico è poco potrei anche fregarmene se vengono perduti pacchetti ogni tanto nel layer2, e gestire tutto al layer4.

*/

/*

Layer2 frame specifications:

   byte 0       byte 1     byte 2     byte 3     byte 4     byte 5     byte 6     byte 7    
  +----------+----------+----------+----------+----------+----------+----------+----------+----   ---+----------+----------+
  | PPPPPPPP | LLLLLLLL | SSSSSSSS | DDDDDDDD | PPPPPPPP | PPPPPPPP | PPPPPPPP | PPPPPPPP | ........ | CCCCCCCC | CCCCCCCC |
  +----------+----------+----------+----------+----------+----------+----------+----------+----   ---+----------+----------+
where:
    P = preamble: 01111110 (0x7E)
    L = payload length in bytes
    S = source MAC
    D = destination MAC
    P = payload (variabile length, with a maximum)
    C = CRC CCITT 16 bit
    
IMPORTANT: if any byte after preamble contains these two values: 0x7E (preamble) or 0x7D (escape), it will be send first an escape byte (0x7D), then the original byte XOR-ed with 0x20. The decoder when it sees the escape byte, must xor the subsequent byte and put them in the decoded message.
    
*/

#define MAX_FRAME_LENGTH 40
const uint8_t serialPreamble = 0x7E;
const uint8_t serialEscape = 0x7D;

typedef struct {
    uint8_t payloadLen;
    uint8_t srcMac;
    uint8_t destMac;
    unsigned char data[MAX_FRAME_LENGTH-5];
} serialFrame __attribute__((packed));

serialFrame serialRxFrame;
uint8_t serialRxFrameLen = 0; // this is actually sizeof(serialFrame)+2 (the 2 byte CRC)
uint8_t serialIsLastEscape = 0;
uint8_t serialRxFirstCrcByte;

uint8_t serialMyMacAddress;



static void serial_put_char(char data){
    Serial.write(data);
}


uint16_t serialCalculateCrc(uint8_t *msg,int n){
  uint16_t x = 0;
  while(n--)
  {
    x = _crc_xmodem_update(x, (uint8_t)*msg++);
  }
  return(x);
}


void serialInit(){
    serialMyMacAddress = random(1,255);    
}


void serialOnByteReceived(uint8_t byte){

    if(byte == serialPreamble){
        serialRxFrameLen = 0;
        serialIsLastEscape = 0;
    } else if(byte == serialEscape){
        serialIsLastEscape = 1;
    } else {
        if(serialIsLastEscape == 1){
            serialIsLastEscape = 0;
            byte = byte ^ 0x20;
        }
        // now I have the data byte
        if(serialRxFrameLen > 2 && (serialRxFrame.payloadLen > MAX_FRAME_LENGTH-5 || (serialRxFrame.destMac != serialMyMacAddress && serialRxFrame.destMac != 0))){
            // frame corrupted or not destined to me
            return;
        }
        if(serialRxFrameLen == serialRxFrame.payloadLen+3){
            // this is the CRC byte1
            serialRxFirstCrcByte = byte;
        } else if(serialRxFrameLen == serialRxFrame.payloadLen+4){
            uint16_t calcCrc = serialCalculateCrc((unsigned char *)&serialRxFrame, serialRxFrame.payloadLen+3);
            struct crc {
               uint8_t crc1;
               uint8_t crc2;
            } __attribute__((packed));
            struct crc recvCrc;
            recvCrc.crc1 = serialRxFirstCrcByte;
            recvCrc.crc2 = byte;
            if(*((uint16_t *) &recvCrc) == calcCrc){
                // The frame is valid!! I pass them to the layer3
                processIncomingPacket((unsigned char*) &serialRxFrame.data, (uint8_t) serialRxFrameLen-4, SERIAL_INTERFACE, serialRxFrame.srcMac);
            }
        } else {
             *(((uint8_t *)&serialRxFrame)+serialRxFrameLen) = byte;
        }
        serialRxFrameLen++;
    }
}


void serialReceive(){
    while(Serial.available()>0){
        uint8_t data = Serial.read();
        serialOnByteReceived(data);
    }
}


int serialSendPacket(unsigned char* message, uint8_t len, uint8_t macAddress){
    
    DEBUG_PRINT("serialSendPacket!macDest:");
    DEBUG_PRINT(macAddress);
    serialFrame frame;
    if(len>sizeof(frame.data)){
        return 0;
    }
    frame.srcMac = serialMyMacAddress;
    frame.destMac = macAddress;
    frame.payloadLen = len;
    memcpy(&frame.data,message, len);
    DEBUG_PRINT("packet:");
    printPacket((unsigned char*)&frame, len+3);
    
    // Calculate crc    
    uint16_t crc = serialCalculateCrc((unsigned char *)&frame, len+3);
    DEBUG_PRINT("crc:");
    printPacket((unsigned char*)&crc, 2);
    
    Serial.write(serialPreamble);
    
    // Send struct
    uint8_t i;
    for(i=0; i<(len+3); i++){
       unsigned char byte = *(((unsigned char*)&frame)+i);
       if(byte == serialEscape || byte == serialPreamble){
           Serial.write(serialEscape);
           byte = byte ^ 0x20;
       }
       Serial.write(byte);
    }
    
    // Send crc
    //Serial.write(crc);
    
    for(i=0;i<2;i++){
    unsigned char byte = *(((unsigned char *)&crc)+i);
        if(byte == serialEscape || byte == serialPreamble){
           Serial.write(serialEscape);
           byte = byte ^ 0x20;
       }
       Serial.write(byte);
    }

}
