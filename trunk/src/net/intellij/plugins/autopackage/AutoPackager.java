package net.intellij.plugins.autopackage;

import com.intellij.javaee.artifact.JavaeeArtifactUtil;
import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.artifact.WebArtifactUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.packaging.artifacts.ArtifactType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * Automatically packages web content to the exploded folder.
 * It creates new folders and files, if necessary,
 * <p/>
 * note: There are some cases when IDEA doesn't fire the listeners:
 * when user manually adds folder and files to the web content, idea
 * only fire signal that a folder is created, and not files. Therefore,
 * user should always first add non-existing folders from IDEA and then files.
 */
public class AutoPackager extends VirtualFileAdapter {

	private static final Logger LOG = Logger.getInstance(AutoPackager.class.getName());

	private final ProjectManager projectManager;

	public AutoPackager() {
		this.projectManager = ProjectManager.getInstance();
	}

	/**
	 * Fired when the contents of a virtual file is changed.
	 */
	@Override
	public void contentsChanged(VirtualFileEvent event) {
		packageFile(event.getFile());
	}

	/**
	 * Fired when a virtual file is created. This event is not fired for files discovered during initial VFS initialization.
	 */
	@Override
	public void fileCreated(VirtualFileEvent event) {
		packageFile(event.getFile());
	}

	// ---------------------------------------------------------------- interface

	public static interface ProcessCallback {

		/**
		 * Invoked when destination files is founded. Returns <code>true</code>
		 * if destination is processed.
		 */
		boolean onDestinationFile(VirtualFile vfile, String destination);

		/**
		 * Prepares info message to show in status bar.
		 * If <code>null<code> message is not shown.
		 */
		String prepareInfoMessage(VirtualFile vfile, int autopackageCount);
	}

	// ---------------------------------------------------------------- delete

	/**
	 * Fired when a virtual file is deleted.
	 */
	@Override
	public void fileDeleted(VirtualFileEvent event) {
		VirtualFile vfile = event.getFile();
		process(vfile, deleteCallback);
	}

	private ProcessCallback deleteCallback = new ProcessCallback() {
		@Override
		public boolean onDestinationFile(VirtualFile vfile, String destination) {
			if (vfile.isDirectory() == true) {
//				if (LOG.isDebugEnabled()) {
//					LOG.debug("delete dir '" + destination + "'.");
//				}
				//deleteDirectory(destination);	// todo
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("delete '" + vfile + "' on '" + destination + "'.");
				}
				try {
					File dest = new File(destination);
					if (dest.exists()) {
						dest.delete();
					}
					return true;
				} catch (Exception ioex) {
					LOG.error(ioex);
				}
			}
			return false;
		}

