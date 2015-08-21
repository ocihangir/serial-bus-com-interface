package paddlefish.hal;

import java.io.IOException;
import java.util.ArrayList;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;


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
		try {
			usbComm.disconnect();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isConnected()
	{
		return usbComm.isConnected();
	}
	
	public void txData(byte data[]) throws IOException
	{
		try {
			usbComm.sendData(data);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] rxData() throws IOException, SerialPortException, SerialPortTimeoutException
	{		
		return usbComm.receiveData();
	}

	public void close()
	{
		if(this.usbComm!=null)
			try {
				this.usbComm.close();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			System.out.println("No UsbComm available");
	}
}
