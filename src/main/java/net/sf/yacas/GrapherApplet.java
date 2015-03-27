package net.sf.yacas;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GrapherApplet extends java.applet.Applet implements KeyListener {

	private static final long serialVersionUID = -4810408968163307085L;
	Dimension offDimension;
	Image offImage;
	Graphics offGraphics;

	Grapher grapher;

	String iRenderOperations;

	@Override
	public void init() {
		iRenderOperations = getParameter("CallList");
		if (iRenderOperations == null) {
			iRenderOperations = "";
		}
		grapher = new Grapher(iRenderOperations);
		addKeyListener(this);
		repaint();
	}

	public void SetupCallList(final String aCallList) {
		if (grapher != null) {
			grapher.SetupCallList(aCallList);
			offGraphics = null;
			repaint();
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		final double scf = 1.05;
		switch (e.getKeyChar()) {
		case 'o':
		case 'O':
			grapher.xmin *= scf;
			grapher.xmax *= scf;
			offImage = null;
			offGraphics = null;
			repaint();
			break;
		case 'p':
		case 'P':
			grapher.xmin /= scf;
			grapher.xmax /= scf;
			offImage = null;
			offGraphics = null;
			repaint();
			break;

		case 'a':
		case 'A':
			grapher.ymin *= scf;
			grapher.ymax *= scf;
			offImage = null;
			offGraphics = null;
			repaint();
			break;
		case 'z':
		case 'Z':
			grapher.ymin /= scf;
			grapher.ymax /= scf;
			offImage = null;
			offGraphics = null;
			repaint();
			break;
		}
	}

	@Override
	public void start() {
		repaint();
	}

	@Override
	public void stop() {
		offImage = null;
		offGraphics = null;
	}

	void drawToOffscreen() {
		// Create the offscreen graphics context
		final Dimension d = getSize();
		if ((offGraphics == null) || (d.width != offDimension.width) || (d.height != offDimension.height)) {
			offDimension = d;
			offImage = createImage(d.width, d.height);
			offGraphics = offImage.getGraphics();
			paintFrame(offGraphics);
		}
	}

	@Override
	public void update(final Graphics g) {
		drawToOffscreen();

		// Paint the frame into the image
		synchronized (offImage) {
			// Paint the image onto the screen
			g.drawImage(offImage, 0, 0, null);
		}
	}

	@Override
	public void paint(final Graphics g) {
		drawToOffscreen();
		if (offImage != null) {
			synchronized (offImage) {
				g.drawImage(offImage, 0, 0, null);
			}
		}
	}

	synchronized public void paintFrame(final Graphics g) {
		final Dimension d = getSize();
		grapher.paint(g, 0, 0, d);
	}
};
