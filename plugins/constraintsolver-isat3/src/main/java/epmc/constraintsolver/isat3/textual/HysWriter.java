/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.constraintsolver.isat3.textual;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.operator.OperatorAdd;
import epmc.value.operator.OperatorAddInverse;
import epmc.value.operator.OperatorAnd;
import epmc.value.operator.OperatorDivide;
import epmc.value.operator.OperatorEq;
import epmc.value.operator.OperatorGe;
import epmc.value.operator.OperatorGt;
import epmc.value.operator.OperatorIff;
import epmc.value.operator.OperatorImplies;
import epmc.value.operator.OperatorLe;
import epmc.value.operator.OperatorLog;
import epmc.value.operator.OperatorLt;
import epmc.value.operator.OperatorMax;
import epmc.value.operator.OperatorMin;
import epmc.value.operator.OperatorMultiply;
import epmc.value.operator.OperatorNe;
import epmc.value.operator.OperatorNot;
import epmc.value.operator.OperatorOr;
import epmc.value.operator.OperatorSubtract;

final class HysWriter {
	private final static String SPACE = " ";
	private final static String COMMA = ",";
	private final static String LBRACE = "(";
	private final static String RBRACE = ")";
	private final static String LBRACK = "[";
	private final static String RBRACK = "]";
	private final static String SEMICOLON = ";";
	private final static String DECL = "DECL";
	private final static String EXPR = "EXPR";
	private final static String TYPE_FLOAT = "float";
	private final static String TYPE_INT = "int";
	private final static String TYPE_BOOLE = "boole";

	private final static Map<Operator,ISatOperator> EPMC_TO_ISAT3;
	static {
		Map<Operator,ISatOperator> iscasMCToISat3 = new HashMap<>();
		iscasMCToISat3.put(OperatorAnd.AND, newOperator()
				.setIdentifier("and").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorOr.OR, newOperator()
				.setIdentifier("or").setType(ISatOperator.Type.INFIX).build());
		/* "nand" not supported */ 
		/* "nor" not supported */ 
		/* "xor" not supported */ 
		iscasMCToISat3.put(OperatorIff.IFF, newOperator()
				.setIdentifier("nxor").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorImplies.IMPLIES, newOperator()
				.setIdentifier("impl").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorNot.NOT, newOperator()
				.setIdentifier("not").setType(ISatOperator.Type.PREFIX).build());

		iscasMCToISat3.put(OperatorAddInverse.ADD_INVERSE, newOperator()
				.setIdentifier("-").setType(ISatOperator.Type.PREFIX).build());
		iscasMCToISat3.put(OperatorAdd.ADD, newOperator()
				.setIdentifier("+").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorSubtract.SUBTRACT, newOperator()
				.setIdentifier("-").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorMultiply.MULTIPLY, newOperator()
				.setIdentifier("*").setType(ISatOperator.Type.INFIX).build());
		// does this work?
		iscasMCToISat3.put(OperatorDivide.DIVIDE, newOperator()
				.setIdentifier("/").setType(ISatOperator.Type.INFIX).build());

		/* "abs" not supported */ 
		iscasMCToISat3.put(OperatorMin.MIN, newOperator()
				.setIdentifier("min").setType(ISatOperator.Type.PREFIX).build());
		iscasMCToISat3.put(OperatorMax.MAX, newOperator()
				.setIdentifier("max").setType(ISatOperator.Type.PREFIX).build());
		/* "exp" not supported */
		/* "exp2" not supported */
		/* "exp10" not supported */
		iscasMCToISat3.put(OperatorLog.LOG, newOperator()
				.setIdentifier("log").setType(ISatOperator.Type.PREFIX).build());
		/* "log2" not supported */
		/* "log10" not supported */
		/* "sin" not supported */
		/* "cos" not supported */
		/* "pow" not supported */
		/* "nrt" not supported */
		
		iscasMCToISat3.put(OperatorLt.LT, newOperator()
				.setIdentifier("<").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorLe.LE, newOperator()
				.setIdentifier("<=").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorEq.EQ, newOperator()
				.setIdentifier("=").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorNe.NE, newOperator()
				.setIdentifier("!=").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorGt.GT, newOperator()
				.setIdentifier(">").setType(ISatOperator.Type.INFIX).build());
		iscasMCToISat3.put(OperatorGe.GE, newOperator()
				.setIdentifier(">=").setType(ISatOperator.Type.INFIX).build());

		EPMC_TO_ISAT3 = Collections.unmodifiableMap(iscasMCToISat3);
	}
	private static ISatOperator.Builder newOperator() {
		return new ISatOperator.Builder();
	}


