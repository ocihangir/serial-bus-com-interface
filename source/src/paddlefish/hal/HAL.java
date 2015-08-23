package paddlefish.hal;

import java.io.IOException;
import java.util.ArrayList;

import jssc.SerialPortException;


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
	
	public void connect(String port, int baud) throws SerialPortException
	{
		usbComm.connect(port,baud);
	}
	
	public void disconnect() throws SerialPortException
	{
		usbComm.disconnect();
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
	
	public byte[] rxData() throws Exception
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
