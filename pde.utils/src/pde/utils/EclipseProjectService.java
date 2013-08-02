package pde.utils;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.plugin.PluginRegistry.PluginFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Version;
import org.xml.sax.InputSource;
import org.eclipse.pde.launching.IPDELauncherConstants;

public class EclipseProjectService {

	static class PDEModel {
		private String symbolicName;
		private Version version;

		public PDEModel(String name, String ver) {
			this.symbolicName = name;
			if (null != ver) {
				this.version = Version.parseVersion(ver);
			}
		}

		public String getSymbolicName() {
			return symbolicName;
		}

		public void setSymbolicName(String symbolicName) {
			this.symbolicName = symbolicName;
		}

		public Version getVersion() {
			return version;
		}

		@Override
		public String toString() {
			return "symbolicName:" + symbolicName + " version:" + version;
		}

	}

	protected Collection<PDEModel> parsePdeModels(String content) {
		String[] tokens = content.split(",");
		if (null == tokens || tokens.length == 0) {
			return null;
		}
		Collection<PDEModel> models = new ArrayList<PDEModel>(tokens.length);
		for (String token : tokens) {
			String sym = token.split("@")[0];
			if (sym.contains("*")) {
				String[] stoken = sym.split("\\*");
				models.add(new PDEModel(stoken[0], stoken[1]));
			} else {
				models.add(new PDEModel(sym, null));
			}
		}
		return models;
	}

	public void merge(String launchName) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		StringWriter writer = new StringWriter();
		writer.write(launchName);
		ILaunchConfiguration[] luans = manager.getLaunchConfigurations();
		for (ILaunchConfiguration launch : luans) {
			if (launch.getName().equals(launchName)) {
				String content = launch.getAttribute(
						IPDELauncherConstants.SELECTED_TARGET_PLUGINS, "");
				Collection<PDEModel> syms = parsePdeModels(content);
				for (PDEModel model : syms) {
					String sym = model.getSymbolicName();
					IPluginModelBase[] ret = PluginRegistry.findModels(sym,
							VersionRange.emptyRange, new PluginFilter());
					if (null != ret && ret.length == 0) {
						writer.write("cannot find pde model : "
								+ model.toString());
					} else if (ret.length > 1) {
						for (IPluginModelBase dm : ret) {
							writer.write("deplicated:"
									+ dm.getBundleDescription()
											.getSymbolicName()
									+ dm.getBundleDescription().getVersion()
											.toString());
						}
					}
				}
				return;
			}
		}
	}

	public IWorkingSet createWorkSet(String workset) {
		IWorkbench workbanch = PlatformUI.getWorkbench();
		IWorkingSetManager workingManager = workbanch.getWorkingSetManager();
		IWorkingSet set = workingManager.createWorkingSet(workset,
				new IAdaptable[] {});
		set.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
		workingManager.addWorkingSet(set);
		return set;
	}

	public void create(String typeName, String defaultName) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType[] types = manager
				.getLaunchConfigurationTypes();
		for (ILaunchConfigurationType type : types) {
			if (typeName.equals(type.getName())) {
				try {
					ILaunchConfigurationWorkingCopy wc = type.newInstance(null,
							defaultName);
					wc.doSave();
				} catch (CoreException e) {
					StatusManager.getManager().handle(e, Activator.plugin);
				}
			}
		}
	}

	public void updateProject(final IProject project, IProgressService pservice) {
		try {
			pservice.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						StatusManager.getManager().handle(e, Activator.plugin);
					}

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateProject(String name, PrintStream intp) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		IProgressService pservice = PlatformUI.getWorkbench()
				.getProgressService();
		if (null == name || name.trim().isEmpty()) {
			for (final IProject project : projects) {
				updateProject(project, pservice);
			}
		} else {
			for (IProject project : projects) {
				if (project.getName().equals(name)) {
					updateProject(project, pservice);
					return;
				}
			}

		}
	}

	private boolean isDefaultLocation(IPath path) {
		if (path.segmentCount() < 2)
			return false;
		return path.removeLastSegments(2).toFile()
				.equals(Platform.getLocation().toFile());
	}

	public void createWorkspace(String filepath, String rootPath) {
		File root = new File(rootPath);
		try {
			javax.xml.xpath.XPathFactory xfactory = javax.xml.xpath.XPathFactory
					.newInstance();
			javax.xml.xpath.XPath xpath = xfactory.newXPath();
			final javax.xml.xpath.XPathExpression expression = xpath
					.compile("/launchConfiguration/stringAttribute[@key='selected_workspace_plugins']/@value");
			URLConnection connection = URI.create(filepath).toURL()
					.openConnection();
			InputStream stream = connection.getInputStream();
			String ret = expression.evaluate(new InputSource(stream));
			Collection<PDEModel> list = parsePdeModels(ret);
			File[] models = root.listFiles();
			for (File module : models) {
				if (module.isDirectory()) {
					final String moduleName = module.getName();
					File[] prl = module.listFiles();
					for (final File projectDir : prl) {
						String pName = projectDir.getName();
						if (list.remove(pName)) {
							try {
								loadProject(projectDir, moduleName);
							} catch (CoreException e) {
								StatusManager.getManager().handle(e,
										Activator.plugin);
							}
						}
					}
				}
			}
//			if (!list.isEmpty()) {
//				intp.println(" no find projects:");
//				for (String pn : list) {
//					intp.println("[" + pn + "]");
//				}
//
//			}
		} catch (Exception e) {
//			intp.printStackTrace(e);
		}
	}

	public void loadProject(File projectfile, final String ws)
			throws CoreException {
		File file = new File(projectfile, ".project");
		String projectName;
		final IProjectDescription description;
		if (file.exists()) {
			IPath path = new Path(file.getPath());
			if (isDefaultLocation(path)) {
				projectName = path.segment(path.segmentCount() - 2);
				description = ResourcesPlugin.getWorkspace()
						.newProjectDescription(projectName);
			} else {
				description = ResourcesPlugin.getWorkspace()
						.loadProjectDescription(path);
				projectName = description.getName();
			}
			final IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (project.exists()) {
				return;
			}
			description.setName(projectName);
			IProgressService pservice = PlatformUI.getWorkbench()
					.getProgressService();
			try {
				pservice.run(true, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						try {
							monitor.beginTask("create project", 100);
							project.create(description, new SubProgressMonitor(
									monitor, 30));
							project.open(IResource.BACKGROUND_REFRESH,
									new SubProgressMonitor(monitor, 70));
							if (null != ws) {
								IWorkbench workbanch = PlatformUI
										.getWorkbench();
								IWorkingSetManager workingManager = workbanch
										.getWorkingSetManager();
								IWorkingSet set = workingManager
										.getWorkingSet(ws);
								if (null == set) {
									set = workingManager.createWorkingSet(ws,
											new IAdaptable[] {});
									set.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
									workingManager.addWorkingSet(set);
								}
								workingManager.addToWorkingSets(project,
										new IWorkingSet[] { set });
							}
						} catch (CoreException e) {
							StatusManager.getManager().handle(e,
									Activator.plugin);
						} finally {
							monitor.done();
						}
					}
				});
			} catch (Exception e) {
			}

		} else {
			return;
		}
	}
}
