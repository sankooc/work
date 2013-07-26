package pde.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;

import org.xml.sax.InputSource;

import pde.utils.EclipseProjectService.PDEModel;

public class TestEp {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		testResolvPde();
	}

	static void testResolvPde() throws Exception {
		EclipseProjectService service = new EclipseProjectService();
//		InputStream stream = new FileInputStream("mdm.launch");
		InputStream stream = new FileInputStream("D:/work/trunk/.metadata/.plugins/org.eclipse.debug.core/.launches/New_configuration (1).launch");
		javax.xml.xpath.XPathFactory xfactory = javax.xml.xpath.XPathFactory
				.newInstance();
		javax.xml.xpath.XPath xpath = xfactory.newXPath();
		final javax.xml.xpath.XPathExpression expression = xpath
				.compile("/launchConfiguration/stringAttribute[@key='selected_target_plugins']/@value");
		String ret = expression.evaluate(new InputSource(stream));
		Collection<PDEModel> models = service.parsePdeModels(ret);
		for (PDEModel model : models) {
			System.out.println("name:" + model.getSymbolicName() + "  vesion:"
					+ model.getVersion());
		}
	}

}
