/*
 * Terminal.java
 *
 * Created on April 14, 2007, 10:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dssh;

/**
 *
 * @author juraj
 */
public class Terminal {
	
	public native void initConsole();
	public native void finishConsole();
	public native int getWsRow();
	public native int getWsCol();
	public native int getWsXPixel();
	public native int getWsYPixel();
	public native boolean shouldChangeWindowSize();
	
	static {
		System.loadLibrary("jniconsole-"+getLibraryArch());
	}
	
	private static String getLibraryArch() {
		String name    = System.getProperty("os.name");
		String arch    = System.getProperty("os.arch");
		String version = System.getProperty("os.version");
		String majorVersion = version.substring(0, 1);
		
		if (arch.endsWith("86")) {
			arch = "x86";
		}
		
		if (name.equals("Linux")) {
			return arch + "-linux";
		}
		
		if (name.equals("SunOS")) {
			if ((arch.startsWith("sparcv")) &&
				("64".equals(System.getProperty("sun.arch.data.model")))) {
				arch = "sparc64";
			}
			return arch + "-solaris";
		}
		
		if ( (name.equals("Mac OS X")) || (name.equals("Darwin")) ) {
			return arch + "-macosx";
		} else if (name.equals("FreeBSD")) {
			//none of the 4,5,6 major versions are binary compatible
			return arch + "-freebsd-" + majorVersion;
		}
		
		return "unsupported";
		
	}
	
	/** Creates a new instance of Terminal */
	public Terminal() {
	}
	
}
