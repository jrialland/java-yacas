package net.sf.yacas;

import java.util.HashMap;

class LispOperators extends HashMap<String, LispInFixOperator> {

	private static final long serialVersionUID = 3347494864168614097L;

	public void SetOperator(final int aPrecedence, final String aString) {
		final LispInFixOperator op = new LispInFixOperator(aPrecedence);
		put(aString, op);
	}

	public void SetRightAssociative(final String aString) throws Exception {
		final LispInFixOperator op = get(aString);
		LispError.Check(op != null, LispError.KLispErrNotAnInFixOperator);
		op.SetRightAssociative();
	}

	public void SetLeftPrecedence(final String aString, final int aPrecedence) throws Exception {
		final LispInFixOperator op = get(aString);
		LispError.Check(op != null, LispError.KLispErrNotAnInFixOperator);
		op.SetLeftPrecedence(aPrecedence);
	}

	public void SetRightPrecedence(final String aString, final int aPrecedence) throws Exception {
		final LispInFixOperator op = get(aString);
		LispError.Check(op != null, LispError.KLispErrNotAnInFixOperator);
		op.SetRightPrecedence(aPrecedence);
	}
}
