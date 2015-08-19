/* Paddlefish */

#include "TimerOne.h"

//#define LEONARDO 1

#define BAUDRATE 115200

// I2C related constants
#define START_CONDITION 0x00
#define SEND_CONDITION 0x01
#define SEND_CONDITION_NACK 0x02
#define STOP_CONDITION 0x03
#define REPEATED_START_CONDITION 0x04

// ** Communication related constants **
// i2c
#define CMD_READ_BYTES 0xC0
#define CMD_WRITE_BYTES 0xC1
#define CMD_WRITE_BITS 0xC2
// stream
#define CMD_STREAM_ON 0xB0
#define CMD_STREAM_ADD 0xB1
#define CMD_STREAM_RST 0xB2
#define CMD_STREAM_PERIOD 0xBF
#define CMD_STREAM_START 0xBE
#define CMD_STREAM_END 0xBD
// basic
#define CMD_START 0xA5
#define CMD_ANSWER 0xA6
#define CMD_NULL 0x00
#define CMD_END 0x0C
#define CMD_ESC 0x0E
#define CMD_OK 0x0D


#define MAX_DEV 20 // max number of devices to stream/dump
#define STREAM_LENGTH 5 // i2cadd[1]+regadd[1]+len[1]+period[2]


int ledb = 13;
int blinkCounter = 0;

boolean startReceive = false;
byte receivedCmd = 0x00;

byte streamCmdArray[5+MAX_DEV*STREAM_LENGTH];
unsigned long streamPeriod = 1000; // ms. This is going to be obsolete by individual periods for commands.

boolean streamON = false;

long time;

void setup()
{
  // initialize the serial communication:
  Serial.begin(BAUDRATE);
#ifdef LEONARDO
  Serial1.begin(BAUDRATE);
   while (!Serial1) {
    ; // wait for Serial1 to be ready
  }
#endif

  // heartbeat led
  pinMode(ledb, OUTPUT);
  digitalWrite(ledb,LOW);
  
  Timer1.initialize(1000000);
  Timer1.attachInterrupt(heartBeat);
  Timer1.stop();
  
  TWBR = 0x1F; // TODO : i2c speed slowed down to below 400KHz for eeprom
  
  streamCmdArray[0] = 0; // number of stream devices is zero
  
  time = millis();
  
}


void loop()
{
  // your code here...
  // caution!
  // don't use interrupts
  // don't use timer1
  pfControl(); // TODO : call pfControl from Timer3 interrupt. Set main loop free.
  delay(20);
}

