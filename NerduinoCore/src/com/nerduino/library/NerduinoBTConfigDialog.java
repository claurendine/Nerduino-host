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

import jssc.SerialPortList;
import org.openide.nodes.Node;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NerduinoBTConfigDialog extends javax.swing.JDialog
{
	public NerduinoBT m_nerduino;
	
	/** Creates new form SkitConfigDialog */
	public NerduinoBTConfigDialog(java.awt.Frame parent, boolean modal)
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
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        cmbCommPort = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Name:");
        jLabel1.setName("jLabel1"); // NOI18N

        txtName.setName("txtName"); // NOI18N

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

        jLabel4.setText("Comm Port:");
        jLabel4.setName("jLabel4"); // NOI18N

        cmbCommPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Structured", "Mobile Template", "Desktop Template" }));
        cmbCommPort.setName("cmbCommPort"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(124, 124, 124)
                .add(btnOk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(btnCancel)
                .addContainerGap(135, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(33, 33, 33)
                        .add(jLabel1))
                    .add(jLabel4))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cmbCommPort, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 286, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(39, 39, 39)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbCommPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 59, Short.MAX_VALUE)
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
	}
	
	setVisible(false);
}//GEN-LAST:event_btnOkMouseClicked

private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_btnOkActionPerformed

private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked

	m_nerduino = null;
	
	setVisible(false);
}//GEN-LAST:event_btnCancelMouseClicked

	public void setNerduinoBT(NerduinoBT nerduino)
	{
		m_nerduino = nerduino;
		
		if (m_nerduino != null)
		{
			txtName.setText(m_nerduino.getName());

			try
			{
				String[] portNames = SerialPortList.getPortNames();
				
				//Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
				cmbCommPort.removeAllItems();

				for(String name : portNames)
				{
					boolean found = false;

					// make sure this port is not already being used by the host, or any other nerduino
					if (FamilyXBee.Current.getComPort().equals(name))
						found = true;

					for(Node node : NerduinoManager.Current.getChildren().getNodes())
					{
						if (node instanceof  NerduinoBT)
						{
							NerduinoBT nu = (NerduinoBT) node;

							if (nu != m_nerduino && nu.getComPort().equals(name))
								found = true;
						}
					}

					if (!found)
						cmbCommPort.addItem(name);
				}
			}
			catch(Exception e)
			{
			}
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cmbCommPort;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
