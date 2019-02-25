
/*
 *  I2C: Experiments with interfacing ATmega328p
 * Created: 5/10/2018 12:39:39 PM
 * Author : Shamsuddoha Shameem
 */ 


#ifndef  F_CPU
#define F_CPU 8000000UL // run CPU at 8 MHz
#endif

//Inline Macros
#define bit_clear(x,y) (x &= ~_BV(y))  //equivalent of (x&=~y) and BV-> LBShift
#define bit_set(x,y) (x |= _BV(y))
#define bit_flip(x,y) (x ^= _BV(y))

//Includes
#include <avr/io.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>


//Typedef
typedef uint8_t byte; //unsigned char byte
typedef int8_t sbyte; //

// I2C routines---------------------------------------------------------------------------
#ifndef  F_SCL
#define F_SCL 100000UL // I2C clock speed 100 KHz
#endif

#define TWBR_VAL (((F_CPU/F_SCL)-16)/2)
#define READ 1
#define TW_START (1<<TWINT)| (1<<TWSTA)|(1<<TWEN) // start condition
#define TW_STOP (1<<TWINT)| (1<<TWSTO)|(1<<TWEN) // stop condition
#define TW_ACK (1<<TWINT)|(1<<TWEN)|(1<<TWEA)) // Ack to Slave
#define TW_NACK (1<<TWINT)|(1<<TWEN) // Not-ACK to Slave
#define TW_SEND (1<<TWINT)|(1<<TWEN) // send data
#define TW_READY (TWCR & (1<<TWINT)) // ready for new transmission
#define TW_STATUS (TWSR & 0xF8) // returns value of status register
#define TW_STARTED (TW_STATUS == 0x08) //returns 1 or 0 if I2C started
#define TW_RESTARTED (TW_STATUS == 0x10) //returns 1 or 0 if I2C re-started
#define TW_CONTINUE_TO_READ (TW_STATUS == 0x40) //returns 1 or 0 if MR mode and ACK is sent
#define TW_LASTBYTE_RECIEVED (TW_STATUS == 0x58) //returns 1 or 0 if MR mode and NACK is sent
#define I2C_Stop() (TWCR = TW_STOP) //TWI macro for stop condition

void I2C_Start (byte slaveAddr);
#define I2C_Restart(byte slaveAddr) (I2C_Start (byte slaveAddr))



/*Initialize TWI/I2C interface*/
void I2C_Init(){
	/*set pre-scalar to one*/
	TWSR = 0;
	TWBR = TWBR_VAL; 
	
}

/* Looks for device at specified slave address 
 * returns 1 if found, 0 otherwise
 */
byte I2C_Detect(byte addr){
	/*sends start condition*/
	TWCR = TW_START; 
	/*wait for I2C to be ready*/
	while (!TW_READY){
		//Do nothing
	}; 
	/*adds the bus address to data register*/
	TWDR = addr;
	/*sends it*/
	TWCR = TW_SEND;
	while (!TW_READY); // Wait till start condition is transmitted
	/*TWSR register status: 0x18, if SLA+W has been transmitted and ACK has been received*/
	return (TW_STATUS==0x18); // return 1 if found; 0 otherwise
}

/*Returns 8bit bus address of the first device if found else 0*/
byte I2C_FindDevice(byte start){
	//address bit are within 8 bit or byte and a byte starts at 0b00000000 to 0b11111111
	for (byte addr=start;addr<0xFF;addr++){
		if (I2C_Detect(addr))
		return addr;
	}
	return 0;
}

/*Start to-do's for I2C*/
void I2C_Start (byte slaveAddr){
	I2C_Detect(slaveAddr);
}

/*Sends a data (byte) to slave*/
byte I2C_Write (byte data){
	//MCU is in MT(master-transmitter) TWI mode
	//task: Load the data -> send it through TWCR and check for TWSR to be 0x28
	TWDR = data; // load data
	TWCR = TW_SEND; // and send it
	while (!TW_READY); //wait for transmission
	
	//TWSR==0x28 if Data byte has been transmitted and ACK has been received
	return (TW_STATUS!=0x28);
}

/*Reads a data byte from slave with ACK*/
byte I2C_ReadACK (){
	//TWI mode:MR (SLA+R)
	//Set TWCR to read more data (Master sends ACK)
	TWCR = TW_ACK;
	//Wait for transmission of the condition
	while (!TW_READY);
	//return the loaded data from the register
	return TWDR;
}

/*Reads the last data byte from slave and stop the transmission (NACK)*/
byte I2C_ReadNACK (){
	//TWI mode:MR
	//Set TWCR to end transmitting more data
	TWCR = TW_NACK;
	//Wait for transmission of the condition
	while (!TW_READY);
	//Return the last data byte
	return TWDR;
}

/*I2C Write data byte to a device*/
void I2C_Write_Byte(byte busAddr, byte data){
	//If device address is known then-> find the device, then write to it
	//Find the device
	I2C_Start(busAddr);
	//Send data to that address
	I2C_Write(data);
	//Complete the transmission
	I2C_Stop();
}

/*I2C write a data to device specific register (forcefully change values)*/
void I2C_Write_To_Register(byte busAddr, byte deviceRegister, byte data){
	//Note: We do not need this function at all since we are reading only from Temp. sensor
	//It would be useful for RTC devices
	//Find the device
	I2C_Start(busAddr);
	//Sends the device register name first
	I2C_Write(deviceRegister);//First byte sent
	//Assuming it sends the ACK back and we continue to Write on it
	//Write data on to the device register
	I2C_Write(data); 
	//Stop Transmission
	I2C_Stop();
}

/*I2C reads from a specific register of the device if known*/
byte I2C_Read_From_Register(byte busAddr, byte deviceRegister){
	byte data = 0;
	//I2C reading procedure: Start + Address -> Restart with (R/W): here R
	//Start I2C
	I2C_Start(busAddr);
	//Mark the specific Device Register
	I2C_Write(deviceRegister); // set register pointer
	//Restart I2C with read operation
	I2C_Restart(busAddr+READ);
	//Read the data byte (from register) and close transmission
	data = I2C_ReadNACK(); 
	//Stop I2C Operation
	I2C_Stop();
	//Return the read data byte
	return data;
}