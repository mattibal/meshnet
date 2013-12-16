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
 * The Layer2 of MeshNet that uses the RF24 Arduino library
 */


#include "RF24Layer2.h"



/*

  SPECIFICATIONS:

Devo tenere la tabella "phyDestTable" con queste colonne:
- MAC (indirizzo MAC del device a cui si vuole inviare un messaggio)
- PHY (indirizzo fisico a cui va inviato un pacchetto se si vuole inviarlo al device corrispondente all'indirizzo MAC qui sopra)
questa tabella potrebbe avere grandezza massima infinita, ma va limitata a seconda della RAM disponibile. Se viene riempita, continua tutto a funzionare ma si perde un po' di efficienza.

Devo tenere la tabella "myPipeTable" con queste colonne:
- PHY (indirizzo fisico di una pipe che ho attiva quando sono in ascolto, e alla quale uno ed un solo altro device può contattarmi)
- MAC (indirizzo MAC del device che può contattarmi alla mia pipe con l'indirizzo fisico qui sopra)
questa tabella deve avere grandezza massima 4 (il numero delle mie pipe disponibili). Se proprio non ho RAM libera posso farla più piccola, si perde solo un po' di efficienza.


Il payload di un pacchetto fisico (proprio quello usato dai chip RF) è fatto così:

1 byte: srcMac (indirizzo MAC mittente del pacchetto) ATTENZIONE: se è un broadcast deve essere 0 !!!!
1 byte: replyPhy (indirizzo fisico a cui il destinatario di questo pacchetto, può replicare a questo pacchetto)
da 1 a 30 byte: dati del layer3


Gli indirizzi fisici veri e propri che saranno inviati ai moduli RF sono fatti così:
byte 1: 11010010
byte 2: 11010010
byte 3-4: networkID
byte 5: phyAddr (indirizzo fisico destinazione)


Quando ricevo un pacchetto dal chip RF devo:
- guardare dentro il payload se il srcMac è uguale al replyPhy:
    - se sono uguali tutto ok, proseguo.
    - se non sono uguali, guarda se il srcMac è presente nella tabella "phyDestTable" come "MAC":
    	- se è presente tutto ok, prosegui.
    	- se non è presente, aggiungi una riga nella "phyDestTable" scrivendo il srcMac come "MAC" e il replyPhy come "PHY".
- passa al layer3 i dati, indicando come macAddress quello scritto in srcMac.

Quando il layer3 mi passa un pacchetto per essere inviato:
- guarda se il macAddress che mi ha dato il layer3 è uguale a 0 (broadcast):
    - se lo è, scrivi nel pacchetto da inviare come replyPhy il mio indirizzo MAC (che devo avere in una variabile)
    - se non lo è:
	- guarda se il macAddress di destinazione c'è nella "phyDestTable" sotto "MAC":
	    - se c'è, metti il "PHY" scritto in quella riga della tabella come indirizzo fisico di destinazione da dare al chip RF, ed attiva gli ACK
	    - se non c'è, metti come indirizzo fisico da dare al chip il macAddress che mi ha dato il layer3, e disattiva gli ACK
	- guarda nella tabella "myPipeTable" se c'è sotto "MAC" il macAddress di destinazione che mi ha passato il layer3:
	    - se c'è, imposta come replyPhy del pacchetto da inviare, il "PHY" scritto in quella riga della tabella
	    - se non c'è, guarda se la tabella "myPipeTable" è piena:
	        - se è piena, imposta come replyPhy del pacchetto da inviare, il mio indirizzo MAC.
	        - se non è piena, aggiungi una riga con come "MAC" l'indirizzo MAC destinazione che mi ha inviato il layer3, e come "PHY" metti un numero generato casualmente da 1 a 256 (volendo puoi controllare che sto numero casuale non ci sia mai nei campi MAC e PHY di tutte e due le mie tabelle, e che non sia uguale al mio MAC address). Nel pacchetto da inviare, setta come replyPhy sempre quel numero che ho appena generato.
- imposta come srcMac del pacchetto da inviare, il mio MAC address.
- passa il pacchetto da inviare al chip RF.

*/


typedef struct {
    uint8_t srcMac;
    uint8_t replyPhy;
    unsigned char data[30];
} rf24frame __attribute__((packed));


typedef struct {
    uint8_t phyAddr;
    uint16_t netId;
    uint16_t first16bits;
    uint16_t padding1;
    uint8_t padding2;
} rf24addr __attribute__((packed));



typedef struct {
    uint8_t mac;
    uint8_t phy;
} macPhyStruct;

#define PHY_DEST_TABLE_MAX_LEN 10
macPhyStruct phyDestTable[PHY_DEST_TABLE_MAX_LEN];
uint8_t phyDestTableLen = 0;

#define MY_PIPE_TABLE_MAX_LEN 4
macPhyStruct myPipeTable[MY_PIPE_TABLE_MAX_LEN];
uint8_t myPipeTableLen = 0;


uint8_t rf24myMacAddress;



