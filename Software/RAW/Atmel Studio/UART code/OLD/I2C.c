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


unsigned char i2c_transmit(unsigned char type) {
	switch(type) {
		case I2C_START:
			// Send Start condition
			// MCU -> Master, enable TWI and interrupt routing
			TWCR = (1 << TWINT) | (1 << TWSTA) | (1 << TWEN);
			break;
		case I2C_DATA:
			// Send data with No-Acknowledge
			TWCR = (1 << TWINT) | (1 << TWEN);
			break;
		case I2C_DATA_ACK:
			// Send data with Acknowledge
			TWCR = (1 << TWEA) | (1 << TWINT) | (1 << TWEN);
			break;
		case I2C_STOP:
			// Send Stop condition
			TWCR = (1 << TWINT) | (1 << TWEN) | (1 << TWSTO);
			return 0;
	}

	// Wait for TWINT flag set on Register TWCR
	while (!(TWCR & (1 << TWINT)));

	return TWSR ;//(TWSR & 0xF8);-> 64bit prescaler mask with TWSR
}


char i2c_start(unsigned int dev_id, unsigned int dev_addr, unsigned char rw_type) {
	unsigned char twi_status; //TWSR
	char r_val = -1;

	// Transmit Start condition
	twi_status = i2c_transmit(I2C_START);

	// Check the TWI status
	if ((twi_status != TW_START) && (twi_status != TW_REP_START))
		return -1;

	// Send slave address (SLA_W)
	TWDR = (dev_id & 0xF0) | (dev_addr & 0x07) | rw_type;

	// Transmit i2c data
	twi_status = i2c_transmit(I2C_DATA);

	// Check the TWSR status
	if (twi_status != TW_MT_SLA_ACK)
		return -1;

	r_val = 0;
	return r_val;
}


void i2c_stop(void) {
	// Transmit i2c Data
	i2c_transmit(I2C_STOP);
}

char i2c_write(char data) {
	unsigned char twi_status;
	char r_val = -1;

	// Send/load the data to i2c Bus TWDR register
	TWDR = data;

	// Transmit i2c data
	twi_status = i2c_transmit(I2C_DATA);

	// Check the TWSR status
	if (twi_status != TW_MT_DATA_ACK) //M is not sending ACK
		return -1;

	r_val = 0;
	return r_val;
}

char i2c_write_string(char* data){
	unsigned char twi_status;
	char r_val = -1;
	twi_status = i2c_transmit(I2C_START);
	for (uint16_t i = 0; i < strlen(data); i++){	
		r_val = i2c_write(data[i]);
		return r_val;
	}
	i2c_stop();
	return 0;
}
	
}

char i2c_read(char *data,char ack_type) {
	unsigned char twi_status;
	char r_val = -1;

	if (ack_type) {
		// Read i2c data and send Acknowledge
		twi_status = i2c_transmit(I2C_DATA_ACK);
		if (twi_status != TW_MR_DATA_ACK)
			return r_val;
	} else {
		// Read i2c data and send No Acknowledge
		twi_status = i2c_transmit(I2C_DATA);
		if (twi_status != TW_MR_DATA_NACK)
			return r_val;
	}

	// Get the Data
	*data = TWDR;
	r_val = 0;
	return r_val;
}

