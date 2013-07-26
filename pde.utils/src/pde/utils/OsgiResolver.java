package pde.utils;

import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class OsgiResolver {

	private BundleContext context;

	public OsgiResolver(BundleContext context) {
		if (null == context) {
			Bundle bundle = FrameworkUtil.getBundle(this.getClass());
//			bundle.adapt(Wire)
			this.context = bundle.getBundleContext();
		} else {
			this.context = context;
		}
	}
//	public Collection<String> getPackageBundles(){
//		
//	}
}
