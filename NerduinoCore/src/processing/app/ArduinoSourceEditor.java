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

package processing.app;

import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import com.nerduino.scrolls.Scroll;
import com.nerduino.services.NerduinoService;
import com.nerduino.skits.Skit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListDataListener;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.StatusDisplayer;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class ArduinoSourceEditor extends CloneableEditor implements MultiViewElement
{
	static ArduinoSourceEditor currentSourceEditor;
	
	private transient final Lookup lookup;
	private transient JComponent toolbar;
	private transient MultiViewElementCallback callback;
	private JLabel tooltipLabel;
	Object[] m_nerds;
	NerduinoBase m_targetNerd;
	JComboBox<Object> m_targetList;
	boolean m_busy;
	ArduinoSourceEditor m_editor;
	
	Sketch m_sketch;
	String m_displayName = "";
	Skit m_skit;
	Scroll m_scroll;
	NerduinoService m_service;

	ArduinoSourceEditor(Lookup lookup)
	{
		super(lookup.lookup(CloneableEditorSupport.class));
		this.lookup = lookup;
		m_editor = this;
	}

	@Override
	public JComponent getVisualRepresentation()
	{
		return this;
	}
	
	void updateTargets()
	{
		if (m_sketch != null)
			m_nerds = NerduinoManager.Current.getNerduinos(m_sketch);
	}
	
	@Override
	public JComponent getToolbarRepresentation()
	{
		if (toolbar == null)
		{
			this.pane.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e)
				{					
				}

				@Override
				public void keyPressed(KeyEvent e)
				{
				}

				@Override
				public void keyReleased(KeyEvent e)
				{
					if (m_sketch != null && !m_sketch.isDirty())
					{
						// check for a change in the source.. mark as dirty if a change is found
						String source = getText();
						String orig = m_sketch.getCode(0).getProgram();
						
						if (source.compareTo(orig) != 0)
							setIsDirty(true);
					}
				}
			});
			
			// remove the source button in the toolstrip
			JComponent comp =  (JComponent) this.getParent().getParent();
			JToolBar tb = (JToolBar) comp.getComponent(0);
			JComponent sourceButton = (JComponent) tb.getComponent(0);
			sourceButton.setVisible(false);

			//attempt to create own toolbar?
			toolbar = new JPanel();

			FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
			layout.setHgap(0);

			toolbar.setLayout(layout);

			m_targetList = new JComboBox<Object>()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					super.actionPerformed(e); //To change body of generated methods, choose Tools | Templates.
				}
			};
			
			updateTargets();
			
			m_targetList.setModel(new ComboBoxModel<Object>()
			{
				@Override
				public void setSelectedItem(Object anItem)
				{
					m_targetNerd = (NerduinoBase) anItem;
				}
				
				@Override
				public Object getSelectedItem()
				{
					return m_targetNerd;
				}
				
				@Override
				public int getSize()
				{
					if (m_nerds != null)
						return m_nerds.length;
					
					return 0;
				}
				
				@Override
				public Object getElementAt(int index)
				{
					if (m_nerds != null)
						return m_nerds[index];

					return null;
				}
				
				@Override
				public void addListDataListener(ListDataListener l)
				{
				}
				
				@Override
				public void removeListDataListener(ListDataListener l)
				{
				}
			});
			
			m_targetList.setMinimumSize(new Dimension(150, 22));
			m_targetList.setPreferredSize(new Dimension(150, 22));
			
			toolbar.add(m_targetList);
			
			JButton verifyButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/Verify24.png")));
			verifyButton.setBorderPainted(false);
			verifyButton.setRolloverEnabled(true);
			verifyButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/Verify24Rollover.png")));
			
			verifyButton.addMouseListener(new java.awt.event.MouseAdapter()
			{
				@Override
				public void mouseClicked(java.awt.event.MouseEvent evt)
				{
					verifyClicked(evt);
				}
				
				@Override
				public void mouseEntered(MouseEvent e)
				{
					super.mouseEntered(e); 
					
					if (m_targetNerd != null)
						tooltipLabel.setText("Verify for " + m_targetNerd.getBoardType());						
					else
						tooltipLabel.setText("Verify");
				}
				
				@Override
				public void mouseExited(MouseEvent e)
				{
					super.mouseExited(e); 
					tooltipLabel.setText("");
				}
			});
			
			toolbar.add(verifyButton);
			
			JButton uploadButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/Upload24.png")));
			uploadButton.setBorderPainted(false);
			uploadButton.setRolloverEnabled(true);
			uploadButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/Upload24Rollover.png")));
			
			uploadButton.addMouseListener(new java.awt.event.MouseAdapter()
			{
				@Override
				public void mouseClicked(java.awt.event.MouseEvent evt)
				{
					uploadClicked(evt);
				}
				
				@Override
				public void mouseEntered(MouseEvent e)
				{
					super.mouseEntered(e); 
					
					if (m_targetNerd != null)
						tooltipLabel.setText("Upload to " + m_targetNerd.getName());						
					else
						tooltipLabel.setText("Upload");
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					super.mouseExited(e); 
					tooltipLabel.setText("");
				}
			});

			toolbar.add(uploadButton);

			JButton uploadAllButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/UploadAll24.png")));
			uploadAllButton.setBorderPainted(false);
			uploadAllButton.setRolloverEnabled(true);
			uploadAllButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/UploadAll24Rollover.png")));

			uploadAllButton.addMouseListener(new java.awt.event.MouseAdapter()
			{
				@Override
				public void mouseClicked(java.awt.event.MouseEvent evt)
				{
					uploadAllClicked(evt);
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					super.mouseEntered(e); 
					tooltipLabel.setText("Upload To All");
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					super.mouseExited(e); 
					tooltipLabel.setText("");
				}
			});
			
			toolbar.add(uploadAllButton);
			
			tooltipLabel = new JLabel("");
			
			toolbar.add(tooltipLabel);
		}
		
		return toolbar;
	}
	
	void targetClicked(java.awt.event.MouseEvent evt)
	{
	}
	
	void verifyClicked(java.awt.event.MouseEvent evt)
	{
		compile(m_targetNerd);
	}

	void compileBlocking(NerduinoBase target)
	{
		InputOutput io = IOProvider.getDefault().getIO("Build", false);
		io.select();

		if (target == null)
		{
			io.getOut().println("Target not specified!");
			return;
		}
		
		Preferences.set("target", "arduino");
		
		Board board = BoardManager.Current.getBoard(m_targetNerd.getBoardType());
		
		if (board == null)
		{
			io.getOut().println("The Target device does not specify a board type!");
			return;
		}
		
		Preferences.set("board", board.getShortName());

		String mcu = board.getBuildMCU();
		
		if (mcu != null)
			Preferences.set("build.mcu", mcu);

		Preferences.set("upload.speed", Integer.toString(board.getUploadSpeed()));

		Sketch sketch = SketchManager.Current.getSketch(m_targetNerd.getSketch());
		
		if (sketch != null)
			sketch.compile();
	}
	
	NerduinoBase m_tempTarget;
	void compile(NerduinoBase target)
	{		
		if (!m_busy)
		{	
			InputOutput io = IOProvider.getDefault().getIO("Build", false);
			io.select();

			try
			{
				io.getOut().reset();
			}
			catch(IOException ex)
			{
			}
			
			m_tempTarget = target;
			
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					m_busy = true;
					
					StatusDisplayer.getDefault().setStatusText("Compiling " + m_tempTarget.getSketch() + " ...");
					
					compileBlocking(m_tempTarget);
					
					m_busy = false;
				}
			});

			thread.start();
		}
	}
	
	void uploadBlocking(NerduinoBase target)
	{
		if (!m_sketch.hasError()) // don't bother uploading if compiling failed
		{			
			InputOutput io = IOProvider.getDefault().getIO("Build", false);
			
			io.getOut().print("Uploading to " + target.getName());
			
			String message = target.upload(m_sketch);				
			
			if (message == null)
			{
				io.getOut().println(" Complete!");
				
				target.engage();
			}
			else
			{
				io.getOut().println(" Failed!");
				io.getErr().println(message);
			}
		}		
	}
	
	void uploadClicked(java.awt.event.MouseEvent evt)
	{
		if (!m_busy)
		{
			InputOutput io = IOProvider.getDefault().getIO("Build", false);
			io.select();

			try
			{
				io.getOut().reset();
			}
			catch(IOException ex)
			{
			}

			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					m_busy = true;

					StatusDisplayer.getDefault().setStatusText("Compiling " + m_targetNerd.getSketch() + " ...");

					compileBlocking(m_targetNerd);
					
					StatusDisplayer.getDefault().setStatusText("Uploading to  " + m_targetNerd.getName() + " ...");

					uploadBlocking(m_targetNerd);
					
					m_busy = false;
				}
			});

			thread.start();
		}
	}
	
	void uploadAllClicked(java.awt.event.MouseEvent evt)
	{
		if (!m_busy)
		{
			InputOutput io = IOProvider.getDefault().getIO("Build", false);
			io.select();
			
			try
			{
				io.getOut().reset();
			}
			catch(IOException ex)
			{
			}
			
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					m_busy = true;

					for(Object nerd : m_nerds)
					{
						NerduinoBase target = (NerduinoBase) nerd;
						
						StatusDisplayer.getDefault().setStatusText("Compiling " + target.getSketch() + " ...");

						compileBlocking(target);
						
						StatusDisplayer.getDefault().setStatusText("Uploading to  " + target.getName() + " ...");						
						
						uploadBlocking(target);
					}

					m_busy = false;
				}
			});

			thread.start();
		}
	}
	
	@Override
	public void setMultiViewCallback(MultiViewElementCallback callback)
	{
		this.callback = callback;
	}

	@Override
	public void componentActivated()
	{		
		currentSourceEditor = this;
		
		super.componentActivated();
		
		callback.getTopComponent().setDisplayName(m_displayName);
		
		if (m_sketch != null)
			setTarget(m_sketch.getTarget());
		
		if (m_targetNerd != null)
		{
			ExplorerTopComponent.Current.setSelectedNode(m_targetNerd);
		}
	}
	
	public boolean getIsDirty()
	{
		return m_sketch.isDirty();
	}
	
	private void setIsDirty(boolean value)
	{
		if (value && m_sketch != null)
			m_sketch.setIsDirty(true);
	}

	@Override
	public void componentClosed()
	{
		super.componentClosed();
	}
	
	@Override
	public void componentDeactivated()
	{
		super.componentDeactivated();
	}
	
	@Override
	public void componentHidden()
	{
		super.componentHidden();
	}

	@Override
	public void componentOpened()
	{
		super.componentOpened();
	}

	@Override
	public void componentShowing()
	{
		super.componentShowing();
	}
	
	@Override
	public void doLayout()
	{
		super.doLayout();
		
		Component[] components = this.getComponents();
		
		if (components.length > 1)
			components[1].setVisible(false);
	}

	@Override
	public org.openide.util.Lookup getLookup()
	{
		return lookup;
	}

	@Override
	public void requestVisible()
	{
		if (callback != null)
			callback.requestVisible();
		else
			super.requestVisible();
	}

	@Override
	public void requestActive()
	{
		if (callback != null)
			callback.requestActive();
		else
			super.requestActive();
	}
	
	@Override
	public void open()
	{
		if (callback != null)
			callback.requestVisible();
		else
			super.open();
	}
	
	public String getText()
	{
		return this.pane.getText();
	}
	
	public NerduinoBase getTarget()
	{
		return m_targetNerd;
	}
	
	public void setTarget(NerduinoBase nerduino)
	{
		m_targetList.setSelectedItem(nerduino);
	}

	@Override
	public CloseOperationState canCloseElement()
	{
		return CloseOperationState.STATE_OK;
	}
}
