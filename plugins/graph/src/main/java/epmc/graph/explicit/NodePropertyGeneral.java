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

package epmc.graph.explicit;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeHasNativeArray;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueEnum;
import epmc.value.ValueObject;

/**
 * General node property.
 * This node property can be used for an explicit-state graph, for any type of
 * value. All values of the property have to be set explicitly.
 * 
 * @author Ernst Moritz Hahn
 */
public final class NodePropertyGeneral implements NodeProperty {
    /** Graph to which this property belogns. */
    private final GraphExplicit graph;
    /** Value returned by {@link #get()}. */
    private final Value value;
    /** Array value storing the values for all nodes so far. */
    private final ValueArray content;
    /** Value for nodes for which no value was set. */
    private final Value defaultValue;

    /**
     * Construct new general node property.
     * The type of the node property is derived from the default value.
     * None of the parameters may be {@code null}.
     * 
     * @param graph graph to which the property shall belong
     * @param defaultValue value for nodes for which no value was set
     * @param forNative whether to store values in native memory if possible
     */
    public NodePropertyGeneral(GraphExplicit graph, Value defaultValue, boolean forNative) {
        assert graph != null;
        assert defaultValue != null;
        Type type = defaultValue.getType();
        this.graph = graph;
        this.value = type.newValue();
        this.defaultValue = UtilValue.clone(defaultValue);
        TypeArray typeArray = forNative
                ? TypeHasNativeArray.getTypeNativeArray(type)
                        : type.getTypeArray();
                this.content = UtilValue.newArray(typeArray, 1);
    }

    /**
     * Construct new general node property.
     * The default value used is the one obtained by {@link Type#newValue()}.
     * None of the parameters may be {@code null}.
     * 
     * @param graph graph to which the property shall belong
     * @param type type of the node property
     * @param forNative whether to store values in native memory if possible
     */
    public NodePropertyGeneral(GraphExplicit graph, Type type, boolean forNative) {
        assert graph != null;
        assert type != null;
        this.graph = graph;
        this.value = type.newValue();
        this.defaultValue = type.newValue();
        TypeArray typeArray = forNative
                ? TypeHasNativeArray.getTypeNativeArray(type)
                : type.getTypeArray();
        if (typeArray == null) {
            typeArray = type.getTypeArray();
        }
        this.content = UtilValue.newArray(typeArray, 1);
    }

    @Override
    public Value get() {
        ensureSize();
        content.get(value, graph.getQueriedNode());
        return value;
    }

    @Override
    public void set(Value value) throws EPMCException {
        ensureSize();
        content.set(value, graph.getQueriedNode());
    }

    @Override
    public void set(Object object) throws EPMCException {
        assert object != null;
        assert ValueObject.isObject(value);
        ensureSize();
        ValueObject.asObject(value).set(object);
        content.set(value, graph.getQueriedNode());
    }    

    @Override
    public void set(Enum<?> object) throws EPMCException {
        assert object != null;
        assert ValueEnum.isEnum(value);
        ensureSize();
        ValueEnum.asEnum(value).set(object);
        content.set(value, graph.getQueriedNode());
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    /**
     * Extends the size of the array storing node values, if necessary.
     * The new size will be at least as large as the
     * {@link GraphExplicit#getQueriedNode()} + 1, ensuring that a value for
     * the queried node can be stored or read.
     * This function should be called before any operation reading or storing
     * node values to ensure {@link #content} is large enough.
     */
    private void ensureSize() {
        int newSize = graph.getQueriedNode() + 1;
        int size = content.size();
        int oldSize = size;
        if (newSize <= size) {
            return;
        }
        if (size == 0) {
            size = 1;
        }
        while (size < newSize) {
            size *= 2;
        }
        content.resize(size);
        for (int entry = oldSize; entry < size; entry++) {
            content.set(defaultValue, entry);
        }
    }
}