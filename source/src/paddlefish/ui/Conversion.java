package paddlefish.ui;

import java.util.Formatter;

public class Conversion {
	public static String hex2str(byte[] data)
	{
		StringBuilder stringBuild = new StringBuilder(data.length * 2);
		Formatter formatter = new Formatter(stringBuild);
		for ( int i = 0; i<data.length; i++ )		
			formatter.format("%02x ", data[i]);
		
		formatter.close();
				
		return stringBuild.toString().toUpperCase();
	}
	
	public static byte str2hex(String str) throws Exception
	{
		// TODO : accept 0xFFF format
		
		str = str.toUpperCase();
		
		if ( str.length() > 4 )
			throw (new Exception("Hex number must be in FF or 0xFF format!"));
		
		if ( str.length() > 2 )
		{
			String[] hexStr = str.split("X");
			
			if ( (hexStr.length < 2) || (hexStr.length > 2) )
				throw (new Exception("Hex number must be in FF or 0xFF format!"));
			
			if ( !hexStr[0].equals("0") )
				throw (new Exception("Hex number must be in FF or 0xFF format!"));
			
			str = hexStr[1];
		}
		
		
		
		byte res = 0;
		
		for ( int i = 0; i < str.length(); i++)
		{
			switch ( str.charAt(i) )
			{
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					res |= (byte) (Character.getNumericValue(str.charAt(i)) << (4 * ( str.length() - i - 1 ) ));
					break;
				case 'A':
					res |= (byte) (10 << (4 * ( str.length() - i - 1 ) ));
					break;
				case 'B':
					res |= (byte) (11 << (4 * ( str.length() - i - 1 ) ));
					break;
				case 'C':
					res |= (byte) (12 << (4 * ( str.length() - i - 1 ) ));
					break;
				case 'D':
					res |= (byte) (13 << (4 * ( str.length() - i - 1 ) ));
					break;
				case 'E':
					res |= (byte) (14 << (4 * ( str.length() - i - 1 ) ));
					break;
				case 'F':
					res |= (byte) (15 << (4 * ( str.length() - i - 1 ) ));
					break;
				default:
					throw (new Exception("Not a hex number!"));
			}
		}
		
		return res;
	}
}
