/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.processing.app;

import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.core.PropertiesTopComponent;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author chaselaurendine
 */
public class BoardEngagePanel extends javax.swing.JPanel implements IBuildTask
{
	NerduinoBase m_nerduino;
	Sketch m_sketch;
	boolean m_success;
	
	/**
	 * Creates new form BoardUploadPanel
	 */
	public BoardEngagePanel()
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
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();

        jCheckBox1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(BoardEngagePanel.class, "BoardEngagePanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setActionCommand(org.openide.util.NbBundle.getMessage(BoardEngagePanel.class, "BoardEngagePanel.jCheckBox1.actionCommand")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BoardEngagePanel.class, "BoardEngagePanel.jLabel1.text")); // NOI18N

        jProgressBar1.setPreferredSize(new java.awt.Dimension(99, 20));

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
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 286, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jCheckBox1)
                        .add(jLabel1)))
                .add(4, 4, 4))
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
			io.getOut().print("Engaging " + jLabel1.getText());

			if (ExplorerTopComponent.Current.getSelectedNode() == m_nerduino)
			{
				PropertiesTopComponent.Current.setObject(NerduinoManager.Current);
			}
			
			String message = m_nerduino.engage(this);
						
			if (m_success)
			{
				io.getOut().println(" Complete!");
				
				// reassert the selected node in the explorer if it is the associate nerduino
				if (ExplorerTopComponent.Current.getSelectedNode() == m_nerduino)
				{
					PropertiesTopComponent.Current.setObject(m_nerduino);
				}
			}
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
		m_success = success;
		
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
