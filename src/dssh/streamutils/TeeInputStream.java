/*
 * TeeInputStream.java
 *
 * Created on May 8, 2007, 8:42:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dssh.streamutils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author juraj
 */
public class TeeInputStream extends FilterInputStream {
	
	private OutputStream out;
	
	public TeeInputStream(InputStream in, OutputStream out) {
		super(in);
		this.out = out;
	}
	
	public int read() throws IOException {
		int r = super.read();
		out.write(r);
		return r;
	}
	
	public int read(byte[] b) throws IOException {
		int i = super.read(b);
		out.write(b);
		return i;
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		int i = super.read(b, off, len);
		out.write(b, off, i);
		return i;
	}
}
