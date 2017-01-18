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

package epmc.automaton;

import java.util.HashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeBoolean;

final class ExpressionToTypeBoolean implements ExpressionToType {
	private final Set<Expression> mapped = new HashSet<>();
	private final ContextValue contextValue;

	ExpressionToTypeBoolean(ContextValue contextValue, Expression[] expressions) {
		this.contextValue = contextValue;
		Set<Expression> seen = new HashSet<>();
		assert expressions != null;
		for (Expression expression : expressions) {
			assert expression != null;
			assert !seen.contains(expression);
			seen.add(expression);
		}
		for (Expression expression : expressions) {
			mapped.add(expression);
		}
	}
	
	@Override
	public Type getType(Expression expression) throws EPMCException {
		assert expression != null;
		if (mapped.contains(expression)) {
			return TypeBoolean.get(contextValue);
		}
		return null;
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}

}