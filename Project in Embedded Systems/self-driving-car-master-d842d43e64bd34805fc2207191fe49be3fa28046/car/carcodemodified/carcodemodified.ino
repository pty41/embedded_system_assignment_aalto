/*
 * Example code for the car project of the course T-106.5300 Embedded Systems
 * (c) Jussi Hanhirova
 */

#include <stdint.h>
#include <stdbool.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#ifndef F_CPU
#define F_CPU 16000000UL
#endif
#include <util/delay.h>

//port definitions
#define SERVO_PORT  PORTB
#define SERVO_DDR   DDRB
#define STEERING_SERVO PB5

#define BUMPER_PORT PORTA
#define BUMPER_DDR  DDRA
#define BUMPER_PIN  PINA

#define LEFT4 460
#define LEFT3 420
#define LEFT2 390
#define LEFT1 375
#define RIGHT1 365
#define RIGHT2 350
#define RIGHT3 310
#define RIGHT4 280

#define MOTOR_PWM PH3
#define INA PK0
#define INB PK1
#define CW 0b00000001
#define STOP 0b00000000
#define BRAKE 0b00000011
#define MINIMAL_SPEED 145//130 //90 // adjust these depending on the track
#define SLOW_SPEED 100 //130
#define MEDIUM_SPEED 160 //150
#define TOP_SPEED 185//290 //185
#define SOFT_BREAKING 1
#define HARD_BREAKING 2

#define START_BUTTON_PORT PORTE
#define START_BUTTON_PIN PINE
#define START_BUTTON_BIT PE5
#define BUTTON_PRESS_DELAY_TIME 50
#define SAFETY_COUNTER_LIMIT 270000
uint8_t running = 0;
uint8_t bumper_status = 0b11111111;
uint16_t speed = 0;
uint32_t safety_counter = 0;
uint8_t servalue = 0;
uint8_t old_servalue = 0; 


int start_button_pressed() {
        if (bit_is_clear(START_BUTTON_PIN, START_BUTTON_BIT)) {
                _delay_ms(BUTTON_PRESS_DELAY_TIME);
                if (bit_is_clear(START_BUTTON_PIN, START_BUTTON_BIT)) return 1;
        }
        return 0;
}

void init() {
   START_BUTTON_PORT |= _BV(START_BUTTON_BIT);

   // set bumper pins to input
   BUMPER_DDR = 0b00000000;

   // configure TIMER1, steering servo
   TCCR1A|=(1<<COM1A1)|(1<<COM1B1)|(1<<WGM11);        // NON Inverted PWM
   TCCR1B|=(1<<WGM13)|(1<<WGM12)|(1<<CS11)|(1<<CS10); // PRESCALER=64, MODE 14(FAST PWM)
   ICR1=4999;                                         // PWM f=50Hz
   DDRB|=1<<STEERING_SERVO;                           // PWM out

   // configure TIMER4, motor pwm
   TCCR4A|=(1<<COM4A1)|(1<<COM4B1)|(1<<WGM41);        // NON Inverted PWM
   TCCR4B|=(1<<WGM43)|(1<<WGM42)|(1<<CS40);           // NO PRESCALING, MODE 14(FAST PWM)
   ICR4=799;                                          // PWM f=20kHz
   DDRH|=1<<MOTOR_PWM;                                // PWM out

   // set motor control pins to output
   DDRK|=(1<<INA)|(1<<INB);
}

/*void accelerate() {
   if (running) {
      //speed = speed+1;
      PORTK = CW;   // CW rotation
   }
}*/

// use bumber status to adjust steering and motor speed
void steering(int bumber_status) {
      if ((bumper_status ^ 0b01111111)==0) {
         OCR1A=LEFT4;
         safety_counter = 0;
         speed = SLOW_SPEED;
         servalue = 1;
      }
      else if ((bumper_status ^ 0b10111111)==0) {
         OCR1A=LEFT3;
         safety_counter = 0;
         speed = SLOW_SPEED;
         servalue = 2;
      }
      else if ((bumper_status ^ 0b11011111)==0) {
         OCR1A=LEFT2;
         safety_counter = 0;
         speed = SLOW_SPEED;
         servalue = 3;
      }
      else if ((bumper_status ^ 0b11101111)==0) {
         OCR1A=LEFT1;
         speed = SLOW_SPEED;
         safety_counter = 0;
         servalue = 4;
      }
      else if ((bumper_status ^ 0b11110111)==0) {
         OCR1A=RIGHT1;
         speed = SLOW_SPEED;
         safety_counter = 0;
         servalue = 5;
      }
      else if ((bumper_status ^ 0b11111011)==0) {
         OCR1A=RIGHT2;
         speed = SLOW_SPEED;
         safety_counter = 0;
         servalue = 6;
      }
      else if ((bumper_status ^ 0b11111101)==0) {
         OCR1A=RIGHT3;
         safety_counter = 0;
         speed = SLOW_SPEED;
         servalue = 7;
      }
      else if ((bumper_status ^ 0b11111110)==0) {
         OCR1A=RIGHT4;
         safety_counter = 0;
         speed = SLOW_SPEED;
         servalue = 8;
      }
      else {
         safety_counter++;
         servalue = 9;
      }

      if (speed < SLOW_SPEED) {
         speed = SLOW_SPEED;
      }
      //if (speed > TOP_SPEED) {
        // speed = TOP_SPEED;
      //}
      OCR4A = speed;
      if(old_servalue != servalue)
      {
        Serial.println(servalue);
        old_servalue = servalue;
      }
      
      
}

void setup()
{
  Serial.begin(9600);
  // do the init stuff
   init();
}

void loop()
{
      // read bumper status
      bumper_status = BUMPER_PIN;

      // adjust steering & speed
      steering(bumper_status);

      // check for start/stop
      if (start_button_pressed()) {
         if (running == 1) {
            running = 0;
            PORTK = STOP; // STOP
            OCR4A=0;      // set speed
         }
         else {
            running = 1;
            PORTK = CW;   // CW rotation
            OCR4A=90;     // set speed
         }
      }

      if (safety_counter > SAFETY_COUNTER_LIMIT) {
            running = 0;
            PORTK = STOP; // STOP
            OCR4A=0;      // set speed
            safety_counter = 0;
      }
}
