/*
 * ExpectInputStream.java
 *
 * Created on May 8, 2007, 8:58:11 PM
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
public class ExpectInputStream {
	
	private InputStream in;
	protected byte[] backlog;
	protected int pos = 0;
	
	public ExpectInputStream(InputStream in, int backlogsize) {
		this.in = in;
		backlog = new byte[backlogsize*2];
	}
	
	public int expectMux(Pattern[] mux) throws IOException {
		final int cutoffpos = backlog.length / 2;
		if (in.available() > 0) {
			int l = in.read(backlog, pos, backlog.length - pos - 1);
			pos += l;
			String s = new String(backlog, 0, pos);
			for (int i = 0; i< mux.length; i++) {
				if (mux[i].matcher(s).find()) 
					return i;
			}
			if (pos > cutoffpos) {
				int cutoffsize = pos - cutoffpos;
				// roll backlog back
				for (int i=0; i<cutoffpos; i++)
					backlog[i] = backlog[cutoffsize+i];
				pos = cutoffpos;
			}
		}
		return -1;
		
	}
	
	public int waitForMux(Pattern[] mux) throws IOException {
		int i = -1;
		while (  (i=expectMux(mux)) == -1) {
			try {
				Thread.sleep(20L);
			} catch (InterruptedException e) {
				return -1;
			}
		}
		
		return i;
	}
	
	public boolean expect(Pattern p) throws IOException {
		return ( expectMux( new Pattern[] {p}) == 0);
	}
	
	public boolean waitFor(Pattern p) throws IOException {
		return ( waitForMux( new Pattern[] {p}) == 0);
	}
	
}