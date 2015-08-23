package paddlefish.test;

import java.io.IOException;

import paddlefish.protocol.CommController;

import paddlefish.protocol.CommConstants;

public class CommunicationTester {
	static CommController commCont;
	public static byte[] testReadBytes(byte deviceAddress, byte registerAddress, int length) throws IOException, InterruptedException
	{
		byte[] data = null;
		try {
			data = commCont.readByteArray(deviceAddress, registerAddress, length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] buffer = new byte[data.length];
		for (int i=0;i<data.length;i++)
			buffer[i] = (byte) data[i];
		System.out.print("Data received : ");
		for (int i = 0;i<length+2;i++){
			System.out.print(buffer[i] & 0xFF); 
			System.out.print(" ");
		}
		System.out.println(" ");
		
		return buffer;
	}
	
	public static void testWriteSingleByte(byte deviceAddress, byte registerAddress, byte data)
	{
		try {
			commCont.writeSingleByte(deviceAddress, registerAddress, data);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Data written");		
	}
	
	public static void testWriteMultiBytes(byte deviceAddress, byte registerAddress, byte[] data)
	{
		try {
			commCont.writeByteArray(deviceAddress, registerAddress, data.length, data);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Data written");		
	}
	
	public static boolean testReadWriteMultiBytes() throws IOException, InterruptedException
	{
		System.out.println("-Testing reading and writing multiple bytes-");
		
		System.out.println("Reading ADXL345 multiple addresses 0x1E 0x1F 0x20...");
		testReadBytes((byte)0x53, (byte)0x1E, 3);
		
		byte[] data = {0x55, 0x55, 0x55};
		System.out.println("Writing 0x55 to ADXL345 0x1E 0x1F 0x20 address...");
		testWriteMultiBytes((byte)0x53, (byte)0x1E, data);
		
		System.out.println("Reading ADXL345 multiple addresses 0x1E 0x1F 0x20...");
		byte[] res = testReadBytes((byte)0x53, (byte)0x1E, 3);
		
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)res[1] != (byte)0x55)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[2] != (byte)0x55)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[3] != (byte)0x55)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)(res[4] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		byte[] data2 = {0x00, 0x00, 0x00};
		System.out.println("Writing 0x00 to ADXL345 0x1E 0x1F 0x20 address...");
		testWriteMultiBytes((byte)0x53, (byte)0x1E, data2);
		
		System.out.println("Reading ADXL345 multiple addresses 0x1E 0x1F 0x20...");
		res = testReadBytes((byte)0x53, (byte)0x1E, 3);
		
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)res[1] != (byte)0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[2] != (byte)0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[3] != (byte)0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)(res[4] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		System.out.println("\n");
		
		return true;
	}
	
	public static boolean testReadWriteSingleByte() throws IOException, InterruptedException
	{
		System.out.println("-Testing reading and writing single byte-");
		
		System.out.println("Reading ADXL345 0x2D address...");
		testReadBytes((byte)0x53, (byte)0x2D, 1);
		
		System.out.println("Writing 0x08 to ADXL345 0x2D address...");
		testWriteSingleByte((byte)0x53,(byte)0x2D,(byte)0x08);
		
		System.out.println("Reading back ADXL345 0x2D address...");
		byte[] res = testReadBytes((byte)0x53, (byte)0x2D, 1);
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)res[1] != (byte)0x08)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)(res[2] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		System.out.println("Writing 0x00 to ADXL345 0x2D address...");
		testWriteSingleByte((byte)0x53,(byte)0x2D,(byte)0x00);
		
		System.out.println("Reading back ADXL345 0x2D address...");
		res = testReadBytes((byte)0x53, (byte)0x2D, 1);
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)res[1] != (byte)0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)(res[2] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		System.out.println("\n");
		
		return true;
	}	
	
	public static boolean testADXL345ID() throws IOException, InterruptedException
	{
		System.out.println("-Testing reading ADXL345 ID-");
		
		System.out.println("Reading ADXL345 ID...");
		byte[] res = testReadBytes((byte)0x53, (byte)0x00, 1);
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)(res[1] & 0xFF) != (byte)0xE5)
		{
			System.out.print("ID is wrong!");
			return false;
		}
		if ((byte)(res[2] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		System.out.println("\n");
		
		return true;
	}
	
	
	public static boolean testEEPROMReadWriteMultiBytes() throws IOException, InterruptedException
	{
		System.out.println("-Testing EEPROM reading and writing multiple bytes-");
		
		System.out.println("Reading multiple addresses 0x1E 0x1F 0x20...");
		testReadBytes((byte)0x50, (byte)0x00, 3);
		
		Thread.sleep(300);
		
		byte[] data = {0x55, 0x55, 0x55};
		System.out.println("Writing 0x55 to 0x1E 0x1F 0x20 address...");
		testWriteMultiBytes((byte)0x50, (byte)0x00, data);
		
		Thread.sleep(300);
		
		System.out.println("Reading multiple addresses 0x1E 0x1F 0x20...");
		byte[] res = testReadBytes((byte)0x50, (byte)0x00, 3);
		
		
		
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)res[1] != 0x55)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[2] != 0x55)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[3] != 0x55)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)(res[4] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		Thread.sleep(300);
		
		byte[] data2 = {0x00, 0x00, 0x00};
		System.out.println("Writing 0x00 to 0x1E 0x1F 0x20 address...");
		testWriteMultiBytes((byte)0x50, (byte)0x00, data2);
		
		Thread.sleep(300);
		
		System.out.println("Reading multiple addresses 0x1E 0x1F 0x20...");
		res = testReadBytes((byte)0x50, (byte)0x00, 3);
		
		if ((byte)(res[0] & 0xFF) != CommConstants.CMD_ANSWER)
		{
			System.out.print("Answer start byte is wrong!");
			return false;
		}
		if ((byte)res[1] != 0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[2] != 0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)res[3] != 0x00)
		{
			System.out.print("Written and read data don't match!");
			return false;
		}
		if ((byte)(res[4] & 0xFF) != CommConstants.CMD_END)
		{
			System.out.print("Answer end byte wrong!");
			return false;
		}
		
		System.out.println("\n");
		
		return true;
	}
	
	
	public static void main(String[] args) throws Exception
	{
		commCont = CommController.getInstance();
		try {Thread.sleep(2000);} catch (InterruptedException ie) {} // Wait for communication channel is up
		
		boolean tst = true;
		
		tst &= testADXL345ID();
		
		tst &= testReadWriteSingleByte();
		
		tst &= testReadWriteMultiBytes();
		
		Thread.sleep(100);
		
		tst &= testEEPROMReadWriteMultiBytes();
		
		if (tst)
			System.out.print("Communication test - OK!");
		else
			System.out.print("Communication test - FAILED!");
	}
}