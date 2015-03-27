package net.sf.yacas;

import java.io.FileInputStream;
import java.io.InputStream;

class StdFileInput extends StringInput {
	public StdFileInput(final String aFileName, final InputStatus aStatus) throws Exception {
		super(new StringBuffer(), aStatus);
		final FileInputStream stream = new FileInputStream(aFileName);
		int c;
		while (true) {
			c = stream.read();
			if (c == -1) {
				break;
			}
			iString.append((char) c);
		}
		stream.close();
	}

	public StdFileInput(final InputStream aStream, final InputStatus aStatus) throws Exception {
		super(new StringBuffer(), aStatus);
		int c;
		while (true) {
			c = aStream.read();
			if (c == -1) {
				break;
			}
			iString.append((char) c);
		}
	}

}
