package net.sf.yacas;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

class CDataReader {

	public CDataReader() {
	}

	public int Open(final URL source) {

		in = null;
		try {

			in = new BufferedReader(new InputStreamReader(source.openStream()));

			in.readLine();
		} catch (final Exception e) {
			in = null;
		}
		if (in != null) {
			return 1;
		}
		return 0;
	}

	public String ReadLine() {
		try {
			final String mark = in.readLine();
			return mark;
		} catch (final Exception e) {
		}
		return null;
	}

	public void Close() {
		try {
			if (in != null) {
				in.close();
			}
		} catch (final Exception e) {
		}
		in = null;
	}

	BufferedReader in;
};
