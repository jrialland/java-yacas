package net.sf.yacas;

public class CYacas {
	public CYacas(final LispOutput stdoutput) {
		try {
			env = new LispEnvironment(stdoutput);
			tokenizer = new LispTokenizer();
			printer = new InfixPrinter(env.iPrefixOperators, env.iInfixOperators, env.iPostfixOperators, env.iBodiedOperators);
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	public String Evaluate(final String input) throws Exception {
		if (input.length() == 0) {
			return "";
		}
		String rs = "";

		env.iEvalDepth = 0;
		env.iEvaluator.ResetStack();

		iError = null;

		final LispPtr in_expr = new LispPtr();
		if (env.iPrettyReader != null) {
			final StringBuilder inp = new StringBuilder();
			inp.append(input);
			final InputStatus oldstatus = env.iInputStatus;
			env.iInputStatus.SetTo("String");
			final StringInput newInput = new StringInput(new StringBuffer(input), env.iInputStatus);

			final LispInput previous = env.iCurrentInput;
			env.iCurrentInput = newInput;
			try {
				final LispPtr args = new LispPtr();
				LispStandard.InternalApplyString(env, in_expr, env.iPrettyReader, args);
			} catch (final Exception e) {
				throw e;
			} finally {
				env.iCurrentInput = previous;
				env.iInputStatus.RestoreFrom(oldstatus);
			}
		} else {
			final InputStatus someStatus = new InputStatus();
			final StringBuffer inp = new StringBuffer();
			inp.append(input);
			inp.append(";");
			final StringInput input_str = new StringInput(inp, someStatus);
			final LispParser parser = new InfixParser(tokenizer, input_str, env, env.iPrefixOperators, env.iInfixOperators, env.iPostfixOperators, env.iBodiedOperators);
			parser.Parse(in_expr);
		}

		final LispPtr result = new LispPtr();
		env.iEvaluator.Eval(env, result, in_expr);

		final String percent = env.HashTable().LookUp("%");
		env.SetVariable(percent, result, true);

		final StringBuffer string_out = new StringBuffer();
		final LispOutput output = new StringOutput(string_out);

		if (env.iPrettyPrinter != null) {
			final LispPtr nonresult = new LispPtr();
			LispStandard.InternalApplyString(env, nonresult, env.iPrettyPrinter, result);
			rs = string_out.toString();
		} else {
			printer.RememberLastChar(' ');
			printer.Print(result, output, env);
			rs = string_out.toString();
		}

		return rs;
	}

	public LispEnvironment env = null;
	LispTokenizer tokenizer = null;
	LispPrinter printer = null;
	String iError = null;
}
