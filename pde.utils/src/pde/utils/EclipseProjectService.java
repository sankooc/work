package pde.utils;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Version;
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

	void merge(PrintStream intp, String launchName) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		intp.println(launchName);
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
					if (null != ret && ret.length > 1) {
						for (IPluginModelBase dm : ret) {
							intp.println("deplicated:"
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

	void create(CommandInterpreter intp, String typeName, String defaultName) {
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
					intp.printStackTrace(e);
				}
			}
		}
	}

	void getWorkspaceProjects(CommandInterpreter intp) {

		IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
		if (null == models || models.length == 0) {
			intp.println("no workspace model");
			return;
		}
		for (IPluginModelBase modelBase : models) {
			intp.println(modelBase.getBundleDescription().getSymbolicName()
					+ modelBase.getBundleDescription().getVersion().toString());
		}
	}

	void updateProject(final IProject project, IProgressService pservice,
			CommandInterpreter intp) {
		intp.println("start to run " + project.getName());
		try {
			pservice.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						e.printStackTrace();
					}

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void updateProject(String name, CommandInterpreter intp) {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		IProgressService pservice = PlatformUI.getWorkbench()
				.getProgressService();
		if (null == name || name.trim().isEmpty()) {
			for (final IProject project : projects) {
				updateProject(project, pservice, intp);
			}
		} else {
			for (IProject project : projects) {
				if (project.getName().equals(name)) {
					updateProject(project, pservice, intp);
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

	void loadProject(File projectfile, final String ws) throws CoreException {
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
							throw new InvocationTargetException(e);
						} finally {
							monitor.done();
						}

					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			return;
		}
	}
}
