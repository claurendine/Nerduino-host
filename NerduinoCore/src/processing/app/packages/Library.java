package processing.app.packages;

import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

import processing.app.helpers.PreferencesMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import processing.app.SourceFile;
import processing.app.SourceFolder;

import static processing.app.helpers.StringUtils.wildcardMatch;

public class Library extends TreeNode
{
	File m_file;
	private Children m_nodes;
	private String name;
	private String version;
	private String author;
	private String email;
	private String url;
	private String sentence;
	private String paragraph;
	private List<String> coreDependencies;
	private List<String> dependencies;
	private File folder, srcFolder, archFolder;
	private List<String> architectures;
	private boolean pre15Lib;
	private static final List<String> MANDATORY_PROPERTIES = Arrays
			.asList(new String[]
			{
				"architectures", "author", "core-dependencies",
				"dependencies", "email", "name", "paragraph", "sentence", "url",
				"version"
			});
	private static final List<String> OPTIONAL_FOLDERS = Arrays
			.asList(new String[]
			{
				"arch", "examples", "extras", "src"
			});
	private static final List<String> OPTIONAL_FILES = Arrays
			.asList(new String[]
			{
				"keywords.txt", "library.properties"
			});

	public Library()
	{
		super(new Children.Array(), "Library", "/com/nerduino/resources/Library16.png");
	}

	public Library(File file)
	{
		super(new Children.Array(), "Library", "/com/nerduino/resources/Library16.png");

		m_file = file;
		m_name = m_file.getName();
		m_nodes = this.getChildren();

		loadChildren();
	}

	/**
	 * Scans inside a folder and create a Library object out of it.
	 * Automatically detects pre-1.5 libraries. Automatically fills metadata
	 * from library.properties file if found.
	 *
	 * @param libFolder
	 * @return
	 */
	static public Library create(File libFolder) throws IOException
	{
		// A library is considered "new" if it contains a file called
		// "library.properties"
		File check = new File(libFolder, "library.properties");
		if (!check.exists() || !check.isFile())
		{
			return createPre15Library(libFolder);
		}
		else
		{
			return createLibrary(libFolder);
		}
	}

	private static Library createLibrary(File libFolder) throws IOException
	{
		// Parse metadata
		File propertiesFile = new File(libFolder, "library.properties");
		PreferencesMap properties = new PreferencesMap();
		properties.load(propertiesFile);

		// Library sanity checks
		// ---------------------

		// 1. Check mandatory properties
		for (String p : MANDATORY_PROPERTIES)
		{
			if (!properties.containsKey(p))
			{
				throw new IOException("Missing '" + p + "' from library");
			}
		}

		// 2. Check mandatory folders
		File srcFolder = new File(libFolder, "src");
		if (!srcFolder.exists() || !srcFolder.isDirectory())
		{
			throw new IOException("Missing 'src' folder");
		}

		// 3. check if root folder contains prohibited stuff
		for (File file : libFolder.listFiles())
		{
			if (file.isDirectory())
			{
				if (!OPTIONAL_FOLDERS.contains(file.getName()))
				{
					throw new IOException("Invalid folder '" + file.getName() + "'.");
				}
			}
			else
			{
				if (!OPTIONAL_FILES.contains(file.getName()))
				{
					throw new IOException("Invalid file '" + file.getName() + "'.");
				}
			}
		}

		// Extract metadata info
		List<String> archs = new ArrayList<String>();
		for (String arch : properties.get("architectures").split(","))
		{
			archs.add(arch.trim());
		}

		List<String> coreDeps = new ArrayList<String>();
		for (String dep : properties.get("core-dependencies").split(","))
		{
			coreDeps.add(dep.trim());
		}

		List<String> dependencies = new ArrayList<String>();
		for (String dependency : properties.get("dependencies").split(","))
		{
			dependency = dependency.trim();
			if (!dependency.equals(""))
			{
				dependencies.add(dependency);
			}
		}

		Library res = new Library();
		res.folder = libFolder;
		res.srcFolder = srcFolder;
		res.archFolder = new File(libFolder, "arch");
		res.name = properties.get("name").trim();
		res.author = properties.get("author").trim();
		res.email = properties.get("email").trim();
		res.sentence = properties.get("sentence").trim();
		res.paragraph = properties.get("paragraph").trim();
		res.url = properties.get("url").trim();
		res.architectures = archs;
		res.coreDependencies = coreDeps;
		res.dependencies = dependencies;
		res.version = properties.get("version").trim();
		res.pre15Lib = false;
		return res;
	}

	private static Library createPre15Library(File libFolder)
	{
		// construct an old style library
		Library res = new Library();
		res.folder = libFolder;
		res.srcFolder = libFolder;
		res.name = libFolder.getName();
		res.architectures = Arrays.asList(new String[]
				{
					"*"
				});
		res.pre15Lib = true;
		return res;
	}

	public List<File> getSrcFolders(String reqArch)
	{
		if (!supportsArchitecture(reqArch))
		{
			return null;
		}
		List<File> res = new ArrayList<File>();
		res.add(srcFolder);
		File archSpecificFolder = new File(archFolder, reqArch);
		if (archSpecificFolder.exists() && archSpecificFolder.isDirectory())
		{
			res.add(archSpecificFolder);
		}
		else
		{
			// If specific architecture folder is not found try with "default"
			archSpecificFolder = new File(archFolder, "default");
			if (archSpecificFolder.exists() && archSpecificFolder.isDirectory())
			{
				res.add(archSpecificFolder);
			}
		}
		return res;
	}

