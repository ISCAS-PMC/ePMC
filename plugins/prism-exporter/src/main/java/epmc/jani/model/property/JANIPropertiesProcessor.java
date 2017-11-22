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

package epmc.jani.model.property;

import epmc.modelchecker.RawProperty;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class JANIPropertiesProcessor implements JANI2PRISMProcessorStrict {

    private JANIProperties properties = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof JANIProperties; 

        properties = (JANIProperties) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert properties != null;

        StringBuilder prism = new StringBuilder();

        for (RawProperty raw : properties.getRawProperties()) {
            prism.append(ProcessorRegistrar.getProcessor(properties.getParsedProperty(raw))
                    .toPRISM())
            .append("\n");
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert properties != null;

        for (RawProperty raw : properties.getRawProperties()) {
            ProcessorRegistrar.getProcessor(properties.getParsedProperty(raw))
            .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert properties != null;

        boolean usesTransient = false;
        for (RawProperty raw : properties.getRawProperties()) {
            usesTransient |= ProcessorRegistrar.getProcessor(properties.getParsedProperty(raw))
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
