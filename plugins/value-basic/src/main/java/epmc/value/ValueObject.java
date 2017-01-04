package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Value;

public final class ValueObject implements Value {
	public static boolean isObject(Value value) {
		return value instanceof ValueObject;
	}
	
	public static ValueObject asObject(Value value) {
		if (isObject(value)) {
			return (ValueObject) value;
		} else {
			return null;
		}
	}
	
    private final static String SPACE = " ";
    
    private Object content;
    private final TypeObject type;
    private boolean immutable;
    
    ValueObject(TypeObject type) {
        assert type != null;
        this.type = type;
    }

    ValueObject(TypeObject type, Object content) {
        assert type != null;
        assert content == null || type.getUsedClass().isInstance(content);
        this.type = type;
        this.content = content;
    }

    @Override
    public TypeObject getType() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getObject() {
        return (T) content;
    }

    public void set(Object content) {
        assert !isImmutable();
        assert content == null ||
                getType().getUsedClass().isInstance(content) :
                    content + SPACE + content.getClass()
                    + SPACE + getType().getUsedClass();
        this.content = content;
    }
    
    @Override
    public Value clone() {
        return new ValueObject(getType(), content);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueObject)) {
            return false;
        }
        ValueObject other = (ValueObject) obj;
        return content.equals(other.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public String toString() {
        return "value(" + content + ")";
    }

    @Override
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
        assert ValueObject.isObject(op);
        content = ValueObject.asObject(op).getObject();
    }
    
    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public double distance(Value other) throws EPMCException {
    	ValueObject otherObject = asObject(other);
    	return content.equals(otherObject.content) ? 0.0 : 1.0;
    }

	@Override
	public int compareTo(Value other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEq(Value other) throws EPMCException {
    	ValueObject otherObject = asObject(other);
    	return content.equals(otherObject.content);
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
