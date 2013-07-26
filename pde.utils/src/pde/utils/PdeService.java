package pde.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
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
import org.eclipse.osgi.framework.console.CommandProvider;
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
import org.xml.sax.InputSource;

public class PdeService implements CommandProvider {

	public void _createlc(CommandInterpreter intp) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		String ur = intp.nextArgument();
		try {
			InputStream stream = URI.create(ur).toURL().openStream();
			byte[] content = IOUtils.toByteArray(stream);
			String memont = new String(content);
			ILaunchConfiguration icl = manager.getLaunchConfiguration(memont);
			ILaunchConfigurationWorkingCopy wc = icl.getWorkingCopy();
			wc.doSave();
		} catch (Exception e) {
			intp.printStackTrace(e);
		}

	}

	public void _creatws(CommandInterpreter intp) {
		String arg = intp.nextArgument();
		if (null == arg || arg.isEmpty()) {
			return;
		}
		IWorkbench workbanch = PlatformUI.getWorkbench();
		IWorkingSetManager workingManager = workbanch.getWorkingSetManager();
		IWorkingSet set = workingManager.createWorkingSet(arg,
				new IAdaptable[] {});
		set.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
		workingManager.addWorkingSet(set);
	}

	Collection<String> ala(CommandInterpreter intp, String content) {
		String[] tokens = content.split(",");
		intp.println("tokens size : " + tokens.length);
		Collection<String> syms = new ArrayList<String>(tokens.length);
		for (String token : tokens) {
			String sym = token.split("@")[0];
			if (sym.contains("*")) {
				sym = sym.split("\\*")[0];
			} else {
				syms.add(sym);
			}
		}
		return syms;
	}

	public void _merge(CommandInterpreter intp) {
		String next = intp.nextArgument();
		if (null == next) {
			return;
		}
		try {
			merge(intp, next);
		} catch (CoreException e) {
			intp.printStackTrace(e);
		}

	}

	void merge(CommandInterpreter intp, String launchName) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		intp.println(launchName);
		ILaunchConfiguration[] luans = manager.getLaunchConfigurations();
		for (ILaunchConfiguration launch : luans) {
			if (launch.getName().equals(launchName)) {
				String content = launch.getAttribute("selected_target_plugins",
						"");
				Collection<String> syms = ala(intp, content);
				for (String sym : syms) {
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

	public void _createOsgi(final CommandInterpreter intp) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				create(intp, "OSGi Framework", "new osgi");
			}
		});

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

	public void _wplugin(final CommandInterpreter intp) {
		getWorkspaceProjects(intp);
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

	public void _updateProject(final CommandInterpreter intp) {
		final String arg = intp.nextArgument();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				updateProject(arg, intp);
			}
		});
	}

	private boolean isDefaultLocation(IPath path) {
		// The project description file must at least be within the project,
		// which is within the workspace location
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

	public void _talend(final CommandInterpreter intp) {
		String path = intp.nextArgument();
		String rootPath = intp.nextArgument();
		File root = new File(rootPath);
		try {
			javax.xml.xpath.XPathFactory xfactory = javax.xml.xpath.XPathFactory
					.newInstance();
			javax.xml.xpath.XPath xpath = xfactory.newXPath();
			final javax.xml.xpath.XPathExpression expression = xpath
					.compile("/launchConfiguration/stringAttribute[@key='selected_workspace_plugins']/@value");
			// "D:/repo/talend-branch52";
			// String path =
			// "https://raw.github.com/sankooc/work/master/luanch.xml";
			URLConnection connection = URI.create(path).toURL()
					.openConnection();
			InputStream stream = connection.getInputStream();
			String ret = expression.evaluate(new InputSource(stream));
			Collection<String> list = ala(intp, ret);
			File[] models = root.listFiles();
			for (File module : models) {
				if (module.isDirectory()) {
					final String moduleName = module.getName();
					File[] prl = module.listFiles();
					for (final File projectDir : prl) {
						String pName = projectDir.getName();
						if (list.remove(pName)) {
							intp.println("find project :" + pName + " module :"
									+ moduleName);
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									try {
										loadProject(projectDir, moduleName);
									} catch (CoreException e) {
										intp.printStackTrace(e);
									}
								}
							});
						}
					}
				}
			}
			if (!list.isEmpty()) {
				intp.println(" no find projects:");
				for (String pn : list) {
					intp.println("[" + pn + "]");
				}

			}
		} catch (Exception e) {
			intp.printStackTrace(e);
		}
	}

	public void _loadProject(final CommandInterpreter intp) {
		// org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard
		String nextArg = intp.nextArgument();
		final String ws = intp.nextArgument();
		if (null == nextArg) {
			intp.println("no file path");
			return;
		}
		final File file = new File(nextArg);
		if (!file.exists() || file.isFile()) {
			intp.println("no project dir exist");
			return;
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					loadProject(file, ws);
				} catch (CoreException e) {
					intp.printStackTrace(e);
				}
			}
		});
	}

	@Override
	public String getHelp() {
		return null;
	}
}
