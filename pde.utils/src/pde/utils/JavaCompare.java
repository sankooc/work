package pde.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.swt.graphics.Image;

public class JavaCompare {
	public static FileFilter filter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".java")
					|| (pathname.isDirectory() && !pathname.getName().equals(
							".svn"));
			// return !pathname.getName().equals(".svn");
		}
	};

	// static FilenameFilter ffilter = new FilenameFilter() {
	// @Override
	// public boolean accept(File dir, String name) {
	// return name.endsWith(".java")
	// || (pathname.isDirectory() && !name.equals(".svn"));
	// }
	//
	// };

	static byte[] getFile(File file) {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			byte[] tmp = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (true) {
				int inx = stream.read(tmp);
				if (inx < 1) {
					break;
				}
				baos.write(tmp, 0, inx);
			}
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != stream) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		return null;

	}

	static String getDesist(File file) {
		byte[] tmp = getFile(file);
		try {
			MessageDigest dis = MessageDigest.getInstance("SHA");
			dis.update(tmp);
			byte[] data =  dis.digest();
			StringBuilder builder= new StringBuilder();
			for(byte b : data){
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
//			return dis.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}

	static void compare(File f1, File f2) throws IOException {
		if (f1.isDirectory() && f2.isDirectory()) {
			File[] fs1 = f1.listFiles(filter);
			File[] fs2 = f2.listFiles(filter);
			if (null == fs1 && null == fs2) {
				return;
			}
			if (fs1.length != fs2.length) {
				System.err.println("two dir is diff : " + f1.getPath());
			}
			for (File f : fs1) {
				String fname = f.getName();
				File ff = new File(f2, fname);
				if (ff.exists()) {
					compare(f, ff);
				} else {
					System.err
							.println("no file found " + ff.getCanonicalPath());
				}

			}

		} else if (f1.isFile() && f2.isFile()) {
			if (getDesist(f1).equals(getDesist(f2))) {
//				System.out.println("two file is equal " + f1.getName()
//						+ " and " + f2.getName());
			} else {
				System.err.println("two file is diff " + f1.getCanonicalPath());
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		URL url = new URL("https://raw.github.com/sankooc/work/master/plugin/src/undeploy.png");
//		Image image = new Image(null,url.openConnection().getInputStream());
		
//		File f1 = new File("D:/repo/talend-branch52/tom/org.talend.mdm.workbench/src/com/amalto/workbench/detailtabs/sections/BasePropertySection.java");
//		File f2 = new File("D:/repo/talend/tom/org.talend.mdm.workbench/src/com/amalto/workbench/detailtabs/sections/BasePropertySection.java");
//		System.out.println(getDesist(f1));
//		System.out.println(getDesist(f2));
		
		File f1 = new File(
				"D:/repo/talend-branch52/tom/org.talend.mdm.workbench/src");
		File f2 = new File("D:/repo/talend/tom/org.talend.mdm.workbench/src");
		try {
			compare(f2, f1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