	public boolean supportsArchitecture(String reqArch)
	{
		for (String arch : architectures)
		{
			if (wildcardMatch(reqArch, arch))
			{
				return true;
			}
		}
		return false;
	}
	public static final Comparator<Library> CASE_INSENSITIVE_ORDER = new Comparator<Library>()
	{
		@Override
		public int compare(Library o1, Library o2)
		{
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};

	public File getSrcFolder()
	{
		return srcFolder;
	}

	public String getName()
	{
		return name;
	}

	public boolean isPre15Lib()
	{
		return pre15Lib;
	}

	public File getFolder()
	{
		return folder;
	}

	public List<String> getArchitectures()
	{
		return architectures;
	}

	public String getAuthor()
	{
		return author;
	}

	public List<String> getCoreDependencies()
	{
		return coreDependencies;
	}

	public List<String> getDependencies()
	{
		return dependencies;
	}

	public String getEmail()
	{
		return email;
	}

	public String getParagraph()
	{
		return paragraph;
	}

	public String getSentence()
	{
		return sentence;
	}

	public String getUrl()
	{
		return url;
	}

	public String getVersion()
	{
		return version;
	}

	@Override
	public String toString()
	{
		String res = "Library:";
		res += " (name=" + name + ")";
		res += " (architectures=" + architectures + ")";
		res += " (author=" + author + ")";
		res += " (core-dependencies=" + coreDependencies + ")";
		res += " (dependencies=" + dependencies + ")";
		res += " (email=" + email + ")";
		res += " (paragraph=" + paragraph + ")";
		res += " (sentence=" + sentence + ")";
		res += " (url=" + url + ")";
		res += " (version=" + version + ")";
		return res;
	}

	void loadChildren()
	{
		File[] files = m_file.listFiles();

		for (File file : files)
		{
			if (file.isDirectory())
			{
				SourceFolder sf = new SourceFolder(file);

				addNode(sf);
			}
			else
			{
				SourceFile sf = new SourceFile(file);

				addNode(sf);
			}
		}
	}

	public String getFilePath()
	{
		return m_file.getPath();
	}

	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set sheet = Sheet.createPropertiesSet();

		sheet.setDisplayName("Library Information");

		addProperty(sheet, String.class, null, "Name", "Name");

		return new Node.PropertySet[]
				{
					sheet
				};
	}

	public void addNode(Node node)
	{
		if (node != null && !contains(node))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = node;

			m_nodes.add(nodes);
		}
	}

	public boolean contains(Node node)
	{
		for (int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			if (node == m_nodes.getNodeAt(i))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
				{
					new Library.CreateFileAction(getLookup()),
					new Library.CreateFolderAction(getLookup()),
					new Library.DeleteAction(getLookup()),
				};
	}

	public final class CreateFileAction extends AbstractAction
	{
		private Library node;

		public CreateFileAction(Lookup lookup)
		{
			node = lookup.lookup(Library.class);

			putValue(AbstractAction.NAME, "Create File");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createFile();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}

	public final class CreateFolderAction extends AbstractAction
	{
		private Library node;

		public CreateFolderAction(Lookup lookup)
		{
			node = lookup.lookup(Library.class);

			putValue(AbstractAction.NAME, "Create Folder");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createFolder();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}

	public final class DeleteAction extends AbstractAction
	{
		private Library node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(Library.class);

			putValue(AbstractAction.NAME, "Delete Library");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.delete();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}

	public void delete()
	{
		if (m_file.listFiles().length > 0)
		{
			JOptionPane.showMessageDialog(null, "The Library folder must be empty before it can be deleted!");
			return;
		}

		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this Library?", "Delete Library", JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION)
		{
			try
			{
				m_file.delete();

				destroy();
			}
			catch(IOException ex)
			{
			}
		}
	}

	public void createFile()
	{
		try
		{
			// prompt for the new file name
			String filename = JOptionPane.showInputDialog(null, "New File Name", "File");

			if (filename.isEmpty())
			{
				return;
			}

			File newfile = new File(getFilePath() + "/" + filename);

			if (newfile.exists())
			{
				JOptionPane.showMessageDialog(null, "This file already exists!");
				return;
			}

			newfile.createNewFile();

			SourceFile sf = new SourceFile(newfile);

			addNode(sf);

			sf.select();
		}
		catch(IOException ex)
		{
		}
	}

	public void createFolder()
	{
		// prompt for the new folder name
		String foldername = JOptionPane.showInputDialog(null, "New Folder Name", "Folder");

		if (foldername.isEmpty())
		{
			return;
		}

		File newfolder = new File(getFilePath() + "/" + foldername);

		if (newfolder.exists())
		{
			JOptionPane.showMessageDialog(null, "This file already exists!");
			return;
		}

		newfolder.mkdir();

		SourceFolder sf = new SourceFolder(newfolder);

		addNode(sf);

		sf.select();
	}
}
