/*
 * engg2800g43.c
 * USART MODEL
 * Created: 25/08/2018 12:39:39 PM
 * Author : Shamsuddoha Shameem
 */ 

//Reference: https://appelsiini.net/2011/simple-usart-with-avr-libc/
//			 http://maxembedded.com/2013/09/the-usart-of-the-avr/
//			 https://hekilledmywire.wordpress.com/2011/02/23/direct-port-manipulation-using-the-digital-ports-tutorial-part-3/


#ifndef  F_CPU
#define F_CPU 1000000UL //1MHz
#endif

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#define BAUDRATE 9600
#define BAUD_PRESCALLER F_CPU/16UL/BAUDRATE-1 

//Define simple char
#define CHAR_NEWLINE '\n'
#define CHAR_RETURN '\r'
#define RETURN_NEWLINE "\r\n"

//Declaration of our functions
void USART_init(void);
unsigned char USART_receive(void);
void USART_send( unsigned char data);
void USART_send_string(char* String);
void USART_send_sequence();
void USART_handshsake();


char String[]="Hello from MCU";
char* sequence = "$B";

/*Main function of USART*/
int main(void){
	/*Call the USART initialization code*/
	USART_init();        

//	while(1){ //Infinite loop
//		USART_send_string(String);    //Pass the string to the USART_putstring function and sends it over the serial
//		_delay_ms(5000);        //Delay for 5 seconds so it will re-send the string every 5 seconds
//	}
	while(1){
		char* Send = "SEND CHECK : PASS";
		USART_send_string(String);
		_delay_ms(5000);
		USART_send_string(Send);
		_delay_ms(5000);			
	}
	
	return 0;
}

void USART_init(void){
	//Calculating the baud rate by high and low bit of UBRR0 register
	UBRR0H = ((uint8_t)BAUD_PRESCALLER)>>8;
	UBRR0L = (uint8_t)(BAUD_PRESCALLER);
	//Enable the RX and TX pin for receiving and sending
	UCSR0B = (1<<RXEN0)|(1<<TXEN0);
	//Define the frame format: 8 bit, zero stop bit
	UCSR0C = (3<<UCSZ00);//set 1 1 bit to UCSZ00 as UCSZ00 |= (1<<UCSZ00)|(1<<UCSZ01)
	//For 8 bit, 1 stop bit: UCSR0C |= (1<<UCSZ00)|(1<<UCSZ01)|(0<<USBS0);
	
	/* 
	// Enable the USART Receive interrupt
	UCSR0B |= (1 << RXCIE0 );

	// Enable the USART Send interrupt if we want
	UCSR0B |= (1 << TXCIE0);

	// Globally enable interrupts
	sei(); //This set ISR for USART_RX_vect

	while(1) {//Do whatever } 
	*/
}

unsigned char USART_receive(void){
	/* Wait for data to be received */
	while(!(UCSR0A & (1<<RXC0)));//RXC bit is enable
	return UDR0;
}

void USART_send( unsigned char data){//Send a char using serial communication
	/* Wait for empty transmit buffer */
	while(!(UCSR0A & (1<<UDRE0)));//when UDRE is 1 = there are some data in the buffer ready to be transferred
	/* Put data into buffer, sends the data */
	UDR0 = data;//User data register holds the data
}

void USART_send_string(char* String){
	while(*String != 0x00){//Check if the char in String pointer is not null char.
		USART_send(*String);
	String++;}	
}

void USART_send_sequence(){
	USART_send_string(sequence);
}

//Note: not implemented yet
void USART_handshsake(){//This is for acknowledgment
	//Implement a 3 way handshake
	/*
	MCU-A sends a sequency (SYN) to GUI-B
	GUI-B receives A's SYN
	GUI-B sends a SYN+ACK bit
	MCU-A receives B's SYN-ACK
	MCU-A sends ACKnowledge
	Host B receives ACK.
	
	THAT's normally how TCP acknowledgment system works
	*/
	
	//Comment: I think we probably don't need it. Since I2C will take care of acknowledgement
	
}