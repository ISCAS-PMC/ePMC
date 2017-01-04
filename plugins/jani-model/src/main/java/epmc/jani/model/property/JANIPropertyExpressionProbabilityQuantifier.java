package epmc.jani.model.property;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

/**
 * JANI quantifier expression.
 * 
 * @author Ernst Moritz Hahn
 * @author Andrea Turrini
 */
public final class JANIPropertyExpressionProbabilityQuantifier implements JANIExpression {
	/** Identifier of this JANI expression type. */
	public final static String IDENTIFIER = "jani-property-expression-probability-quantifier";
	private final static String OP = "op";
	private final static String PMIN = "Pmin";
	private final static String PMAX = "Pmax";
	private final static String EXP = "exp";
	private final static Map<String,DirType> STRING_TO_DIR_TYPE;
	static {
		Map<String,DirType> stringToDirType = new LinkedHashMap<>();
		stringToDirType.put(PMIN, DirType.MIN);
		stringToDirType.put(PMAX, DirType.MAX);
		STRING_TO_DIR_TYPE = Collections.unmodifiableMap(stringToDirType);
	}
	
	private Map<String, ? extends JANIIdentifier> validIdentifiers;
	private ModelJANI model;
	private boolean forProperty;
	
	private boolean initialized;
	private String opValue;
	private DirType dirType;
	private JANIExpression exp;
	
	private void resetFields() {
		initialized = false;
		opValue = null;
		dirType = null;
		exp = null;
	}
	
	public JANIPropertyExpressionProbabilityQuantifier() {
		resetFields();
	}

	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		return parseAsJANIExpression(value);
	}
	
	@Override 
	public JANIExpression parseAsJANIExpression(JsonValue value) throws EPMCException {
		assert model != null;
		assert validIdentifiers != null;
		assert value != null;
		resetFields();
		if (!forProperty) {
			return null;
		}
		if (!(value instanceof JsonObject)) {
			return null;
		}
		JsonObject object = (JsonObject) value;
		if (!object.containsKey(OP)) {
			return null;
		}
		if (!(object.get(OP) instanceof JsonString)) {
			return null;
		}
		if (!object.containsKey(EXP)) {
			return null;
		}
		dirType = UtilJSON.toOneOfOrNull(object, OP, STRING_TO_DIR_TYPE);
		if (dirType == null) {
			return null;
		}
		opValue = UtilJSON.getString(object, OP);
		ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
		exp = parser.parseAsJANIExpression(object.get(EXP));
		if (exp == null) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		assert initialized;
		assert model != null;
		assert validIdentifiers != null;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(OP, opValue);
		builder.add(EXP, exp.generate());
		return builder.build();
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) throws EPMCException {
		assert expression != null;
		assert model != null;
		assert validIdentifiers != null;
		resetFields();
		if (!(expression instanceof ExpressionQuantifier)) {
			return null;
		}
		ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
		Expression quantified = expressionQuantifier.getQuantified();
		if (quantified instanceof ExpressionReward || quantified instanceof ExpressionSteadyState) {
			return null;
		}
		ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
		exp = parser.matchExpression(model, quantified);
		dirType = expressionQuantifier.getDirType();
		switch (dirType) {
		case MAX:
			opValue = PMAX;
			break;
		case MIN:
			opValue = PMIN;
			break;
		default:
//			the only possibility is "NONE", i.e., we are in a dtmc/ctmc model
//			thus, every operator is OK.
			opValue = PMAX;
			break;
		}
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() throws EPMCException {
		assert initialized;
		assert model != null;
		assert validIdentifiers != null;
		return new ExpressionQuantifier.Builder()
				.setContext(this.model.getContextValue())
				.setCmpType(CmpType.IS)
				.setDirType(dirType)
				.setQuantified(exp.getExpression())
				.build();
	}

	@Override
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
		this.validIdentifiers = identifiers;
	}	

	@Override
	public void setForProperty(boolean forProperty) {
		this.forProperty = forProperty;
	}
	
	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}

	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
