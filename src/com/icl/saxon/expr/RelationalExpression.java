package com.icl.saxon.expr;
import com.icl.saxon.Context;
import com.icl.saxon.functions.*;


/**
* Relational Expression: a boolean expression that compares two expressions
* for equals, not-equals, greater-than or less-than.
*/

final class RelationalExpression extends BinaryExpression {

    /**
    * Default constructor
    */

    public RelationalExpression(){};

    /**
    * Create a relational expression identifying the two operands and the operator
    * @param p1 the left-hand operand
    * @param op the operator, as a token returned by the Tokenizer (e.g. Tokenizer.LT)
    * @param p2 the right-hand operand
    */

    public RelationalExpression(Expression p1, int op, Expression p2) {
        super(p1, op, p2);
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws XPathException {

        p1 = p1.simplify();
        p2 = p2.simplify();

        // detect common case such as @att='x'

        if (p1 instanceof SingletonExpression &&
        		(p2 instanceof StringValue ||
        		 p2 instanceof NumericValue ||
        		 p2 instanceof FragmentValue ||
        		 p2 instanceof TextFragmentValue )) {

            Expression s = new SingletonComparison((SingletonExpression)p1, operator, (Value)p2);
			s.setStaticContext(getStaticContext());
			return s;
        }

        if (p2 instanceof SingletonExpression &&
        		(p1 instanceof StringValue ||
        		 p1 instanceof NumericValue ||
        		 p1 instanceof FragmentValue ||
        		 p1 instanceof TextFragmentValue )) {

            Expression s = new SingletonComparison((SingletonExpression)p2,
            								 Value.inverse(operator), (Value)p1);
			s.setStaticContext(getStaticContext());
			return s;
        }

        // detect common case such as [element='string']

        if (p1 instanceof NodeSetExpression &&
        		(p2 instanceof StringValue ||
        		 p2 instanceof NumericValue ||
        		 p2 instanceof FragmentValue ||
        		 p2 instanceof TextFragmentValue )) {

            Expression s = new NodeSetComparison((NodeSetExpression)p1, operator, (Value)p2);
			s.setStaticContext(getStaticContext());
			return s;
        }

        if (p2 instanceof NodeSetExpression &&
        		(p1 instanceof StringValue ||
        		 p1 instanceof NumericValue ||
        		 p1 instanceof FragmentValue ||
        		 p1 instanceof TextFragmentValue )) {

            Expression s = new NodeSetComparison((NodeSetExpression)p2,
            								 Value.inverse(operator), (Value)p1);
			s.setStaticContext(getStaticContext());
			return s;
        }

        // evaluate the expression now if both arguments are constant

        if ((p1 instanceof Value) && (p2 instanceof Value)) {
            return evaluate(null);
        }

        // optimise count(x) = 0 (or >0, !=0, etc)

        if ((p1 instanceof Count) && (((Count)p1).getNumberOfArguments()==1) &&
        		(((Count)p1).argument[0].getDataType()==Value.NODESET) &&
        		(p2 instanceof NumericValue) && (((Value)p2).asNumber()==0)) {
        	if (operator == Tokenizer.EQUALS || operator == Tokenizer.LE) {
        		// rewrite count(x)=0 as not(x)
        		Not fn = new Not();
        		fn.addArgument(((Count)p1).argument[0]);
        		fn.setStaticContext(getStaticContext());
        		return fn;
        	} else if (operator == Tokenizer.NE || operator == Tokenizer.GT) {
        		// rewrite count(x)!=0, count(x)>0 as boolean(x)
        		BooleanFn fn = new BooleanFn();
        		fn.addArgument(((Count)p1).argument[0]);
        		fn.setStaticContext(getStaticContext());
        		return fn;
        	} else if (operator == Tokenizer.GE) {
        		// rewrite count(x)>=0 as true()
        		return new BooleanValue(true);
        	} else {  // operator == Tokenizer.LT
        		// rewrite count(x)<0 as false()
        		return new BooleanValue(false);
        	}
        }

        // optimise (0 = count(x)), etc

        if ((p2 instanceof Count) &&
        	 (p1 instanceof NumericValue) && (((Value)p1).asNumber()==0)) {
        	Expression s = new RelationalExpression(p2, Value.inverse(operator), p1).simplify();
			s.setStaticContext(getStaticContext());
			return s;
        }

        // optimise string-length(x) = 0, >0, !=0 etc

        if ((p1 instanceof StringLength) &&
        	    (((StringLength)p1).getNumberOfArguments()==1) &&
        		(p2 instanceof NumericValue) && (((Value)p2).asNumber()==0)) {

			// force conversion of argument to a string
        	Expression arg = ((StringLength)p1).argument[0];
        	if (!(arg instanceof StringValue)) {
        		StringFn fn = new StringFn();
        		fn.addArgument(arg);
        		arg = fn;
        	}

        	if (operator == Tokenizer.EQUALS || operator == Tokenizer.LE) {
        		// rewrite string-length(x)=0 as not(string(x))
        		Not fn = new Not();
        		fn.addArgument(arg);
				fn.setStaticContext(getStaticContext());
        		return fn;
        	} else if (operator == Tokenizer.GT || operator == Tokenizer.NE) {
        		// rewrite string-length(x)!=0 or >0 as boolean(string(x))
        		BooleanFn fn = new BooleanFn();
        		fn.addArgument(arg);
				fn.setStaticContext(getStaticContext());
        		return fn;
        	} else if (operator == Tokenizer.GE) {
        		// rewrite string-length(x)>=0 as true()
        		return new BooleanValue(true);
        	} else /* if (operator == Tokenizer.LT) */ {
        		// rewrite string-length(x)<0 as false()
        		return new BooleanValue(false);
        	}
        }

        // optimise (0 = string-length(x)), etc

        if ((p2 instanceof StringLength) &&
        	 (p1 instanceof NumericValue) && (((Value)p1).asNumber()==0)) {
        	Expression s = new RelationalExpression(p2, Value.inverse(operator), p1).simplify();
			s.setStaticContext(getStaticContext());
			return s;
        }

        // optimise [position() < n] etc

        if ((p1 instanceof Position) && (p2 instanceof NumericValue)) {
            double pos = ((NumericValue)p2).asNumber();
            switch (operator) {
                case Tokenizer.EQUALS:
                    return new PositionRange((int)pos, (int)pos);
                case Tokenizer.GE:
                    return new PositionRange((int)pos, Integer.MAX_VALUE);
                case Tokenizer.NE:
                    break;
                case Tokenizer.LT:
                    return new PositionRange(1, (int)Math.floor(pos - 0.00000000001) );
                case Tokenizer.GT:
                    return new PositionRange((int)Math.ceil(pos + 0.00000000001), Integer.MAX_VALUE);
                case Tokenizer.LE:
                    return new PositionRange(1, ((int)pos));
            }
        }
        if ((p1 instanceof NumericValue) && (p2 instanceof Position)) {
            double pos = ((NumericValue)p1).asNumber();
            switch (operator) {
                case Tokenizer.EQUALS:
                    return new PositionRange((int)pos, (int)pos);
                case Tokenizer.LE:
                    return new PositionRange((int)pos, Integer.MAX_VALUE);
                case Tokenizer.NE:
                    break;
                case Tokenizer.GT:
                    return new PositionRange(1, (int)Math.floor(pos - 0.00000000001) );
                case Tokenizer.LT:
                    return new PositionRange((int)Math.ceil(pos + 0.00000000001), Integer.MAX_VALUE);
                case Tokenizer.GE:
                    return new PositionRange(1, (int)pos);
            }
        }

        // optimise [position()=last()] etc

        if ((p1 instanceof Position) && (p2 instanceof Last)) {
            switch (operator) {
                case Tokenizer.EQUALS:
                case Tokenizer.GE:
                    return new IsLastExpression(true);
                case Tokenizer.NE:
                case Tokenizer.LT:
                    return new IsLastExpression(false);
                case Tokenizer.GT:
                    return new BooleanValue(false);
                case Tokenizer.LE:
                    return new BooleanValue(true);
            }
        }
        if ((p1 instanceof Last) && (p2 instanceof Position)) {
            switch (operator) {
                case Tokenizer.EQUALS:
                case Tokenizer.LE:
                    return new IsLastExpression(true);
                case Tokenizer.NE:
                case Tokenizer.GT:
                    return new IsLastExpression(false);
                case Tokenizer.LT:
                    return new BooleanValue(false);
                case Tokenizer.GE:
                    return new BooleanValue(true);
            }
        }
        return this;
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a BooleanValue representing the result of the numeric comparison of the two operands
    */

    public Value evaluate(Context c) throws XPathException {
        return new BooleanValue(evaluateAsBoolean(c));
    }

    /**
    * Evaluate the expression in a given context
    * @param c the given context for evaluation
    * @return a boolean representing the result of the numeric comparison of the two operands
    */

    public boolean evaluateAsBoolean(Context c) throws XPathException {
        Value s1 = p1.evaluate(c);
        Value s2 = p2.evaluate(c);
        return s1.compare(operator, s2);
    }

    /**
    * Determine the data type of the expression
    * @return Value.BOOLEAN
    */

    public int getDataType() {
        return Value.BOOLEAN;
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dependencies, Context context) throws XPathException {

        if ((getDependencies() & dependencies) != 0 ) {
            Expression e = new RelationalExpression(
                                p1.reduce(dependencies, context),
                                operator,
                                p2.reduce(dependencies, context));
            e.setStaticContext(getStaticContext());
            return e.simplify();
        } else {
            return this;
        }
    }


}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is
// Michael Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
