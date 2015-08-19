package paddlefish.hal;

// Code in this class was taken from : 
// http://eclipsesource.com/blogs/2012/10/17/serial-communication-in-java-with-raspberry-pi-and-rxtx/

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration; 

import paddlefish.hal.SerialPortList;

public class USB
{
	InputStream in;
	OutputStream out;

	CommPort commPort;
	SerialPortList serialPorts;
	
	private boolean connected = false;
	
	public USB() throws Exception
	{	
		
		
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	/*
	 * Connect to the serial port and hold it
	 */
	void connect( String portName, int baudRate ) throws Exception 
	{
		System.setProperty("gnu.io.rxtx.SerialPorts", portName); // A hack for rxtx cannot find ttyACMx ports on linux. See https://bugs.launchpad.net/ubuntu/+source/rxtx/+bug/367833
	    CommPortIdentifier portIdentifier = CommPortIdentifier
	        .getPortIdentifier( portName );
	    if( portIdentifier.isCurrentlyOwned() ) 
	    {
	      System.out.println( "Error: Port is currently in use" );
	    } 
	    else 
	    {
	      int timeout = 2000;
	      commPort = portIdentifier.open( this.getClass().getName(), timeout );
	 
	      if( commPort instanceof SerialPort ) 
	      {
	        SerialPort serialPort = ( SerialPort )commPort;
	        serialPort.setSerialPortParams( baudRate,
	                                        SerialPort.DATABITS_8,
	                                        SerialPort.STOPBITS_1,
	                                        SerialPort.PARITY_NONE );
	 
	        in = serialPort.getInputStream();
	        out = serialPort.getOutputStream();
	        connected = true;
	      }
	      else 
	      {
	        System.out.println( "Error: Only serial ports are handled by this example." );
	      }
	    }
	  }
	
	public void disconnect()
	{
		connected = false;
		commPort.close();
	}
	
	public void close()
	{
		if(this.commPort!=null)
			this.commPort.close();
		//TODO: Log
		else
			System.out.println("No CommPort available");
	}
	
	public void sendData(byte[] data) throws IOException
	{
		byte buffer[] = new byte[data.length];
		for (int i=0;i<data.length;i++)
			buffer[i] = (byte) data[i];
		this.out.write(buffer);
	}
	
	public byte[] receiveData() throws IOException
	{
		byte[] resBuffer = new byte[1024];
		byte[] buffer = new byte[1024];
		int len = 0;
		int prev_len = 0;
		boolean loop = true;
		do 
		{
			len = this.in.read( buffer );
			System.arraycopy(buffer, 0, resBuffer, prev_len, len);
			prev_len+=len;
			
			if (prev_len>0)
			{
				if (resBuffer[prev_len-1] == 0x0C)
				{
					loop = false;
				}
			}
		} while( loop );	
		byte[] res = new byte[prev_len];
		System.arraycopy(resBuffer, 0, res, 0, prev_len);
		return res;
	}
	
	public static class SerialReader implements Runnable {
		 
	    InputStream in;
	 
	    public SerialReader( InputStream in ) {
	      this.in = in;
	    }
	 
	    public void run() {
	      byte[] buffer = new byte[ 1024 ];
	      int len = -1;
	      try {
	        while( ( len = this.in.read( buffer ) ) > -1 ) {
	          System.out.print( new String( buffer, 0, len ) );
	        }
	      } catch( IOException e ) {
	        e.printStackTrace();
	      }
	    }
	  }
	 
	  public static class SerialWriter implements Runnable {
	 
	    OutputStream out;
	 
	    public SerialWriter( OutputStream out ) {
	      this.out = out;
	    }
	 
	    public void run() {
	      try {
	        int c = 0;
	        while( ( c = System.in.read() ) > -1 ) {
	          this.out.write( c );
	        }
	      } catch( IOException e ) {
	        e.printStackTrace();
	      }
	    }
	  }
	  
	  private static String getPortTypeName ( int portType )
	    {
	        switch ( portType )
	        {
	        case CommPortIdentifier.PORT_I2C:
	            return "I2C";
	        case CommPortIdentifier.PORT_PARALLEL:
	            return "Parallel";
	        case CommPortIdentifier.PORT_RAW:
	            return "Raw";
	        case CommPortIdentifier.PORT_RS485:
	            return "RS485";
	        case CommPortIdentifier.PORT_SERIAL:
	            return "Serial";
	        default:
	            return "unknown type";
	        }
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
