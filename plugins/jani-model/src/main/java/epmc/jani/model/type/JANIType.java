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

package epmc.jani.model.type;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.JANINode;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public interface JANIType extends JANINode {
	
	void setContextValue(ContextValue contextValue);
	
	JANIType parseAsJANIType(JsonValue value) throws EPMCException;
	
	Type toType() throws EPMCException;

	Value getDefaultValue() throws EPMCException;
	
	default JANIType replace(Map<Expression, Expression> map) {
		return this;
	}
}