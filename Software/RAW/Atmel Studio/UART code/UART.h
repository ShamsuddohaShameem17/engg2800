/*
 * UART.h
 *
 * Created: 10/10/2018 1:02:18 PM
 * Author : Shamsuddoha Shameem
 */
#include <stdlib.h>

#ifndef SERIAL_UART_H
#define SERIAL_UART_H


#ifndef  F_CPU
#define F_CPU 8000000UL //clock speed
#endif

#define BAUD 9600
#define MYUBRR F_CPU/16/BAUD-1

//Short-cut
//#define loop_until_bit_is_set(A, B) while(!(A&(1<<B)));

char sequence;
#define nullptr 0x00
#define NEWLINE '\n'
#define RETURN '\r'

typedef enum { false, true } boolean;

void UART_Init(unsigned int ubrr){
    /*Set baud rate */
    UBRR0H = (unsigned char)(ubrr>>8);
    UBRR0L = (unsigned char)ubrr;
    //Enable receiver and transmitter */
    UCSR0B = (1<<RXEN0)|(1<<TXEN0);
    /* Set frame format: 8data, 1stop bit */
    UCSR0C = (0<<USBS0)|(3<<UCSZ00);
}

void UART_Transmit(unsigned char data){
    /* Wait for empty transmit buffer */
    while ( !( UCSR0A & (1<<UDRE0)) );
    /* Put data into buffer, sends the data */
    UDR0 = data;
}

void UART_Set_Sequence(unsigned char s){
    sequence=s;
}

unsigned char UART_Receive(void){
    /* Wait for data to be received */
    while ( !(UCSR0A & (1<<RXC0)) );
    /* Get and return received data from buffer */
    return UDR0;
}

void UART_Send_String(char *string){
    while((char)*string != nullptr){//Check if string is not null
		//Transmit each char
        UART_Transmit(*string);
        string++;
	}
}

unsigned char* UART_Recieve_nbytes(int n){
	// Wait for data to be received
	boolean checkDataReady = (UCSR0A & (1<<RXC0));
	char* recievedStr;
	
	int index = 0;
	//////////////////////////
	//String concatenate
	//////////////////////////
	//Reserve memory 
	//Can be deleted if needed or if does not work
	if((recievedStr = (char*) malloc(n)) != NULL){
		// ensures string is empty
		recievedStr[index] = '\0';//index is zero
		index+=1;
		} 
		else {printf("malloc failed!\n");
		}
	///////////////////////////
	do{
		if(index<=(n+1)){
			recievedStr[index]=UDR0;
			index++;
		}else if(index>n){
			//End the string
			recievedStr[index] = '\0';
			break;
		}
	}while(checkDataReady);
	printf("String read %s \n",recievedStr);
	return recievedStr;
}


#pragma GCC diagnostic ignored "-Wunused-but-set-variable"
void UART_Flush(void){
    unsigned char dummy;
    while ( UCSR0A & (1<<RXC0) ) dummy = UDR0;
}

void UART_Send_Sequence(){
    UART_Transmit(sequence);
}


void UART_Send_NewLine(){
    UART_Transmit(RETURN);
    UART_Transmit(NEWLINE);
}

void UART_Send_Char_Newline(char c){
    UART_Transmit(c);
    UART_Send_NewLine();
}

void UART_Send_String_Newline(char *String){
    UART_Send_String(String);
    UART_Send_NewLine();
}

void UART_loop_back(){
	unsigned char ReceivedChar;
	for (;;) {//Loop forever
		ReceivedChar = UART_Receive();
		UART_Transmit(ReceivedChar);//echo back
	}
}

void UART_Test(void){
    UART_Init(MYUBRR);
    sequence ='H';
	//Fancy test
    /*char* buff ="Write a char here=> ";
    while (1){
        UART_Send_Sequence();
        //UART_WAIT(1);
        UART_Send_NewLine();
        _delay_ms(3000);
        UART_Send_String(buff);
        UART_Flush();
        char recieveChar = UART_Receive();
        _delay_ms(5000);
        UART_Send_Char_Newline(recieveChar);
        //UART_Send_String("You have send:");
        //UART_Transmit(recieveChar);
        UART_Send_NewLine();
    }*/
	//Simple Loop back test
	printf("MCU initiating Loop back::\n");
	UART_loop_back();
}

#endif //SERIAL_UART_H
