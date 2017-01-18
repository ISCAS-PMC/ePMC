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

package epmc.graph.explicit.induced;

import epmc.error.EPMCException;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.value.Type;
import epmc.value.Value;

final class EdgePropertyInduced implements EdgeProperty {
    private final GraphExplicitInduced graph;
    private final EdgeProperty inner;

    EdgePropertyInduced(GraphExplicitInduced graph, EdgeProperty inner) {
        assert graph != null;
        assert inner != null;
        this.graph = graph;
        this.inner = inner;
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int successor) throws EPMCException {
        assert successor >= 0;
        int decision = graph.getDecision();
        assert successor < (decision == -1 ? graph.getNumSuccessors() : 1);
        return inner.get(decision == -1 ? successor : decision);
    }

    @Override
    public void set(Value value, int successor) {
        assert value != null;
        assert successor >= 0;
        int decision = graph.getDecision();
        assert successor < (decision == -1 ? graph.getNumSuccessors() : 1);
        inner.set(value, decision == -1 ? successor : decision);
    }

    @Override
    public Type getType() {
        return inner.getType();
    }

}