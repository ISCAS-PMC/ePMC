package epmc.guardedcommand.model.convert;

/**
 * Method to integrate rewards into JANI model converted from GuardedCommand.
 * 
 * @author Ernst Moritz Hahn
 */
public enum RewardMethod {
	/** Integrate reward assignments into existing automata. */
	INTEGRATE,
	/** Create new automaton to integrate rewards into model. */
	EXTERNAL,
	/** Do not convert rewards. */
	NONE
}
