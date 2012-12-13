/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages(
{
	"LBL_PDE_LOADER=Files of PDE"
})
@MIMEResolver.ExtensionRegistration(
    displayName = "#LBL_PDE_LOADER",
mimeType = "text/x-arduino",
extension =
{
	".pde", ".PDE", ".ino", ".INO", ".h", ".H", ".cpp", "CPP", ".c", ".C"
})
@DataObject.Registration(
    mimeType = "text/x-arduino",
iconBase = "com/arduinofile/Sketch16.png",
displayName = "#LBL_PDE_LOADER",
position = 300)
@ActionReferences(
{
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "System", id = "org.openide.actions.OpenAction"),
	position = 100,
	separatorAfter = 200),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
	position = 300),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
	position = 400,
	separatorAfter = 500),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
	position = 600),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "System", id = "org.openide.actions.RenameAction"),
	position = 700,
	separatorAfter = 800),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
	position = 900,
	separatorAfter = 1000),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
	position = 1100,
	separatorAfter = 1200),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
	position = 1300),
	@ActionReference(
        path = "Loaders/text/x-arduino/Actions",
	id =
	@ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
	position = 1400)
})
public class PDEDataObject extends MultiDataObject
{
	public PDEDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException
	{
		super(pf, loader);
		registerEditor("text/x-arduino", true);
	}

	@Override
	protected int associateLookup()
	{
		return 1;
	}

	@MultiViewElement.Registration(
        displayName = "#LBL_PDE_EDITOR",
	iconBase = "com/arduinofile/Sketch16.png",
	mimeType = "text/x-arduino",
	persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
	preferredID = "PDE",
	position = 1000)
	@Messages("LBL_PDE_EDITOR=Source")
	public static MultiViewEditorElement createEditor(Lookup lkp)
	{
		return new MultiViewEditorElement(lkp);
	}
}
