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

import epmc.expression.Expression;
import epmc.prism.exporter.operatorprocessor.OperatorProcessorRegistrar;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class PRISMExporter_ExpressionOperatorProcessor implements JANI2PRISMProcessorStrict {

    private ExpressionOperator expressionOperator = null;
    private String prefix = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionOperator; 

        expressionOperator = (ExpressionOperator) obj;
        return this;
    }

    @Override
    public JANI2PRISMProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert expressionOperator != null;

        StringBuilder prism = new StringBuilder();

        if (prefix != null) {
            prism.append(prefix);
        }
        
        prism.append(OperatorProcessorRegistrar.getOperatorProcessor(expressionOperator)
                .toPRISM());

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert expressionOperator != null;

        for (Expression child : expressionOperator.getChildren()) {
            ProcessorRegistrar.getProcessor(child).validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert expressionOperator != null;

        boolean usesTransient = false;
        for (Expression child : expressionOperator.getChildren()) {
            usesTransient |= ProcessorRegistrar.getProcessor(child).usesTransientVariables();
        }

        return usesTransient;
    }	
}