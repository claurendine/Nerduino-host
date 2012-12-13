package com.nerduino.services;

public class ServiceConfigDialog extends javax.swing.JDialog
{
	public NerduinoService m_service;
	
	/** Creates new form SkitConfigDialog */
	public ServiceConfigDialog(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);
		initComponents();		
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(40, 40, 40)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 292, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(124, 124, 124)
                .add(btnOk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(btnCancel)
                .addContainerGap(135, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 24, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnCancel)
                    .add(btnOk))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnOkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOkMouseClicked
	// apply the values
	if (m_service != null)
	{
		m_service.setName(txtName.getText());
	}
	
	setVisible(false);
}//GEN-LAST:event_btnOkMouseClicked

private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_btnOkActionPerformed

private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked

	m_service = null;
	
	setVisible(false);
}//GEN-LAST:event_btnCancelMouseClicked

	public void setService(NerduinoService service)
	{
		m_service = service;
		
		if (m_service != null)
		{
			txtName.setText(m_service.getName());
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}