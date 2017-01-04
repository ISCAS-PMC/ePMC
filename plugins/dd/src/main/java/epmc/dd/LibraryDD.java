package epmc.dd;

import java.io.Closeable;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

/**
 * Interface for classes providing support for a given DD library.
 * 
 * @author Ernst Moritz Hahn
 */
public interface LibraryDD extends Closeable {
    /* methods to be implemented by implementing classes */
    
    String getIdentifier();

    void setContextDD(ContextDD contextDD) throws EPMCException;

    ContextDD getContextDD();
    
    boolean canApply(Operator operation, Type resultType, long...operands);
    
    long apply(Operator operator, Type resultType, long... operands) throws EPMCException;

    long newConstant(Value value) throws EPMCException;

    long newVariable() throws EPMCException;

    boolean isLeaf(long dd);

    Value value(long dd);

    int variable(long dd);

    void reorder() throws EPMCException;

    void addGroup(int startVariable, int numVariables, boolean fixedOrder);
    
    long permute(long dd, PermutationLibraryDD permutation)
            throws EPMCException;

    long clone(long uniqueId);
    
    void free(long uniqueId);
    
    long getWalker(long uniqueId);

    boolean walkerIsLeaf(long dd);

    Value walkerValue(long dd);

    int walkerVariable(long dd);

    long walkerLow(long uniqueId);

    long walkerHigh(long uniqueId);

    long walkerRegular(long from);

    boolean walkerIsComplement(long node);
    
    long walkerComplement(long from);

    boolean isComplement(long node);

    // TODO replace by
//    long abstractApply(long dd, long cube, Operator operator) throws EPMCException;
//    boolean supportsAbstract(Operator operator);
    
    long abstractExist(long dd, long cube) throws EPMCException;

    long abstractForall(long dd, long cube) throws EPMCException;

    long abstractSum(Type type, long dd, long cube) throws EPMCException;

    long abstractProduct(Type type, long dd, long cube) throws EPMCException;

    long abstractMax(Type type, long dd, long cube) throws EPMCException;

    long abstractMin(Type type, long dd, long cube) throws EPMCException;

    
    long abstractAndExist(long dd1, long dd2, long cube) throws EPMCException;

    boolean hasAndExist();    
    
    PermutationLibraryDD newPermutation(int[] permutation) throws EPMCException;
    
    boolean equals(long op1, long op2);

    boolean hasInverterArcs();

    public int hashCode(long uniqueId);
    
    @Override
    void close();

    
    /* default methods */
    
    default boolean checkConsistent() {
        return true;
    }
}