uint8_t rf24getUnusedMacPhyAddress(){

    uint8_t tries;
    for(tries=0; tries < 255; tries++){
        uint8_t num;
        num = random(1,255);
        if(num != rf24myMacAddress){
            uint8_t i;
            bool found = false;
            for(i=0; i < phyDestTableLen; i++){
                if( (phyDestTable[i].mac == num) || (phyDestTable[i].phy == num) ){
                    found = true;
                    break;
                }
            }
            if(!found){
                for(i=0; i < myPipeTableLen; i++){
                    if( (myPipeTable[i].mac == num) || (myPipeTable[i].phy == num) ){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    return num;
                }
            }
        }
    }
    
    // There is no unused address!!! My network is way too big! O.o
    return 0;
}



void rf24startListening(){

    // Open pipe 0 (broadcast), I must do this every time because his address is modified when I transmit data
    rf24addr broadcastPipe;
    broadcastPipe.first16bits = 0xD2D2;
    broadcastPipe.padding1 = 0x0000;
    broadcastPipe.padding2 = 0x00;
    broadcastPipe.netId = networkId;
    broadcastPipe.phyAddr = 0;
    radio.openReadingPipe(0, *(uint64_t *) &broadcastPipe);
    
    radio.startListening();
}


void rf24init(){

    DEBUG_PRINTLN("rf24init begin");

    radio.begin();
    radio.setChannel(90);
    radio.setDataRate(RF24_250KBPS);
    radio.setCRCLength(RF24_CRC_16);
    radio.enableDynamicPayloads();
    radio.enableDynamicAck();
    
    // Reset data structures
    phyDestTableLen = 0;
    myPipeTableLen = 0;
    
    // Pick a random number as my MAC address of this RF24 interface
    rf24myMacAddress = random(1,255);
    DEBUG_PRINT("rf24myMacAddress: ");
    DEBUG_PRINTLN(rf24myMacAddress);
    
    // Open my no-ACK pipe (this must be done only one time during initialization)
    rf24addr noackPipe;
    noackPipe.first16bits = 0xD2D2;
    noackPipe.padding1 = 0x0000;
    noackPipe.padding2 = 0x00;
    noackPipe.netId = networkId;
    noackPipe.phyAddr = rf24myMacAddress;
    radio.openReadingPipe(1, *(uint64_t *) &noackPipe);
    
    DEBUG_PRINT("opened noack pipe 1 addr: ");
    printPacket((unsigned char *) &noackPipe, sizeof(noackPipe));
    
    
    rf24startListening();
}



void rf24receive(){
    
    if(radio.available()){
        
        boolean isLastPacket = false;
        while(!isLastPacket){
        
            rf24frame rf24rxFrame;
            uint8_t rf24rxFrameLen = 0;
        
            // Receive payload from the radio
            isLastPacket = radio.read( &rf24rxFrame, sizeof(rf24rxFrame));
            rf24rxFrameLen = radio.getDynamicPayloadSize();
            
            if(rf24rxFrame.srcMac != rf24rxFrame.replyPhy){
                uint8_t i;
                bool found = false;
                for(i=0; i < phyDestTableLen; i++){
                    if(phyDestTable[i].mac == rf24rxFrame.srcMac) {
                        found = true;
                        break;
                    }
                }
                if(!found){
                    if(phyDestTableLen < PHY_DEST_TABLE_MAX_LEN){
                       // Add a row in phyDestTable
                       phyDestTable[phyDestTableLen].mac = rf24rxFrame.srcMac;
                       phyDestTable[phyDestTableLen].phy = rf24rxFrame.replyPhy;
                       phyDestTableLen++;
                    }
                }
            }
            
            // Pass the data to the layer3!!
            processIncomingPacket( rf24rxFrame.data, rf24rxFrameLen-2, RF24_INTERFACE, rf24rxFrame.srcMac );
        }
    }   
}



int rf24sendPacket(unsigned char * message, uint8_t len, uint8_t macAddress){

    // Initialize data structures of the packet to be sent
    rf24frame frameToSend;
    if(len>sizeof(frameToSend.data)){ // message too large!!
       return 0;
    }
    memcpy(frameToSend.data, message, len);
    rf24addr destAddr;
    destAddr.first16bits = 0xD2D2;
    destAddr.padding1 = 0x0000;
    destAddr.padding2 = 0x00;
    destAddr.netId = networkId;
    bool enableAck = false;
    
    if(macAddress == 0){
        // I'm sending a broadcast packet
        frameToSend.replyPhy = rf24myMacAddress;
        destAddr.phyAddr = 0;
    } else {
    
        destAddr.phyAddr = macAddress;
        uint8_t i;
        for(i=0; i < phyDestTableLen; i++){
            if(phyDestTable[i].mac == macAddress){
                destAddr.phyAddr = phyDestTable[i].phy;
                enableAck = true;
                break;
            }
        }
        
        bool found = false;
        for(i=0; i < myPipeTableLen; i++){
            if(myPipeTable[i].mac == macAddress){
                frameToSend.replyPhy = myPipeTable[i].phy;
                found = true;
                break;
            }
        }
        if(!found){
            if(myPipeTableLen == MY_PIPE_TABLE_MAX_LEN){
                frameToSend.replyPhy = rf24myMacAddress;
            } else {
                // Assing a receive pipe to this device
                uint8_t newPhy;
                newPhy = rf24getUnusedMacPhyAddress();
                if(newPhy != 0){
                    frameToSend.replyPhy = newPhy;
                    myPipeTable[myPipeTableLen].phy = newPhy;
                    myPipeTable[myPipeTableLen].mac = macAddress;
                    myPipeTableLen++;
                    // Open the new pipe in the radio
                    rf24addr newPipe;
		    newPipe.first16bits = 0xD2D2;
		    newPipe.padding1 = 0x0000;
		    newPipe.padding2 = 0x00;
                    newPipe.netId = networkId;
                    newPipe.phyAddr = newPhy;
                    radio.openReadingPipe(myPipeTableLen+1, *(uint64_t *) &newPipe);
                }
            }
        }
    }
    
    frameToSend.srcMac = rf24myMacAddress;
    
    DEBUG_PRINT("rf24sendPacket sending this frame: ");
    printPacket((unsigned char *)&frameToSend, len+2);
    
    // Send the packet to the radio!
    radio.stopListening();
    radio.openWritingPipe(*(uint64_t *) &destAddr);
    radio.write( &frameToSend, len+2, !enableAck);
    
    
    // Restart radio listening
    rf24startListening();
    
    return 1;
}
