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
		byte[] resBuffer = new byte[1024];
		int len = 0;
		int prev_len = 0;
		boolean loop = true;
		do 
		{
			byte[] buffer = usbComm.receiveData(2000);
			len = buffer.length;
			
			System.arraycopy(buffer, 0, resBuffer, prev_len, len);
			prev_len+=len;
			
			if (prev_len>0)
			{
				if (resBuffer[prev_len-1] == 0x0C)
				{
					loop = false;
				}
				
				if (resBuffer[prev_len-1] == 0x0E)
				{
					loop = false;
					throw new Exception("I2C Error! Check if I2C device connected properly. Slow down the I2C speed from Advanced tab.");
					// TODO : Create an exception class for I2C
				}
			}
		} while( loop );
		byte[] res = new byte[prev_len];
		System.arraycopy(resBuffer, 0, res, 0, prev_len);
		return res;
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
