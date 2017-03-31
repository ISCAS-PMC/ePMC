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

package epmc.jani.type.smg;

import epmc.error.EPMCException;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.ModelJANIProcessor;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class PlayerJANIProcessor implements JANI2PRISMProcessorExtended {

	private PlayerJANI player = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof PlayerJANI; 
		
		player = (PlayerJANI) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert player != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		boolean remaining = false;

		prism.append("player ").append(player.getName());
		
		for (Automaton automaton: player.getAutomataOrEmpty()) {
			if (remaining) {
				prism.append(", ");
			} else {
				remaining = true;
			}
			prism.append("\n")
				 .append(ModelJANIProcessor.INDENT)
				 .append(automaton.getName());
		}
		for (Action action: player.getActionsOrEmpty()) {
			if (remaining) {
				prism.append(", ");
			} else {
				remaining = true;
			}
			prism.append("\n")
				 .append(ModelJANIProcessor.INDENT)
				 .append("[")
				 .append(JANIComponentRegistrar.getActionName(action))
				 .append("]");
		}
		
		prism.append("\nendplayer\n");
		
		return prism;
	}
}