		@Override
		public String prepareInfoMessage(VirtualFile vfile, int autopackageCount) {
			StringBuilder sb = new StringBuilder();
			sb.append("File [").append(vfile.getName()).append("] deleted from ");
			sb.append(autopackageCount).append(" exploded folder");
			if (autopackageCount > 1) {
				sb.append('s');
			}
			sb.append('.');
			return sb.toString();
		}
	};



	// ---------------------------------------------------------------- package

	/**
	 * Package a file.
	 */
	protected void packageFile(VirtualFile vfile) {
		if (vfile.isValid() == false) {
			return;
		}
		process(vfile, packageCallback);
	}


	private ProcessCallback packageCallback = new ProcessCallback() {
		@Override
		public boolean onDestinationFile(VirtualFile vfile, String destination) {
			if (vfile.isDirectory() == true) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("create dir '" + destination + "'.");
				}
				createDirectory(destination);
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("copy '" + vfile + "' to '" + destination + "'.");
				}
				try {
					copyVirtualFile(vfile, destination);
					return true;
				} catch (IOException ioex) {
					LOG.error(ioex);
				}
			}
			return false;
		}

		@Override
		public String prepareInfoMessage(VirtualFile vfile, int autopackageCount) {
			StringBuilder sb = new StringBuilder();
			sb.append("File [").append(vfile.getName()).append("] auto-packaged to ");
			sb.append(autopackageCount).append(" exploded folder");
			if (autopackageCount > 1) {
				sb.append('s');
			}
			sb.append('.');
			return sb.toString();			
		}
	};

	/**
	 * Process a file.
	 */
	private void process(VirtualFile vfile, ProcessCallback callback) {
		Project[] allProjects = projectManager.getOpenProjects();
		for (Project project : allProjects) {

			// find JAVA module for vfile
			Module module = ModuleUtil.findModuleForFile(vfile, project);
			if (module == null) {
				continue;
			}

            ModuleType moduleType = ModuleType.get(module);
			if (moduleType != StdModuleTypes.JAVA) {
				continue;
			}

			// check if plugin is active for this module
			AutoPackageModuleComponent moduleComponent = module.getComponent(AutoPackageModuleComponent.class);
			if (moduleComponent == null) {
				break;
			}
			if (moduleComponent.isActive() == false) {
				continue;
			}

			int autopackageCount = 0;

			// iterate artifacts
			ArtifactType artifactType = WebArtifactUtil.getInstance().getExplodedWarArtifactType();
			Collection<? extends Artifact> allExplodedArtifacts = ArtifactManager.getInstance(project).getArtifactsByType(artifactType);
			for (Artifact art : allExplodedArtifacts) {
				//
				String explodedPath = art.getOutputPath();

				// iterate all webfacets
				Collection<? extends WebFacet> allWebFacets = JavaeeArtifactUtil.getInstance().getFacetsIncludedInArtifact(project, art, WebFacet.ID);
				for (WebFacet webFacet : allWebFacets) {

					// is file inside web roots?
					List<WebRoot> webRoots = webFacet.getWebRoots(false);
					for (WebRoot webRoot : webRoots) {
						VirtualFile webRootFile = webRoot.getFile();

						if (webRootFile == null || !webRootFile.isValid()) {
							continue;
						}

						if (VfsUtil.isAncestor(webRootFile, vfile, false)) {

							// finally, package the file
							String destination = explodedPath + '/' + webRoot.getRelativePath() + '/' + VfsUtil.getRelativePath(vfile, webRootFile, File.separatorChar);
							destination = destination.replace(File.separatorChar, '/');

							if (callback.onDestinationFile(vfile, destination) == true) {
								autopackageCount++;
							}
						}
					}
				}
			}

			if (autopackageCount == 0) {
				continue;
			}

			String message = callback.prepareInfoMessage(vfile, autopackageCount);
			if (message != null) {
				final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
				if (statusBar != null) {
					statusBar.setInfo(message);
				}
			}
		}
	}

	/**
	 * Creates destination directory if it doesn't exist.
	 */
	private static void createDirectory(String destination) {
		File dest = new File(destination);
		if (dest.exists() == false) {
			dest.mkdirs();
		}
	}


	/**
	 * Copy source file to destination file. Existing files are overwritten. IF destination file doesn't exist,
	 * its directory will be created first if not exist.
	 */
	private static File copyVirtualFile(@NotNull VirtualFile source, @NotNull String destination) throws IOException {
		//VirtualFile vdestination = LocalFileSystem.getInstance().findFileByPath(destination);
		File dest = new File(destination);
		if (dest.exists() == false) {
			File parent = dest.getParentFile();
            if (parent == null) {
                return null;
            }
			if (parent.exists() == false) {
				parent.mkdirs();
			}
		}
		OutputStream os = null;
		try {
			//os = new FileOutputStream(VfsUtil.virtualToIoFile(vdestination));
			os = new FileOutputStream(dest);
			os.write(source.contentsToByteArray());
		} finally {
			if (os != null) {
				try {
					os.flush();
				} catch (IOException ioex) {
					//ignore
				}
				try {
					os.close();
				} catch (IOException ioex) {
					//ignore
				}
			}
		}
		return dest;
	}
}
