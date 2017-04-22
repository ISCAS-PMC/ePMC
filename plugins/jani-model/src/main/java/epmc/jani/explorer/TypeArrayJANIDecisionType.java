package epmc.jani.explorer;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;
import epmc.value.ValueArray;

public final class TypeArrayJANIDecisionType implements TypeArray {
	private final TypeDecision entryType;

	TypeArrayJANIDecisionType(TypeDecision entryType) {
		assert entryType != null;
		this.entryType = entryType;
	}
	
	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeArray getTypeArray() {
		return getContext().makeUnique(new TypeArrayGeneric(this));
	}

	@Override
	public TypeDecision getEntryType() {
		return entryType;
	}

	@Override
	public ValueArray newValue() {
		return new ValueArrayJANIDecision(this);
	}

}