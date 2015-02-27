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

import com.nerduino.library.NerduinoBase;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class CompileCommand extends java.awt.Panel implements StatusUpdateEventListener //ICompileCallback, IBuildTask
{
	NerduinoBase m_nerduino;
	
	boolean m_success = false;
	CompileCommand m_command;
	boolean m_busy = false;
	
	public CompileCommand(NerduinoBase nerduino)
	{
		initComponents();
		
		m_nerduino = nerduino;
		m_command = this;
		
		m_nerduino.addStatusUpdateEventListener(this);
		
		Sketch sketch = SketchManager.Current.getSketch(m_nerduino.getSketch());
		
		if (sketch != null)
		{
			sketch.addStatusUpdateEventListener(this);
			
			if (sketch.isDirty())
				setIcon(compileButton, "/com/nerduino/resources/CheckDirty16.png");
			else
				setIcon(compileButton, "/com/nerduino/resources/Check16.png");				
		}
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		jProgressBar1.setSize(this.getWidth(), 18);
	}
	
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        compileButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();
        engageButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        setMaximumSize(new java.awt.Dimension(2147483647, 22));
        setMinimumSize(new java.awt.Dimension(16, 22));
        setPreferredSize(new java.awt.Dimension(60, 22));
        setSize(new java.awt.Dimension(60, 22));
        setLayout(null);

        compileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/Check16.png"))); // NOI18N
        compileButton.setToolTipText(org.openide.util.NbBundle.getMessage(CompileCommand.class, "CompileCommand.compileButton.toolTipText")); // NOI18N
        compileButton.setBorderPainted(false);
        compileButton.setContentAreaFilled(false);
        compileButton.setIconTextGap(0);
        compileButton.setLabel(org.openide.util.NbBundle.getMessage(CompileCommand.class, "CompileCommand.compileButton.label")); // NOI18N
        compileButton.setMaximumSize(new java.awt.Dimension(18, 18));
        compileButton.setMinimumSize(new java.awt.Dimension(18, 18));
        compileButton.setPreferredSize(new java.awt.Dimension(18, 18));
        compileButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OnCompileAction(evt);
            }
        });
        add(compileButton);
        compileButton.setBounds(0, 0, 20, 18);

        uploadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/Upload16.png"))); // NOI18N
        uploadButton.setToolTipText(org.openide.util.NbBundle.getMessage(CompileCommand.class, "CompileCommand.uploadButton.toolTipText")); // NOI18N
        uploadButton.setBorderPainted(false);
        uploadButton.setContentAreaFilled(false);
        uploadButton.setIconTextGap(0);
        uploadButton.setLabel(org.openide.util.NbBundle.getMessage(CompileCommand.class, "CompileCommand.uploadButton.label")); // NOI18N
        uploadButton.setMaximumSize(new java.awt.Dimension(18, 18));
        uploadButton.setMinimumSize(new java.awt.Dimension(18, 18));
        uploadButton.setPreferredSize(new java.awt.Dimension(18, 18));
        uploadButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OnUploadAction(evt);
            }
        });
        add(uploadButton);
        uploadButton.setBounds(20, 0, 20, 18);

        engageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/nerduino/resources/EngageUnknown16.png"))); // NOI18N
        engageButton.setToolTipText(org.openide.util.NbBundle.getMessage(CompileCommand.class, "CompileCommand.engageButton.toolTipText")); // NOI18N
        engageButton.setBorderPainted(false);
        engageButton.setBounds(new java.awt.Rectangle(40, 1, 20, 18));
        engageButton.setContentAreaFilled(false);
        engageButton.setIconTextGap(0);
        engageButton.setLabel(org.openide.util.NbBundle.getMessage(CompileCommand.class, "CompileCommand.engageButton.label")); // NOI18N
        engageButton.setMaximumSize(new java.awt.Dimension(18, 18));
        engageButton.setMinimumSize(new java.awt.Dimension(18, 18));
        engageButton.setPreferredSize(new java.awt.Dimension(18, 18));
        engageButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OnEngageAction(evt);
            }
        });
        add(engageButton);
        engageButton.setBounds(40, 0, 20, 18);

        jProgressBar1.setBorder(null);
        jProgressBar1.setBorderPainted(false);
        add(jProgressBar1);
        jProgressBar1.setBounds(0, 0, 60, 20);
    }// </editor-fold>//GEN-END:initComponents

    private void OnCompileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OnCompileAction
    {//GEN-HEADEREND:event_OnCompileAction
		if (!m_busy)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					m_success = false;
					m_busy = true;

					setIcon(compileButton, "/com/nerduino/resources/Check16.png");
					setIcon(uploadButton, "/com/nerduino/resources/UploadUnknown16.png");
					setIcon(engageButton, "/com/nerduino/resources/EngageUnknown16.png");
					
					Preferences.set("target", "arduino");

					Board board = BoardManager.Current.getBoard(m_nerduino.getBoardType());

					Preferences.set("board", board.getShortName());
					Preferences.set("build.mcu", board.getBuildMCU());
					Preferences.set("upload.speed", Integer.toString(board.getUploadSpeed()));

					Sketch sketch = SketchManager.Current.getSketch(m_nerduino.getSketch());

					sketch.compile();
					
					/*
					setProgress(0);
					
					if (!m_success)
					{
						setStatus("Compile Error!");
						setCompileSuccess(false);
					}
					else
					{
						setCompileSuccess(true);
					}
					*/
					
					m_busy = false;
				}
			});

			thread.start();
		}
    }//GEN-LAST:event_OnCompileAction

    private void OnUploadAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OnUploadAction
    {//GEN-HEADEREND:event_OnUploadAction
		if (!m_busy)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					m_busy = true;

					InputOutput io = IOProvider.getDefault().getIO("Build", false);
					io.select();

					try
					{
						io.getOut().reset();
					}
					catch(IOException ex)
					{
					}

					m_success = false;
					
					setIcon(compileButton, "/com/nerduino/resources/Check16.png");
					setIcon(uploadButton, "/com/nerduino/resources/UploadUnknown16.png");
					setIcon(engageButton, "/com/nerduino/resources/EngageUnknown16.png");

					Preferences.set("target", "arduino");

					Board board = BoardManager.Current.getBoard(m_nerduino.getBoardType());

					Preferences.set("board", board.getShortName());
					Preferences.set("build.mcu", board.getBuildMCU());
					Preferences.set("upload.speed", Integer.toString(board.getUploadSpeed()));

					Sketch sketch = SketchManager.Current.getSketch(m_nerduino.getSketch());

					sketch.compile();

					if (m_success)
					{
						//setIcon(compileButton, "/com/nerduino/resources/CheckSucceed16.png");
						
						//setProgress(0);
						
						io.getOut().print("Uploading to " + m_nerduino.getName());
						
						String result = m_nerduino.upload(sketch);				
						
						setProgress(0);
						
						m_busy = false;
						
						if (result == null)
							OnEngageAction(null);
					}
					
					setProgress(0);
					
					m_busy = false;
					
				}
			});

			thread.start();
		}
    }//GEN-LAST:event_OnUploadAction

    private void OnEngageAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OnEngageAction
    {//GEN-HEADEREND:event_OnEngageAction
		if (!m_busy)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					m_busy = true;
					
					InputOutput io = IOProvider.getDefault().getIO("Build", false);
					io.select();
					
//					try
//					{
//						io.getOut().reset();
//					}
//					catch(IOException ex)
//					{
//					}
					
					setIcon(engageButton, "/com/nerduino/resources/EngageUnknown16.png");
					
					io.getOut().print("Engaging " + m_nerduino.getName() + " ");
					
					m_nerduino.engage();
					
					m_busy = false;
				}
			});
			
			thread.start();
		}
    }//GEN-LAST:event_OnEngageAction

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton compileButton;
    private javax.swing.JButton engageButton;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JButton uploadButton;
    // End of variables declaration//GEN-END:variables

	void setProgress(int progress)
	{
		jProgressBar1.setValue(progress);
		jProgressBar1.paintImmediately(0, 0, 60, 20);
	}
	
	void setIcon(JButton button, String path)
	{
		java.net.URL imgURL = getClass().getResource(path);
		
		if (imgURL != null)
			button.setIcon(new ImageIcon(imgURL));
	}

	@Override
	public void handleStatusUpdateEvent(StatusUpdateEventClass e)
	{
		InputOutput io = IOProvider.getDefault().getIO("Build", false);
		io.select();

		/*
		try
		{
			io.getOut().reset();
		}
		catch(IOException ex)
		{
		}
		*/
		
		switch(e.statusType)
		{
			case 0: // compile
				if (e.pending)
				{
					setIcon(compileButton, "/com/nerduino/resources/Check16.png");
					setIcon(uploadButton, "/com/nerduino/resources/Upload16.png");
				}
				else
				{
					if (e.succeeded)
					{
						m_success = true;
						
						setIcon(compileButton, "/com/nerduino/resources/CheckSucceed16.png");						
					}
					else
						setIcon(compileButton, "/com/nerduino/resources/CheckFailed16.png");
				}
				
				break;
			case 1: // upload
				if (e.pending)
				{
					setIcon(uploadButton, "/com/nerduino/resources/Upload16.png");
				}
				else
				{
					if (e.succeeded)
					{
						setIcon(uploadButton, "/com/nerduino/resources/UploadSucceed16.png");
						io.getOut().println(" Complete!");
					}
					else
					{
						setIcon(uploadButton, "/com/nerduino/resources/UploadFailed16.png");
						io.getOut().println(" Failed!");
						io.getOut().println(e.error);
					}
				}
				
				break;
			case 2: // engage
				if (e.pending)
				{
					setIcon(engageButton, "/com/nerduino/resources/EngageUnknown16.png");
				}
				else
				{	
					if (e.succeeded)
					{
						setIcon(engageButton, "/com/nerduino/resources/EngageSucceed16.png");
						io.getOut().println("Complete!");
					}
					else
					{
						setIcon(engageButton, "/com/nerduino/resources/EngageFailed16.png");
						io.getOut().println("Failed!");
						io.getOut().println(e.error);
					}
				}
				
				break;
			case 3: // dirty
				setIcon(compileButton, "/com/nerduino/resources/CheckDirty16.png");				
				break;
		}
		
		setProgress(e.percentComplete);
	}

}