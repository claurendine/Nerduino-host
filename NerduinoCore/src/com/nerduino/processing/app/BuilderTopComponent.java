/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.processing.app;

import com.nerduino.core.OutputTopComponent;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//com.nerduino.processing.app//Builder//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "BuilderTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
@ActionID(category = "Window", id = "com.nerduino.processing.app.BuilderTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_BuilderAction",
preferredID = "BuilderTopComponent")
@Messages(
{
	"CTL_BuilderAction=Builder",
	"CTL_BuilderTopComponent=Builder",
	"HINT_BuilderTopComponent=This is the Builder window"
})
public final class BuilderTopComponent extends TopComponent
{
	boolean m_building;
	public static BuilderTopComponent Current;
	ArrayList<BoardCompilePanel> m_boards = new ArrayList<BoardCompilePanel>()
	{
	};

	public BuilderTopComponent()
	{
		initComponents();
		setName(Bundle.CTL_BuilderTopComponent());
		setToolTipText(Bundle.HINT_BuilderTopComponent());

		Current = this;
	}

	public void setSketch(Sketch sketch)
	{
		m_boards.clear();

		if (sketch != null)
		{
			// loop through each nerduino in search for ones that reference this sketch
			for (Node node : NerduinoManager.Current.getChildren().getNodes())
			{
				NerduinoBase nerd = (NerduinoBase) node;

				String sketchname = nerd.getSketch();

				if (sketchname != null && sketchname.matches(sketch.getName()))
				{
					String boardtype = nerd.getBoardType();
					boolean found = false;

					for (BoardCompilePanel board : m_boards)
					{
						if (board.getBoard().getName().matches(boardtype))
						{
							found = true;

							board.addUpload(nerd);
							break;
						}
					}

					if (!found)
					{
						Board board = BoardManager.Current.getBoard(nerd.getBoardType());

						BoardCompilePanel boardpanel = new BoardCompilePanel(sketch, board);

						boardpanel.addUpload(nerd);

						m_boards.add(boardpanel);
					}
				}
			}
		}

		jPanel1.removeAll();

		// add panels to the dialog
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));

		// if no boards are associated with this sketch, then add a note
		if (m_boards == null || m_boards.isEmpty())
		{
			JLabel label = new JLabel();

			label.setText("    No devices are available that reference this sketch!");

			jPanel1.add(label);

			Board board = BoardManager.Current.getBoard("Arduino Uno");

			BoardCompilePanel boardpanel = new BoardCompilePanel(sketch, board);

			m_boards.add(boardpanel);
		}

		for (BoardCompilePanel compile : m_boards)
		{
			compile.setMinimumSize(new Dimension(600, 20));
			compile.setPreferredSize(new Dimension(600, 20));
			compile.setMaximumSize(new Dimension(600, 20));

			compile.setAlignmentX(0.0f);

			jPanel1.add(compile);

			for (JPanel task : compile.getTasks())
			{
				task.setMinimumSize(new Dimension(600, 20));
				task.setPreferredSize(new Dimension(600, 20));
				task.setMaximumSize(new Dimension(600, 20));

				task.setAlignmentX(0.0f);

				jPanel1.add(task);
			}
		}

		jPanel1.validate();
		jPanel1.repaint();
	}

	public void setNerduino(NerduinoBase nerduino)
	{
		m_boards.clear();

		String sketchName = nerduino.getSketch();
		Sketch sketch = SketchManager.Current.getSketch(sketchName);
		Board board = null;

		if (sketch != null)
		{
			board = BoardManager.Current.getBoard(nerduino.getBoardType());

			if (board != null)
			{
				BoardCompilePanel boardpanel = new BoardCompilePanel(sketch, board);

				boardpanel.addUpload(nerduino);

				m_boards.add(boardpanel);
			}
		}

		jPanel1.removeAll();

		// add panels to the dialog
		jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));

		// if no boards are associated with this sketch, then add a note
		if (m_boards.isEmpty())
		{
			if (sketch == null)
			{
				JLabel label = new JLabel();

				label.setText("    No associated sketch is available for " + nerduino.getDisplayName() + "!");

				jPanel1.add(label);
			}

			if (board == null)
			{
				JLabel label = new JLabel();

				label.setText("    The device type has not been specified for " + nerduino.getDisplayName() + "!");

				jPanel1.add(label);
			}
		}
		else
		{
			for (BoardCompilePanel compile : m_boards)
			{
				compile.setMinimumSize(new Dimension(600, 20));
				compile.setPreferredSize(new Dimension(600, 20));
				compile.setMaximumSize(new Dimension(600, 20));

				compile.setAlignmentX(0.0f);

				jPanel1.add(compile);

				for (JPanel task : compile.getTasks())
				{
					task.setMinimumSize(new Dimension(600, 20));
					task.setPreferredSize(new Dimension(600, 20));
					task.setMaximumSize(new Dimension(600, 20));

					task.setAlignmentX(0.0f);

					jPanel1.add(task);
				}
			}
		}

		jPanel1.validate();
		jPanel1.repaint();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(BuilderTopComponent.class, "BuilderTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onBuild(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 253, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 274, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void onBuild(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBuild
    {//GEN-HEADEREND:event_onBuild
		if (m_building)
		{
			return;
		}

		m_building = true;

		// reset the progress and status of all of the build tasks
		for (BoardCompilePanel compile : m_boards)
		{
			compile.reset();
		}
		
		// make sure that the output topcomponent is opened
		if (OutputTopComponent.Current == null)
		{
			new OutputTopComponent();
		}

		if (!OutputTopComponent.Current.isOpened())
		{
			OutputTopComponent.Current.open();
		}

		InputOutput io = IOProvider.getDefault().getIO("Build", false);
		io.select();

		try
		{
			io.getOut().reset();
		}
		catch(IOException ex)
		{
			//Exceptions.printStackTrace(ex);
		}

		// make sure that the sketchsource has been saved


		// Traverse through the build/upload tasks
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					for (BoardCompilePanel compile : m_boards)
					{
						if (compile.isSelected())
						{
							InputOutput io = IOProvider.getDefault().getIO("Build", false);
							io.getOut().println(I18n.format("Compiling {0} for {1}:", compile.m_sketch.getName(), compile.m_board.getName()));

							compile.compile();

							int errors = compile.getErrorCount();

							if (errors > 0)
							{
								io.getOut().println(I18n.format("Compile failed with {0} errors", errors));
								compile.setCompileSuccess(false);
								compile.setProgress(0);
								break;
							}
							else
							{
								io.getOut().println(I18n.format("Compiled succesfully"));
								compile.setCompileSuccess(true);
							}
						}
					}
				}
				catch(Exception ex)
				{
				}

				m_building = false;
			}
		});

		thread.start();

    }//GEN-LAST:event_onBuild
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

	@Override
	public void componentOpened()
	{
		// TODO add custom code on component opening
	}

	@Override
	public void open()
	{
		Mode m = WindowManager.getDefault().findMode("navigator");

		if (m != null)
		{
			m.dockInto(this);
		}

		super.open();

		super.toFront();
	}

	@Override
	public void componentClosed()
	{
		// TODO add custom code on component closing
	}

	@Override
	public void componentHidden()
	{
		super.componentHidden();
	}

	@Override
	public void componentActivated()
	{
		super.componentActivated();
	}

	@Override
	public void componentDeactivated()
	{
		super.componentDeactivated();
	}

	@Override
	public void componentShowing()
	{
		super.componentShowing();
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
}
