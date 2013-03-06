#include "MeshNet.h"

#include "SerialLayer2.h"

// DEVICE TYPE
const uint32_t deviceType = 123;

// DEVICE UNIQUE ID
uint32_t deviceUniqueId = 384932;


/** LAYER 2 DEPENDENT CODE **/

const uint8_t SERIAL_INTERFACE = 1;

// The number of network interfaces that this device has
const int NUM_INTERFACES = 1;

// Pass a layer3 packet to the layer2
int sendPacket(unsigned char* message, uint8_t len, uint8_t interface, uint8_t macAddress){
  
    // Here should be called the layer2 function corresponding to interface
    if(interface == SERIAL_INTERFACE){
         serialSendPacket(message, len, macAddress);
         return 1;
    }

    return 0;
}



/** LAYER 7 CODE */


struct setLedStateRx {
  uint8_t ledState;
} __attribute__((packed));

void onSetLedStateRx(struct setLedStateRx* data){
  if(data->ledState == 1){
      digitalWrite(13, HIGH);
      //delay(10);
      //digitalWrite(4, LOW);
  } else {
      digitalWrite(13, LOW);
  }
}


void onSetLedPwmStateRx(uint8_t level){
  analogWrite(3, level);
}

struct sendAnalogReadPacket {
  uint16_t analogValue;
} __attribute__((packed));
  
void sendAnalogRead(int pin){
  uint8_t command;
  if(pin==A0){
    command = 3;
  } else {
    command = 4;
  }
  struct sendAnalogReadPacket packet;
  packet.analogValue = analogRead(pin);
  sendCommand(command, (void*) &packet, sizeof(packet));
}

void onCommandReceived(uint8_t command, void* data, uint8_t dataLen){
  if(command==1 && dataLen >= sizeof(struct setLedStateRx)){
    onSetLedStateRx((struct setLedStateRx*)data);
  }
  if(command==2 && dataLen >= sizeof(uint8_t)){
    onSetLedPwmStateRx(*((uint8_t *) data));
  }
  if(command==3){
    sendAnalogRead(A0);
  }
  if(command==4){
    sendAnalogRead(A1);
  }
}


void setup(){
  
    Serial.begin(9600);
    
    pinMode(13, OUTPUT); // for the LED
    
    int rfin;
    int r=0;
    for(int i=0; i<5; i++){
        rfin += analogRead(random(1,5));
        randomSeed(rfin);
        delay(100);
    }
    
    serialInit();
    
    delay(2000);
}


void loop(){
    
    serialReceive();
    
}
