/*
 Part of the Nerduino IOT project - http://nerduino.com

 Copyright (c) 2013 Chase Laurendine

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.nerduino.library;

import com.nerduino.xbee.DataReceivedEvent;
import com.nerduino.xbee.DataReceivedListener;
import com.nerduino.xbee.Serial;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.EventObject;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

@ConvertAsProperties(
    dtd = "-//com.nerduino.library//NerduinoDashboard//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "NerduinoDashboardTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.nerduino.library.NerduinoDashboardTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_NerduinoDashboardAction",
preferredID = "NerduinoDashboardTopComponent")
@Messages(
{
	"CTL_NerduinoDashboardAction=NerduinoDashboard",
	"CTL_NerduinoDashboardTopComponent=NerduinoDashboard Window",
	"HINT_NerduinoDashboardTopComponent=This is a NerduinoDashboard window"
})
@SuppressWarnings({"unchecked", "rawtypes"})
public final class NerduinoDashboardTopComponent extends TopComponent implements HyperlinkListener, UpdateEventListener, CommandEventListener, DataReceivedListener
{
	NerduinoBase m_nerduino;
	Serial m_serial;
	final DefaultStyledDocument commanddoc;
	final Style responseStyle;
	final Style errorStyle;
	final Style commandStyle;
	final Style inCommandStyle;
	final Style warningStyle;
	final DefaultStyledDocument serialdoc;
	final Style incomingStyle;
	final Style outgoingStyle;
	
	private int m_baudrate = 9600;
	
	@SuppressWarnings({"LeakingThisInConstructor", "BooleanConstructorCall"})
	public NerduinoDashboardTopComponent()
	{
		initComponents();
		setName(Bundle.CTL_NerduinoDashboardTopComponent());
		setToolTipText(Bundle.HINT_NerduinoDashboardTopComponent());
		
		// add an html editor kit
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane2.setEditorKit(kit);
		jEditorPane2.addHyperlinkListener(this);
        
        // add some styles to the html
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        styleSheet.addRule("h1 {color: blue;}");
        styleSheet.addRule("h2 {color: #ff0000;}");
        styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
		
		StyleContext sc = new StyleContext();
	    commanddoc = new DefaultStyledDocument(sc);
		
		responseStyle = sc.addStyle("Response", null);
		responseStyle.addAttribute(StyleConstants.Foreground, Color.black);
		responseStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		responseStyle.addAttribute(StyleConstants.FontFamily, "serif");
		responseStyle.addAttribute(StyleConstants.Bold, new Boolean(false));
		
		errorStyle = sc.addStyle("Error", null);
		errorStyle.addAttribute(StyleConstants.Foreground, Color.red);
		errorStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		errorStyle.addAttribute(StyleConstants.FontFamily, "serif");
		errorStyle.addAttribute(StyleConstants.Bold, new Boolean(true));
		
		commandStyle = sc.addStyle("Command", null);
		commandStyle.addAttribute(StyleConstants.Foreground, Color.blue);
		commandStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		commandStyle.addAttribute(StyleConstants.FontFamily, "serif");
		commandStyle.addAttribute(StyleConstants.Bold, new Boolean(false));
		
		inCommandStyle = sc.addStyle("InCommand", null);
		inCommandStyle.addAttribute(StyleConstants.Foreground, Color.green);
		inCommandStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		inCommandStyle.addAttribute(StyleConstants.FontFamily, "serif");
		inCommandStyle.addAttribute(StyleConstants.Bold, new Boolean(false));
		
		warningStyle = sc.addStyle("Warning", null);
		warningStyle.addAttribute(StyleConstants.Foreground, Color.yellow);
		warningStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		warningStyle.addAttribute(StyleConstants.FontFamily, "serif");
		warningStyle.addAttribute(StyleConstants.Bold, new Boolean(true));
		
		
		textCommand.setDocument(commanddoc);
		
		
		StyleContext sc2 = new StyleContext();
	    serialdoc = new DefaultStyledDocument(sc2);
		
		incomingStyle = sc2.addStyle("Incoming", null);
		incomingStyle.addAttribute(StyleConstants.Foreground, Color.black);
		incomingStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		incomingStyle.addAttribute(StyleConstants.FontFamily, "serif");
		incomingStyle.addAttribute(StyleConstants.Bold, new Boolean(false));

		outgoingStyle = sc2.addStyle("Outgoing", null);
		outgoingStyle.addAttribute(StyleConstants.Foreground, Color.green);
		outgoingStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		outgoingStyle.addAttribute(StyleConstants.FontFamily, "serif");
		outgoingStyle.addAttribute(StyleConstants.Bold, new Boolean(false));

		textSerial.setDocument(serialdoc);


        // create a document, set it on the jeditorpane, then add the html
        Document doc = kit.createDefaultDocument();
        jEditorPane2.setDocument(doc);
		
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        propertySheet1 = new org.openide.explorer.propertysheet.PropertySheet();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textCommand = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        editCommand = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        cbVerbose = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        textSerial = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        editSerial = new javax.swing.JTextField();
        btnSerialSend = new javax.swing.JButton();
        btnSerialClear = new javax.swing.JButton();
        cmbBaudRate = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane2 = new javax.swing.JEditorPane();

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(250);
        jSplitPane2.setDividerSize(5);
        jSplitPane2.setToolTipText(org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.jSplitPane2.toolTipText")); // NOI18N

        propertySheet1.setQuickSearchAllowed(false);
        jSplitPane2.setLeftComponent(propertySheet1);

        textCommand.setEditable(false);
        jScrollPane3.setViewportView(textCommand);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.jLabel1.text")); // NOI18N

        editCommand.setText(org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.editCommand.text")); // NOI18N
        editCommand.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                onCommandKeyPressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnSend, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.btnSend.text")); // NOI18N
        btnSend.setIconTextGap(0);
        btnSend.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnSend.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onSend(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnClear, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.btnClear.text")); // NOI18N
        btnClear.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onClear(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbVerbose, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.cbVerbose.text")); // NOI18N
        cbVerbose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OnVerboseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(editCommand)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 339, Short.MAX_VALUE)
                        .addComponent(cbVerbose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClear)
                    .addComponent(jLabel1)
                    .addComponent(cbVerbose))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSend))
                .addContainerGap())
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        textSerial.setEditable(false);
        jScrollPane4.setViewportView(textSerial);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.jLabel2.text")); // NOI18N

        editSerial.setText(org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.editSerial.text")); // NOI18N
        editSerial.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                onSerialMonitorKeyPressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnSerialSend, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.btnSerialSend.text")); // NOI18N
        btnSerialSend.setIconTextGap(0);
        btnSerialSend.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnSerialSend.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onSerialMonitorSend(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnSerialClear, org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.btnSerialClear.text")); // NOI18N
        btnSerialClear.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onSerialMonitorClear(evt);
            }
        });

        cmbBaudRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "300 baud", "1200 baud", "2400 baud", "4800 baud", "9600 baud", "14400 baud", "19200 baud", "28800 baud", "38400 baud", "57600 baud", "115200 baud" }));
        cmbBaudRate.setSelectedIndex(4);
        cmbBaudRate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                baudRateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 259, Short.MAX_VALUE)
                                .addComponent(cmbBaudRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(editSerial))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSerialClear, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSerialSend, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(btnSerialClear)
                    .addComponent(cmbBaudRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editSerial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSerialSend))
                .addContainerGap())
        );

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(NerduinoDashboardTopComponent.class, "NerduinoDashboardTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jSplitPane2.setRightComponent(jTabbedPane2);

        jSplitPane1.setRightComponent(jSplitPane2);

        jScrollPane1.setBorder(null);

        jEditorPane2.setEditable(false);
        jScrollPane1.setViewportView(jEditorPane2);

        jSplitPane1.setTopComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 867, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void onSend(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onSend
    {//GEN-HEADEREND:event_onSend
		sendCommand();
    }//GEN-LAST:event_onSend

    private void onCommandKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_onCommandKeyPressed
    {//GEN-HEADEREND:event_onCommandKeyPressed
		if (evt.getKeyCode() == 10)
			sendCommand();
    }//GEN-LAST:event_onCommandKeyPressed

    private void onClear(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onClear
    {//GEN-HEADEREND:event_onClear
		try
		{
			commanddoc.remove(0, commanddoc.getLength());
		}
		catch(BadLocationException ex)
		{
		}
    }//GEN-LAST:event_onClear

    private void onSerialMonitorKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_onSerialMonitorKeyPressed
    {//GEN-HEADEREND:event_onSerialMonitorKeyPressed
		if (evt.getKeyCode() == 10)
			sendSerial();
    }//GEN-LAST:event_onSerialMonitorKeyPressed

    private void onSerialMonitorSend(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onSerialMonitorSend
    {//GEN-HEADEREND:event_onSerialMonitorSend
        sendSerial();
    }//GEN-LAST:event_onSerialMonitorSend

    private void onSerialMonitorClear(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onSerialMonitorClear
    {//GEN-HEADEREND:event_onSerialMonitorClear
		try
		{
			serialdoc.remove(0, serialdoc.getLength());
		}
		catch(BadLocationException ex)
		{
		}
    }//GEN-LAST:event_onSerialMonitorClear

    private void baudRateChanged(java.awt.event.ActionEvent evt)//GEN-FIRST:event_baudRateChanged
    {//GEN-HEADEREND:event_baudRateChanged
        // reopen the serial port with the new baud rate
		int index = cmbBaudRate.getSelectedIndex();
		
		int[] bauds = new int[] { 300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200 };
		
		m_baudrate = bauds[index];
		
		if (m_serial != null)
		{
			m_serial.setEnabled(false);
			m_serial.setBaudRate(m_baudrate);
			m_serial.setEnabled(true);
		}

    }//GEN-LAST:event_baudRateChanged

    private void OnVerboseClicked(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OnVerboseClicked
    {//GEN-HEADEREND:event_OnVerboseClicked
        m_nerduino.setVerbose(cbVerbose.isSelected());
    }//GEN-LAST:event_OnVerboseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnSerialClear;
    private javax.swing.JButton btnSerialSend;
    private javax.swing.JCheckBox cbVerbose;
    private javax.swing.JComboBox cmbBaudRate;
    private javax.swing.JTextField editCommand;
    private javax.swing.JTextField editSerial;
    private javax.swing.JEditorPane jEditorPane2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane2;
    private org.openide.explorer.propertysheet.PropertySheet propertySheet1;
    private javax.swing.JTextPane textCommand;
    private javax.swing.JTextPane textSerial;
    // End of variables declaration//GEN-END:variables
	@Override
	public void componentOpened()
	{
		// TODO add custom code on component opening
	}
	
	@Override
	public void componentClosed()
	{
		if (m_nerduino != null)
		{
			m_nerduino.removeUpdateEventListener(this);
			m_nerduino.removeCommandEventListener(this);
		}
	}
	
	void sendCommand()
	{
		if (m_nerduino != null)
		{
			String command = editCommand.getText();
			
			if (command != null && !command.isEmpty())
			{
				m_nerduino.sendExecuteCommand(null, (short) 1, DataTypeEnum.DT_String.Value(), (byte) command.length(), command.getBytes());
			}
		}
	}
	
	void sendSerial()
	{
		if (m_serial != null)
		{
			String command = editSerial.getText();
			
			if (command != null && !command.isEmpty())
			{
				byte[] data = command.getBytes();
				
				m_serial.writeData(data, data.length);
			}
		}
	}


	void writeProperties(java.util.Properties p)
	{
		// better to version settings since initial version as advocated at
		// http://wiki.apidesign.org/wiki/PropertyFiles
		p.setProperty("version", "1.0");
		// TODO store your settings
	}

	void readProperties(java.util.Properties p)
	{
		String version = p.getProperty("version");
		// TODO read your settings according to their version
	}
	
	public void setNerduino(NerduinoBase nerduino)
	{
		m_nerduino = nerduino;
		
		handleUpdateEvent(null);

		m_nerduino = nerduino;

		nerduino.addUpdateEventListener(this);
		nerduino.addCommandEventListener(this);

		cbVerbose.setSelected(nerduino.getVerbose());
		
		if (m_serial == null)
		{
			m_serial = nerduino.getSerialMonitor();

			if (m_serial != null)
			{
//				m_serial.setEnabled(false);
//				m_serial.setBaudRate(m_baudrate);
//				m_serial.Protocol = 2;

				m_serial.addDataReceivedListener(this);
//				m_serial.setEnabled(true);
			}
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			String command = e.getDescription();
			
			m_nerduino.executeCommand(command);
		}
	}

	@Override
	public void handleUpdateEvent(EventObject e)
	{
		String html = m_nerduino.getHTML();
		
		jEditorPane2.setText(html);

		Node[] nodes = new Node[1];
		nodes[0] = null;

		propertySheet1.setNodes(nodes);
		
		nodes[0] = m_nerduino;
		
		propertySheet1.setNodes(nodes);
		
		super.setDisplayName(m_nerduino.getName() + " Dashboard");
	}

	@Override
	public void handleCommandEvent(CommandEventClass e)
	{
		appendCommand(e.Command, e.Type);
	}
	
	void appendCommand(String command, CommandMessageTypeEnum type)
	{
		try
		{
			int pos = commanddoc.getLength();
			
			switch (type)
			{
				case OutgoingCommand:
					commanddoc.insertString(pos, command + "\n", commandStyle);
					break;
				case Response:
					commanddoc.insertString(pos, command + "\n", responseStyle);
					break;
				case IncomingCommand:
					commanddoc.insertString(pos, command + "\n", inCommandStyle);
					break;
				default:
					commanddoc.insertString(pos, command + "\n", errorStyle);
					break;
			}
			
			textCommand.scrollRectToVisible(new Rectangle(0,textCommand.getBounds(null).height,1,1));
		}
		catch(BadLocationException ex)
		{
		}
	}

	@Override
	public void dataReceived(DataReceivedEvent e)
	{
		try
		{
			int pos = serialdoc.getLength();
			
			StringBuilder sb = new StringBuilder();

			for(int i = 0; i < e.Data.length; i++)
			{
				sb.append((char) e.Data[i]);
			}
			
			serialdoc.insertString(pos, sb.toString(), incomingStyle);
			
			//textSerial.scrollRectToVisible(new Rectangle(0,textSerial.getBounds(null).height,1,1));
		}
		catch(BadLocationException ex)
		{
		}
	}
	
	//@Override
	public void dataSent(DataReceivedEvent e)
	{
		try
		{
			int pos = serialdoc.getLength();
			
			StringBuilder sb = new StringBuilder();

			for(int i = 0; i < e.Data.length; i++)
			{
				sb.append((char) e.Data[i]);
			}
			
			serialdoc.insertString(pos, sb.toString(), outgoingStyle);
			
			//textSerial.scrollRectToVisible(new Rectangle(0,textSerial.getBounds(null).height,1,1));
		}
		catch(BadLocationException ex)
		{
		}
	}
}
