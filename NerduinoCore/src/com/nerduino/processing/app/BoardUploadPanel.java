/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.processing.app;

import com.nerduino.library.NerduinoBase;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author chaselaurendine
 */
public class BoardUploadPanel extends javax.swing.JPanel implements IBuildTask
{
	NerduinoBase m_nerduino;
	Sketch m_sketch;
	
	/**
	 * Creates new form BoardUploadPanel
	 */
	public BoardUploadPanel()
	{
		initComponents();
	}
	
	@Override
	public void configure(Sketch sketch, NerduinoBase nerduino)
	{
		m_sketch = sketch;
		setNerduino(nerduino);
	}

	public NerduinoBase getNerduino()
	{
		return m_nerduino;
	}
	
	public void setNerduino(NerduinoBase nerduino)
	{
		if (nerduino != null)
		{
			m_nerduino = nerduino;
			
			jLabel1.setText(m_nerduino.getName());
		}
	}
	
	public boolean isSelected()
	{
		return jCheckBox1.isSelected();
	}
	
	public void setProgress(int progress)
	{
		jProgressBar1.setValue(progress);
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jCheckBox1 = new javax.swing.JCheckBox();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();

        jCheckBox1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(BoardUploadPanel.class, "BoardUploadPanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setActionCommand(org.openide.util.NbBundle.getMessage(BoardUploadPanel.class, "BoardUploadPanel.jCheckBox1.actionCommand")); // NOI18N

        jProgressBar1.setPreferredSize(new java.awt.Dimension(99, 20));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BoardUploadPanel.class, "BoardUploadPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 272, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jCheckBox1)
                .add(jLabel1))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables

	@Override
	public void execute()
	{
		if (jCheckBox1.isSelected())
		{
			InputOutput io = IOProvider.getDefault().getIO("Build", false);
			io.getOut().print("Uploading to " + jLabel1.getText());
			
			String message = m_nerduino.upload(m_sketch);				
			
			boolean success = (message == null);
			
			setSuccess(success);
			
			if (success)
				io.getOut().println(" Complete!");
			else
			{
				io.getOut().println(" Failed!");
				io.getErr().println(message);
			}
		}
	}
	
	@Override
	public void setSuccess(boolean success)
	{
		if (success)
		{
			jCheckBox1.setForeground(new java.awt.Color(102, 255, 102));
			jLabel1.setForeground(new java.awt.Color(102, 255, 102));
			jProgressBar1.setValue(100);
		}
		else
		{
			jCheckBox1.setForeground(new java.awt.Color(255, 0, 0));
			jLabel1.setForeground(new java.awt.Color(255, 0, 0));
			jProgressBar1.setValue(0);
		}
	}

	@Override
	public void reset()
	{
		jCheckBox1.setForeground(new java.awt.Color(0, 0, 0));
		jLabel1.setForeground(new java.awt.Color(0, 0, 0));
		jProgressBar1.setValue(0);
	}

}