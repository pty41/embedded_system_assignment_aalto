#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
#include <avr/io.h>
#include <avr/interrupt.h>

//serial
#define BAUD 9600
#include <util/setbaud.h>
#include "uart.h"

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
#define RIGHT4 280
#define STRAIGHT 373

#define MOTOR_PWM PH3
#define INA PK0
#define INB PK1
#define CW 0b00000001
#define STOP 0b00000000
#define BRAKE 0b00000011
#define MINIMAL_SPEED 145//130 //90 // adjust these depending on the track
#define SLOW_SPEED 160 //130
#define MEDIUM_SPEED 180 //150
#define TOP_SPEED 185//290 //185
#define SOFT_BREAKING 1
#define HARD_BREAKING 2

#define START_BUTTON_PORT PORTE
#define START_BUTTON_PIN PINE
#define START_BUTTON_BIT PE5
#define BUTTON_PRESS_DELAY_TIME 50
#define SAFETY_COUNTER_LIMIT 570000

#define RUN_INTERVAL_RUN 65000
#define RUN_INTERVAL_TOTAL 800000

uint8_t running = 0;
uint8_t running_interval = 1;
uint8_t bumper_status = 0b11111111;
uint16_t speed = 0;
uint32_t safety_counter = 0;
char prev_servalue = '2';
uint32_t run_counter = 0;
uint32_t stop_counter = 0;

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

void steering(char servalue)
{
    if (servalue == '1')
    {
        // set tires to LEFT
        OCR1A=LEFT4;
        speed = SLOW_SPEED;
    }
    else if (servalue == '2')
    {
        // set tires to STRAIGHT
        OCR1A=STRAIGHT;
        speed = MEDIUM_SPEED;
    }
    else if (servalue == '3')
    {
        // set tires to RIGHT
        OCR1A=RIGHT4;
        speed = SLOW_SPEED;
    }
    else if (servalue == '9')
    {
        // do nothing
    }
    else
    {
        safety_counter++;
    }
}

int main()
{
    // do the init stuff
    init();
    uart_init();
    stdout = &uart_output;
    stdin  = &uart_input;

    while(1)
    {
        // check if there is some input in UART
        if ((UCSR0A & (1<<RXC0)))
        {
            char input;
            input = getchar();
            if (input == '0')
            {
                // running = 0;
                // PORTK = STOP; // STOP
                // OCR4A=0;      // set speed
                // safety_counter = 0;
            }
            steering(input);
        }
        if (run_counter > RUN_INTERVAL_TOTAL)
        {
            run_counter = 0;
        }
        if( run_counter < RUN_INTERVAL_RUN && running == 1)
        {
            OCR4A=MEDIUM_SPEED;     // set speed
        }
        else if (run_counter >= RUN_INTERVAL_RUN && running == 1)
        {
            // PORTK = STOP; // STOP
            OCR4A=0;      // set speed
        }
        run_counter++;
        // check for start/stop
        if (start_button_pressed())
        {
            if (running == 1)
            {
                running = 0;
                PORTK = STOP; // STOP
                OCR4A=0;      // set speed
            }
            else
            {
                running = 1;
                PORTK = CW;   // CW rotation
                OCR4A=SLOW_SPEED;     // set speed
            }
        }

    }
}
