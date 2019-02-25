/*
 * I2C.c
 * I2C MODEL
 * Created: 1/09/2018 12:39:39 PM
 * Author : Shamsuddoha Shameem
 */ 


//Assume CPU Clock is 125Khz at max
//Given that F_CPU in the Slave must >= 16 x SCL frequency
#ifndef  F_CPU
#define F_CPU 8000000UL //clock speed
#endif


#include <avr/io.h>
#include <util/twi.h>
#include <string.h>

#define F_SCL 500000UL // SCL frequency
#define Prescaler 1
#define TWBR_val ((((F_CPU / F_SCL) / Prescaler) - 16 ) / 2)




/*
 Reference: http://www.ermicro.com/blog/?p=950
 MCU atmega328 uses TWI register to handle i2c
 This code is not completely written by Me.
 ..........This code was used for 64 prescaler bits (3<<TWPS)..I changed it
 https://www.exploreembedded.com/wiki/A5.AVR_Communication_Protocols:I2C,_SPI
*/

/*
Note: The Device ID is read-only, hard-wired in the device.
This i2c works as follows: M-Master S - Slave
	i) SEND start condition
	ii) M sends device_Id with Read/write bit. 0=> Write ;1 => read
	iii) S must acknowledge to identify it.
		S sends ACK-bit to M => Acknowledged
		S sends NACK-bit to M => No-acknowledgment
		S sends ACK only if it has the I2C-slave address. (SLAVE_ADD +ACK)
		If device not identified, (No ACK ->NACK) master re-start the start condition. 
	iv) S sends the Slave Address
	v) As long as M sends ACK bit transmission continues,
		M ends the reading sequence by NACKing the last byte
		
NOTE: @ 3rd ACK from M resets the slave

For a I2C write:
	-External trigger is set to logical 1 
	-Start bit
	-Device address + slave ACK
	-Memory address + slave ACK
	-Data + slave ACK
	-Stop bit
	-External trigger is set to logical 0 

For a I2C write:
	-External trigger is set to logical 1
	-Start bit
	-Device address + slave ACK
	-Memory address + slave ACK
	-Data + slave ACK
	-Stop bit
	-External trigger is set to logical 0
	
Note: Setting TWSTA (TWI START Condition Bit) MCU becomes the Master

Data: requires=>I2C address, data (in char or bit or int)

	TWDR ( TWI Data Register )
	In Transmit mode: contains the next byte to be transmitted
	In Receive mode: contains the last byte received.	
*/

// I2C constants
#define I2C_START 0 
#define I2C_DATA 1
#define I2C_DATA_ACK 2
#define I2C_STOP 3
#define ACK 1
#define NACK 0

//Now we need a device address and Id for this to run

void i2c_init(void){
	//bit generator
	TWBR = (uint8_t)TWBR_val;
}
