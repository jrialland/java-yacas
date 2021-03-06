package net.sf.yacas;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

public class FormulaViewApplet extends Applet {

	private static final long serialVersionUID = -2930040025227120219L;

	@Override
	public void init() {
		setBackground(Color.white);
		setLayout(null);
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

	Image offImage = null;
	Graphics offGraphics = null;
	Dimension offDimension = null;

	@Override
	public void update(final Graphics g) {
		final Dimension d = getSize();
		// Create the offscreen graphics context
		if ((offGraphics == null) || (d.width != offDimension.width) || (d.height != offDimension.height)) {
			offDimension = d;
			offImage = createImage(d.width, d.height);
			offGraphics = offImage.getGraphics();
			// Paint the frame into the image
			paintFrame(offGraphics);
		}

		// Paint the image onto the screen
		g.drawImage(offImage, 0, 0, null);
	}

	/**
	 * Paint the previous frame (if any).
	 */
	@Override
	public void paint(final Graphics g) {
		// System.out.println("paint");
		final Dimension d = getSize();
		if ((offGraphics == null) || (d.width != offDimension.width) || (d.height != offDimension.height)) {
			offDimension = d;
			offImage = createImage(d.width, d.height);
			offGraphics = offImage.getGraphics();

			// Paint the frame into the image
			paintFrame(offGraphics);
		}
		if (offImage != null) {
			g.drawImage(offImage, 0, 0, null);
		}
	}

	void paintFrame(final Graphics g) {
		// System.out.println("paintFrame");
		// Tell the rendering system we'd like to have anti-aliasing please
		if (g instanceof Graphics2D) {
			Graphics2D g2d = null;
			g2d = (Graphics2D) g;
			g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		}

		// Clear Background
		final Dimension d = getSize();
		// g.setColor(Color.white);
		// g.fillRect(0, 0, d.width, d.height);

		// All graphics should be black from now on
		g.setColor(Color.black);

		final GraphicsPrimitives gp = new GraphicsPrimitives(g);

		gp.SetLineThickness(0);

		if (expression == null) {
			final String s = getParameter("expression");
			if (s != null) {
				System.out.println("re-rendering the whole formula!");
				final TeXParser parser = new TeXParser();
				expression = parser.parse(s);
			}
		}
		if (expression != null) {
			expression.calculatePositions(gp, 3, new java.awt.Point(1, d.height / 2));
			expression.render(gp);
		}
	}

	SBox expression = null;
}
