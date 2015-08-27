package paddlefish.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Formatter;

import javax.swing.JFrame;

import paddlefish.hal.CommControllerInterface;
import paddlefish.hal.CommStreamerInterface;
import paddlefish.protocol.CommConstants;
import paddlefish.protocol.CommController;
import paddlefish.protocol.CommStreamer;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;

public class Sbuscom implements CommControllerInterface, CommStreamerInterface{

	private JFrame frame;
	private JButton btnConnect;
	private static JLabel lblShowStat;
	private static CommController commCont = null;
	private static CommStreamer commStreamer = null;
	DefaultListModel<String> flowModel = null;
	JTextPane txtReg;
	JTextPane txtI2CSpeed;
	JTextPane txtI2C;
	
	DefaultListModel<String> streamFlowModel = null;
	DefaultListModel<String> streamDeviceFlowModel = null;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//System.setOut(new PrintStream(new FileOutputStream("output.txt")));
					commCont = new CommController();
					commStreamer = new CommStreamer();
					Sbuscom window = new Sbuscom();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Sbuscom() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 530);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane();
				
		
		JPanel panelBasic = new JPanel(false);
		
		tabbedPane.addTab("Basic", panelBasic);
		tabbedPane.setBounds(0, 0, 650, 500);
		panelBasic.setLayout(null);
		
		
		JLabel lblComPort = new JLabel("COM Port :");
		lblComPort.setBounds(30, 25, 117, 15);
		panelBasic.add(lblComPort);
		
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setBounds(30, 45, 150, 24);
		panelBasic.add(comboBox);
		
		listPortsToComboBox(comboBox);
		
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(185, 45, 117, 25);
		panelBasic.add(btnConnect);
		
		JLabel lblIcAddress = new JLabel("I2C Address :");
		lblIcAddress.setBounds(30, 85, 117, 15);
		panelBasic.add(lblIcAddress);
		
		txtI2C = new JTextPane();
		txtI2C.setBounds(30, 105, 98, 21);
		txtI2C.setToolTipText("One byte I2C Device Address. Can be found from device data sheet. Hex Format : 50");
		panelBasic.add(txtI2C);		
		
		JLabel lblRegisterAddress = new JLabel("Register Address :");
		lblRegisterAddress.setBounds(30, 145, 141, 15);
		panelBasic.add(lblRegisterAddress);
		
		txtReg = new JTextPane();
		txtReg.setBounds(30, 165, 98, 21);
		txtReg.setToolTipText("One byte Device Register Address. Hex Format : 01");
		panelBasic.add(txtReg);
		
		JLabel lblLength = new JLabel("Data Length :");
		lblLength.setBounds(30, 205, 141, 15);
		panelBasic.add(lblLength);
		
		final JTextPane txtLength = new JTextPane();
		txtLength.setBounds(30, 225, 98, 21);
		txtLength.setToolTipText("Number of bytes to be read starting from Register Address. Decimal value.");
		panelBasic.add(txtLength);
		
		JLabel lblData = new JLabel("Data :");
		lblData.setBounds(30, 265, 141, 15);
		panelBasic.add(lblData);		
		
		final JTextArea txtData = new JTextArea();
		txtData.setBounds(30, 285, 300, 130);		
		txtData.setLineWrap(true);
		txtData.setWrapStyleWord(true);		
		txtData.setToolTipText("Bytes to be read. Accepts multiple bytes. Hex Format : 00 11 22 33 44 55 ...");
		panelBasic.add(txtData);
		
		JScrollPane scrData = new JScrollPane(txtData);
		scrData.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrData.setPreferredSize(new Dimension(250, 250));
		scrData.setBounds(30, 285, 300, 130);
		panelBasic.add(scrData);	
		
		JLabel lblList = new JLabel("I2C | Reg | Len | Dir | Data");
		lblList.setBounds(350, 25, 267, 15);
		panelBasic.add(lblList);
		
		flowModel = new DefaultListModel<String>();  
		JList<String> lstFlow = new JList<String>(flowModel);
		lstFlow.setBounds(350, 45, 267, 415);
		panelBasic.add(lstFlow);
		
		JScrollPane scrList = new JScrollPane(lstFlow);
		scrList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrList.setPreferredSize(new Dimension(250, 250));
		scrList.setBounds(350, 45, 267, 415);
		panelBasic.add(scrList);
		
		JLabel lblStat = new JLabel("Status:");
		lblStat.setBounds(5, 505, 90, 15);		
		frame.getContentPane().add(lblStat);
		
		lblShowStat = new JLabel("OK");
		lblShowStat.setBounds(60, 505, 580, 15);
		lblShowStat.setToolTipText(lblShowStat.getText());
		frame.getContentPane().add(lblShowStat);
		
		JButton btnWrite = new JButton("Write");
		btnWrite.setBounds(185, 435, 117, 25);
		panelBasic.add(btnWrite);			
		
		btnWrite.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					String[] splitData = txtData.getText().split(" ");
					byte data[] = new byte[splitData.length];
					for (int i=0;i<splitData.length;i++)
						data[i] = (byte) str2hex(splitData[i]);
					commCont.writeByteArray(str2hex(txtI2C.getText()), str2hex(txtReg.getText()), data.length, data);
					flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |  " + data.length + "    |  >  | " + txtData.getText());
					lblShowStat.setText("OK");
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JButton btnRead = new JButton("Read");
		btnRead.setBounds(30, 435, 117, 25);
		panelBasic.add(btnRead);
		
		
				
		btnRead.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					int len = Integer.parseInt(txtLength.getText());
					commCont.readByteArray(str2hex(txtI2C.getText()), str2hex(txtReg.getText()), len);
					//flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   " + len + "   |  <  | " + hex2str(data));
					lblShowStat.setText("OK");
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		btnConnect.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				try
				{
				if ( commCont.isConnected() )
				{
					commCont.disconnect();
					btnConnect.setText("Connect");
				} else {
					commCont.connect((String)comboBox.getSelectedItem(), 115200);
					btnConnect.setText("Disconnect");
				}
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		
		JPanel panelAdvanced = new JPanel(false);
		
		tabbedPane.addTab("Advanced", panelAdvanced);
		panelAdvanced.setLayout(null);

		
		JLabel lblI2CSpeed = new JLabel("Set I2C Speed (Hz) :");
		lblI2CSpeed.setBounds(30, 25, 160, 15);		
		panelAdvanced.add(lblI2CSpeed);
		
		txtI2CSpeed = new JTextPane();
		txtI2CSpeed.setBounds(30, 45, 150, 24);
		txtI2CSpeed.setToolTipText("A value between 500Hz~880000Hz");
		panelAdvanced.add(txtI2CSpeed);	
		
		JButton btnSetI2CSpeed = new JButton("Set");
		btnSetI2CSpeed.setBounds(200, 45, 150, 24);
		panelAdvanced.add(btnSetI2CSpeed);			
		
		btnSetI2CSpeed.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					long speed = Long.parseLong(txtI2CSpeed.getText().trim());
					commCont.setI2CSpeed(speed);
					flowModel.addElement("->Set I2C Speed : " + speed);
					lblShowStat.setText("OK");
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JPanel panelStream = new JPanel(false);
		
		tabbedPane.addTab("Stream", panelStream);
		panelStream.setLayout(null);
		
		
		JLabel lblIcStreamAddress = new JLabel("I2C Addr :");
		lblIcStreamAddress.setBounds(30, 300, 70, 15);
		panelStream.add(lblIcStreamAddress);
		
		final JTextPane txtI2CStream = new JTextPane();
		txtI2CStream.setBounds(30, 320, 60, 21);
		txtI2CStream.setToolTipText("One byte I2C Device Address. Can be found from device data sheet. Hex Format : 50");
		panelStream.add(txtI2CStream);		
		
		JLabel lblRegisterStreamAddress = new JLabel("Reg Addr :");
		lblRegisterStreamAddress.setBounds(130, 300, 80, 15);
		panelStream.add(lblRegisterStreamAddress);
		
		final JTextPane txtRegStream = new JTextPane();
		txtRegStream.setBounds(130, 320, 60, 21);
		txtRegStream.setToolTipText("One byte Device Register Address. Hex Format : 01");
		panelStream.add(txtRegStream);
		
		JLabel lblLengthStream = new JLabel("Length :");
		lblLengthStream.setBounds(230, 300, 70, 15);
		panelStream.add(lblLengthStream);
		
		final JTextPane txtLengthStream = new JTextPane();
		txtLengthStream.setBounds(230, 320, 60, 21);
		txtLengthStream.setToolTipText("Number of bytes to be read starting from Register Address. Decimal value.");
		panelStream.add(txtLengthStream);
		
		JLabel lblDeviceList = new JLabel("Stream Device List :");
		lblDeviceList.setBounds(30, 25, 267, 15);
		panelStream.add(lblDeviceList);
		
		streamDeviceFlowModel = new DefaultListModel<String>();  
		JList<String> lstStreamFlow = new JList<String>(streamDeviceFlowModel);
		lstStreamFlow.setBounds(30, 45, 267, 250);
		panelStream.add(lstStreamFlow);
		
		JScrollPane scrListStream = new JScrollPane(lstStreamFlow);
		scrListStream.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrListStream.setPreferredSize(new Dimension(250, 250));
		scrListStream.setBounds(30, 45, 267, 250);
		panelStream.add(scrListStream);
		
		JLabel lblPeriod = new JLabel("Stream Period :");
		lblPeriod.setBounds(30, 360, 141, 15);
		panelStream.add(lblPeriod);
		
		final JTextPane txtPeriod = new JTextPane();
		txtPeriod.setBounds(150, 357, 98, 21);
		txtPeriod.setToolTipText("Stream Period in ms.");
		panelStream.add(txtPeriod);
		
		txtPeriod.setText("1000");
		
		JButton btnAddStreamDevice = new JButton("Add");
		btnAddStreamDevice.setBounds(30, 380, 80, 24);
		panelStream.add(btnAddStreamDevice);			
		
		btnAddStreamDevice.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					int period = Integer.parseInt(txtPeriod.getText().trim());
					int len = Integer.parseInt(txtLengthStream.getText());
					commStreamer.setPeriod(period);
					commStreamer.addDevice(str2hex(txtI2CStream.getText()), str2hex(txtRegStream.getText()), len, period);
					streamDeviceFlowModel.addElement("I2C:" + txtI2CStream.getText() + " Reg:" + txtRegStream.getText() + " Len:" + txtLengthStream.getText());
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JButton btnResetStream = new JButton("Reset");
		btnResetStream.setBounds(130, 380, 80, 24);
		panelStream.add(btnResetStream);			
		
		btnResetStream.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					commStreamer.reset();
					streamDeviceFlowModel.clear();
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JButton btnStartStream = new JButton("Start");
		btnStartStream.setBounds(30, 410, 80, 24);
		panelStream.add(btnStartStream);			
		
		btnStartStream.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					commStreamer.start();
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JButton btnStopStream = new JButton("Stop");
		btnStopStream.setBounds(130, 410, 80, 24);
		panelStream.add(btnStopStream);			
		
		btnStopStream.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					commStreamer.stop();
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JLabel lblStreamList = new JLabel("Stream Data :");
		lblStreamList.setBounds(350, 25, 267, 15);
		panelStream.add(lblStreamList);
		
		streamFlowModel = new DefaultListModel<String>();  
		JList<String> lstStreamDataFlow = new JList<String>(streamFlowModel);
		lstStreamDataFlow.setBounds(350, 45, 267, 415);
		panelStream.add(lstStreamDataFlow);
		
		JScrollPane scrStreamDataList = new JScrollPane(lstStreamDataFlow);
		scrStreamDataList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrStreamDataList.setPreferredSize(new Dimension(250, 250));
		scrStreamDataList.setBounds(350, 45, 267, 415);
		panelStream.add(scrStreamDataList);

		
		frame.getContentPane().add(tabbedPane);
		
		commCont.addDataReceiver(this);
		commCont.addCommandReceiver(this);
		
		commStreamer.addStreamReceiver(this);
		
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
		for ( int i = 0; i<data.length; i++ )		
			formatter.format("%02x ", data[i]);
		
		formatter.close();
				
		return stringBuild.toString().toUpperCase();
	}
	
	private byte str2hex(String str) throws Exception
	{
		// TODO : accept 0xFFF format				
		if ( str.length() > 2 )
			throw (new Exception("Hex number must be in FF format!"));
		
		str = str.toUpperCase();
		
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

	@Override
	public void commDataReceiver(byte[] buffer) {
		flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   " + buffer.length + "   |  <  | " + hex2str(buffer));
	}
	
	@Override
	public void commCommandReceiver(byte[] buffer) {
		if ( !checkOK(buffer) )
			lblShowStat.setText("I2C Error! Check if I2C device connected properly. Slow down the I2C speed from Advanced tab.");
		else
			lblShowStat.setText("OK");		
		
		flowModel.addElement("CMD:" + hex2str(buffer));
	}
	
	private static boolean checkOK(byte ans[])
	{
		if ( ((byte)ans[0] != (byte)CommConstants.CMD_ANSWER) || ((byte)ans[2] != (byte)CommConstants.CMD_OK) || ((byte)ans[3] != (byte)CommConstants.CMD_END))
			return false;
		return true;
	}

	@Override
	public void streamReceiver(byte[] buffer) {
		// TODO Auto-generated method stub
		streamFlowModel.addElement(hex2str(buffer));
	}
}
