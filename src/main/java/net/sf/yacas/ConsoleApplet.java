package net.sf.yacas;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ConsoleApplet extends Applet implements KeyListener, FocusListener, ClipboardOwner, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -321191584478987490L;
	AppletOutput out;

	// / Applet initialization
	@Override
	public void init() {
		setBackground(bkColor);
		setLayout(null);
		addKeyListener(this);
		addFocusListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		out = new AppletOutput(this);
		ResetInput();

		String hintsfilename = getDocumentBase().toString();
		final int slash = hintsfilename.lastIndexOf('/');
		if (slash >= 0) {
			hintsfilename = hintsfilename.substring(0, slash + 1);
		}
		hintsfilename = hintsfilename + "hints.txt";
		LoadHints(hintsfilename);
	}

	boolean focusGained = false;

	@Override
	public void focusGained(final FocusEvent evt) {
		focusGained = true;
		inputDirty = true;
		outputDirty = true;
		if (!gotDatahubInit) {
			start();
		}
		repaint();
	}

	@Override
	public void focusLost(final FocusEvent evt) {
		focusGained = false;
		inputDirty = true;
		outputDirty = true;
		repaint();
	}

	@Override
	public void mouseClicked(final MouseEvent event) {
	}

	@Override
	public void mouseEntered(final MouseEvent event) {
	}

	@Override
	public void mouseExited(final MouseEvent event) {
	}

	boolean scrolling = false;
	int yDown = 0;
	int yStart = 0;

	@Override
	public void mousePressed(final MouseEvent event) {
		scrolling = false;
		final int th = calcThumbHeight();
		final int canvasHeight = getHeight() - fontHeight - 1;
		final int w = getWidth();
		if (canvasHeight < totalLinesHeight) {
			final int x = event.getX();
			final int y = event.getY();
			if (x > w - scrollWidth && y < canvasHeight) {

				if (y >= thumbPos + 2 && y <= thumbPos + 2 + th) {
					yDown = y;
					yStart = thumbPos;
				} else {
					thumbPos = y - 2;
					clipThumbPos();
				}
				scrolling = true;
				thumbMoused = true;
				outputDirty = true;
				repaint();
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent event) {
		if (scrolling) {
			scrolling = false;
			return;
		} else if (hintWindow != null) {
			if (matchToInsert.length() > 0) {
				inputLine = inputLine.substring(0, ito) + matchToInsert + inputLine.substring(ito, inputLine.length());
				cursorPos += matchToInsert.length();
				RefreshHintWindow();
				repaint();
				return;
			}
		}
	}

	@Override
	public void mouseMoved(final MouseEvent event) {
		boolean newthumbMoused = false;
		final int canvasHeight = getHeight() - fontHeight - 1;

		if (canvasHeight < totalLinesHeight) {
			final int x = event.getX();
			final int y = event.getY();
			if (x > getWidth() - scrollWidth && y < canvasHeight) {
				newthumbMoused = true;
			}
		}
		if (thumbMoused != newthumbMoused) {
			thumbMoused = newthumbMoused;
			outputDirty = true;
			repaint();
		}
	}

	void clipThumbPos() {
		final int th = calcThumbHeight();
		final int canvasHeight = getHeight() - fontHeight - 1;
		if (thumbPos < 0) {
			thumbPos = 0;
		}
		if (thumbPos > canvasHeight - th - 4) {
			thumbPos = canvasHeight - th - 4;
		}
	}

	@Override
	public void mouseDragged(final MouseEvent event) {

		if (scrolling) {
			final int y = event.getY();
			thumbPos = yStart + (y - yDown);
			clipThumbPos();
			outputDirty = true;
			repaint();
		}
	}

	@Override
	public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
	}

	boolean calculating = false;

	LispOutput stdoutput = null;
	CYacas yacas = null;
	StringBuffer outp = new StringBuffer();

	@Override
	public void start() {
		clearOutputLines();
		stdoutput = new StringOutput(outp);
		yacas = new CYacas(stdoutput);
		yacas.env.iCurrentInput = new CachedStdFileInput(yacas.env.iInputStatus);

		if (yacasLogo != null) {
			addLine(new ImageLine(yacasLogo, this));
		}

		{
			String s;
			int bkred = 255;
			int bkgrn = 255;
			int bkblu = 255;
			s = getParameter("bkred");
			if (s != null) {
				bkred = Integer.parseInt(s);
			}
			s = getParameter("bkgrn");
			if (s != null) {
				bkgrn = Integer.parseInt(s);
			}
			s = getParameter("bkblu");
			if (s != null) {
				bkblu = Integer.parseInt(s);
			}
			bkColor = new Color(bkred, bkgrn, bkblu);
			setBackground(bkColor);
		}

		{
			final Font font = new Font("helvetica", Font.PLAIN, 12);
			final Color c = new Color(96, 96, 96);

			AddLineStatic(100, "", "", font, c);
			AddLineStatic(100, "", "", font, c);
			AddLineStatic(100, "", "Yacas version '" + CVersion.VERSION + "'.", font, c);
			AddLineStatic(100, "", "Type 'restart' to restart Yacas, or 'cls' to clear screen.\n", font, c);
			AddLineStatic(100, "", "To see example commands, keep typing 'Example();'\n", font, c);
		}

		{
			final String docbase = getDocumentBase().toString();

			if (docbase.substring(0, 4).equals("file")) {
				final int pos = docbase.lastIndexOf('/');
				final String zipFileName = docbase.substring(0, pos + 1) + "yacas.jar";
				if (getParameter("debug") != null) {
					AddLineStatic(100, "", " '" + zipFileName + "'.", font, Color.red);
				}
			}
			if (docbase.startsWith("http")) {
				final int pos = docbase.lastIndexOf('/');
				final String scriptBase = "jar:" + docbase.substring(0, pos + 1) + "yacas.jar!/scripts/";
				if (getParameter("debug") != null) {
					AddLineStatic(100, "", " '" + scriptBase + "'.", font, Color.red);
				}
				try {
					yacas.Evaluate("DefaultDirectory(\"" + scriptBase + "\");");
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		try {
			out.println("");
		} catch (final Exception e) {
			out.println(e);
		}
		int i = 1;
		while (true) {
			final String argn = "init" + i;
			String s = getParameter(argn);
			if (s == null) {
				break;
			}
			s = unescape(s);
			try {
				yacas.Evaluate(s);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			i++;
		}

		gotDatahubInit = false;
		TryInitThroughDatahub();
		try {
			yacas.Evaluate("Plot2D'outputs();");
			yacas.Evaluate("Plot2D'outputs() := {{\"default\", \"java\"},{\"data\", \"Plot2D'data\"},{\"gnuplot\", \"Plot2D'gnuplot\"},{\"java\", \"Plot2D'java\"},};");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		i = 1;
		while (true) {
			final String argn = "history" + i;
			String s = getParameter(argn);
			if (s == null) {
				break;
			}
			s = unescape(s);
			AppendHistoryLine(s);
			i++;
		}

		ResetInput();
	}

	boolean gotDatahubInit = false;

	void TryInitThroughDatahub() {
		if (!gotDatahubInit) {
			final String programMode = getParameter("programMode");
			if (programMode == null) {
				gotDatahubInit = true;
			} else {
				try {
					final Applet dataHub = getAppletContext().getApplet("datahub");
					if (dataHub != null) {
						final net.sf.yacas.DatahubApplet cons = (net.sf.yacas.DatahubApplet) dataHub;
						cons.setProgramMode(programMode);

						final String programContentsToLoad = "[" + cons.getProgram() + "];";
						gotDatahubInit = true; // We're already satisfied here, as we got the contents from the datahub.
						InvokeCalculationSilent(programContentsToLoad);
					}
				} catch (final Exception e) {
				}
			}
		}
	}

	@Override
	public void stop() {
	}

	public void AppendHistoryLine(final String line) {
		// TODO optimize! We need to wrap around the history buffer, this is inefficient.
		if (currentHistoryLine == nrHistoryLines) {
			int i;
			for (i = 0; i < currentHistoryLine - 1; i++) {
				history[i] = history[i + 1];
			}
			currentHistoryLine--;
		}
		history[currentHistoryLine] = line;
		currentHistoryLine++;
	}

	private String unescape(final String s) {
		final StringBuilder buf = new StringBuilder();
		int i;
		final int nr = s.length();
		for (i = 0; i < nr; i++) {
			if (s.charAt(i) == '\'' && s.charAt(i + 1) == '\'') {
				buf.append('\"');
				i++;
			} else {
				buf.append(s.charAt(i));
			}
		}
		return buf.toString();
	}

	public void ResetInput() {
		if (inputLine.length() > 0) {
			if (inputLine.charAt(inputLine.length() - 1) != '\\') {
				gatheredMultiLine = "";
			}
		}
		inputLine = "";
		cursorPos = 0;
		historyBrowse = currentHistoryLine;
		inputDirty = true;
	}

	// / Applet destruction
	@Override
	public void destroy() {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		processKeyEvent(e);
	}

	@Override
	public void keyTyped(final KeyEvent e) {
		// processKeyEvent(e);
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		// processKeyEvent(e);
	}

	public void setClipboardContents(final String aString) {
		final StringSelection stringSelection = new StringSelection(aString);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	public String getClipboardContents() {
		String result = "";
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// odd: the Object param of getContents is not currently used
		final Transferable contents = clipboard.getContents(null);
		final boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (final java.awt.datatransfer.UnsupportedFlavorException ex) {
				// highly unlikely since we are using a standard DataFlavor
				System.out.println(ex);
			} catch (final IOException ex) {
				System.out.println(ex);
			}
		}
		return result;
	}

	@Override
	protected void processKeyEvent(final KeyEvent e) {
		inputDirty = true;
		if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
			if (KeyEvent.KEY_PRESSED != e.getID()) {
				return;
			}
			if (e.getKeyCode() == 'C') {
				// out.println("Copy");
				setClipboardContents(gatheredMultiLine + inputLine);
			} else if (e.getKeyCode() == 'V') {
				try {
					String toInsert = getClipboardContents();
					if (toInsert != null) {
						int cr = toInsert.indexOf('\n');
						while (cr >= 0) {
							inputLine = inputLine + toInsert.substring(0, cr);
							toInsert = toInsert.substring(cr + 1, toInsert.length());
							cr = toInsert.indexOf('\n');

							AppendHistoryLine(inputLine);
							AddLinesStatic(48, inputPrompt, inputLine);
							if (inputLine.charAt(inputLine.length() - 1) == '\\') {
								gatheredMultiLine = gatheredMultiLine + inputLine.substring(0, inputLine.length() - 1);
							} else {
								PerformRequest("Out> ", gatheredMultiLine + inputLine, true);
							}
							ResetInput();
						}
						inputLine = inputLine + toInsert;
						RefreshHintWindow();
						repaint();
						return;
					}
				} catch (final Exception ex) {
				}
			} else {
				return;
			}
		}

		if (KeyEvent.KEY_PRESSED == e.getID()) {
			if (KeyEvent.VK_SHIFT == e.getKeyCode()) {
				return;
			}
			if (KeyEvent.VK_CONTROL == e.getKeyCode()) {
				return;
			}
			if (KeyEvent.VK_ALT == e.getKeyCode()) {
				return;
			} else if (KeyEvent.VK_HOME == e.getKeyCode()) {
				cursorPos = 0;
			}
			/*Does not seem to work?
			      else if (e.VK_COPY == e.getKeyCode())
			      {
			        System.out.println("COPY");
			      }
			      else if (e.VK_PASTE == e.getKeyCode())
			      {
			        System.out.println("PASTE");
			      }
			 */
			else if (KeyEvent.VK_END == e.getKeyCode()) {
				cursorPos = inputLine.length();
			} else if (KeyEvent.VK_LEFT == e.getKeyCode()) {
				if (cursorPos > 0) {
					cursorPos--;
					RefreshHintWindow();
					repaint();
					return;
				}
			} else if (KeyEvent.VK_BACK_SPACE == e.getKeyCode()) {
				if (cursorPos > 0) {
					cursorPos--;
					inputLine = new StringBuffer(inputLine).delete(cursorPos, cursorPos + 1).toString();
					RefreshHintWindow();
					repaint();
					return;
				}
			} else if (KeyEvent.VK_DELETE == e.getKeyCode()) {
				if (inputLine.length() > 0) {
					if (cursorPos == inputLine.length()) {
						cursorPos--;
					}
					inputLine = new StringBuffer(inputLine).delete(cursorPos, cursorPos + 1).toString();
					RefreshHintWindow();
					repaint();
					return;
				}
			} else if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
				if (hintWindow != null) {
					hintWindow = null;
				} else {
					ResetInput();
				}
				repaint();
				return;
			} else if (KeyEvent.VK_UP == e.getKeyCode()) {
				boolean handled = false;
				if (hintWindow != null) {
					if (hintWindow.iAllowSelection) {
						handled = true;
						if (hintWindow.iCurrentPos > 0) {
							hintWindow.iCurrentPos--;
							repaint();
						}
					}
				}

				if (!handled) {
					handled = true;
					final String prefix = inputLine.substring(0, cursorPos);
					int i = historyBrowse - 1;
					while (i > 0) {
						if (history[i].startsWith(prefix)) {
							break;
						}
						i--;
					}
					if (i >= 0 && i != historyBrowse && history[i].startsWith(prefix)) {
						historyBrowse = i;
						inputLine = history[historyBrowse];
					}
				}
			} else if (KeyEvent.VK_DOWN == e.getKeyCode()) {
				boolean handled = false;
				if (hintWindow != null) {
					if (hintWindow.iAllowSelection) {
						handled = true;
						if (hintWindow.iCurrentPos < hintWindow.iNrLines - 1) {
							hintWindow.iCurrentPos++;
							repaint();
						}
					}
				}

				if (!handled) {
					final String prefix = inputLine.substring(0, cursorPos);
					int i = historyBrowse + 1;
					while (i < currentHistoryLine) {
						if (history[i].startsWith(prefix)) {
							break;
						}
						i++;
					}
					if (i < currentHistoryLine && history[i].startsWith(prefix)) {
						historyBrowse = i;
						inputLine = history[historyBrowse];
					} else {
						final int pos = cursorPos;
						ResetInput();
						inputLine = prefix;
						cursorPos = pos;
					}
				}
			} else if (KeyEvent.VK_RIGHT == e.getKeyCode()) {
				boolean handled = false;

				if (!handled) {
					handled = true;
					if (cursorPos < inputLine.length()) {
						cursorPos++;
						RefreshHintWindow();
						repaint();
						return;
					}
				}
			} else if (KeyEvent.VK_ENTER == e.getKeyCode()) {
				boolean handled = false;

				if (!handled) {
					if (cursorPos == ito && matchToInsert.length() > 0) {
						// System.out.println("matchToInsert = "+matchToInsert);
						handled = true;
						inputLine = inputLine.substring(0, ito) + matchToInsert + inputLine.substring(ito, inputLine.length());
						cursorPos += matchToInsert.length();
						RefreshHintWindow();
						repaint();
						return;
					}
				}
				if (!handled) {
					if (hintWindow != null) {
						if (cursorPos == ito && hintWindow.iAllowSelection) {
							handled = true;
							String item = hintWindow.iText[hintWindow.iCurrentPos];
							if (lastMatchedWord.equals(item)) {
								item = "(";
							} else {
								item = item.substring(lastMatchedWord.length(), item.length());
							}
							inputLine = inputLine.substring(0, ito) + item + inputLine.substring(ito, inputLine.length());
							cursorPos += item.length();
							RefreshHintWindow();
							repaint();
							return;
						}
					}
				}
				if (!handled) {
					if (inputLine.length() > 0) {
						AppendHistoryLine(inputLine);
						AddLinesStatic(48, inputPrompt, inputLine);
						if (inputLine.charAt(inputLine.length() - 1) == '\\') {
							gatheredMultiLine = gatheredMultiLine + inputLine.substring(0, inputLine.length() - 1);
						} else {
							PerformRequest("Out> ", gatheredMultiLine + inputLine, true);
						}
						ResetInput();
						RefreshHintWindow();
						repaint(0);
					}
				}
			}
			inputDirty = true;
			repaint();
		} else if (KeyEvent.KEY_TYPED == e.getID()) {
			final int c = e.getKeyChar();
			if (c >= 32 && c < 127) {
				inputLine = new StringBuffer(inputLine).insert(cursorPos, e.getKeyChar()).toString();
				cursorPos++;
				RefreshHintWindow();
				inputDirty = true;
				repaint();
			}
		}
	}

	boolean DirectCommand(final String inputLine) {
		if (inputLine.equals("restart")) {
			stop();
			start();
			return true;
		} else if (inputLine.equals("cls")) {
			clearOutputLines();
			return true;
		} else if (inputLine.equals(":test")) {
			try {
				final Applet dataHub = getAppletContext().getApplet("datahub");
				if (dataHub != null) {
					final net.sf.yacas.DatahubApplet cons = (net.sf.yacas.DatahubApplet) dataHub;
					final String programContentsToLoad = "[" + cons.getTestcode() + "];";
					InvokeCalculationSilent(programContentsToLoad);
				}
			} catch (final Exception e) {
			}
			return true;
		} else if (inputLine.trim().startsWith("?")) {
			final String key = inputLine.trim().substring(1);

			final String prefix = "http://yacas.sourceforge.net/";

			try {
				URI uri = new URI(prefix + "ref.html?" + key);

				if (key.equals("license") || key.equals("licence")) {
					uri = new URI(prefix + "refprogchapter9.html");
				} else if (key.equals("warranty")) {
					uri = new URI(prefix + "refprogchapter9.html#c9s2");
				} else if (key.equals("?")) {
					uri = new URI(prefix + "refmanual.html");
				}

				getAppletContext().showDocument(uri.toURL(), "YacasHelp");

			} catch (final URISyntaxException e) {
				// it's a cold night in Hell
				return false;
			} catch (final MalformedURLException e) {
				// it's a cold night in Hell
				return false;
			}

			return true;
		}

		return false;
	}

	void PerformRequest(final String outputPrompt, final String inputLine, final boolean doRepaint) {
		boolean succeed = false;
		if (DirectCommand(inputLine)) {
			return;
		} else {
			ResetInput();
			RefreshHintWindow();

			calculating = true;
			if (doRepaint) {
				paint(getGraphics());
			}
			outp.delete(0, outp.length());
			try {
				final String response = yacas.Evaluate(inputLine);

				calculating = false;

				AddOutputLine(outp.toString());
				if (yacas.iError != null) {
					AddLinesStatic(48, "Error> ", yacas.iError);
				}
				AddLinesStatic(48, outputPrompt, response);
				succeed = true;
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		{
			if (!succeed) {
				out.println("Request failed");
			}
		}
	}

	void AddLinesStatic(final int indent, final String prompt, String str) {
		int pos;
		while ((pos = str.indexOf('\n')) >= 0) {
			AddLineStatic(indent, prompt, str.substring(0, pos), font, Color.black);
			str = str.substring(pos + 1, str.length());
		}
		if (str.length() > 0) {
			AddLineStatic(indent, prompt, str, font, Color.black);
		}
	}

	public abstract class MOutputLine {
		public abstract void draw(Graphics g, int x, int y);

		public abstract int height(Graphics g);
	}

	class StringLine extends MOutputLine {
		StringLine(final String aText, final Font aFont, final Color aColor) {
			iText = aText;
			iFont = aFont;
			iColor = aColor;
		}

		@Override
		public void draw(final Graphics g, final int x, final int y) {
			g.setColor(iColor);
			g.setFont(iFont);
			final FontMetrics fontMetrics = g.getFontMetrics();
			g.drawString(iText, x, y + fontMetrics.getHeight());
		}

		@Override
		public int height(final Graphics g) {
			g.setFont(iFont);
			final FontMetrics fontMetrics = g.getFontMetrics();
			return fontMetrics.getHeight();
		}

		private final String iText;
		private final Font iFont;
		private final Color iColor;
	}

	class PromptedFormulaLine extends MOutputLine {
		PromptedFormulaLine(final int aIndent, final String aPrompt, final Font aPromptFont, final Color aPromptColor, final String aLine) {
			iIndent = aIndent;
			iPrompt = aPrompt;
			iPromptFont = aPromptFont;
			iPromptColor = aPromptColor;

			final TeXParser parser = new TeXParser();
			expression = parser.parse(aLine);
		}

		SBox expression;

		@Override
		public void draw(final Graphics g, final int x, final int y) {
			final int hgt = height(g);

			{
				g.setColor(iPromptColor);
				g.setFont(iPromptFont);
				final FontMetrics fontMetrics = g.getFontMetrics();
				g.drawString(iPrompt, x, y + fontMetrics.getAscent() + (hgt - fontMetrics.getHeight()) / 2);
			}

			g.setColor(Color.black);
			final GraphicsPrimitives gp = new GraphicsPrimitives(g);
			gp.SetLineThickness(0);
			expression.calculatePositions(gp, 3, new java.awt.Point(x + iIndent, y + expression.getCalculatedAscent() + 10));
			expression.render(gp);
		}

		@Override
		public int height(final Graphics g) {
			if (height == -1) {
				final GraphicsPrimitives gp = new GraphicsPrimitives(g);
				expression.calculatePositions(gp, 3, new java.awt.Point(0, 0));
				height = expression.getDimension().height + 20;
			}
			return height;
		}

		int height = -1;
		int iIndent;
		private final String iPrompt;
		private final Font iPromptFont;
		private final Color iPromptColor;
	}

	class PromptedGraph2DLine extends MOutputLine {
		PromptedGraph2DLine(final int aIndent, final String aPrompt, final Font aPromptFont, final Color aPromptColor, final String aLine) {
			iIndent = aIndent;
			iPromptFont = aPromptFont;
			iPromptColor = aPromptColor;
			iGrapher = new Grapher(aLine);
		}

		Grapher iGrapher;

		@Override
		public void draw(final Graphics g, final int x, final int y) {
			iGrapher.paint(g, x, y, size);
		}

		@Override
		public int height(final Graphics g) {
			return size.height;
		}

		Dimension size = new Dimension(320, 240);
		int iIndent;

	}

	class PromptedStringLine extends MOutputLine {
		PromptedStringLine(final int aIndent, final String aPrompt, final String aText, final Font aPromptFont, final Font aFont, final Color aPromptColor, final Color aColor) {
			iIndent = aIndent;
			iPrompt = aPrompt;
			iText = aText;
			iPromptFont = aPromptFont;
			iFont = aFont;
			iPromptColor = aPromptColor;
			iColor = aColor;
		}

		@Override
		public void draw(final Graphics g, int x, final int y) {
			{
				g.setColor(iPromptColor);
				g.setFont(iPromptFont);
				final FontMetrics fontMetrics = g.getFontMetrics();
				g.drawString(iPrompt, x, y + fontMetrics.getAscent());
				if (iIndent != 0) {
					x += iIndent;
				} else {
					x += fontMetrics.stringWidth(iPrompt);
				}
			}
			{
				g.setColor(iColor);
				g.setFont(iFont);
				final FontMetrics fontMetrics = g.getFontMetrics();
				g.drawString(iText, x, y + fontMetrics.getAscent());
			}
		}

		@Override
		public int height(final Graphics g) {
			g.setFont(iFont);
			final FontMetrics fontMetrics = g.getFontMetrics();
			return fontMetrics.getHeight();
		}

		int iIndent;
		private final String iPrompt;
		private final String iText;
		private final Font iPromptFont;
		private final Font iFont;
		private final Color iPromptColor;
		private final Color iColor;
	}

	class ImageLine extends MOutputLine {
		ImageLine(final Image aImage, final Applet aApplet) {
			iImage = aImage;
			iApplet = aApplet;
		}

		@Override
		public void draw(final Graphics g, final int x, final int y) {
			if (iImage != null) {
				final Dimension d = iApplet.getSize();
				g.drawImage(iImage, (d.width - iImage.getWidth(iApplet)) / 2, y, bkColor, iApplet);
			}
		}

		@Override
		public int height(final Graphics g) {
			return iImage.getHeight(iApplet);
		}

		Image iImage;
		Applet iApplet;
	}

	final static int nrLines = 100;
	MOutputLine lines[] = new MOutputLine[nrLines];
	int currentLine = 0;
	int totalLinesHeight = 0;

	void clearOutputLines() {
		int i;
		for (i = 0; i < nrLines; i++) {
			lines[i] = null;
		}
		totalLinesHeight = 0;
		thumbPos = 0;
		outputDirty = true;
	}

	void addLine(final MOutputLine aLine) {
		{
			CreateOffscreenImage();
			if (lines[currentLine] != null) {
				totalLinesHeight -= lines[currentLine].height(offGra);
			}
			lines[currentLine] = aLine;
			if (lines[currentLine] != null) {
				totalLinesHeight += lines[currentLine].height(offGra);
			}
			currentLine = (currentLine + 1) % nrLines;

			{
				final int canvasHeight = getHeight() - fontHeight - 1;
				if (canvasHeight < totalLinesHeight) {
					final int th = calcThumbHeight();
					thumbPos = canvasHeight - th - 4;
				}
			}
			outputDirty = true;
		}
	}

	void AddLine(final int index, final String text) {
		AddLineStatic(index, text);
		repaint(0);
	}

	void AddLineStatic(final int indent, final String text) {
		AddLineStatic(indent, "", text, font, Color.black);
	}

	Color iPromptColor = new Color(128, 128, 128);
	Font iPromptFont = new Font("Verdana", Font.PLAIN, 12);

	void AddLineStatic(final int indent, final String prompt, final String text, final Font aFont, final Color aColor) {
		addLine(new PromptedStringLine(indent, prompt, text, iPromptFont, aFont, iPromptColor, aColor));
		outputDirty = true;
	}

	// / Drawing current view
	Image yacasLogo = null;
	Image offImg = null;
	Graphics offGra = null;

	@Override
	public void update(final Graphics g) {
		paint(g);
	}

	void CreateOffscreenImage() {
		// draw an offScreen drawing
		final Dimension dim = getSize();
		if (offGra == null) {
			offImg = createImage(dim.width, dim.height);
			offGra = offImg.getGraphics();
		}
	}

	@Override
	public void paint(final Graphics g) {
		CreateOffscreenImage();

		// Render image
		paintToBitmap(offGra);
		// put the OffScreen image OnScreen
		g.drawImage(offImg, 0, 0, null);
		if (hintWindow != null) {
			if (g instanceof Graphics2D) {
				Graphics2D g2d = null;
				g2d = (Graphics2D) g;
				g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
			}
			final YacasGraphicsContext context = new YacasGraphicsContext(g, 0, 0);
			context.SetFontSize(1, fontHeight/*12*/);
			final int nr_total_lines = 1;
			final Dimension d = getSize();
			hintWindow.draw(5, (int) (d.getHeight() - context.FontHeight() - nr_total_lines * context.FontHeight()), context);
		}
	}

	boolean thumbMoused = false;
	int scrollWidth = 16;
	int thumbPos = 0;

	int calcThumbHeight() {
		final int canvasHeight = getHeight() - fontHeight - 1;
		int hgt = ((canvasHeight - 4) * canvasHeight) / totalLinesHeight;
		if (hgt < 16) {
			hgt = 16;
		}
		return hgt;
	}

	Color bkColor = new Color(255, 255, 255);

	public void paintToBitmap(final Graphics g) {
		synchronized (this) {
			if (g instanceof Graphics2D) {
				Graphics2D g2d = null;
				g2d = (Graphics2D) g;
				g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

				g2d.setStroke(new BasicStroke((2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			}

			final FontMetrics metrics = getFontMetrics(font);

			g.setColor(bkColor);
			int yfrom = 0;

			g.setFont(font);
			final int inHeight = fontHeight;

			int yto = getHeight();
			if (!outputDirty) {
				yfrom += getHeight() - inHeight;
			}
			if (!inputDirty) {
				yto -= inHeight;
			}
			g.clearRect(0, yfrom, getWidth(), yto);
			g.setColor(Color.black);

			int i;
			int y = getHeight() - inHeight - g.getFontMetrics().getHeight();

			final int canvasHeight = getHeight() - fontHeight - 1;
			if (outputDirty) {
				y -= totalLinesHeight;
				if (canvasHeight < totalLinesHeight) {
					final int th = calcThumbHeight();
					final double scale = (1.0 * thumbPos) / (canvasHeight - th - 4);
					y += (int) ((1 - scale) * (totalLinesHeight - canvasHeight));
				}
				g.setClip(0, 0, getWidth(), getHeight() - fontHeight - 1);
				for (i = 0; i < nrLines; i++) {
					final int index = (currentLine + i) % nrLines;
					if (lines[index] != null) {
						if (y + lines[index].height(g) > 0) {
							lines[index].draw(g, inset, y);
						}
						y += lines[index].height(g);
					}
				}
				g.setClip(0, 0, getWidth(), getHeight());
				final int w = getWidth();
				// System.out.println("height = "+totalLinesHeight+", screen = "+(canvasHeight));
				if (canvasHeight < totalLinesHeight) {
					final int thumbHeight = calcThumbHeight();
					g.setColor(Color.white);
					g.fillRect(w - scrollWidth, 0, scrollWidth, canvasHeight);

					if (thumbMoused) {
						g.setColor(new Color(192, 192, 240));
					} else {
						g.setColor(new Color(124, 124, 224));
					}

					g.fillRect(w - scrollWidth + 2, thumbPos + 2, scrollWidth - 4, thumbHeight);

					g.setColor(Color.black);
					g.drawRect(w - scrollWidth, 0, scrollWidth, canvasHeight);

					g.drawRect(w - scrollWidth + 2, thumbPos + 2, scrollWidth - 4, thumbHeight);
				}
			}
			y = getHeight() - g.getFontMetrics().getDescent();
			outputDirty = false;
			if (focusGained && !calculating) {
				if (inputDirty) {
					if (y + fontHeight > 0) {
						final int promptLength = metrics.stringWidth(inputPrompt);
						g.setColor(Color.red);
						g.setFont(font);

						g.drawString(inputPrompt, inset, y);
						g.drawString(inputLine, inset + promptLength, y);
						int cursorLocation = promptLength;
						for (i = 0; i < cursorPos; i++) {
							cursorLocation += metrics.charWidth(inputLine.charAt(i));
						}
						y += g.getFontMetrics().getDescent();
						g.setColor(Color.blue);
						g.drawLine(inset + cursorLocation, y - 2, inset + cursorLocation, y - fontHeight + 1);
					}
				}
			} else {
				String toPrint = "Click here to enter an expression";
				if (calculating) {
					toPrint = "Calculating...";
				}

				g.setColor(Color.blue);
				g.setFont(font);

				g.drawString(toPrint, inset, y);
				y += g.getFontMetrics().getDescent();
			}
			inputDirty = false;
		}
	}

	String inputLine = new String();
	String gatheredMultiLine = new String();

	int cursorPos = 0;
	final int inset = 5;

	final static String inputPrompt = "In> ";
	final static String outputPrompt = "Out> ";

	static final int fontHeight = 14;
	private final Font font = new Font("Verdana", Font.PLAIN, fontHeight);

	private static final int nrHistoryLines = 100;
	public static String history[] = new String[nrHistoryLines];
	public static int currentHistoryLine = 0;
	static int historyBrowse = 0;

	boolean inputDirty = true;
	boolean outputDirty = true;

	class AppletOutput {
		public AppletOutput(final ConsoleApplet aApplet) {
			iApplet = aApplet;
		}

		ConsoleApplet iApplet;

		public void write(final int c) throws IOException {
			if (c == '\n') {
				iApplet.AddLineStatic(0, buffer.toString());
				buffer = new StringBuffer();
			} else {
				buffer.append((char) c);
			}
		}

		public void print(final String s) {
			try {
				int i, nr;
				nr = s.length();
				for (i = 0; i < nr; i++) {
					write(s.charAt(i));
				}
			} catch (final IOException e) {
			}
		}

		public void println(final Exception e) {
			println(e.getMessage());
		}

		public void println(final String s) {
			print(s);
			print("\n");
		}

		StringBuffer buffer = new StringBuffer();
	}

	HintWindow hintWindow = null;
	Hints the_hints = new Hints();

	void LoadHints(final String filename) {
		final CDataReader file = new CDataReader();
		int opened = 0;
		try {
			final URL url = new URL(filename);
			opened = file.Open(url);
		} catch (final Exception e) {
		}
		if (opened != 0) {
			String line = file.ReadLine();
			final String[] tokens = new String[16];
			int nrTokens = 0;
			while (line != null) {
				if (line.substring(0, 2).equals("::")) {
					break;
				}
				int i = 0;
				nrTokens = 0;
				while (i < line.length()) {
					final int start = i;
					while (line.charAt(i) != ':') {
						i++;
					}
					tokens[nrTokens] = line.substring(start, i);
					nrTokens++;
					i++;
				}
				if (nrTokens > 3) {
					final HintItem hi = new HintItem();
					hi.base = tokens[1];
					hi.hint = tokens[2];
					hi.description = tokens[3];
					the_hints.hintTexts[the_hints.nrHintTexts] = hi;
					the_hints.nrHintTexts++;
				}

				line = file.ReadLine();
			}
			file.Close();
		} else {
			out.println("could not read hints");
		}
	}

	HintWindow CreateHints(final int fontsize) {
		final HintWindow hw = new HintWindow(fontsize);
		return hw;
	}

	void AddHintLine(final HintWindow hints, final String aText, final String aDescription) {
		hints.AddLine(aText);
		if (aDescription.length() > 0) {
			hints.AddDescription(aDescription);
		}
	}

	HintWindow TryToHint(final String text, final int length) {
		HintWindow hints = null;
		final int nrhints = the_hints.nrHintTexts;
		int i, start;
		start = 0;
		if (start < 0) {
			return null;
		}
		for (i = start; i < nrhints; i++) {
			if (text.charAt(0) > the_hints.hintTexts[i].base.charAt(0)) {
				continue;
			}
			if (text.charAt(0) < the_hints.hintTexts[i].base.charAt(0)) {
				continue;
			}
			final int baselen = the_hints.hintTexts[i].base.length();
			if (length == baselen) {
				if (text.substring(0, baselen).equals(the_hints.hintTexts[i].base)) {
					if (hints == null) {
						hints = CreateHints(12 /*iDefaultFontSize*/);
						hints.iAllowSelection = false;
					}
					AddHintLine(hints, the_hints.hintTexts[i].hint, the_hints.hintTexts[i].description);
				}
			}
		}
		return hints;
	}

	String lastMatchedWord = "";
	String matchToInsert = "";
	int ito = -1;

	void RefreshHintWindow() {
		ito = cursorPos;

		while (true) {
			if (ito == inputLine.length()) {
				break;
			}
			if (!LispTokenizer.IsAlpha(inputLine.charAt(ito))) {
				break;
			}
			ito++;
		}
		if (ito > 0) {
			final int c = inputLine.charAt(ito - 1);
			if (c == ',' || c == ')') {
				int braces = -1;
				if (c == ')') {
					ito--;
					braces = -2;
				}
				while (braces != 0) {
					if (ito <= 0) {
						break;
					}
					if (inputLine.charAt(ito - 1) == '(') {
						braces++;
					}
					if (inputLine.charAt(ito - 1) == ')') {
						braces--;
					}
					ito--;
				}
			}
		}
		if (ito > 0) {
			if (inputLine.charAt(ito - 1) == '(') {
				ito--;
			}
		}
		if (ito == 0) {
			while (true) {
				if (ito == cursorPos) {
					break;
				}
				if (!LispTokenizer.IsAlpha(inputLine.charAt(ito))) {
					break;
				}
				ito++;
			}
		}
		int ifrom = ito;
		while (true) {
			if (ifrom == 0) {
				break;
			}
			final char c = inputLine.charAt(ifrom - 1);
			if (!LispTokenizer.IsAlpha(c) && !LispTokenizer.IsDigit(c)) {
				break;
			}
			ifrom--;
		}
		// Name of function *has* to start with alphabetic letter
		while (ifrom < ito && LispTokenizer.IsDigit(inputLine.charAt(ifrom))) {
			ifrom++;
		}

		matchToInsert = "";
		lastMatchedWord = "";
		if (ito > ifrom) {
			lastMatchedWord = inputLine.substring(ifrom, ito);
		}

		hintWindow = null;
		if (lastMatchedWord.length() > 0) {
			// System.out.println("word is "+word);

			final int nr = lastMatchedWord.length();
			final int maxHintLines = 18;
			final String texts[] = new String[maxHintLines + 1];
			int nrHintLines = 0;

			int i;
			for (i = 0; i < the_hints.nrHintTexts; i++) {
				if (nrHintLines == maxHintLines) {
					break;
				}

				if (nr <= (the_hints.hintTexts[i].base).length() && lastMatchedWord.equals(the_hints.hintTexts[i].base.substring(0, nr))) {
					boolean add = true;
					if (nrHintLines > 0) {
						if (texts[nrHintLines - 1].equals(the_hints.hintTexts[i].base)) {
							add = false;
						}
					}
					if (add) {
						texts[nrHintLines++] = the_hints.hintTexts[i].base;
					}
					// Exact match, keep this one line
					if (nrHintLines == 1 && ito != cursorPos && lastMatchedWord.equals(the_hints.hintTexts[i].base)) {
						break;
					}
				}
			}

			if (nrHintLines == maxHintLines) {
				texts[nrHintLines++] = "...";
			}
			if (nrHintLines == 1) {
				if (lastMatchedWord.length() < texts[0].length()) {
					matchToInsert = texts[0].substring(lastMatchedWord.length(), texts[0].length());
				}
				hintWindow = TryToHint(texts[0], texts[0].length());
			} else if (nrHintLines > 1) {
				hintWindow = CreateHints(12);
				hintWindow.iAllowSelection = true;

				for (i = 0; i < nrHintLines; i++) {
					AddHintLine(hintWindow, texts[i], "");
				}
			}
		}
	}

	public void InvokeCalculation(final String expression) {
		if (!gotDatahubInit) {
			start();
		}
		AppendHistoryLine(expression);
		AddLinesStatic(48, "In> ", expression);
		ResetInput();
		RefreshHintWindow();
		inputDirty = true;
		outputDirty = true;
		PerformRequest("Out> ", expression, false);
		inputDirty = true;
		outputDirty = true;
		repaint();
	}

	String lastError;

	public String calculate(final String expression) {
		if (!gotDatahubInit) {
			start();
		}
		try {
			final String result = yacas.Evaluate(expression);

			lastError = yacas.iError;
			return result;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getLastError() {
		if (lastError != null) {
			return lastError;
		} else {
			return "";
		}
	}

	private void AddOutputLine(String outp) {
		if (outp.length() > 0) {
			int dollarPos = outp.indexOf('$');
			while (dollarPos >= 0) {
				// Print plain text before the dollared content
				if (dollarPos > 0) {
					AddLinesStatic(48, "", outp.substring(0, dollarPos));
				}
				// Strip off the left dollar sign
				outp = outp.substring(dollarPos + 1, outp.length());

				// Find the right dollar sign, and split there too
				dollarPos = outp.indexOf('$');
				String dollared = outp.substring(0, dollarPos);
				outp = outp.substring(dollarPos + 1, outp.length());

				// System.out.println("Dollared: "+dollared);
				final int plotPos = dollared.indexOf("plot2d:");
				if (plotPos >= 0) {
					dollared = dollared.substring(plotPos + 7);
					// System.out.println("Plotting: ["+dollared+"]");
					addLine(new PromptedGraph2DLine(48, "Out>", iPromptFont, iPromptColor, dollared));
				} else {
					addLine(new PromptedFormulaLine(48, "Out>", iPromptFont, iPromptColor, dollared));
				}
				dollarPos = outp.indexOf('$');
			}
			// If there is some plain text left at the end, print
			if (outp.length() > 0) {
				AddLinesStatic(48, "", outp);
			}
		}
		outputDirty = true;
	}

	public void AddInputLine(final String expression) {
		synchronized (this) {
			if (!gotDatahubInit) {
				start();
			}
			AppendHistoryLine(expression);
			AddLinesStatic(48, "In> ", expression);
			ResetInput();
			RefreshHintWindow();
			inputDirty = true;
			outputDirty = true;
			calculating = true;
		}
		repaint();
	}

	public void InvokeCalculationSilent(final String expression) throws Exception {
		synchronized (this) {
			if (DirectCommand(expression)) {
				return;
			} else {
				outp.delete(0, outp.length());
				yacas.Evaluate(expression);
				calculating = false;
				AddOutputLine(outp.toString());
				if (yacas.iError != null) {
					AddLinesStatic(48, "Error> ", yacas.iError);
				}

				ResetInput();
				RefreshHintWindow();
				inputDirty = true;
				outputDirty = true;
			}
		}
		repaint();
	}

	public void StopCurrentCalculation() {
		yacas.env.iEvalDepth = yacas.env.iMaxEvalDepth + 100;
	}
}
