package paddlefish.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;

import javax.swing.JFrame;

import paddlefish.protocol.CommController;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.xml.crypto.Data;

public class Sscomm {

	private JFrame frame;
	private JButton btnConnect;
	private static CommController commCont = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					commCont = CommController.getInstance();
					Sscomm window = new Sscomm();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Sscomm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setBounds(69, 74, 141, 24);
		frame.getContentPane().add(comboBox);
		
		listPortsToComboBox(comboBox);
		
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(69, 135, 117, 25);
		frame.getContentPane().add(btnConnect);
		
		final JTextPane txtI2C = new JTextPane();
		txtI2C.setBounds(69, 228, 98, 21);
		frame.getContentPane().add(txtI2C);
		
		JLabel lblIcAddress = new JLabel("I2C Address :");
		lblIcAddress.setBounds(69, 201, 117, 15);
		frame.getContentPane().add(lblIcAddress);
		
		JLabel lblRegisterAddress = new JLabel("Register Address :");
		lblRegisterAddress.setBounds(69, 276, 141, 15);
		frame.getContentPane().add(lblRegisterAddress);
		
		final JTextPane txtReg = new JTextPane();
		txtReg.setBounds(69, 303, 98, 21);
		frame.getContentPane().add(txtReg);
		
		JLabel lblData = new JLabel("Data :");
		lblData.setBounds(69, 349, 141, 15);
		frame.getContentPane().add(lblData);
		
		final JTextPane txtData = new JTextPane();
		txtData.setBounds(69, 376, 98, 21);
		frame.getContentPane().add(txtData);		
		
		final DefaultListModel<String> flowModel = new DefaultListModel<String>();  
		JList<String> lstFlow = new JList<String>(flowModel);
		lstFlow.setBounds(385, 74, 267, 438);
		frame.getContentPane().add(lstFlow);
		
		flowModel.addElement("I2C | Reg | Len | Dir | Data");
		
		JButton btnWrite = new JButton("Write");
		btnWrite.setBounds(69, 409, 117, 25);
		frame.getContentPane().add(btnWrite);
		
		btnWrite.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					byte data[] = new byte[] {(byte) str2hex(txtData.getText())};
					commCont.writeByteArray(str2hex(txtI2C.getText()), str2hex(txtReg.getText()), 1, data);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   1    |  >  | " + txtData.getText());
			}
	    });
		
		JButton btnRead = new JButton("Read");
		btnRead.setBounds(69, 446, 117, 25);
		frame.getContentPane().add(btnRead);
		
		btnRead.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					
					byte data[] = commCont.readByteArray(str2hex(txtI2C.getText()), str2hex(txtReg.getText()), 1);
					flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   1    |  <  | " + hex2str(data));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
	    });
		
		btnConnect.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				if ( commCont.isConnected() )
				{
					commCont.disconnect();
					btnConnect.setText("Connect");
				} else {
					commCont.connect((String)comboBox.getSelectedItem(), 115200);
					btnConnect.setText("Disconnect");
				}
				
				
			}
	    });
	}
	
	private void listPortsToComboBox(JComboBox<String> comboBox)
	{
		ArrayList<String> portList = commCont.listPorts();
		for (int i=0; i<portList.size(); i++)
			comboBox.addItem(portList.get(i));
	}
	
	private String hex2str(byte[] data)
	{
		StringBuilder stringBuild = new StringBuilder((data.length-2) * 2);
		Formatter formatter = new Formatter(stringBuild);
		// Get rid of start and end characters of the answer
		for ( int i = 1; i<data.length-1; i++ )		
			formatter.format("%02x", data[i]);
				
		return stringBuild.toString().toUpperCase();
	}
	
	private byte str2hex(String str) throws Exception
	{
		// TODO : accept 0xFFF format				
		if ( str.length() > 2 )
			throw (new Exception("Hex number must be in FF format!"));
		
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
