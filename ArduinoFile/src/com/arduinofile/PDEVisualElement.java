/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@MultiViewElement.Registration(
    displayName = "#LBL_PDE_VISUAL",
iconBase = "com/arduinofile/Sketch16.png",
mimeType = "text/x-arduino",
persistenceType = TopComponent.PERSISTENCE_NEVER,
preferredID = "PDEVisual",
position = 2000)
@Messages("LBL_PDE_VISUAL=Visual")
public final class PDEVisualElement extends JPanel implements MultiViewElement
{
	private PDEDataObject obj;
	private JToolBar toolbar = new JToolBar();
	private transient MultiViewElementCallback callback;

	public PDEVisualElement(Lookup lkp)
	{
		obj = lkp.lookup(PDEDataObject.class);
		assert obj != null;
		initComponents();
	}

	@Override
	public String getName()
	{
		return "PDEVisualElement";
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	@Override
	public JComponent getVisualRepresentation()
	{
		return this;
	}

	@Override
	public JComponent getToolbarRepresentation()
	{
		return toolbar;
	}

	@Override
	public Action[] getActions()
	{
		return new Action[0];
	}

	@Override
	public Lookup getLookup()
	{
		return obj.getLookup();
	}

	@Override
	public void componentOpened()
	{
	}

	@Override
	public void componentClosed()
	{
	}

	@Override
	public void componentShowing()
	{
	}

	@Override
	public void componentHidden()
	{
	}

	@Override
	public void componentActivated()
	{
	}

	@Override
	public void componentDeactivated()
	{
	}

	@Override
	public UndoRedo getUndoRedo()
	{
		return UndoRedo.NONE;
	}

	@Override
	public void setMultiViewCallback(MultiViewElementCallback callback)
	{
		this.callback = callback;
	}

	@Override
	public CloseOperationState canCloseElement()
	{
		return CloseOperationState.STATE_OK;
	}
}
