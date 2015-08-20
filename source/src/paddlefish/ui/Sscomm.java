package paddlefish.ui;

import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.xml.crypto.Data;
import javax.swing.JTextArea;

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
		frame.setBounds(100, 100, 650, 515);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblComPort = new JLabel("COM Port :");
		lblComPort.setBounds(30, 25, 117, 15);
		frame.getContentPane().add(lblComPort);
		
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setBounds(30, 45, 150, 24);
		frame.getContentPane().add(comboBox);
		
		listPortsToComboBox(comboBox);
		
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(185, 45, 117, 25);
		frame.getContentPane().add(btnConnect);
		
		JLabel lblIcAddress = new JLabel("I2C Address :");
		lblIcAddress.setBounds(30, 85, 117, 15);
		frame.getContentPane().add(lblIcAddress);
		
		final JTextPane txtI2C = new JTextPane();
		txtI2C.setBounds(30, 105, 98, 21);
		frame.getContentPane().add(txtI2C);		
		
		JLabel lblRegisterAddress = new JLabel("Register Address :");
		lblRegisterAddress.setBounds(30, 145, 141, 15);
		frame.getContentPane().add(lblRegisterAddress);
		
		final JTextPane txtReg = new JTextPane();
		txtReg.setBounds(30, 165, 98, 21);
		frame.getContentPane().add(txtReg);
		
		JLabel lblLength = new JLabel("Data Length :");
		lblLength.setBounds(30, 205, 141, 15);
		frame.getContentPane().add(lblLength);
		
		final JTextPane txtLength = new JTextPane();
		txtLength.setBounds(30, 225, 98, 21);
		frame.getContentPane().add(txtLength);
		
		JLabel lblData = new JLabel("Data :");
		lblData.setBounds(30, 265, 141, 15);
		frame.getContentPane().add(lblData);		
		
		final JTextArea txtData = new JTextArea();
		txtData.setBounds(30, 285, 300, 130);		
		txtData.setLineWrap(true);
		txtData.setWrapStyleWord(true);		
		frame.getContentPane().add(txtData);
		
		JScrollPane scrData = new JScrollPane(txtData);
		scrData.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrData.setPreferredSize(new Dimension(250, 250));
		scrData.setBounds(30, 285, 300, 130);
		frame.getContentPane().add(scrData);	
		
		JLabel lblList = new JLabel("I2C | Reg | Len | Dir | Data");
		lblList.setBounds(350, 25, 267, 15);
		frame.getContentPane().add(lblList);
		
		final DefaultListModel<String> flowModel = new DefaultListModel<String>();  
		JList<String> lstFlow = new JList<String>(flowModel);
		lstFlow.setBounds(350, 45, 267, 415);
		frame.getContentPane().add(lstFlow);
		
		JScrollPane scrList = new JScrollPane(lstFlow);
		scrList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrList.setPreferredSize(new Dimension(250, 250));
		scrList.setBounds(350, 45, 267, 415);
		frame.getContentPane().add(scrList);
		
		JButton btnWrite = new JButton("Write");
		btnWrite.setBounds(185, 435, 117, 25);
		frame.getContentPane().add(btnWrite);			
		
		btnWrite.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					byte data[] = new byte[] {(byte) str2hex(txtData.getText())};
					commCont.writeByteArray(str2hex(txtI2C.getText()), str2hex(txtReg.getText()), 1, data);
				} catch (Exception e) {
					e.printStackTrace();
				}
				flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   1    |  >  | " + txtData.getText());
			}
	    });
		
		JButton btnRead = new JButton("Read");
		btnRead.setBounds(30, 435, 117, 25);
		frame.getContentPane().add(btnRead);
		
		
				
		btnRead.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					
					byte data[] = commCont.readByteArray(str2hex(txtI2C.getText()), str2hex(txtReg.getText()), 1);
					flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   1    |  <  | " + hex2str(data));
				} catch (Exception e) {
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
		
		formatter.close();
				
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
