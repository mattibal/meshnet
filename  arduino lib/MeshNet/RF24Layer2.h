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
   The Layer2 of MeshNet that uses the RF24 Arduino library
*/

#ifndef __RF24LAYER2_H__
#define __RF24LAYER2_H__

#include <stddef.h>
#include <stdint.h>

/** These includes must be put also in the sketck, otherwise for some reason it doesn't compile */
#include "MeshNet.h"
#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"

extern RF24 radio;

const extern uint8_t RF24_INTERFACE;

void rf24init();
void rf24receive();
int rf24sendPacket(unsigned char *, uint8_t, uint8_t);


#endif
