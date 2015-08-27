package paddlefish.hal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;

import paddlefish.protocol.CommController;

import jssc.SerialPortException;

/*Singleton class Pattern is used*/
/*Observer Pattern is used*/
public class HAL implements CommRxInterface {
	private static HAL instance = null;
	USB usbComm;
	private List<CommRxInterface> receiverList = new ArrayList<CommRxInterface>();
	private byte rxBuffer[] = new byte[1024];
	private int rxBufferLength = 0;
	Mutex mutex = new Mutex();
	
	protected HAL() throws Exception
	{
		if (usbComm == null)
		{
			usbComm = new USB();
			usbComm.addReceiver(this);
		}
	}
	
	public static HAL getInstance() throws Exception {
	      if(instance == null) 
	      {
	         instance = new HAL();
	      }
	      return instance;
	}
	
	public void addReceiver(CommRxInterface commRx)
    {
    	receiverList.add(commRx);
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
				mutex.acquire();
				try {
					try {
					usbComm.sendData(data);
					} catch (SerialPortException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} finally {
					mutex.release();
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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

	@Override
	public void commReceiver(byte[] buffer) {
		int len = 0;
		len = buffer.length;
			
		if (len>0)
		{
			System.arraycopy(buffer, 0, rxBuffer, rxBufferLength, len);
			rxBufferLength+=len;
			if (rxBuffer[rxBufferLength-1] == 0x0C)
			{
				byte tempBuffer[] = new byte[rxBufferLength];
				System.arraycopy(rxBuffer, 0, tempBuffer, 0, rxBufferLength);
				// We need to categorize answers to let observers know 
				for (CommRxInterface commRx : receiverList)
		        	commRx.commReceiver(tempBuffer);
				
				rxBufferLength = 0;
			} else if (rxBuffer[rxBufferLength] == 0x0E)
			{
				try {
					throw new Exception("I2C Error! Check if I2C device connected properly. Slow down the I2C speed from Advanced tab.");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}/* else {
				try {
					throw new Exception("There is an answer from com device but doesn't end with a proper character!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
		}
	}
}
