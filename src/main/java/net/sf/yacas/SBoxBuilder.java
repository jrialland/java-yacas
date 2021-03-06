package net.sf.yacas;

import java.awt.Dimension;
import java.awt.Point;

class SBoxBuilder {
	class SBoxSymbolName extends SBox {
		SBoxSymbolName(final String aSymbol) {
			iSymbol = aSymbol;
			if (iSymbol.indexOf('\\') == 0) {
				if (iSymbol.equals("\\pi")) {
				} else if (iSymbol.equals("\\infty")) {
				} else if (iSymbol.equals("\\cdot")) {
				} else if (iSymbol.equals("\\wedge")) {
				} else if (iSymbol.equals("\\vee")) {
				} else if (iSymbol.equals("\\neq")) {
				} else {
					iSymbol = iSymbol.substring(1);
				}
			}
		}

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			final int height = SBoxBuilder.FontForSize(aSize);
			g.SetFontSize(height);

			iSize = aSize;
			iPosition = aPosition;
			if (iSymbol.equals("\\pi") || iSymbol.equals("\\wedge") || iSymbol.equals("\\vee")) {
				iDimension = new Dimension(g.TextWidth("M"), height);
				iAscent = g.GetAscent();
			} else if (iSymbol.equals("\\neq")) {
				iDimension = new Dimension(g.TextWidth("="), height);
				iAscent = g.GetAscent();
			} else if (iSymbol.equals("\\infty")) {
				iDimension = new Dimension(g.TextWidth("oo"), height);
				iAscent = g.GetAscent();
			} else if (iSymbol.equals("\\cdot")) {
				iDimension = new Dimension(g.TextWidth("."), height);
				iAscent = g.GetAscent();
			} else {
				iAscent = g.GetAscent();
				iDimension = new Dimension(g.TextWidth(iSymbol), height);
			}
		}

		@Override
		public void render(final GraphicsPrimitives g) {
			if (iSymbol.equals("\\pi")) {
				final double deltax = 0.15 * iDimension.width;
				final double deltay = 0.2 * iDimension.height;

				g.DrawLine((int) (iPosition.x + 1 * deltax), (int) (iPosition.y - iAscent + 2 * deltay), (int) (iPosition.x + iDimension.width - 1 * deltax), (int) (iPosition.y - iAscent + 2 * deltay));
				g.DrawLine((int) (iPosition.x + 2 * deltax), (int) (iPosition.y - iAscent + 2 * deltay), (int) (iPosition.x + 2 * deltax), (int) (iPosition.y - iAscent + iDimension.height + 0 * deltay));
				g.DrawLine((int) (iPosition.x + iDimension.width - 2 * deltax), (int) (iPosition.y - iAscent + 2 * deltay), (int) (iPosition.x + iDimension.width - 2 * deltax), (int) (iPosition.y - iAscent + iDimension.height + 0 * deltay));
			} else if (iSymbol.equals("\\wedge") || iSymbol.equals("\\vee")) {
				final double deltax = 0.15 * iDimension.width;
				final double deltay = 0.2 * iDimension.height;
				int ytip = (int) (iPosition.y - iAscent + iDimension.height + 0 * deltay);
				int ybase = (int) (iPosition.y - iAscent + 2 * deltay);
				if (iSymbol.equals("\\wedge")) {
					final int swap = ytip;
					ytip = ybase;
					ybase = swap;
				}
				g.DrawLine((int) (iPosition.x + 1 * deltax), ybase, iPosition.x + iDimension.width / 2, ytip);
				g.DrawLine((int) (iPosition.x + iDimension.width - 1 * deltax), ybase, iPosition.x + iDimension.width / 2, ytip);
			} else if (iSymbol.equals("\\neq")) {
				g.SetFontSize(SBoxBuilder.FontForSize(iSize));
				g.DrawText("=", iPosition.x, iPosition.y);
				g.DrawLine(iPosition.x + (2 * iDimension.width) / 3, iPosition.y - iAscent + (2 * iDimension.height) / 6, iPosition.x + (1 * iDimension.width) / 3, iPosition.y - iAscent + (6 * iDimension.height) / 6);
			} else if (iSymbol.equals("\\infty")) {
				g.SetFontSize(SBoxBuilder.FontForSize(iSize));
				g.DrawText("o", iPosition.x + 1, iPosition.y);
				g.DrawText("o", iPosition.x + g.TextWidth("o") - 2, iPosition.y);
			} else if (iSymbol.equals("\\cdot")) {
				final int height = SBoxBuilder.FontForSize(iSize);
				g.SetFontSize(height);
				g.DrawText(".", iPosition.x, iPosition.y - height / 3);
			} else {
				g.SetFontSize(SBoxBuilder.FontForSize(iSize));
				g.DrawText(iSymbol, iPosition.x, iPosition.y);
			}
		}

