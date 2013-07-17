package plugin.popup.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class Merge implements IObjectActionDelegate {

	IMember getSourceContent(IParent root) throws JavaModelException {
		for (IJavaElement ele : root.getChildren()) {
			if (ele instanceof IMember) {
				return (IMember) ele;
			}
		}
		return null;
	}

	void createNative(IJavaElement file) {
		IParent unit = (IParent) file;
		Collection<String> list = new HashSet<String>();

		try {
			IMember source = getSourceContent(unit);
			for (IJavaElement ele : source.getChildren()) {
				if (ele instanceof IField) {
					if((((IField) ele).getFlags() & Flags.AccPrivate) == 0){
						list.add(ele.getElementName());	 
					}
				}
			}
			IPath root = file.getParent().getPath();
			IPath pro = root.append("messages.properties"); 
			StringBuilder builder = new StringBuilder();
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			IFile fp =workspace.getRoot().getFile(pro);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fp.getContents()));
			while(true){
				String cont = reader.readLine();
				if(null == cont){
					break;
				}
				if(cont.startsWith("#")){
					continue;
				}
				String key = cont.split("=")[0].trim();
				if(list.contains(key)){
					
				}else{
					System.out.println("no this field:"+key);
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run(IAction action) {
		createNative(file);

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	private Shell shell;
	IJavaElement file;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
		ISelectionProvider selectionProvider = targetPart.getSite()
				.getSelectionProvider();
		StructuredSelection ss = (StructuredSelection) selectionProvider
				.getSelection();
		file = (IJavaElement) ss.getFirstElement();
	}

}