/*
*  Receives control commands
*  Communicates with mobile device. The communication is
*  always started by mobile device. Arduino processes the command
*  and answers accordingly.
*  Examples :
* CMD_READ_BYTES:
* |START|Cmd|DevAddr|RegAdd|Length|CRC|End|
  
  Read ID
  A5 C0 53 00 01 00 0C

  Read 3 axis 16bit data
  A5 C0 53 32 06 00 0C

* CMD_WRITE_BYTES: 
* |START|Cmd|DevAddr|RegAdd|Length|End|Data[]|End|
  
  Set measure mode (write bytes command)
  A5 C1 53 2D 01 0C 08 0C
  
* CMD_WRITE_BITS: 
* |START|Cmd|DevAddr|RegAdd|Data|Mask|CRC|End|

  Set measure mode (write bit command)
  A5 C2 53 2D 08 FF 00 0C
  
  CMD_STREAM_ADD: 
  |START|Cmd|DevAddr|RegAdd|Length|Period[2]|CRC|End|
  A5 B1 53 00 01 00 00 00 0C

  CMD_STREAM_PERIOD: 
  |START|Cmd|Period[2]|CRC|End|
  A5 BF 04 E8 00 0C

  CMD_STREAM_ON: 
  |START|Cmd|On|CRC|End|
  A5 B0 01 00 0C

  CMD_STREAM_RST: 
  |START|Cmd|CRC|End|
  A5 B2 00 0C
  
*/
void pfControl()
{
  while (Serial.available() > 0)
  {
    // Communication start after receiving CMD_START
    if (!startReceive)
    {
      if (Serial.read() == CMD_START)
        startReceive = true;
    } else {
      if (receivedCmd == CMD_NULL)
        // Read command after the communication starts
        receivedCmd = Serial.read();
      else {
        switch (receivedCmd)
        {
          case CMD_STREAM_ON: /* |START|Cmd|On|CRC|End| */ 
            if (Serial.available() > 1)
            {
              char buffer[3];
              if (receiveBytes(3,buffer))
              {
                // Start stream if On is CMD_OK
                if (buffer[0]==0x00)
                  setStream(false);
                else
                  setStream(true);
                
                commOK();
                startReceive = false;
                receivedCmd = CMD_NULL;
              } else 
                commError();
            }
            break;
          case CMD_STREAM_RST: /* |START|Cmd|CRC|End| */ 
            if (Serial.available() > 1)
            {
              char buffer[2];
              if (receiveBytes(2,buffer))
              {
                // Reset stream buffer
                streamReset();
                
                commOK();
                startReceive = false;
                receivedCmd = CMD_NULL;
              } else 
                commError();
            }
            break;
          case CMD_STREAM_PERIOD: /* |START|Cmd|Period[2]|CRC|End| */ 
            if (Serial.available() > 3)
            {
              char buffer[4];
              if (receiveBytes(4,buffer))
              {
                // Set timer period
                unsigned int period = ((buffer[0]<<8) + buffer[1]) & 0xFFFF;
                setPeriod(period);
                
                commOK();
                startReceive = false;
                receivedCmd = CMD_NULL;
              } else 
                commError();
            }
            break;
          case CMD_STREAM_ADD: /* |START|Cmd|DevAddr|RegAdd|Length|Period[2]|CRC|End| */ 
            if (Serial.available() > 6)
            {
              char buffer[7];
              if (receiveBytes(7,buffer))
              {
                // Add command to stream buffer
                streamAddCmd(buffer);
                
                commOK();
                startReceive = false;
                receivedCmd = CMD_NULL;
              } else 
                commError();
            }
            break;
          case CMD_READ_BYTES: /* |START|Cmd|DevAddr|RegAdd|Length|CRC|End| */            
            if (Serial.available() > 4)
            {
              char buffer[5];
              if (receiveBytes(5,buffer))
              {
                // Read data from i2c device
                char* recBuf = pfReadBytes(buffer[0],buffer[1],buffer[2]);
                
                Serial.write(CMD_ANSWER);
                // Send Data via UART
                for (int dataCount = 0;dataCount<buffer[2];dataCount++)
                {
                  Serial.write(recBuf[dataCount]);
                }
                
                Serial.write(CMD_END);
                startReceive = false;
                receivedCmd = CMD_NULL;
              } else 
                commError();
            }
            break;
          case CMD_WRITE_BYTES: /* |START|Cmd|DevAddr|RegAdd|Length|End|Data[]|End| */
            if (Serial.available() > 3)
            {
                char buffer[4];
                if ( receiveBytes(4,buffer) )
                {
                  char dataBuffer[buffer[2]];
                  Serial.readBytes(dataBuffer, buffer[2]);
                  if ( Serial.read() == CMD_END )
                    pfWriteBytes(buffer[0], buffer[1], buffer[2], dataBuffer);
                  else 
                    commError();
                    
                  commOK();
                  receivedCmd = CMD_NULL;
                  startReceive = false;
                } else 
                commError();
            }
            break;
          case CMD_WRITE_BITS: /* |START|Cmd|DevAddr|RegAdd|Data|Mask|CRC|End| */
            if (Serial.available() > 5)
            {
              char buffer[6];
              if ( receiveBytes(6,buffer) )
              {
                char sendData[1];
                char* recBuf = pfReadBytes(buffer[0],buffer[1],1);
                sendData[0] = (buffer[3] & buffer[2]) | (~buffer[3] & recBuf[0]);
                commOK();
                pfWriteBytes(buffer[0], buffer[1], 1, sendData);
                receivedCmd = CMD_NULL;
                startReceive = false;
              } else 
                commError();
            }
            break;
          default:
            commError();
            break;
        }
      }
    }
  }
}

void streamAddCmd(char *buffer)
{
  int start = streamCmdArray[0]*STREAM_LENGTH+5;
  for (int i=0;i<5;i++)
    streamCmdArray[start+i]=buffer[i];
  streamCmdArray[0]++;
}

void streamReset()
{
  streamCmdArray[0]=0;
}

void setPeriod(unsigned int period)
{
  streamPeriod = period;
}

void setStream(boolean ON)
{
  if (ON)
  {
    unsigned long streamPeriodInMicroSeconds = streamPeriod*1000;
    Timer1.initialize(streamPeriodInMicroSeconds);
    Timer1.setPeriod(streamPeriodInMicroSeconds);
    Timer1.attachInterrupt(heartBeat);
  } else {
    Timer1.stop();
  }
}

/*
* HeartBeat is called by timer1 interrupt.
*/
void heartBeat()
{
  
  /*char* recBuf = pfReadBytes(0x53,0x32,1);
  Serial.print("read buffer: ");
  Serial.println(recBuf[0],DEC);*/
  Serial.write(CMD_STREAM_START);
  
  // Send 4 bytes timestamp
  time = millis(); // long
  Serial.write(time & 0xFF);
  Serial.write((time>>8) & 0xFF);
  Serial.write((time>>16) & 0xFF);
  Serial.write((time>>24) & 0xFF);  
  // Send device data
  for (int dev=0;dev<streamCmdArray[0];dev++)
  {
    int start = 5 + (dev * STREAM_LENGTH);
    char* recBuf = pfReadBytes(streamCmdArray[start],streamCmdArray[start+1],streamCmdArray[start+2]);
    Serial.write(recBuf);
  }
  
  char CRC = 0x00;
  Serial.write(CRC);
  Serial.write(CMD_STREAM_END);
  blinkLed();
}

