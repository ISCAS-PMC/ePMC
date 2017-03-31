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

package epmc.jani.type.dtmc;

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;

public final class ModelExtensionDTMCProcessor implements JANI2PRISMProcessorStrict {

	@Override
	public void setElement(Object obj) throws EPMCException {
		if (!(obj instanceof ModelExtensionDTMC)) {
			ensure(false, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_INPUT_FEATURE);
		}
	}

	@Override
	public StringBuilder toPRISM() {
		return new StringBuilder(ModelExtensionDTMC.IDENTIFIER).append("\n");
	}
}