	private OutputStream outStream;
	private ConstraintSolverISat3Textual solver;

	HysWriter(ConstraintSolverISat3Textual solver, OutputStream outStream) {
		assert solver != null;
		assert outStream != null;
		this.solver = solver;
		this.outStream = outStream;
	}

	void write() throws EPMCException {
		try (PrintStream out = new PrintStream(outStream);) {
			writeVariableDeclarations(out);
			writeConstraints(out);
		}
	}
	
	private void writeVariableDeclarations(PrintStream out) {
		out.println(DECL);
		for (ISatVariable variable : this.solver.getVariables()) {
			String name = variable.getName();
			Type type = variable.getType();
			out.print(SPACE + SPACE);
			if (TypeBoolean.isBoolean(type)) {
				out.print(TYPE_BOOLE + SPACE);
			} else if (TypeInteger.isInteger(type)) {
				out.print(TYPE_INT + SPACE);
				out.print(LBRACK);
				out.print(variable.getLower());
				out.print(COMMA);
				out.print(variable.getUpper());
				out.print(RBRACK + SPACE);
			} else if (TypeReal.isReal(type)) {
				out.print(TYPE_FLOAT + SPACE);
				out.print(LBRACK);
				out.print(variable.getLower());
				out.print(COMMA);
				out.print(variable.getUpper());
				out.print(RBRACK + SPACE);
			} else {
				assert false : type;
			}
			out.print(name);
			out.println(SEMICOLON);
		}
		out.println();
	}

	private void writeConstraints(PrintStream out) throws EPMCException  {
//		Expression constraintExpression = null;
//		for (Expression expression : solver.getConstraints()) {
//			if (constraintExpression == null) {
//				constraintExpression = expression;
//			} else {
//				constraintExpression = constraintExpression.and(expression);
//			}
//		}
		out.println(EXPR);
		for (Expression expression : solver.getConstraints()) {
		    out.print(SPACE + SPACE);
			out.print(translateExpression(expression));
			out.println(SEMICOLON);
		}
//		out.print(translateExpression(constraintExpression));
//		out.println(SEMICOLON);
	}

	private String translateExpression(Expression expression) throws EPMCException {
		assert expression != null;
		if (expression instanceof ExpressionIdentifierStandard) {
			ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
			return expressionIdentifier.getName();
		} else if (ExpressionLiteral.isLiteral(expression)) {
			return getValue(expression).toString();
		} else if (expression instanceof ExpressionOperator) {
			return translateExpressionOperator((ExpressionOperator) expression);
		} else {
			assert false;
			return null;
		}
	}

	private String translateExpressionOperator(ExpressionOperator expression)
			throws EPMCException {
		ISatOperator operator = EPMC_TO_ISAT3.get(expression.getOperator());
		assert operator != null : expression.getOperator();
		StringBuilder result = new StringBuilder();
		if (operator.isPrefix()) {
			result.append(operator.getIdentifer());
			result.append(SPACE);
			result.append(LBRACE);
			for (Expression operand : expression.getOperands()) {
				result.append(translateExpression(operand));
				result.append(COMMA);
			}
			result.delete(result.length() - 1, result.length());
			result.append(RBRACE);
		} else if (operator.isInfix()) {
			result.append(LBRACE);
			for (Expression operand : expression.getOperands()) {
				result.append(translateExpression(operand));
				result.append(operator.getIdentifer());
			}
			result.delete(result.length() - operator.getIdentifer().length(),
					result.length());
			result.append(RBRACE);
		} else {
			assert false;
			return null;
		}
		return result.toString();
	}
	
    private static Value getValue(Expression expression) throws EPMCException {
        assert expression != null;
        assert ExpressionLiteral.isLiteral(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.asLiteral(expression);
        return expressionLiteral.getValue();
    }
}
