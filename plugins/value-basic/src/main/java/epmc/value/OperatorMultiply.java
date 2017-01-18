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

package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorMultiply implements Operator {
    private ContextValue context;
    /** Multiplication, a * b, binary operator. */
    public final static String IDENTIFIER = "*";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setContext(ContextValue context) {
        this.context = context;
    }

    @Override
    public ContextValue getContext() {
        return context;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	ValueAlgebra.asAlgebra(result).multiply(operands[0], operands[1]);
    }

    @Override
    public Type resultType(Type... types) {
        Type upper = UtilValue.upper(types);
        Type result;
        if (TypeArray.isArray(types[0]) == TypeArray.isArray(types[1])) {
            if (upper == null) {
                return null;
            }
            if (!TypeUnknown.isUnknown(types[0]) && !TypeAlgebra.isAlgebra(types[0])
                    && !TypeArray.isArray(types[0])
                    || !TypeUnknown.isUnknown(types[1]) && !TypeAlgebra.isAlgebra(types[1])
                    && !TypeArray.isArray(types[1])) {
                return null;
            }
            result = upper;
        } else {
            TypeArray array;
            Type nonArray;
            if (TypeArray.isArray(types[0]) && !TypeArray.isArray(types[1])) {
                array = TypeArray.asArray(types[0]);
                nonArray = types[1];
            } else {
                array = TypeArray.asArray(types[1]);
                nonArray = types[0];
            }                
            Type entryType = array.getEntryType();
            Type entryUpper = UtilValue.upper(entryType, nonArray);
            if (entryUpper == null) {
                return null;
            }
            result = entryUpper.getTypeArray();
        }
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}