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

import epmc.value.Value;
import epmc.value.operator.OperatorGe;

public final class ValueInterval implements ValueAlgebra, ValueRange, ValueSetString {
    public static boolean isInterval(Value value) {
        return value instanceof ValueInterval;
    }

    public static ValueInterval asInterval(Value value) {
        if (isInterval(value)) {
            return (ValueInterval) value;
        } else {
            return null;
        }
    }

    private final static String COMMA = ",";
    private final static String LBRACK = "[";
    private final static String RBRACK = "]";

    private final ValueReal lower;
    private final ValueReal upper;
    private final TypeInterval type;
    private boolean immutable;

    ValueInterval(TypeInterval type, ValueReal lower, ValueReal upper) {
        assert type != null;
        assert lower != null;
        assert upper != null;
        this.type = type;
        this.lower = UtilValue.clone(lower);
        this.upper = UtilValue.clone(upper);
    }

    ValueInterval(TypeInterval type) {
        this(type, UtilValue.clone(TypeReal.get().getZero()),
                UtilValue.clone(TypeReal.get().getZero()));
    }

    void setImmutable() {
        this.immutable = true;
    }

    public ValueReal getIntervalLower() {
        return lower;
    }

    public ValueReal getIntervalUpper() {
        return upper;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueInterval)) {
            return false;
        }
        ValueInterval other = (ValueInterval) obj;
        return this.lower.equals(other.lower) && this.upper.equals(other.upper);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = lower.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = upper.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        return LBRACK + lower + COMMA + upper + RBRACK;
    }

    @Override
    public void set(Value operand) {
        assert !isImmutable();
        assert operand != null;
        assert isInterval(operand) || ValueInteger.isInteger(operand) || ValueReal.isReal(operand);
        if (isInterval(operand)) {
            ValueInterval opIv = ValueInterval.asInterval(operand);
            getIntervalLower().set(opIv.getIntervalLower());
            getIntervalUpper().set(opIv.getIntervalUpper());
        } else {
            getIntervalLower().set(operand);
            getIntervalUpper().set(operand);
        }

    }

    public boolean isGe(Value operand) {
        assert operand != null;
        assert isInterval(operand) || ValueInteger.isInteger(operand) || ValueReal.isReal(operand);
        OperatorEvaluator ge = ContextValue.get().getOperatorEvaluator(OperatorGe.GE, TypeReal.get(), TypeReal.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        if (isInterval(operand)) {
            ValueInterval opIv = ValueInterval.asInterval(operand);
            ge.apply(cmp, getIntervalLower(), opIv.getIntervalLower());
            if (!cmp.getBoolean()) {
                return false;
            }
            ge.apply(cmp, getIntervalUpper(), opIv.getIntervalUpper());
            if (!cmp.getBoolean()) {
                return false;
            }
        } else {
            ge.apply(cmp, getIntervalLower(), operand);
            if (!cmp.getBoolean()) {
                return false;
            }
            ge.apply(cmp, getIntervalUpper(), operand);
            if (!cmp.getBoolean()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void set(int value) {
        lower.set(value);
        upper.set(value);
    }

    @Override
    public void set(String string) {
        assert string != null;
        string = string.trim();
        String[] parts = string.split(COMMA);
        if (parts.length == 1) {
            Value point = UtilValue.newValue(TypeReal.get(), parts[0]);
            getIntervalLower().set(point);
            getIntervalUpper().set(point);
        } else if (parts.length == 2) {
            String lowerString = parts[0].substring(1);
            String upperString = parts[1].substring(0, parts[1].length() - 1);
            Value lower = UtilValue.newValue(TypeReal.get(), lowerString);
            Value upper = UtilValue.newValue(TypeReal.get(), upperString);
            getIntervalLower().set(lower);
            getIntervalUpper().set(upper);
        } else {
            assert false;            
        }

    }

    @Override
    public boolean checkRange() {
        return ValueRange.checkRange(lower) && ValueRange.checkRange(upper);
    }

    @Override
    public TypeInterval getType() {
        return type;
    }

    boolean isImmutable() {
        return immutable;
    }

    @Override
    public void add(Value operand1, Value operand2) {
        lower.add(getLower(operand1), getLower(operand2));
        upper.add(getUpper(operand1), getUpper(operand2));
    }

    @Override
    public void multiply(Value operand1, Value operand2) {
        lower.multiply(getLower(operand1), getLower(operand2));
        upper.multiply(getUpper(operand1), getUpper(operand2));
    }

    public static ValueAlgebra getLower(Value operand) {
        if (isInterval(operand)) {
            return ValueInterval.asInterval(operand).getIntervalLower();
        } else {
            return ValueAlgebra.asAlgebra(operand);
        }
    }

    public static Value getUpper(Value operand) {
        if (isInterval(operand)) {
            return ValueInterval.asInterval(operand).getIntervalUpper();
        } else {
            return operand;
        }
    }
}
