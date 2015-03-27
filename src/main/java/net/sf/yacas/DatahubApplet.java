package net.sf.yacas;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/* This little applet should facilitate communication between Java and Javascript.
 * The idea is to allow Javascript to set data in this applet at various times, and
 * for the main Yacas console to then get that data at startup, when it is loaded.
 *
 * The Yacas console thus does not need to be loaded always, but this applet should
 * always be there.
 */

public class DatahubApplet extends Applet {
	/**
	 *
	 */
	private static final long serialVersionUID = -8532177561327739653L;

	@Override
	public void init() {
		final String programMode = getParameter("programMode");
		if (programMode != null) {
			setProgramMode(programMode);
			final String articleFromFile = getParameter("articleFromFile");
			if (articleFromFile != null) {
				setArticleFromFile(articleFromFile);
			}

			final String article = getParameter("article");
			if (article != null) {
				setArticle(article);
			}
		}
	}

	Article currentArticle() {
		switch (currentProgram) {
		case 2:
			return tutorialProgram;
		case 1:
			return journalProgram;
		case 0:
		default:
			return consoleProgram;
		}
	}

	public String getProgram() {
		synchronized (consoleProgram) {
			return currentArticle().codeBody;
		}
	}

	public String getExample() {
		synchronized (consoleProgram) {
			return currentArticle().getExample();
		}
	}

	public String getTestcode() {
		synchronized (consoleProgram) {
			return currentArticle().getTestcode();
		}
	}

	public String getNrExamples() {
		synchronized (consoleProgram) {
			return currentArticle().getNrExamples();
		}
	}

	public String getArticle() {
		synchronized (consoleProgram) {
			return currentArticle().iArticle;
		}
	}

	public String getArticleBody() {
		synchronized (consoleProgram) {
			return currentArticle().articleBody;
		}
	}

	public void setArticle(String p) {
		// System.out.println("article:\n"+p);
		synchronized (consoleProgram) {
			p = unescape(p);
			currentArticle().SetArticle(p);
		}
	}

	public void setProgramMode(final String mode) {
		synchronized (consoleProgram) {
			if (mode.equals("console")) {
				currentProgram = 0;
			} else if (mode.equals("journal")) {
				currentProgram = 1;
			} else if (mode.equals("tutorial")) {
				currentProgram = 2;
			}
		}
	}

	String readArticleFromFile(final String urlStr) {
		String docbase = getDocumentBase().toString();
		final int pos = docbase.lastIndexOf('/');
		if (pos > -1) {
			docbase = docbase.substring(0, pos + 1);
		} else {
			docbase = "";
		}
		docbase = docbase + urlStr;

		String prog = "";
		try {
			final URL url = new URL(docbase);
			final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			if (in != null) {
				while (true) {
					try {
						final String mark = in.readLine();
						if (mark == null) {
							break;
						}
						prog = prog + mark + "\n";
					} catch (final Exception e) {
					}
				}
				in.close();
			}
		} catch (final Exception e) {
		}
		return prog;
	}

	private int unhex(final int c) {
		if (c >= '0' && c <= '9') {
			return (c - '0');
		}
		if (c >= 'a' && c <= 'f') {
			return 10 + (c - 'a');
		}
		if (c >= 'A' && c <= 'F') {
			return 10 + (c - 'A');
		}
		return 65;
	}

	private String unescape(final String s) {
		final StringBuilder buf = new StringBuilder();
		int i;
		final int nr = s.length();
		for (i = 0; i < nr; i++) {
			if (s.charAt(i) == '%') {
				final int high = s.charAt(i + 1);
				final int low = s.charAt(i + 2);
				i += 2;
				final int c = 16 * unhex(high) + unhex(low);
				buf.append((char) c);
			} else {
				buf.append(s.charAt(i));
			}
		}
		return buf.toString();
	}

	// To make sure it is not reloaded each time
	static String prevLoadedArticle = "";

	public void setArticleFromFile(final String urlStr) {
		if (!prevLoadedArticle.equals(urlStr)) {
			prevLoadedArticle = urlStr;
			synchronized (consoleProgram) {
				setArticle(readArticleFromFile(urlStr));
			}
		}
	}

	static Article consoleProgram = new Article("You can start entering your own program between the code delimiters below.\n\n{{code:\n:code}}\n" + "\n\n{{example:Example();:example}}");
	static Article journalProgram = new Article("No article loaded yet.");
	static Article tutorialProgram = new Article("{{code: Echo(\"Welcome to the Yacas tutorial!\"); :code}}");
	static int currentProgram = 0;
}
