package paddlefish.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
	
	JTabbedPane tabbedPane;
	
	long tmptime = 0;

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
		
		tabbedPane = new JTabbedPane();
				
		initBasicTab();
		
		initAdvancedTab();
		
		initStreamTab();

		
		frame.getContentPane().add(tabbedPane);
		
		commCont.addDataReceiver(this);
		commCont.addCommandReceiver(this);
		
		commStreamer.addStreamReceiver(this);
		
	}
	
	private void initBasicTab()
	{
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
		txtI2C.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtI2C.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		panelBasic.add(txtI2C);		
		
		JLabel lblRegisterAddress = new JLabel("Register Address :");
		lblRegisterAddress.setBounds(30, 145, 141, 15);
		panelBasic.add(lblRegisterAddress);
		
		txtReg = new JTextPane();
		txtReg.setBounds(30, 165, 98, 21);
		txtReg.setToolTipText("One byte Device Register Address. Hex Format : 01");
		txtReg.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtReg.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		panelBasic.add(txtReg);
		
		JLabel lblLength = new JLabel("Data Length :");
		lblLength.setBounds(30, 205, 141, 15);
		panelBasic.add(lblLength);
		
		final JTextPane txtLength = new JTextPane();
		txtLength.setBounds(30, 225, 98, 21);
		txtLength.setToolTipText("Number of bytes to be read starting from Register Address. Decimal value.");
		txtLength.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtLength.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		panelBasic.add(txtLength);
		
		JLabel lblData = new JLabel("Data :");
		lblData.setBounds(30, 265, 141, 15);
		panelBasic.add(lblData);		
		
		final JTextArea txtData = new JTextArea();
		txtData.setBounds(30, 285, 300, 130);		
		txtData.setLineWrap(true);
		txtData.setWrapStyleWord(true);		
		txtData.setToolTipText("Bytes to be read. Accepts multiple bytes. Hex Format : 00 11 22 33 44 55 ...");
		txtData.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtData.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
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
						data[i] = (byte) Conversion.str2hex(splitData[i]);
					commCont.writeByteArray(Conversion.str2hex(txtI2C.getText()), Conversion.str2hex(txtReg.getText()), data.length, data);
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
					commCont.readByteArray(Conversion.str2hex(txtI2C.getText()), Conversion.str2hex(txtReg.getText()), len);
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
		
	}
	
	private void initAdvancedTab()
	{
		JPanel panelAdvanced = new JPanel(false);
		
		tabbedPane.addTab("Advanced", panelAdvanced);
		panelAdvanced.setLayout(null);

		
		JLabel lblI2CSpeed = new JLabel("Set I2C Speed (Hz) :");
		lblI2CSpeed.setBounds(30, 25, 160, 15);		
		panelAdvanced.add(lblI2CSpeed);
		
		txtI2CSpeed = new JTextPane();
		txtI2CSpeed.setBounds(30, 45, 150, 24);
		txtI2CSpeed.setToolTipText("A value between 500Hz~880000Hz");
		txtI2CSpeed.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtI2CSpeed.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
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
		
		
		JButton btnTestData = new JButton("Test Data");
		btnTestData.setBounds(400, 45, 150, 24);
		panelAdvanced.add(btnTestData);			
		
		btnTestData.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {					
					commCont.testData((byte)0x05);					
					lblShowStat.setText("OK");
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
	}
	
	private void initStreamTab()
	{
		JPanel panelStream = new JPanel(false);
		
		tabbedPane.addTab("Stream", panelStream);
		panelStream.setLayout(null);
		
		
		JLabel lblIcStreamAddress = new JLabel("I2C Addr :");
		lblIcStreamAddress.setBounds(30, 300, 70, 15);
		panelStream.add(lblIcStreamAddress);
		
		final JTextPane txtI2CStream = new JTextPane();
		txtI2CStream.setBounds(30, 320, 60, 21);
		txtI2CStream.setToolTipText("One byte I2C Device Address. Can be found from device data sheet. Hex Format : 50");
		txtI2CStream.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtI2CStream.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		panelStream.add(txtI2CStream);		
		
		JLabel lblRegisterStreamAddress = new JLabel("Reg Addr :");
		lblRegisterStreamAddress.setBounds(130, 300, 80, 15);
		panelStream.add(lblRegisterStreamAddress);
		
		final JTextPane txtRegStream = new JTextPane();
		txtRegStream.setBounds(130, 320, 60, 21);
		txtRegStream.setToolTipText("One byte Device Register Address. Hex Format : 01");
		txtRegStream.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtRegStream.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		panelStream.add(txtRegStream);
		
		JLabel lblLengthStream = new JLabel("Length :");
		lblLengthStream.setBounds(230, 300, 70, 15);
		panelStream.add(lblLengthStream);
		
		final JTextPane txtLengthStream = new JTextPane();
		txtLengthStream.setBounds(230, 320, 60, 21);
		txtLengthStream.setToolTipText("Number of bytes to be read starting from Register Address. Decimal value.");
		txtLengthStream.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtLengthStream.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
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
		txtPeriod.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		txtPeriod.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		panelStream.add(txtPeriod);
		
		txtPeriod.setText("1000");
		
		JButton btnAddStreamDevice = new JButton("Add");
		btnAddStreamDevice.setBounds(30, 400, 120, 24);
		panelStream.add(btnAddStreamDevice);			
		
		btnAddStreamDevice.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {					
					int period = Integer.parseInt(txtPeriod.getText().trim());
					int len = Integer.parseInt(txtLengthStream.getText());					
					commStreamer.addDevice(Conversion.str2hex(txtI2CStream.getText()), Conversion.str2hex(txtRegStream.getText()), len, period);
					streamDeviceFlowModel.addElement("I2C:" + txtI2CStream.getText() + " Reg:" + txtRegStream.getText() + " Len:" + txtLengthStream.getText());
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JButton btnResetStream = new JButton("Reset");
		btnResetStream.setBounds(170, 400, 120, 24);
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
		btnStartStream.setBounds(30, 430, 120, 24);
		panelStream.add(btnStartStream);			
		
		btnStartStream.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent event) {
				
				try {
					int period = Integer.parseInt(txtPeriod.getText().trim());
					commStreamer.setPeriod(period);
					commStreamer.start();
				} catch (Exception e) {
					e.printStackTrace();
					lblShowStat.setText(e.getMessage());
				}
				
			}
	    });
		
		JButton btnStopStream = new JButton("Stop");
		btnStopStream.setBounds(170, 430, 120, 24);
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
	}
	
	private void listPortsToComboBox(JComboBox<String> comboBox)
	{
		ArrayList<String> portList = commCont.listPorts();
		for (int i=0; i<portList.size(); i++)
			comboBox.addItem(portList.get(i));
	}
	
	

	@Override
	public void commDataReceiver(byte[] buffer) {
		byte[] tmpBuffer = new byte[buffer.length-5];
		System.arraycopy(buffer, 3, tmpBuffer, 0, buffer.length-5);
		flowModel.addElement(txtI2C.getText() + "  |  " + txtReg.getText() + "   |   " + tmpBuffer.length + "   |  <  | " + Conversion.hex2str(tmpBuffer));
	}
	
	@Override
	public void commCommandReceiver(byte[] buffer) {
		if ( !checkOK(buffer) )
			lblShowStat.setText("I2C Error! Check if I2C device connected properly. Slow down the I2C speed from Advanced tab.");
		else
			lblShowStat.setText("OK");		
		
		flowModel.addElement("CMD:" + Conversion.hex2str(buffer));
	}

	@Override
	public void streamReceiver(byte[] buffer) {
		long timestamp = (buffer[1] << 0) + (buffer[2] << 8) + (buffer[3] << 16) + (buffer[4] << 24);
		streamFlowModel.addElement(timestamp + ":" + Conversion.hex2str(buffer) + ":" + (tmptime-System.nanoTime())/1000000);
		tmptime = System.nanoTime();
	}
	
	private static boolean checkOK(byte ans[])
	{
		if ( ((byte)ans[0] != (byte)CommConstants.CMD_ANSWER) || ((byte)ans[3] != (byte)CommConstants.CMD_OK) || ((byte)ans[4] != (byte)CommConstants.CMD_END))
			return false;
		return true;
	}
}
