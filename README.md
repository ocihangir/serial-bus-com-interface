# simple-serial-communicator

simple-serial-communicator (sscomm) is created to access and test I2C devices in a simple way. It consists of two parts; a Java UI application and an Arduino sketch. Please see project Wiki for details.

sscomm project is a side project of project PaddleFish. PaddleFish is designed to facilitate the data acquisition part of IoT and Wearables. Project PaddleFish has an Arduino hardware which has BLE connection and a mobile application to collect sensor data. It also has preconfigured sensor information for variety of sensors. You can connect many sensors at once and start to collect periodic data via mobile phone. It will be open source very soon.

##How it works?

Simply upload Arduino sketch to your Arduino. Then run the Java UI application to access I2C devices which are connected to I2C port of Arduino.

The Java application communicates with Arduino via USB COM port. Alternatively a BLE breakout board can be connected. It has a basic command set to access I2C devices.

##Data format:

The I2C device must accept data in the following forms:
Read:
|Start|I2CAddress|RegisterAddress|ReStart|Data|
Write:
|Start|I2CAddress|RegisterAddress|Data|

Many of the available I2C sensor devices use this format. But some of them might need some aditional data. These devices won't be supported.
