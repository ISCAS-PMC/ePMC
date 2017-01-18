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
import epmc.value.Value;

public interface ValueReal extends ValueNumber {
	static boolean isReal(Value value) {
		return TypeReal.isReal(value.getType());
	}
	
	static ValueReal asReal(Value value) {
		if (isReal(value)) {
			return (ValueReal) value;
		} else {
			return null;
		}
	}
	
	@Override
	TypeReal getType();
	
    void set(double value);
    
    void exp(Value operand) throws EPMCException;
    
    void pow(Value operand1, Value operand2) throws EPMCException;
    
    void log(Value operand1, Value operand2) throws EPMCException;
    
    void sqrt(Value operand) throws EPMCException;
    
    void pi() throws EPMCException;
}