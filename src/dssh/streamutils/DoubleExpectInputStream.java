/*
 * DoubleExpectInputStream.java
 *
 * Created on May 8, 2007, 10:14:58 PM
 *
 */

package dssh.streamutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 *
 * @author juraj
 */
public class DoubleExpectInputStream extends ExpectInputStream {
	
	private ExpectInputStream in2;
	
	public DoubleExpectInputStream(InputStream in1, InputStream in2, int backlogsize) {
		super(in1, backlogsize);
		this.in2 = new ExpectInputStream(in2, backlogsize);
	}
	
	public int expectMux(Pattern[] mux) throws IOException {
		int i = super.expectMux(mux);
		if (i == -1)
			return in2.expectMux(mux);
		return i;
	}
	
}
