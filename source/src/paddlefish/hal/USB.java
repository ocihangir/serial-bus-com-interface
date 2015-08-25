package paddlefish.hal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class USB
{
	InputStream in;
	OutputStream out;

	SerialPort commPort;
	SerialPortList serialPorts;	
	
	public USB() throws Exception
	{	
		
		
	}
	
	public boolean isConnected()
	{
		if ( commPort == null)
			return false;
		
		return commPort.isOpened();
	}
	
	/*
	 * Connect to the serial port and hold it
	 */
	void connect( String portName, int baudRate ) throws SerialPortException 
	{
		commPort = new SerialPort(portName);
		commPort.openPort();
		commPort.setParams(baudRate, 8, 1, 0);
	}
	
	public void disconnect() throws SerialPortException
	{
		commPort.closePort();
	}
	
	public void close() throws SerialPortException
	{
		if(this.commPort!=null)
			commPort.closePort();
		//TODO: Log
		else
			System.out.println("No CommPort available");
	}
	
	public void sendData(byte[] data) throws SerialPortException
	{
		byte buffer[] = new byte[data.length];
		for (int i=0;i<data.length;i++)
			buffer[i] = (byte) data[i];
		commPort.writeBytes(buffer);
	}
	
	public byte[] receiveData(int timeout) throws Exception
	{
		int len = commPort.getInputBufferBytesCount();
		byte[] buffer = commPort.readBytes(len,timeout);
			
		return buffer;
	}
	
	  public ArrayList<String> listPorts()
	    {
		  
		  ArrayList<String> availablePorts = new ArrayList<String>();
		  
		  String[] ports = SerialPortList.getPortNames();
		  
		  for (int i = 0;i<ports.length; i++)
			  availablePorts.add(ports[i]);
		  
	        return availablePorts;
	    }
}