/*
* read bytes from i2c device
*/
char* pfReadBytes(char devAddress, char regAddress, char length)
{
  char sendData[1];
  sendData[0]=regAddress;
  i2c_start();
  i2c_write(devAddress,1,(char*)sendData);
  i2c_repeated_start();
  char* receiveBuffer = i2c_read(devAddress,length);
  i2c_stop();
  
  return receiveBuffer;
}

void pfWriteBytes(char devAddress, char regAddress, char length, char* data)
{
  char sendData[length+1];
  sendData[0]=regAddress;
  for (int charCount = 0;charCount<length;charCount++)
  {
    sendData[charCount+1]=data[charCount];
  }
  i2c_start();
  i2c_write(devAddress,length+1,(char*)sendData);
  i2c_stop();
}

char* i2c_read(char devAddress,char length)
{
  static char receiveBuffer[16];
  // slave address to be written
  char SLA_R = (devAddress << 1) | 1;
    
  // send address
  TWDR = SLA_R; 
  if (i2c_tx(SEND_CONDITION) != 0x40)
    i2cError();
  
  for(int dataCount=0;dataCount<length;dataCount++)
  {
    if ((dataCount!=length-1))
    {
      if (i2c_tx(SEND_CONDITION) != 0x50)
        i2cError();
    } else {
      if (i2c_tx(SEND_CONDITION_NACK) != 0x58)
        i2cError();
    }
    receiveBuffer[dataCount] = TWDR;
  }
  
  return receiveBuffer;
}

void i2c_write(char devAddress, char length, char* data)
{
  // slave address to be read
  char SLA_W = (devAddress << 1);
    
  // send address
  TWDR = SLA_W; 
  if (i2c_tx(SEND_CONDITION) != 0x18)
    i2cError();
    
  for (int dataCount = 0; dataCount<length; dataCount++)
  {
    TWDR = data[dataCount];
    if (i2c_tx(SEND_CONDITION) != 0x28)
      i2cError();
  }
}

void i2c_start()
{
  // start I2C
  if (i2c_tx(START_CONDITION) != 0x08)
    i2cError();
}

void i2c_stop()
{
  i2c_tx(STOP_CONDITION);
}

void i2c_repeated_start()
{
  if (i2c_tx(REPEATED_START_CONDITION) != 0x10)
    i2cError();
}


/*
* Transmit I2C command
* Communication start with START_CONDITION
* SEND_CONDITION transmits command following with an ACK
* SEND_CONDITION_NACK transmits command following with a NACK
* STOP_CONDITION stops the communication and releases I2C line
* REPEATED_START_CONDITION starts another communication without
* stoping the current one. It is required to read I2C device. Master
* writes to the device address and register address first. Then,
* after REPEATED_START_CONDITION, it listens the device.
*/
char i2c_tx(char mode)
{
  //TWCR: TWINT|TWEA|TWSTA|TWSTO|TWWC|TWEN|-|TWIE
  delay(0); // TODO: if this line is removed, the code won't work!!
  switch(mode)
  {
    case START_CONDITION:
      TWCR = (1<<TWINT) | (1<<TWSTA) | (1<<TWEN);
      break;
    case SEND_CONDITION:
      TWCR = (1<<TWINT) | (1<<TWEN) | (1<<TWEA);
      break;
    case SEND_CONDITION_NACK:
      TWCR = (1<<TWINT) | (1<<TWEN);
      break;
    case STOP_CONDITION:
      TWCR = (1<<TWINT) | (0<<TWSTA) | (1<<TWSTO) | (1<<TWEN);
      break;
    case REPEATED_START_CONDITION:
      TWCR = (1<<TWINT) | (1<<TWSTA) | (0<<TWSTO) | (1<<TWEN);
      break;
  }   
  
  if ((mode != STOP_CONDITION))
    while (!(TWCR & (1<<TWINT)));// wait for command complete
    
  return (TWSR & 0xF8);
}

/*
* Blink the LED 1 sec on - 1 sec off
* to indicate correct clock frequency.
* If it doesn't blink in this period,
* timer1 prescaler must be set accordingly.
*/
void blinkLed()
{
  if (blinkCounter>100) // the timer interrupt set to 10ms
  {
    digitalWrite( ledb, digitalRead( ledb ) ^ 1 );
    blinkCounter=0;
  }
  
  blinkCounter++;
}

void i2cError()
{
  digitalWrite( ledb, HIGH );
}

void commError()
{
  // Error in UART communication
  receivedCmd = CMD_NULL;
  startReceive = false;
}

void commOK()
{
  Serial.write(CMD_ANSWER);                
  Serial.write(CMD_OK);
  Serial.write(CMD_END);
}

void disableInterrupt()
{
  /*Timer1.detachInterrupt();
  Timer1.stop();*/
}

void enableInterrupt(long period)
{
  /*Timer1.initialize(period);
  
  // attach timer interrupt
  Timer1.attachInterrupt(heartBeat);*/

}

boolean receiveBytes(int length,char* buffer)
{
  Serial.readBytes(buffer,length);
  if ( buffer[length-1] == CMD_END )
    return true;
  
  return false;
}
