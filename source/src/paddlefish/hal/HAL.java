package paddlefish.hal;

import java.io.IOException;
import java.util.ArrayList;


public class HAL {
	USB usbComm;
	
	public HAL() throws Exception
	{
		usbComm = new USB();
	}
	
	public ArrayList<String> listAvailablePorts()
	{
		return usbComm.listPorts();
	}
	
	public boolean connect(String port, int baud)
	{
		
		try {
			// Linux - Ubuntu
			usbComm.connect(port,baud); //"/dev/ttyACM0",115200
			// Windows
			//usbComm.connect(port,baud);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void disconnect()
	{
		usbComm.disconnect();
	}
	
	public boolean isConnected()
	{
		return usbComm.isConnected();
	}
	
	public void txData(byte data[]) throws IOException
	{
		usbComm.sendData(data);
	}
	
	public byte[] rxData() throws IOException
	{		
		return usbComm.receiveData();
	}

	public void close()
	{
		if(this.usbComm!=null)
			this.usbComm.close();
		//TODO: Log
		else
			System.out.println("No UsbComm available");
	}
}
