
/*
 * Serial.c
 *
 * Created: 20/09/2018 11:33:58 PM
 * Author : Shameem & Jeremy
 */ 

#define F_CPU 8000000UL

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>

#include <avr/io.h>
#include <avr/interrupt.h>

#include <util/delay.h>
#include "UART.h"

#define PI 3.14159265358979323846
#define MAX_SIZE_INT 128
#define BIT_1_4 15
#define BIT_5_8 240

volatile uint8_t buffer[7];
volatile int buffer_len = 0;

void init(void)
{
	DDRB = 255;
	PORTB = 255;
	DDRC = 255;
	PORTC = 255;
	DDRD = 255;
	PORTD = 255;
}

ISR(USART_RX_vect){
	//this function runs when a char is received over UART
	if(buffer_len < 8){
		buffer[buffer_len] = UDR0;
		buffer_len++;
	}

}

int main(void){
	cli();
	UART_Init(MYUBRR);
	init(); //init ports
	
	uint8_t data_package[7];
	
	sei();		
	
	//Set variables//
	double offset = 0;		//Volts
	double amplitude = 3;	//Volts
	int wave1 = 1;			//set wave {1=sine, 2=square, 3 = triangle, 4 = sawtooth, 5 = reverse sawtooth}
	int freq1 = 1000;			//Hz
		
	double offset2 = 0;		//Volts
	double amplitude2 = 3;	//Volts
	int wave2 = 1;			//set wave {1=sine, 2=square, 3 = triangle, 4 = sawtooth, 5 = reverse sawtooth}
	int freq2 = 1000;		//Hz
	//			  //
	int LUT_SIZE = 255;
	int LUT_SIZE2 = 255;


	while(1){
		memset((void*)data_package,0,7*sizeof(uint8_t));
		if (buffer_len == 7) {

			//reset buffer len
			buffer_len = 0;
			//copy buffer to another piece of mem
			for(int k=0; k<7; k++){
				data_package[k] = buffer[k];	
			}
			memset((void*)buffer,0,7*sizeof(uint8_t));
			uint8_t channel = data_package[1] >> 6;
			uint8_t type = (data_package[1] & 0X3F) >> 3;
			uint16_t amplitude_r = (((uint16_t)data_package[1] & 7) << 8) | data_package[2];  
			uint16_t offset = ((uint16_t)data_package[3] << 3) | ((data_package[4] & 224) >> 5);
			double amplitude1 = amplitude_r;
			amplitude1 = amplitude1/100;
			double offset1 = offset;
			offset1 = (offset1 - 300)/100;
			uint16_t frequency = ((uint16_t)data_package[5] << 8) | (uint16_t)data_package[6];

			
			if(data_package[0] == 36){
				//amplitude--;
				//if(data_package[1] == 137)
					//wave1++;
				switch(channel){
					default:
					//case 2: //channel 1
						//wave1++;
						offset = offset1;
						amplitude = amplitude1;
						wave1 = type;
						freq1 = frequency;
						break;
					case 1: //channel 2
						//wave2++;
						offset2 = offset1;
						amplitude2 = amplitude1;
						wave2 = type;
						freq2 = frequency;
						break;
				}
			}
		}
		
		float adjusted_Offset = offset/3*127  ;
		float adjusted_Offset2 = offset2/3*127 ;
		
		int delay_1 = 0, repeat = 0, i1=0, skip = 1;
		int delay_2 = 0, repeat2 = 0, i2=0, skip2 = 1;
		float ratio = 1, ratio2 = 1;
		
		int natural_freq = 410;
		
// 		if(freq1 == 0)
// 			ratio =0;
// 		else
		 ratio = (float)natural_freq/(float)freq1;
		 
// 		if(freq2 == 0)
// 			ratio = 0;
// 		else
			ratio2 = (float)natural_freq/(float)freq2;
		
		if(freq1<=natural_freq){
			for(int i=1; i<1000; i++){
				if(255*ratio/i < 256){
					LUT_SIZE = (int)(255*ratio/((float)i));
					delay_1 = i-1;
					break;
				}
			}
		}else{
			LUT_SIZE = (int)(256*ratio);

		}
		
		if(freq2<=natural_freq){
			for(int i=1; i<1000; i++){
				if(255*ratio2/i < 256){
					LUT_SIZE2 = (int)(255*ratio2/(float)i);
					delay_2 = i-1;
					break;
				}
			}
		}else{
			LUT_SIZE2 = (int)(256*ratio2);
		}
		
		int16_t LUT1[LUT_SIZE];
		int16_t LUT2[LUT_SIZE2];
		int16_t lutTemp = 0;
	
		for (int i = 0; i <= LUT_SIZE; ++i){
			//Checks which wave to generate and generate temp value for LUT1 (Wave 1)
			switch(wave1){
				case 1: //Sine
					lutTemp = (int16_t)roundf((int)(MAX_SIZE_INT * sinf(2.0 * (float)PI * (float)i / LUT_SIZE)*(amplitude/3)+127+adjusted_Offset));
					break;
				
				case 2: //Square
					if(i<LUT_SIZE/2){
						lutTemp = (int16_t)(128*(amplitude/3)+127+adjusted_Offset);
					}else{
						lutTemp = (int16_t)(127-128*(amplitude/3)+adjusted_Offset);
					}
				break;
				
				case 3: //Triangle
					if(i<(LUT_SIZE/4)){
						lutTemp = (int16_t)roundf((MAX_SIZE_INT*((float)i/((float)LUT_SIZE/4)))*(amplitude/3)+127+adjusted_Offset);
					}else if(i<((3*LUT_SIZE)/4)){
						lutTemp = (int16_t)roundf(MAX_SIZE_INT*(((float)LUT_SIZE/2-(float)i)/((float)LUT_SIZE/4))*(amplitude/3)+127+adjusted_Offset);
					}else{
						lutTemp = (int16_t)roundf((MAX_SIZE_INT*((i-(float)LUT_SIZE)/((float)LUT_SIZE/4)))*(amplitude/3)+127+adjusted_Offset);
					}
					break;
				
				case 4:	//Sawtooth
					if(i<LUT_SIZE/2)
						lutTemp = (int16_t)(MAX_SIZE_INT*((float)i/((float)LUT_SIZE/2))*(amplitude/3)+127+adjusted_Offset);
					else
						lutTemp = (int16_t)(MAX_SIZE_INT*((float)i/((float)LUT_SIZE/2)-2)*(amplitude/3)+127+adjusted_Offset);
					break;
				
				case 5: //Reverse Sawtooth
					lutTemp = (int16_t)(MAX_SIZE_INT*(1-(float)i/((float)LUT_SIZE/2))*(amplitude/3)+127+adjusted_Offset);
					break;
			}
			//limit values to 255 and 0
			if(lutTemp > 255){
				lutTemp = 255;
			}else if(lutTemp < 0){
				lutTemp = 0;
			}
			if(freq1 == 0)
				lutTemp = 127;
				
			LUT1[i] = lutTemp;
		}
		
		for (int i = 0; i <= LUT_SIZE2; ++i){
			//Checks which wave to generate and generate temp value for LUT2 (Wave 2)
			switch(wave2){
				case 1: //Sine
					lutTemp = (int16_t)roundf((int)(MAX_SIZE_INT * sinf(2.0 * (float)PI * (float)i / LUT_SIZE2)*(amplitude2/3)+adjusted_Offset2+127));
					break;
					
				case 2: //Square
					if(i<LUT_SIZE2/2){
						lutTemp = (int16_t)(128*(amplitude2/3)+127+adjusted_Offset2);
					}else{
						lutTemp = (int16_t)(127-127*(amplitude2/3)+adjusted_Offset2);
					}
					break;
				
				case 3: //Triangle
					if(i<(LUT_SIZE2/4)){
						lutTemp = (int16_t)roundf((MAX_SIZE_INT*((float)i/((float)LUT_SIZE2/4)))*(amplitude2/3)+127+adjusted_Offset2);
					}else if(i<((3*LUT_SIZE2)/4)){
						lutTemp = (int16_t)roundf(MAX_SIZE_INT*(((float)LUT_SIZE2/2-(float)i)/((float)LUT_SIZE2/4))*(amplitude2/3)+127+adjusted_Offset2);
					}else{
						lutTemp = (int16_t)roundf((MAX_SIZE_INT*((i-(float)LUT_SIZE2)/((float)LUT_SIZE2/4)))*(amplitude2/3)+127+adjusted_Offset2);
					}
					break;
				
				case 4:	//Sawtooth
				if(i<LUT_SIZE2/2)
					lutTemp = (int16_t)(MAX_SIZE_INT*((float)i/((float)LUT_SIZE2/2))*(amplitude2/3)+127+adjusted_Offset2);
				else
					lutTemp = (int16_t)(MAX_SIZE_INT*((float)i/((float)LUT_SIZE2/2)-2)*(amplitude2/3)+127+adjusted_Offset2);
					break;
				
				case 5: //Reverse Sawtooth
					lutTemp = (int16_t)(MAX_SIZE_INT*(1-(float)i/((float)LUT_SIZE2/2))*(amplitude2/3)+127+adjusted_Offset2);
					break;
			}

			//limit values to 255 and 0
			if(lutTemp > 255){
				lutTemp = 255;
			}else if(lutTemp < 0){
				lutTemp = 0;
			}
			
			if(freq2 == 0)
				lutTemp = 127;
			LUT2[i] = lutTemp;
			//Loop for 255 values
		}
		
		
		

		while(1){

			if(buffer_len > 0){
				if(buffer[0] != 36){
					memset((void*)buffer,0,7*sizeof(uint8_t));
					buffer_len = 0;
				}				
				if(buffer_len == 7){
					break;
				}
			}
							
			if(i1>LUT_SIZE)
				i1 = i1 - LUT_SIZE;
			
			if(i2>LUT_SIZE2)
			i2 = i2 - LUT_SIZE2;
			
			if(repeat == 0){
				PORTB = LUT1[i1];
				repeat = delay_1;
				i1 = i1 + skip;
			}else{
				repeat--;
			}

			if(repeat2 == 0){
				PORTC = LUT2[i2] &  BIT_1_4;
				PORTD = LUT2[i2] & BIT_5_8;
				repeat2 = delay_2;
				i2 = i2 + skip2;
				}else{
				repeat2--;
			}
			_delay_us(3);

		}
		
	}
	return 0;
}

/*		
			----PACKAGE FORMAT----
			'$'
			channel:		2bits
				e.i. channel 1: 1,0, channel 2: 01, both: 11
			type:			3bits	(1-5)
			amplitude:		11bits (0-600)(0-6)
			offset:			11bits (0-600)(-3-3)
							5bits empty
			frequency:		16bits (0-10 000)

		
			e.g. wave (1, sine, 3v, 1v, 1000hz): $10 001 00100101100 00110010 00000000 00000011 11101000 
												  0  2   5           16          32             43
			
				wave (1, sine, 3v, 1v, 1000hz) wave (2,sine, 3v, 1v, 1000hz): $11 001 00100101100 00110010 000 00000 01111101000 001 00100101100 00110010000 0000001111101000&
																			   0  2   5           16          27               43  46          57          68              84
																			   
			cp-p $ 10101001 00101111 00110010 10000000 00001111 11101000&  001100100101100001100100000000001111101000&
								/		2			
*/