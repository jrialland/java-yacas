package net.sf.yacas;

/**
 * Use this class in order to access the Yacas interpreter from an external application. Usage: import net.sf.yacas.YacasInterpreter; YacasInterpreter interpreter = new YacasInterpreter(); String output1 = interpreter.Evaluate("a := 5"); String
 * output2 = interpreter.Evaluate("Solve(x*x == a, x)");
 *
 *
 * @author av
 */
public class YacasInterpreter {

	private final CYacas yacas;

	/** Creates a new instance of YacasInterpreter */
	public YacasInterpreter() {
		this(new StringOutput(new StringBuffer()));
	}

	public YacasInterpreter(final LispOutput out) {
		yacas = new CYacas(out);
		try {
			yacas.Evaluate("DefaultDirectory(\"\");");
			yacas.Evaluate("Load(\"yacasinit.ys\");");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use this method to pass an expression to the Yacas interpreter. Returns the output of the interpreter.
	 */
	public String Evaluate(final String input) throws Exception {
		return yacas.Evaluate(input);
	}
}
