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

package epmc.expression.standard.evaluatordd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;

public final class EvaluatorDDVariable implements EvaluatorDD {
    public final static String IDENTIFIER = "variable";

    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private DD dd;
    private List<DD> vector;
    private boolean closed;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        this.variables = variables;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean canHandle() {
        for (Expression variable : variables.keySet()) {
            if (variable.equals(expression)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void build() {
        Options options = Options.get();
        boolean useVector = options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
        ContextDD contextDD = ContextDD.get();
        VariableDD variableDD = variables.get(expression);

        if (useVector && variableDD.isInteger()) {
            List<DD> origVec = new ArrayList<>(contextDD.clone(variableDD.getDDVariables(0)));
            origVec.add(contextDD.newConstant(false));
            List<DD> add = contextDD.twoCplFromInt(variableDD.getLower());
            vector = contextDD.twoCplAdd(origVec, add);
            contextDD.dispose(add);
            contextDD.dispose(origVec);
        } else {
            dd = variableDD.getValueEncoding(0);
        }
    }

    @Override
    public DD getDD() {
        dd = UtilEvaluatorDD.getDD(dd, vector, expression);
        assert dd != null;
        return dd;
    }

    @Override
    public List<DD> getVector() {
        return vector;
    }

    @Override
    public void close() {
        closed = UtilEvaluatorDD.close(closed, dd, vector);
    }
}
