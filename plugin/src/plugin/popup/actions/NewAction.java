package plugin.popup.actions;

import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class NewAction implements IObjectActionDelegate {

    private Shell shell;

    /**
     * Constructor for Action1.
     */
    public NewAction() {
        super();
    }

    static String newLine = System.getProperty("line.separator"); //$NON-NLS-1$

    static String clzName = "Messages"; //$NON-NLS-1$

    static String nlsClz = "org.eclipse.osgi.util.NLS"; //$NON-NLS-1$

    void appendLine(StringBuilder builder, String content, int inx) {
        appendLine(builder, content, inx, false);
    }

    void appendLine(StringBuilder builder, String content) {
        appendLine(builder, content, 1);
    }

    void appendLine(StringBuilder builder, String content, int inx, boolean skip) {
        for (int i = 0; i < inx; i++) {
            builder.append("    "); //$NON-NLS-1$
        }
        builder.append(content);
        builder.append(newLine);
        if (skip)
            builder.append(newLine);
    }

    void createNative(IFile file) {

        IJavaElement resouce = (IJavaElement) file.getParent().getAdapter(IJavaElement.class);
        IPackageFragment pkg = null;
        if (null == resouce) {
            return;// TODO
        } else if (resouce instanceof IPackageFragmentRoot) {
            // TODO

        } else if (resouce instanceof IPackageFragment) {
            pkg = (IPackageFragment) resouce;
        }
        String packageName = pkg.getElementName();
        Properties pro = new Properties();
        InputStream is = null;
        IProject project = file.getProject();
        IJavaProject jp = (IJavaProject) project.getAdapter(IJavaElement.class);
        try {
            IType type = jp.findType(nlsClz);
            if (type == null) {
                return;
            }
            is = file.getContents();
            pro.load(is);
            StringBuilder builder = new StringBuilder();
            appendLine(builder, "package " + packageName + ";", 0, true); //$NON-NLS-1$ //$NON-NLS-2$
            appendLine(builder, "import " + nlsClz + ";", 0, true); //$NON-NLS-1$ //$NON-NLS-2$
            appendLine(builder, "public class " + clzName + " extends NLS {", 0, true); //$NON-NLS-1$ //$NON-NLS-2$

            appendLine(builder, "private static final String BUNDLE_NAME = \"" + packageName + ".messages\"; //$NON-NLS-1$", 1, //$NON-NLS-1$
                    true);

            appendLine(builder, "private " + clzName + "() {}", 1, true); //$NON-NLS-1$ //$NON-NLS-2$

            appendLine(builder, "public static String bind(String message, String... bindings) {"); //$NON-NLS-1$

            appendLine(builder, "return NLS.bind(message, bindings);", 2); //$NON-NLS-1$
            appendLine(builder, "}", 1, true); //$NON-NLS-1$

            appendLine(builder, "static {"); //$NON-NLS-1$
            appendLine(builder, "NLS.initializeMessages(BUNDLE_NAME, Messages.class);", 2); //$NON-NLS-1$
            appendLine(builder, "}", 1, true);

            for (Object obj : pro.keySet()) {
                String key = (String) obj;
                appendLine(builder, "public static String " + key + ";", 1, true); //$NON-NLS-1$
            }
            builder.append(newLine);
            appendLine(builder, "}", 0, false); //$NON-NLS-1$
            pkg.createCompilationUnit(clzName + ".java", builder.toString(), false, new NullProgressMonitor()); //$NON-NLS-1$
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    IFile file;

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
        ISelectionProvider selectionProvider = targetPart.getSite().getSelectionProvider();
        StructuredSelection ss = (StructuredSelection) selectionProvider.getSelection();
        file = (IFile) ss.getFirstElement();
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    @Override
    public void run(IAction action) {
        createNative(file);
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
