package epmc.prism.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.jani.model.type.JANIType;
import epmc.value.Type;

/**
 * A single stochastically chosen alternative of a {@link Command}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Alternative {
	/** Weight (rate or probability) of this command. */
    private final Expression weight;
    /** Effect of this command.
     * Maps each variable which is changed to an expression about its new value.
     * */
    private final Map<Expression,Expression> effect;
    /** Position information about the alternative. */
    private final Positional positional;
    
    public Alternative(Expression weight, Map<Expression,Expression> effect, Positional positional) {
        assert weight != null;
        assert effect != null;
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            assert entry.getKey() instanceof ExpressionIdentifier;
        }
        this.weight = weight;
        this.effect = new HashMap<>();
        this.effect.putAll(effect);
        this.positional = positional;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(weight + " : ");
        if (effect.isEmpty()) {
            builder.append("true");
        }
        int effectNr = 0;
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            builder.append("(");
            builder.append(entry.getKey() + "'=" + entry.getValue());
            builder.append(")");
            if (effectNr < effect.size() - 1) {
                builder.append(" & ");
            }
            effectNr++;
        }
        if (positional != null) {
            builder.append(" (" + positional + ")");
        }
        
        return builder.toString();
    }
    
    public Expression getWeight() {
        return weight;
    }
    
    public Map<Expression,Expression> getEffect() {
        return Collections.unmodifiableMap(effect);
    }
    
    Alternative replaceFormulas(Map<Expression,Expression> formulas) {
        Expression newWeight = UtilExpressionStandard.replace(this.weight, formulas);
        Map<Expression,Expression> newEffects = new HashMap<>();
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            Expression newRhs = entry.getValue();
            newRhs = UtilExpressionStandard.replace(newRhs, formulas);
            newEffects.put(entry.getKey(), newRhs);
        }
        return new Alternative(newWeight, newEffects, positional);
    }
    
    void checkExpressionConsistency(
            Map<Expression, JANIType> globalVariables,
            Map<Expression, JANIType> variables,
            Map<Expression, Type> types)
                    throws EPMCException {
    	// TODO
    	/*
        Type weightType = weight.getType();
        
        ensure(weightType == null || !TypeBoolean.isBoolean(weightType),
                ProblemsPRISM.WEIGHT_BOOLEAN, weight);
                */
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            Expression left = entry.getKey();
            Type definedType = null;
            definedType = types.get(left);
            // TODO
            /*
            Type actualType = entry.getValue().getType();
            ensure(actualType != null, ProblemsPRISM.UNKNOWN_IN_EXPR, entry.getValue());
            ensure(definedType.canImport(actualType), ProblemsPRISM.INCOMPATIBLE_ASSIGNMENT,
                    entry.getKey(), entry.getValue());
	        */
        }
    }    
    
    public Positional getPositional() {
        return positional;
    }
}
