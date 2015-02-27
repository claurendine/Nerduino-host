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

import processing.app.Board;
import processing.app.BoardManager;
import processing.app.Sketch;
import processing.app.SketchManager;
import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import org.openide.nodes.Node;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NerduinoXBeeConfigDialog extends javax.swing.JDialog
{
	public NerduinoXBee m_nerduino;
	
	/** Creates new form SkitConfigDialog */
	public NerduinoXBeeConfigDialog(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);
		initComponents();		
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cmbType = new javax.swing.JComboBox();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        cmbSketch = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cmbCommPort = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Name:");
        jLabel1.setName("jLabel1"); // NOI18N

        txtName.setName("txtName"); // NOI18N

        jLabel2.setText("Board Type:");
        jLabel2.setName("jLabel2"); // NOI18N

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Structured", "Mobile Template", "Desktop Template" }));
        cmbType.setName("cmbType"); // NOI18N

        btnOk.setLabel("Ok");
        btnOk.setName("btnOk"); // NOI18N
        btnOk.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnOkMouseClicked(evt);
            }
        });
        btnOk.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setLabel("Cancel");
        btnCancel.setName("btnCancel"); // NOI18N
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                btnCancelMouseClicked(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Sketch:");
        jLabel3.setName("jLabel3"); // NOI18N

        cmbSketch.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Structured", "Mobile Template", "Desktop Template" }));
        cmbSketch.setName("cmbSketch"); // NOI18N

        jLabel4.setText("Programming Comm Port:");
        jLabel4.setName("jLabel4"); // NOI18N

        cmbCommPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Structured", "Mobile Template", "Desktop Template" }));
        cmbCommPort.setName("cmbCommPort"); // NOI18N

        jLabel5.setText("XBee Port:");
        jLabel5.setName("jLabel5"); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Serial", "Serial1", "Serial2", "Serial3", "I2C" }));
        jComboBox1.setName("jComboBox1"); // NOI18N

        jLabel6.setText("Baud Rate:");
        jLabel6.setName("jLabel6"); // NOI18N

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1200", "2400", "4800", "9600", "19200", "38400", "58600", "115200" }));
        jComboBox2.setName("jComboBox2"); // NOI18N

        jLabel10.setText("XBee Mode:");
        jLabel10.setName("jLabel10"); // NOI18N

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Router", "End Point" }));
        jComboBox3.setName("jComboBox3"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cmbSketch, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cmbCommPort, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(124, 124, 124)
                .add(btnOk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(btnCancel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(81, 81, 81)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel5)
                            .add(jLabel2)
                            .add(jLabel1))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(5, 5, 5)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 286, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(0, 0, Short.MAX_VALUE))
                                    .add(cmbType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 134, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel10)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(174, 174, 174))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 69, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbCommPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbSketch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnCancel)
                    .add(btnOk))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnOkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOkMouseClicked
	// apply the values
	if (m_nerduino != null)
	{
		m_nerduino.setName(txtName.getText());
		
		Object obj = cmbCommPort.getSelectedItem();
		
		if (obj != null)
			m_nerduino.setComPort(obj.toString());
		
		obj = cmbSketch.getSelectedItem();
		
		if (obj != null)
			m_nerduino.setSketch(obj.toString());
		
		obj = cmbSketch.getSelectedItem();
		
		if (obj != null)
			m_nerduino.setSketch(obj.toString());
	}
	
	setVisible(false);
}//GEN-LAST:event_btnOkMouseClicked

private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
	if (validateSettings() && configureXBee())
	{
		setVisible(false);
	}
}//GEN-LAST:event_btnOkActionPerformed

private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked

	m_nerduino = null;
	
	setVisible(false);
}//GEN-LAST:event_btnCancelMouseClicked

	public void setNerduinoXBee(NerduinoXBee nerduino)
	{
		m_nerduino = nerduino;
		
		if (m_nerduino != null)
		{
			txtName.setText(m_nerduino.getName());

			try
			{			
				Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
				cmbCommPort.removeAllItems();

				while (portList.hasMoreElements()) 
				{
					CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
					{
						String name = portId.getName();
						
						boolean found = false;
						
						// make sure this port is not already being used by the host, or any other nerduino
						if (FamilyXBee.Current.getComPort().equals(name))
							found = true;
						
						/*
						for(Node node : NerduinoManager.Current.getChildren().getNodes())
						{
							if (node instanceof  NerduinoUSB)
							{
								NerduinoUSB nu = (NerduinoUSB) node;
								
			FamilyXBee != m_nerduino && nu.getComPort().equals(name))
									found = true;
							}
						}
						*/
						
						if (!found)
							cmbCommPort.addItem(name);
					}
				}
				
				cmbType.removeAllItems();
				
				for(Node node : BoardManager.Current.getChildren().getNodes())
				{
					Board board = (Board) node;
					
					cmbType.addItem(board.getName());
				}
				
				cmbSketch.removeAllItems();
				
				for(Node node : SketchManager.Current.getChildren().getNodes())
				{
					Sketch sketch = (Sketch) node;
					
					cmbSketch.addItem(sketch.getName());
				}
				
			}
			catch(Exception e)
			{
			}
		}
	}
	
	boolean validateSettings()
	{
		return false;
	}
	
	boolean configureXBee()
	{
		// compile the xbee configuration sketch for the selected board
		// upload the compiled sketch to the board
		// open the serial port to the board
		// wait for the board to send a ready message
		// wait for up to 10 seconds for a response
		// when ready, send the xbee settings memory block
		// wait for a reset message
		// prompt the user to install the xbee and restart the board
		// close the serial port
		// wait for the xbee to join the network
		
		return false;
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cmbCommPort;
    private javax.swing.JComboBox cmbSketch;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}