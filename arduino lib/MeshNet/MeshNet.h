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
 
#ifndef __MESHNET_H__
#define __MESHNET_H__

#include "Arduino.h"

#include <stddef.h>
#include <stdint.h>

#include "hmac-sha1.h"

// Enable the debug messages via Serial 
//#define DEBUG_SERIAL_ENABLE

#ifdef DEBUG_SERIAL_ENABLE
#define DEBUG_PRINT(x) ({Serial.print(x);})
#else
#define DEBUG_PRINT(x)
#endif

#ifdef DEBUG_SERIAL_ENABLE
#define DEBUG_PRINTLN(x) ({Serial.println(x);})
#else
#define DEBUG_PRINTLN(x)
#endif

/** These functions must be implemented by the sketch */
int sendPacket(unsigned char *, uint8_t, uint8_t, uint8_t);
extern const int NUM_INTERFACES;
extern const uint32_t deviceType;
extern uint32_t deviceUniqueId;
void onCommandReceived(uint8_t command, void* data, uint8_t dataLen);


/** This is the function that every layer2 implementations must call to pass a packet to the layer3 */
void processIncomingPacket(unsigned char*, uint8_t, uint8_t, uint8_t);

void sendCommand(uint8_t command, void* data, uint8_t dataLen); // called by the sketch

extern uint16_t networkId;

void printDebugStateInfo();
int printPacket(unsigned char *, uint8_t);


#endif