		public String iSymbol;
	}

	abstract class SBoxCompoundExpression extends SBox {
		SBoxCompoundExpression(final int aNrSubExpressions) {
			iExpressions = new SBox[aNrSubExpressions];
		}

		SBox iExpressions[];

		@Override
		public void render(final GraphicsPrimitives g) {
			// drawBoundingBox(g);
			int i;
			for (i = 0; i < iExpressions.length; i++) {
				if (iExpressions[i] != null) {
					iExpressions[i].render(g);
				}
			}
		}

		@Override
		public void drawBoundingBox(final GraphicsPrimitives g) {
			g.SetLineThickness(0);
			final int x0 = iPosition.x;
			final int y0 = iPosition.y - getCalculatedAscent();
			final int x1 = x0 + iDimension.width;
			final int y1 = y0 + iDimension.height;
			g.DrawLine(x0, y0, x1, y0);
			g.DrawLine(x1, y0, x1, y1);
			g.DrawLine(x1, y1, x0, y1);
			g.DrawLine(x0, y1, x0, y0);
			int i;
			for (i = 0; i < iExpressions.length; i++) {
				if (iExpressions[i] != null) {
					iExpressions[i].drawBoundingBox(g);
				}
			}
		}
	}

	class SBoxSubSuperfix extends SBoxCompoundExpression {
		SBoxSubSuperfix(final SBox aExpr, final SBox aSuperfix, final SBox aSubfix) {
			super(3);
			iExpressions[0] = aExpr;
			iExpressions[1] = aSuperfix;
			iExpressions[2] = aSubfix;
		}

		void setSuperfix(final SBox aExpression) {
			iExpressions[1] = aExpression;
		}

		void setSubfix(final SBox aExpression) {
			iExpressions[2] = aExpression;
		}

		boolean hasSuperfix() {
			return (iExpressions[1] != null);
		}

		int iExtent = 0;
		int iSuperOffset = 0;
		int iSubOffset = 0;

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			iSize = aSize;
			iPosition = aPosition;

			// Get dimensions first
			if (iDimension == null) {
				Dimension dsfix = new Dimension(0, 0);
				Dimension dlfix = new Dimension(0, 0);
				iExpressions[0].calculatePositions(g, aSize, null);
				if (iExpressions[1] != null) {
					iExpressions[1].calculatePositions(g, aSize - 1, null);
				}
				if (iExpressions[2] != null) {
					iExpressions[2].calculatePositions(g, aSize - 1, null);
				}
				final Dimension dexpr = iExpressions[0].getDimension();
				if (iExpressions[1] != null) {
					dsfix = iExpressions[1].getDimension();
				}
				if (iExpressions[2] != null) {
					dlfix = iExpressions[2].getDimension();
				}

				if (iExpressions[0] instanceof SBoxSum || iExpressions[0] instanceof SBoxInt) {
					iSuperOffset = 0;
					iSubOffset = 0;
					if (iExpressions[1] != null) {
						iExtent = iExtent + iExpressions[1].iAscent;
					}
					if (iExpressions[2] != null) {
						iExtent = iExtent + iExpressions[2].iAscent;
					}

					int fixMaxWidth = dsfix.width;
					if (dlfix.width > fixMaxWidth) {
						fixMaxWidth = dlfix.width;
					}
					if (dexpr.width > fixMaxWidth) {
						fixMaxWidth = dexpr.width;
					}
					iDimension = new Dimension(fixMaxWidth, dexpr.height + iExtent);
				} else {
					if (iExpressions[1] != null) {
						iSuperOffset = iExpressions[1].getDimension().height - iExpressions[1].iAscent - iExpressions[0].getDimension().height / 4;
						iExtent = iExtent + iSuperOffset + iExpressions[1].iAscent;
					}
					if (iExpressions[2] != null) {
						iSubOffset = iExpressions[2].iAscent;

						final int delta = iSubOffset + (iExpressions[2].getDimension().height - iExpressions[2].iAscent) - (iExpressions[0].getDimension().height - iExpressions[0].iAscent);
						iExtent = iExtent + delta;

					}

					int fixMaxWidth = dsfix.width;
					if (dlfix.width > fixMaxWidth) {
						fixMaxWidth = dlfix.width;
					}
					iDimension = new Dimension(dexpr.width + fixMaxWidth, dexpr.height + iExtent);
				}
				iAscent = iExpressions[0].getCalculatedAscent() + iExtent;

				if (iExpressions[2] != null) {
					iAscent = iAscent - iExpressions[2].getDimension().height;
				}
			}
			if (aPosition != null) {
				Dimension dsfix = new Dimension(0, 0);
				Dimension dlfix = new Dimension(0, 0);
				final Dimension dexpr = iExpressions[0].getDimension();
				if (iExpressions[1] != null) {
					dsfix = iExpressions[1].getDimension();
				}
				if (iExpressions[2] != null) {
					dlfix = iExpressions[2].getDimension();
				}
				iExpressions[0].calculatePositions(g, aSize, new Point(aPosition.x, aPosition.y));

				if (iExpressions[0] instanceof SBoxSum || iExpressions[0] instanceof SBoxInt) {
					if (iExpressions[1] != null) {
						iExpressions[1].calculatePositions(g, aSize - 1, new Point(aPosition.x, aPosition.y - iExpressions[0].iAscent - dsfix.height));
					}
					if (iExpressions[2] != null) {
						iExpressions[2].calculatePositions(g, aSize - 1, new Point(aPosition.x, aPosition.y + iExpressions[2].iAscent + dlfix.height));
					}
				} else {
					if (iExpressions[1] != null) {
						iExpressions[1].calculatePositions(g, aSize - 1, new Point(aPosition.x + dexpr.width, aPosition.y - iExpressions[0].iAscent - iSuperOffset));
					}
					if (iExpressions[2] != null) {
						iExpressions[2].calculatePositions(g, aSize - 1, new Point(aPosition.x + dexpr.width, aPosition.y + iSubOffset));
					}
				}
			}
		}
	}

	class SBoxGrid extends SBoxCompoundExpression {
		SBoxGrid(final int aWidth, final int aHeight) {
			super(aWidth * aHeight);
			iWidth = aWidth;
			iHeight = aHeight;
		}

		void SetSBox(final int x, final int y, final SBox aExpression) {
			iExpressions[x + iWidth * y] = aExpression;
		}

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			final int spacing = 12;

			iSize = aSize;
			iPosition = aPosition;

			// Get dimensions first
			if (iDimension == null) {
				int i, j;
				for (i = 0; i < iWidth * iHeight; i++) {
					iExpressions[i].calculatePositions(g, aSize, null);
				}
				iWidths = new int[iWidth];
				iHeights = new int[iHeight];
				for (i = 0; i < iWidth; i++) {
					iWidths[i] = 0;
				}
				for (i = 0; i < iHeight; i++) {
					iHeights[i] = 0;
				}

				for (i = 0; i < iWidth; i++) {
					for (j = 0; j < iHeight; j++) {
						final Dimension d = iExpressions[i + iWidth * j].getDimension();

						if (iWidths[i] < d.width) {
							iWidths[i] = d.width;
						}
						if (iHeights[j] < d.height) {
							iHeights[j] = d.height;
						}
					}
				}
				int totalWidth = 0;
				for (i = 0; i < iWidth; i++) {
					totalWidth = totalWidth + iWidths[i];
				}

				int totalHeight = 0;
				for (j = 0; j < iHeight; j++) {
					totalHeight = totalHeight + iHeights[j];
				}
				iDimension = new Dimension(totalWidth + spacing * (iWidth), totalHeight + spacing * (iHeight));
				iAscent = iDimension.height / 2;
			}
			if (aPosition != null) {
				int i, j;
				int h = -iAscent;
				for (j = 0; j < iHeight; j++) {
					int maxAscent = -10000;
					for (i = 0; i < iWidth; i++) {
						if (maxAscent < iExpressions[i + j * iWidth].iAscent) {
							maxAscent = iExpressions[i + j * iWidth].iAscent;
						}
					}
					h = h + maxAscent;
					int w = 0;
					for (i = 0; i < iWidth; i++) {
						iExpressions[i + j * iWidth].calculatePositions(g, aSize, new Point(aPosition.x + w, aPosition.y + h));
						w += iWidths[i] + spacing;
					}
					h = h - maxAscent;
					h = h + iHeights[j] + spacing;
				}
			}
		}

		int iWidth;
		int iHeight;
		int iWidths[];
		int iHeights[];
	}

	class SBoxPrefixOperator extends SBoxCompoundExpression {
		SBoxPrefixOperator(final SBox aLeft, final SBox aRight) {
			super(2);
			iExpressions[0] = aLeft;
			iExpressions[1] = aRight;
		}

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			iSize = aSize;
			iPosition = aPosition;

			// Get dimensions first
			if (iDimension == null) {
				iExpressions[0].calculatePositions(g, aSize, null);
				iExpressions[1].calculatePositions(g, aSize, null);

				final Dimension dleft = iExpressions[0].getDimension();
				final Dimension dright = iExpressions[1].getDimension();
				int height = dleft.height;
				if (height < dright.height) {
					height = dright.height;
				}
				iDimension = new Dimension(dleft.width + dright.width + 2, height);
				iAscent = iExpressions[0].getCalculatedAscent();
				if (iAscent < iExpressions[1].getCalculatedAscent()) {
					iAscent = iExpressions[1].getCalculatedAscent();
				}
			}
			if (aPosition != null) {
				final Dimension dleft = iExpressions[0].getDimension();

				iExpressions[0].calculatePositions(g, aSize, new Point(aPosition.x, aPosition.y/*+(iAscent-iExpressions[0].getCalculatedAscent())*/));
				iExpressions[1].calculatePositions(g, aSize, new Point(aPosition.x + dleft.width + 2, aPosition.y/*+(iAscent-iExpressions[1].getCalculatedAscent())*/));
			}
		}
	}

	class SBoxInfixOperator extends SBoxCompoundExpression {
		SBoxInfixOperator(final SBox aLeft, final SBox aInfix, final SBox aRight) {
			super(3);
			iExpressions[0] = aLeft;
			iExpressions[1] = aInfix;
			iExpressions[2] = aRight;
		}

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			iSize = aSize;
			iPosition = aPosition;

			// Get dimensions first
			if (iDimension == null) {
				iExpressions[0].calculatePositions(g, aSize, null);
				iExpressions[1].calculatePositions(g, aSize, null);
				iExpressions[2].calculatePositions(g, aSize, null);

				final Dimension dleft = iExpressions[0].getDimension();
				final Dimension dinfix = iExpressions[1].getDimension();
				final Dimension dright = iExpressions[2].getDimension();
				int height = dleft.height;
				if (height < dinfix.height) {
					height = dinfix.height;
				}
				if (height < dright.height) {
					height = dright.height;
				}
				iDimension = new Dimension(dleft.width + dinfix.width + dright.width + 4, height);
				iAscent = iExpressions[0].getCalculatedAscent();
				if (iAscent < iExpressions[1].getCalculatedAscent()) {
					iAscent = iExpressions[1].getCalculatedAscent();
				}
				if (iAscent < iExpressions[2].getCalculatedAscent()) {
					iAscent = iExpressions[2].getCalculatedAscent();
				}
			}
			if (aPosition != null) {
				final Dimension dleft = iExpressions[0].getDimension();
				final Dimension dinfix = iExpressions[1].getDimension();

				iExpressions[0].calculatePositions(g, aSize, new Point(aPosition.x, aPosition.y));
				iExpressions[1].calculatePositions(g, aSize, new Point(aPosition.x + dleft.width + 2, aPosition.y));
				iExpressions[2].calculatePositions(g, aSize, new Point(aPosition.x + dleft.width + dinfix.width + 4, aPosition.y));
			}
		}
	}

	class SBoxDivisor extends SBoxCompoundExpression {
		SBoxDivisor(final SBox aNumerator, final SBox aDenominator) {
			super(2);
			iExpressions[0] = aNumerator;
			iExpressions[1] = aDenominator;
		}

		int iDashheight = 0;

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			iSize = aSize;
			iPosition = aPosition;

			iDashheight = SBoxBuilder.FontForSize(iSize);

			if (iDimension == null) {
				iExpressions[0].calculatePositions(g, aSize, null);
				iExpressions[1].calculatePositions(g, aSize, null);
				final Dimension ndim = iExpressions[0].getDimension();
				final Dimension ddim = iExpressions[1].getDimension();
				int width = ndim.width;
				if (width < ddim.width) {
					width = ddim.width;
				}
				iDimension = new Dimension(width, ndim.height + ddim.height + iDashheight);
				iAscent = ndim.height + iDashheight;
			}
			if (aPosition != null) {
				final Dimension ndim = iExpressions[0].getDimension();
				final Dimension ddim = iExpressions[1].getDimension();

				final int ynumer = aPosition.y - ndim.height + iExpressions[0].getCalculatedAscent() - iDashheight;
				final int ydenom = aPosition.y + iExpressions[1].getCalculatedAscent();
				iExpressions[0].calculatePositions(g, aSize, new java.awt.Point(aPosition.x + (iDimension.width - ndim.width) / 2, ynumer));
				iExpressions[1].calculatePositions(g, aSize, new java.awt.Point(aPosition.x + (iDimension.width - ddim.width) / 2, ydenom));
			}
		}

		@Override
		public void render(final GraphicsPrimitives g) {
			super.render(g);

			final java.awt.Dimension ndim = iExpressions[0].getDimension();
			final java.awt.Dimension ddim = iExpressions[1].getDimension();
			int width = ndim.width;
			if (width < ddim.width) {
				width = ddim.width;
			}

			g.SetLineThickness(1);
			g.DrawLine(iPosition.x, iPosition.y - iDashheight / 2 + 2, iPosition.x + width, iPosition.y - iDashheight / 2 + 2);
		}
	}

	class SBoxSum extends SBox {
		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			final int height = SBoxBuilder.FontForSize(aSize);
			g.SetFontSize(height);
			iSize = aSize;
			iPosition = aPosition;
			iAscent = height / 2 + g.GetAscent();
			iDimension = new Dimension((4 * height) / 3, 2 * height);
		}

		@Override
		public void render(final GraphicsPrimitives g) {
			final int height = SBoxBuilder.FontForSize(iSize);
			g.SetLineThickness(2);
			final int x0 = iPosition.x;
			final int y0 = iPosition.y - iAscent;
			final int x1 = x0 + iDimension.width;
			final int y1 = y0 + iDimension.height;
			g.DrawLine(x1, y0, x0, y0);
			g.DrawLine(x0, y0, x0 + (2 * height) / 4, (y0 + y1) / 2);
			g.DrawLine(x0 + (2 * height) / 4, (y0 + y1) / 2, x0, y1);
			g.DrawLine(x0, y1, x1, y1);
		}
	}

	class SBoxInt extends SBox {
		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			final int height = SBoxBuilder.FontForSize(aSize);
			g.SetFontSize(height);
			iSize = aSize;
			iPosition = aPosition;
			iAscent = height / 2 + g.GetAscent();
			iDimension = new Dimension((1 * height) / 2, 2 * height);
		}

		@Override
		public void render(final GraphicsPrimitives g) {

			g.SetLineThickness(2);
			final int x0 = iPosition.x;
			final int y0 = iPosition.y - iAscent;
			final int x1 = x0 + iDimension.width;

			g.DrawLine(x1, y0, x1 - iDimension.width / 4, y0);
			g.DrawLine(x1 - iDimension.width / 4, y0, x1 - (2 * iDimension.width) / 4, y0 + iDimension.width / 4);

			g.DrawLine(x1 - (2 * iDimension.width) / 4, y0 + iDimension.width / 4, x1 - (2 * iDimension.width) / 4, y0 + iDimension.height - iDimension.width / 4);

			g.DrawLine(x1 - (2 * iDimension.width) / 4, y0 + iDimension.height - iDimension.width / 4, x1 - (3 * iDimension.width) / 4, y0 + iDimension.height);
			g.DrawLine(x1 - (3 * iDimension.width) / 4, y0 + iDimension.height, x0, y0 + iDimension.height);

		}
	}

	class SBoxSquareRoot extends SBoxCompoundExpression {
		SBoxSquareRoot(final SBox aExpression) {
			super(1);
			iExpressions[0] = aExpression;
		}

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			iSize = aSize;
			iPosition = aPosition;

			if (iDimension == null) {
				iExpressions[0].calculatePositions(g, aSize, null);
				final Dimension dim = iExpressions[0].getDimension();
				iDimension = new Dimension((dim.width + 6), dim.height + 3);
				iAscent = iExpressions[0].getCalculatedAscent() + 3;
			}
			if (aPosition != null) {

				iExpressions[0].calculatePositions(g, aSize, new java.awt.Point((aPosition.x + 6), aPosition.y));
			}
		}

		@Override
		public void render(final GraphicsPrimitives g) {
			super.render(g);
			g.SetLineThickness(1);
			final Dimension dim = iExpressions[0].getDimension();
			final int x0 = iPosition.x;
			final int y0 = iPosition.y - iAscent;
			final int x1 = x0 + dim.width + 6;
			final int y1 = y0 + dim.height + 6;
			g.DrawLine(x0, y0 + 1, x0 + 3, y1 - 1);
			g.DrawLine(x0 + 3, y1 - 1, x0 + 6, y0 + 2);
			g.DrawLine(x0 + 6, y0 + 1, x1, y0 + 1);
		}
	}

	class SBoxBracket extends SBoxCompoundExpression {
		SBoxBracket(final SBox aExpression, final String aOpen, final String aClose) {
			super(1);
			iOpen = aOpen;
			iClose = aClose;
			iExpressions[0] = aExpression;
		}

		@Override
		public void calculatePositions(final GraphicsPrimitives g, final int aSize, final java.awt.Point aPosition) {
			iSize = aSize;
			iPosition = aPosition;

			if (iDimension == null) {
				iExpressions[0].calculatePositions(g, aSize, null);
				final Dimension dim = iExpressions[0].getDimension();
				iFontSize = dim.height;
				g.SetFontSize(dim.height);
				iBracketWidth = SBoxBuilder.FontForSize(aSize) / 2;
				iDimension = new Dimension(dim.width + 2 * iBracketWidth, dim.height);
				iAscent = iExpressions[0].getCalculatedAscent();
			}
			if (aPosition != null) {

				iExpressions[0].calculatePositions(g, aSize, new java.awt.Point(aPosition.x + iBracketWidth, aPosition.y));
			}
		}

		@Override
		public void render(final GraphicsPrimitives g) {
			super.render(g);
			final Dimension dim = iExpressions[0].getDimension();
			drawBracket(g, iOpen, iPosition.x, iPosition.y - getCalculatedAscent());
			drawBracket(g, iClose, iPosition.x + dim.width + iBracketWidth, iPosition.y - getCalculatedAscent());
		}

		void drawBracket(final GraphicsPrimitives g, final String bracket, final int x, final int y) {
			final Dimension dim = iExpressions[0].getDimension();
			if (bracket.equals("[") || bracket.equals("]")) {
				final int margin = 2;
				g.SetLineThickness(2);
				if (bracket.equals("[")) {
					g.DrawLine(x + margin, y, x + margin, y + dim.height);
				} else {
					g.DrawLine(x + iBracketWidth - margin, y, x + iBracketWidth - margin, y + dim.height);
				}
				g.SetLineThickness(1);
				g.DrawLine(x + iBracketWidth - margin, y, x + margin, y);
				g.DrawLine(x + margin, y + dim.height, x + iBracketWidth - margin, y + dim.height);
			} else if (bracket.equals("(") || bracket.equals(")")) {
				int xstart;
				int xend;
				if (bracket.equals("(")) {
					xstart = x + iBracketWidth;
					xend = x;
				} else {
					xstart = x;
					xend = x + iBracketWidth;
				}
				final int delta = xend - xstart;
				final float steps[] = new float[3];
				steps[0] = 0.2f;
				steps[1] = 0.6f;
				steps[2] = 0.8f;
				g.SetLineThickness(1f);
				g.DrawLine((int) (xstart + (delta * steps[0])), y + (0 * dim.height) / 6, (int) (xstart + (delta * steps[1])), y + (1 * dim.height) / 6);
				g.SetLineThickness(1.3f);
				g.DrawLine((int) (xstart + (delta * steps[1])), y + (1 * dim.height) / 6, (int) (xstart + (delta * steps[2])), y + (2 * dim.height) / 6);
				g.SetLineThickness(1.6f);
				g.DrawLine((int) (xstart + (delta * steps[2])), y + (2 * dim.height) / 6, (int) (xstart + (delta * steps[2])), y + (4 * dim.height) / 6);
				g.SetLineThickness(1.3f);
				g.DrawLine((int) (xstart + (delta * steps[2])), y + (4 * dim.height) / 6, (int) (xstart + (delta * steps[1])), y + (5 * dim.height) / 6);
				g.SetLineThickness(1f);
				g.DrawLine((int) (xstart + (delta * steps[1])), y + (5 * dim.height) / 6, (int) (xstart + (delta * steps[0])), y + (6 * dim.height) / 6);
			} else {
				g.SetFontSize(iFontSize);
				final int offset = (iFontSize - iAscent) / 2;
				g.DrawText(bracket, x, y + offset);
			}
		}

		int iFontSize;
		int iBracketWidth;
		String iOpen;
		String iClose;
	}

	static int FontForSize(int aSize) {
		if (aSize > 3) {
			aSize = 3;
		}
		if (aSize < 0) {
			aSize = 0;
		}

		switch (aSize) {
		case 0:
			return 6;
		case 1:
			return 8;
		case 2:
			return 12;
		case 3:
			return 16;
		default:
			return 16;
		}
	}

	SBox stack[] = new SBox[1024];
	int stackDepth = 0;

	public SBox pop() {
		stackDepth--;
		final SBox result = stack[stackDepth];
		return result;
	}

	void push(final SBox aSbox) {
		stack[stackDepth] = aSbox;
		stackDepth++;
	}

	public int StackDepth() {
		return stackDepth;
	}

	void process(final String aType) {
		if (aType.equals("=") || aType.equals("\\neq") || aType.equals("+") || aType.equals(",") || aType.equals("\\wedge") || aType.equals("\\vee") || aType.equals("<") || aType.equals(">") || aType.equals("<=") || aType.equals(">=")) {
			final SBox right = pop();
			final SBox left = pop();
			push(new SBoxInfixOperator(left, new SBoxSymbolName(aType), right));
		} else if (aType.equals("/")) {
			final SBox denom = pop();
			final SBox numer = pop();
			push(new SBoxDivisor(numer, denom));
		} else if (aType.equals("-/2")) {
			final SBox right = pop();
			final SBox left = pop();
			push(new SBoxInfixOperator(left, new SBoxSymbolName("-"), right));
		} else if (aType.equals("-/1")) {
			final SBox right = pop();
			push(new SBoxPrefixOperator(new SBoxSymbolName("-"), right));
		} else if (aType.equals("~")) {
			final SBox right = pop();
			push(new SBoxPrefixOperator(new SBoxSymbolName("~"), right));
		} else if (aType.equals("!")) {
			final SBox left = pop();
			push(new SBoxPrefixOperator(left, new SBoxSymbolName("!")));
		} else if (aType.equals("*")) {
			final SBox right = pop();
			final SBox left = pop();
			push(new SBoxInfixOperator(left, new SBoxSymbolName(""), right));
		} else if (aType.equals("[func]")) {
			final SBox right = pop();
			final SBox left = pop();
			push(new SBoxPrefixOperator(left, right));
		}

		else if (aType.equals("^")) {
			final SBox right = pop();
			final SBox left = pop();
			boolean appendToExisting = false;

			if (left instanceof SBoxSubSuperfix) {
				final SBoxSubSuperfix sbox = (SBoxSubSuperfix) left;
				if (!sbox.hasSuperfix()) {
					appendToExisting = true;
				}
			}
			if (appendToExisting) {
				final SBoxSubSuperfix sbox = (SBoxSubSuperfix) left;
				sbox.setSuperfix(right);
				push(sbox);
			} else {
				push(new SBoxSubSuperfix(left, right, null));
			}
		} else if (aType.equals("_")) {
			final SBox right = pop();
			final SBox left = pop();
			if (left instanceof SBoxSubSuperfix) {
				final SBoxSubSuperfix sbox = (SBoxSubSuperfix) left;
				sbox.setSubfix(right);
				push(sbox);
			} else {
				push(new SBoxSubSuperfix(left, null, right));
			}
		} else if (aType.equals("[sqrt]")) {
			final SBox left = pop();
			push(new SBoxSquareRoot(left));
		} else if (aType.equals("[sum]")) {
			push(new SBoxSum());
		} else if (aType.equals("[int]")) {
			push(new SBoxInt());
		} else if (aType.equals("[roundBracket]")) {
			final SBox left = pop();
			push(new SBoxBracket(left, "(", ")"));
		} else if (aType.equals("[squareBracket]")) {
			final SBox left = pop();
			push(new SBoxBracket(left, "[", "]"));
		} else if (aType.equals("[accoBracket]")) {
			final SBox left = pop();
			push(new SBoxBracket(left, "{", "}"));
		} else if (aType.equals("[grid]")) {
			final SBox widthBox = pop();
			final SBox heightBox = pop();
			final int width = Integer.parseInt(((SBoxSymbolName) widthBox).iSymbol);
			final int height = Integer.parseInt(((SBoxSymbolName) heightBox).iSymbol);

			final SBoxGrid grid = new SBoxGrid(width, height);

			int i, j;
			for (j = height - 1; j >= 0; j--) {
				for (i = width - 1; i >= 0; i--) {
					final SBox value = pop();
					grid.SetSBox(i, j, value);
				}
			}
			push(grid);
		} else {
			push(new SBoxSymbolName(aType));
		}
	}

	public void processLiteral(final String aExpression) {
		push(new SBoxSymbolName(aExpression));
	}
}
