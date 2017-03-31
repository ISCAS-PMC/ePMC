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

package epmc.expression.standard;

import static epmc.error.UtilError.ensure;

import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.extensions.derivedoperators.OperatorAbs;
import epmc.jani.extensions.derivedoperators.OperatorExp;
import epmc.jani.extensions.derivedoperators.OperatorSgn;
import epmc.jani.extensions.derivedoperators.OperatorTrunc;
import epmc.jani.extensions.hyperbolicfunctions.OperatorAcosh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorAsinh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorAtanh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorCosh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorSinh;
import epmc.jani.extensions.hyperbolicfunctions.OperatorTanh;
import epmc.jani.extensions.trigonometricfunctions.OperatorAcos;
import epmc.jani.extensions.trigonometricfunctions.OperatorAsin;
import epmc.jani.extensions.trigonometricfunctions.OperatorAtan;
import epmc.jani.extensions.trigonometricfunctions.OperatorCos;
import epmc.jani.extensions.trigonometricfunctions.OperatorSin;
import epmc.jani.extensions.trigonometricfunctions.OperatorTan;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.value.Operator;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorAnd;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivideIgnoreZero;
import epmc.value.OperatorFloor;
import epmc.value.OperatorGe;
import epmc.value.OperatorIff;
import epmc.value.OperatorImplies;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorPow;

public class ExpressionOperatorProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionOperator expressionOperator = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionOperator; 
		
		expressionOperator = (ExpressionOperator) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert expressionOperator != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		Operator operator = expressionOperator.getOperator();
		
		Expression exp;
		
        switch (operator.getIdentifier()) {
        case OperatorNot.IDENTIFIER:
        case OperatorFloor.IDENTIFIER: 
        case OperatorCeil.IDENTIFIER:
        	prism.append(operator.getIdentifier());
            prism.append("(");
    		exp = expressionOperator.getOperand1();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
    		prism.append(")");
        	break;
        case OperatorAddInverse.IDENTIFIER:
            prism.append("-(");
    		exp = expressionOperator.getOperand1();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
            prism.append(")");
            break;
        case OperatorIte.IDENTIFIER:
        	boolean needBraces = true;
            prism.append("(");
    		exp = expressionOperator.getOperand1();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
            prism.append(")");
            prism.append(" ? ");
    		exp = expressionOperator.getOperand2();
    		processor = ProcessorRegistrar.getProcessor(exp);
            if (exp instanceof ExpressionLiteral) {
            	needBraces = false;
            }
            if (exp instanceof ExpressionIdentifier) {
            	needBraces = false;
            }
            if (needBraces) {
            	prism.append("(");
            }
    		prism.append(processor.toPRISM().toString());
            if (needBraces) {
            	prism.append(")");
            }
            prism.append(" : ");
            needBraces = true;
    		exp = expressionOperator.getOperand3();
    		processor = ProcessorRegistrar.getProcessor(exp);
            if (exp instanceof ExpressionLiteral) {
            	needBraces = false;
            }
            if (exp instanceof ExpressionIdentifier) {
            	needBraces = false;
            }
            if (needBraces) {
            	prism.append("(");
            }
    		prism.append(processor.toPRISM().toString());
            if (needBraces) {
            	prism.append(")");
            }
            break;
        case OperatorMin.IDENTIFIER: 
        case OperatorMax.IDENTIFIER: 
        case OperatorPow.IDENTIFIER: 
        case OperatorLog.IDENTIFIER:
            prism.append(operator.getIdentifier());
            prism.append("(");
    		exp = expressionOperator.getOperand1();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
            prism.append(",");
    		exp = expressionOperator.getOperand2();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
            prism.append(")");
            break;
        case OperatorMod.IDENTIFIER: 
            prism.append("mod(");
    		exp = expressionOperator.getOperand1();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
            prism.append(",");
    		exp = expressionOperator.getOperand2();
    		processor = ProcessorRegistrar.getProcessor(exp);
    		prism.append(processor.toPRISM().toString());
            prism.append(")");
            break;
        default:
        	List<Expression> children = expressionOperator.getChildren();
        	String operatorSymbol = null;
        	switch (operator.getIdentifier()) {
    		case OperatorAnd.IDENTIFIER:
    			operatorSymbol = "&";
    			break;
    		case OperatorDivideIgnoreZero.IDENTIFIER:
    			operatorSymbol = "/";
    			break;
    		case OperatorGe.IDENTIFIER:
    			operatorSymbol = ">=";
    			break;
    		case OperatorLe.IDENTIFIER:
    			operatorSymbol = "<=";
    			break;
    		case OperatorIff.IDENTIFIER:
    			operatorSymbol = "<=>";
    			break;
    		case OperatorImplies.IDENTIFIER:
    			operatorSymbol = "=>";
    			break;
    		case OperatorOr.IDENTIFIER:
    			operatorSymbol = "|";
    			break;
    		case OperatorMultiplyInverse.IDENTIFIER:
    			operatorSymbol = "1/";
    			break;
    		case OperatorNe.IDENTIFIER:
    			operatorSymbol = "!=";
    			break;
    			//TODO: Maybe the following operators can be recovered
    		case OperatorAbs.IDENTIFIER:
    		case OperatorExp.IDENTIFIER:
    		case OperatorSgn.IDENTIFIER:
    		case OperatorTrunc.IDENTIFIER:
    			//these no.
    		case OperatorAcosh.IDENTIFIER:
    		case OperatorAsinh.IDENTIFIER:
    		case OperatorAtanh.IDENTIFIER:
    		case OperatorCosh.IDENTIFIER:
    		case OperatorSinh.IDENTIFIER:
    		case OperatorTanh.IDENTIFIER:
    		case OperatorAcos.IDENTIFIER:
    		case OperatorAsin.IDENTIFIER:
    		case OperatorAtan.IDENTIFIER:
    		case OperatorCos.IDENTIFIER:
    		case OperatorSin.IDENTIFIER:
    		case OperatorTan.IDENTIFIER:
    			ensure(false, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_INPUT_FEATURE);
    		default:
    			operatorSymbol = operator.getIdentifier();
    			break;
        	}
            if (children.size() == 1) {
        		processor = ProcessorRegistrar.getProcessor(children.get(0));
                prism.append(operatorSymbol)
                	 .append("(")
                	 .append(processor.toPRISM().toString())
                	 .append(")");
            } else {
            	boolean remaining = false;
	            for (Expression child : children) {
	                needBraces = true;
	                if (remaining) {
	                    prism.append(" " + operatorSymbol + " ");
	                } else {
	                	remaining = true;
	                }
	                if (child instanceof ExpressionOperator) {
	                    ExpressionOperator childOp = (ExpressionOperator) child;
	                    if (operator == childOp.getOperator()) {
	                        needBraces = false;
	                    }
	                    if ((expressionOperator.isAdd() || expressionOperator.isSubtract())
	                            && (childOp.isMultiply() || childOp.isDivide())) {
	                        needBraces = false;
	                    }
	                }
	                if (child instanceof ExpressionLiteral) {
	                	needBraces = false;
	                }
	                if (child instanceof ExpressionIdentifier) {
	                	needBraces = false;
	                }
	                if (needBraces) {
	                    prism.append("(");
	                }
	        		processor = ProcessorRegistrar.getProcessor(child);
	        		prism.append(processor.toPRISM().toString());
	                if (needBraces) {
	                    prism.append(")");
	                }
	            }
            }
            break;
        }

        return prism;
	}
}