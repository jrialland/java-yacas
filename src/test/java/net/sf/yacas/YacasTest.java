package net.sf.yacas;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class YacasTest {

	@Test
	public void predicates() {
		runTestCase("predicates");
	}

	@Test
	public void newly() {
		runTestCase("newly");
	}

	@Test
	public void lists() {
		runTestCase("lists");
	}

	@Test
	public void nummethods() {
		runTestCase("nummethods");
	}

	@Test
	public void journal() {
		runTestCase("journal");
	}

	@Test
	public void precision() {
		runTestCase("precision");
	}

	@Test
	public void tr() {
		runTestCase("tr");
	}

	@Test
	public void calculus() {
		runTestCase("calculus");
	}

	@Test
	public void cyclotomic() {
		runTestCase("cyclotomic");
	}

	@Test
	public void radsimp() {
		runTestCase("radsimp");
	}

	@Test
	public void programming() {
		runTestCase("programming");
	}

	@Test
	public void simplify() {
		runTestCase("simplify");
	}

	@Test
	public void scopestack() {
		runTestCase("scopestack");
	}

	@Test
	public void macro() {
		runTestCase("macro");
	}

	@Test
	public void numbers() {
		runTestCase("numbers");
	}

	@Test
	public void numerics() {
		runTestCase("numerics");
	}

	@Test
	public void poly() {
		runTestCase("poly");
	}

	@Test
	public void calendar() {
		runTestCase("calendar");
	}

	@Test
	public void orthopoly() {
		runTestCase("orthopoly");
	}

	@Test
	public void trace() {
		runTestCase("trace");
	}

	@Test
	public void integrate() {
		runTestCase("integrate");
	}

	@Test
	public void comments() {
		runTestCase("comments");
	}

	@Test
	public void dot() {
		runTestCase("dot");
	}

	@Test
	public void sturm() {
		runTestCase("sturm");
	}

	@Test
	public void logic_simplify_test() {
		runTestCase("logic_simplify_test");
	}

	@Test
	public void multivar() {
		runTestCase("multivar");
	}

	@Test
	public void transforms() {
		runTestCase("transforms");
	}

	@Test
	public void dimensions() {
		runTestCase("dimensions");
	}

	@Test
	public void padic() {
		runTestCase("padic");
	}

	@Test
	public void GaussianIntegers() {
		runTestCase("GaussianIntegers");
	}

	@Test
	public void linalg() {
		runTestCase("linalg");
	}

	@Test
	public void outer() {
		runTestCase("outer");
	}

	@Test
	public void openmath() {
		runTestCase("openmath");
	}

	@Test
	public void plots() {
		runTestCase("plots");
	}

	@Test
	public void complex() {
		runTestCase("complex");
	}

	@Test
	public void ode() {
		runTestCase("ode");
	}

	@Test
	public void binaryfactors() {
		runTestCase("binaryfactors");
	}

	@Test
	public void tensors() {
		runTestCase("tensors");
	}

	@Test
	public void regress() {
		runTestCase("regress");
	}

	@Test
	public void arithmetic() {
		runTestCase("arithmetic");
	}

	@Test
	public void io() {
		runTestCase("io");
	}

	@Test
	public void solve() {
		runTestCase("solve");
	}

	@Test
	public void canprove() {
		runTestCase("canprove");
	}

	@Test
	public void sums() {
		runTestCase("sums");
	}

	@Test
	public void nthroot() {
		runTestCase("nthroot");
	}

	@Test
	public void matrixpower() {
		runTestCase("matrixpower");
	}

	@Test
	public void deriv() {
		runTestCase("deriv");
	}

	@Test
	public void c_tex_form() {
		runTestCase("c_tex_form");
	}

	protected void runTestCase(final String name) {
		final URL url = YacasTest.class.getClassLoader().getResource(name + ".yts");
		if (url == null) {
			throw new IllegalStateException("resource not found : " + name);
		}
		try {
			final StringBuffer buf = new StringBuffer();
			final YacasInterpreter yacas = new YacasInterpreter(new StringOutput(buf));
			final String cmd = "Load(\"" + url.toExternalForm() + "\");";
			System.out.println("<< " + cmd);
			System.out.println(">> " + yacas.Evaluate(cmd));
			if (buf.toString().contains("******************")) {
				Assert.fail(buf.toString());
			}
			System.out.println("------------------------------------------------------------");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
