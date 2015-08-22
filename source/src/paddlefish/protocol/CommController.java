package paddlefish.protocol;

import java.io.IOException;
import java.util.ArrayList;

import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

import paddlefish.hal.HAL;
import paddlefish.protocol.CommConstants;


/*Singleton class Pattern is used*/
public class CommController 
{
	private static CommController instance = null;
	private static HAL hal;
	
	protected CommController() throws Exception 
	{
		// Exists only to defeat instantiation.
		//http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
		 if(hal==null)
			hal = new HAL();
		 // TODO : implement CRC
		 // TODO : timeout control needed
		 // TODO : create a thread for serial read and write
	}
	   

    public static CommController getInstance() throws Exception {
	      if(instance == null) 
	      {
	         instance = new CommController();
	      }
	      return instance;
	}
    
    public boolean isConnected()
    {
    	return hal.isConnected();
    }
    
    public ArrayList<String> listPorts()
    {
    	ArrayList<String> ports = hal.listAvailablePorts(); 
    	return ports;
    }
    
    public boolean connect(String port, int baud)
    {
    	return hal.connect(port, baud);    	
    }
	   
    public void disconnect()
    {
    	hal.disconnect();
    }
    
	public byte[] readByteArray(byte deviceAddress, byte registerAddress, int length) throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[7];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_READ_BYTES;
		cmd[2] = deviceAddress;
		cmd[3] = registerAddress;
		cmd[4] = (byte) length;
		cmd[5] = 0x00;
		cmd[6] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return receivedData;
	}
	
	public boolean writeSingleByte(byte deviceAddress, byte registerAddress, byte data) throws IOException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[8];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_WRITE_BYTES;
		cmd[2] = deviceAddress;
		cmd[3] = registerAddress;
		cmd[4] = 0x01;
		cmd[5] = CommConstants.CMD_END;
		cmd[6] = data;
		cmd[7] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public boolean writeByteArray(byte deviceAddress, byte registerAddress, int length, byte data[]) throws IOException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[8+length];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_WRITE_BYTES;
		cmd[2] = deviceAddress;
		cmd[3] = registerAddress;
		cmd[4] = (byte)length;
		cmd[5] = CommConstants.CMD_END;		
		cmd[6+length] = CommConstants.CMD_END;
		
		System.arraycopy(data, 0, cmd, 6, length);
		
		hal.txData(cmd);
		
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public boolean writeBits(byte deviceAddress, byte registerAddress, byte data, byte mask) throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[8];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_WRITE_BITS;
		cmd[2] = deviceAddress;
		cmd[3] = registerAddress;
		cmd[4] = data;
		cmd[5] = mask;
		cmd[6] = 0x00;
		cmd[7] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	
	public boolean addDevice(byte deviceAddress, byte registerAddress, byte length, int period) throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[9];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_STREAM_ADD;
		cmd[2] = deviceAddress;
		cmd[3] = registerAddress;
		cmd[4] = (byte) length;
		cmd[5] = (byte)( period & 0xFF );
		cmd[6] = (byte)( ( period >> 8 ) & 0xFF );
		cmd[7] = 0x00;
		cmd[8] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public boolean setPeriod(int period) throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[6];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_STREAM_ADD;
		cmd[2] = (byte)( period & 0xFF );
		cmd[3] = (byte)( ( period >> 8 ) & 0xFF );
		cmd[4] = 0x00;
		cmd[5] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public boolean start() throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[5];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_STREAM_ADD;
		cmd[2] = 0x01;
		cmd[3] = 0x00;
		cmd[4] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public boolean stop() throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[5];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_STREAM_ADD;
		cmd[2] = 0x00;
		cmd[3] = 0x00;
		cmd[4] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public boolean reset() throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[4];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_STREAM_ADD;
		cmd[2] = 0x00;
		cmd[3] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return checkOK(receivedData);
	}
	
	public byte[] setI2CSpeed(long speed) throws IOException, InterruptedException, SerialPortException, SerialPortTimeoutException
	{
		byte cmd[] = new byte[8];
		
		cmd[0] = CommConstants.CMD_START;
		cmd[1] = CommConstants.CMD_SET_I2C_SPEED;
		cmd[2] = (byte)((speed>>24) & 0xFF);
		cmd[3] = (byte)((speed>>16) & 0xFF);
		cmd[4] = (byte)((speed>>8) & 0xFF);
		cmd[5] = (byte)((speed>>0) & 0xFF);
		cmd[6] = 0x00;
		cmd[7] = CommConstants.CMD_END;
		
		hal.txData(cmd);
		
		Thread.sleep(50);
		byte[] receivedData = hal.rxData();
		
		return receivedData;
	}
	

	public void close()
	{
		if(hal!=null)
		{
			hal.close();
		}
		//TODO: Log
		else
			System.out.println("No HAL available");
	}
	
	private static boolean checkOK(byte ans[])
	{
		if ( (ans[0] != CommConstants.CMD_START) || (ans[1] != CommConstants.CMD_OK) || (ans[2] != CommConstants.CMD_END))
			return false;
		return true;
	}
}
