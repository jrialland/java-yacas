package net.sf.yacas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

class StreamGobbler extends Thread {

	private final InputStream is;
	private final ArrayList<String> strings = new ArrayList<String>();

	public StreamGobbler(final InputStream is) {
		this.is = is;
	}

	@Override
	public void run() {
		final InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				strings.add(line);
			}
		} catch (final IOException e) {
		}
	}

	public ArrayList<String> shutdown() {
		if (isAlive()) {
			interrupt();
		}
		return strings;
	}
};
