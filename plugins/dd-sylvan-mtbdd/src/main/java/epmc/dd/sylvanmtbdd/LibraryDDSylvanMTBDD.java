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

package epmc.dd.sylvanmtbdd;

import static epmc.error.UtilError.ensure;

import java.util.Collection;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

import epmc.dd.ContextDD;
import epmc.dd.LibraryDD;
import epmc.dd.PermutationLibraryDD;
import epmc.dd.ProblemsDD;
import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.util.JNATools;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorId;
import epmc.value.OperatorIte;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMultiply;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;

public class LibraryDDSylvanMTBDD implements LibraryDD {
    public final static String IDENTIFIER = "sylvan-mtbdd";
    
    private final static class LowLevelPermutationSylvan
        implements PermutationLibraryDD {

        private long permMap;

        LowLevelPermutationSylvan(int [] perm, long falseNode, long trueNode) {
            assert perm != null;
            permMap = Sylvan.MTBDD_map_empty();
            // Walk backwards to avoid walking the complete tree each time
            for(int i = perm.length - 1; i >= 0; i--) {
                // Only add it to the map in case this variable should
                // actually be permuted.
                if(i != perm[i]) {
                    permMap = Sylvan.MTBDD_map_add(permMap, i, perm[i]);
                }
            }
            Sylvan.mtbdd_ref(permMap);
        }

        long getMap() {
            return permMap;
        }
        
    }

    private static interface DD_VOP1 extends Callback {
        long invoke(int op, long f);
    }

    private static interface DD_VOP2 extends Callback {
        long invoke(int op, long f, long g);
    }
    
    private class DD_VOP1Impl implements DD_VOP1 {
        @Override
        public long invoke(int op, long f) {
            Value opValue = numberToValue(f);
            try {
                Value result = resultType.newValue();
                Operator operator = operators[op];
                operator.apply(result, opValue);
                return valueToNumber(result);
            } catch (EPMCException e) {
                valueProblem = e;
                return valueToNumber(TypeInteger.get().getZero());
            }
        }
    }
    
    private class DD_VOP2Impl implements DD_VOP2 {
        @Override
        public long invoke(int op, long f, long g) {
            Value op1Value = numberToValue(f);
            Value op2Value = numberToValue(g);
            Operator operator = operators[op];
            try {
                Value result = resultType.newValue();
                operator.apply(result, op1Value, op2Value);
                return valueToNumber(result);
            } catch (EPMCException e) {
                valueProblem = e;
                return valueToNumber(TypeInteger.get().getZero());
            }
        }
    }
    
    private void checkValueProblem() throws EPMCException {
        if (valueProblem != null) {
            EPMCException toThrow = valueProblem;
            valueProblem = null;
            throw toThrow;
        }
    }

    private interface GetOperatorNumber extends Callback  {
        int invoke(String name);
    }

    private class GetOperatorNumberImpl implements GetOperatorNumber {

        @Override
        public int invoke(String name) {
            assert name != null;
            int number = operatorToNumber.get(name);
//            assert operators[number].getIdentifier().equals(name) : 
  //              operators[number].getIdentifier() + " " + name;
            return number;
        }
    }
    
    /**
     * Obtain number of existing {@code Value} object in table or create new.
     * 
     * @param value {@code Value} object to obtain value of
     * @return number representing {@code Value} object
     */
    private long valueToNumber(Value value) {
        valueToNumberTime -= System.nanoTime();
        valueToNumberCalled++;
        if (valueToNumber.containsKey(value)) {
            valueToNumberTime += System.nanoTime();
            return valueToNumber.get(value);
        }
        /*
        for (TLongObjectIterator<Value> it = numberToValue.iterator(); it.hasNext();) {
            it.advance();
            if (it.value().getType() == value.getType()
                    && it.value().isEq(value)) {
                valueToNumberEquals++;
                valueToNumberTime += System.nanoTime();
                return it.key();
            }
        }
        */
        long currentNumber = nextNumber;
    //        currentNumber |= (long) value.getTypeNumber() << 32;
        Value clone = UtilValue.clone(value);
        clone.setImmutable();
        numberToValue.put(currentNumber, clone);
        valueToNumber.put(clone, currentNumber);
        nextNumber++;
        valueToNumberTime += System.nanoTime();
        return currentNumber;
    }
    
    /**
     * Obtain {@code Value} object with given number.
     * 
     * @param number number of {@code Value} object
     * @return {@code Value} object with given number
     */
    private Value numberToValue(long number) {
        return numberToValue.get(number);
    }
    
    /**
     * Interface to call native functions of Sylvan library.
     * The functions that start with a capital letter call
     * the wrapper functions in jna.c.
     */
    private final static class Sylvan {
        static native void lace_init(int workers, int queueSize);
        static native void lace_startup(int workers, Pointer p1, Pointer p2);
        /** initialise Sylvan */
        static native void sylvan_init_package(long initial_tablesize,
                long max_tablesize, long initial_cachesize, long max_cachesize);
        static native void sylvan_init_mtbdd(int granularity);
        static native void epmc_init_mtbdd(DD_VOP1 vop1, DD_VOP2 vop2, GetOperatorNumber getOperatorNumber,
                long trueNode, long falseNode);
        /** free Sylvan resources */
        static native void sylvan_quit();

        /** increase reference counter of Sylvan DD node */
        static native long mtbdd_ref(long n);
        /** decrease reference counter of Sylvan DD node */
        static native void mtbdd_deref(long n);
        
        static native long MTBDD_uapply(long f, int op);
        static native long MTBDD_apply(long f, long g, int op);
        
        static native long MTBDD_ite(long f, long g, long h);
        
        static native long mtbdd_getlow(long f);
        
        static native long mtbdd_gethigh(long f);

        /** obtain variable BDD node (with boolean branches) */
        static native long mtbdd_makenode(long var, long low, long high);
        static native int mtbdd_getvar(long f);
        static native long mtbdd_makeleaf(int type, long value);
        static native int mtbdd_isleaf(long f);
        static native long mtbdd_getvalue(long g);
        
        static native long MTBDD_abstract(long f, long cube, int op);
        
        /** functions used for permutations */
        static native long MTBDD_compose(long f, long map);
        static native long MTBDD_map_empty();
        static native long MTBDD_map_add(long map, int fromVar, int toVar);
        
        /** debug function to print f to stdout */
        static native void MTBDD_print(long f);
        
        private final static boolean loaded =
                JNATools.registerLibrary(Sylvan.class, "sylvan-mtbdd");
    }
    
    private static final long COMPLEMENT = 0x8000000000000000L;
    private long falseNode;
    private long trueNode;
    private Value valueTrue;
    private Value valueFalse;
    private Type resultType;
    private long nextVariable = 0;
    /** number to use for next new Value object in table */
    private long nextNumber = 0;
    /** maps long to Value object it represents */
    private TLongObjectHashMap<Value> numberToValue;
    /** maps Value object to its number */
    private TObjectLongHashMap<Value> valueToNumber;
    private Operator[] operators;
    private TObjectIntHashMap<String> operatorToNumber = new TObjectIntHashMap<>();

    private long valueToNumberTime;
    private long valueToNumberCalled;
    private long valueToNumberEqq;
    private long valueToNumberEquals;
    private EPMCException valueProblem;
        
    private ContextDD contextDD;
    private DD_VOP1 vop1;
    private DD_VOP2 vop2;
    private boolean alive = true;
    
    // TODO make sure mapping of operators still works
    @Override
    public void setContextDD(ContextDD contextDD) throws EPMCException {
        assert contextDD !=null;
        ensure(Sylvan.loaded, ProblemsDD.SYLVAN_NATIVE_LOAD_FAILED);
        
        this.contextDD = contextDD;
        Collection<Operator> operators = ContextValue.get().getOperators().values();
        this.operators = new Operator[operators.size()];
        this.operators = operators.toArray(this.operators);
        int opNr = 0;
        for (String operator : ContextValue.get().getOperators().keySet()) {
            this.operatorToNumber.put(operator, opNr);
        	opNr++;
        }
        this.numberToValue = new TLongObjectHashMap<>();
        this.valueToNumber = new TObjectLongHashMap<>();
        
        vop1 = new DD_VOP1Impl();
        vop2 = new DD_VOP2Impl();
        GetOperatorNumber getOperatorNumber = new GetOperatorNumberImpl();
        int workers = Options.get().getInteger(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_WORKERS);
        long initMem = Options.get().getInteger(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_INIT_NODES);
        long initCache = Options.get().getInteger(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_INIT_CACHE_SIZE);
        int cacheGranularity = Options.get().getInteger(OptionsDDSylvanMTBDD.DD_SYLVAN_MTBDD_CACHE_GRANULARITY);
        
        Sylvan.lace_init(workers, 1000000);
        Sylvan.lace_startup(0, Pointer.NULL, Pointer.NULL);
        Sylvan.sylvan_init_package(initMem, 1 << 27, initCache, 1 << 26);
        Sylvan.sylvan_init_mtbdd(cacheGranularity);
        
        this.valueTrue = TypeBoolean.get().getTrue();
        this.valueFalse = TypeBoolean.get().getFalse();
        falseNode = newConstant(valueFalse);
        trueNode = newConstant(valueTrue);
        Sylvan.epmc_init_mtbdd(vop1, vop2, getOperatorNumber, trueNode, falseNode);
    }

    @Override
    public ContextDD getContextDD() {
        return contextDD;
    }

    @Override
    public long apply(String operation, Type type, long... operands) throws EPMCException {
        assert operation != null;
        assert type != null;
        this.resultType = type;
        long result;
        if (operands.length == 1) {
            result = Sylvan.MTBDD_uapply(operands[0], operatorToNumber.get(operation));
        } else if (operands.length == 2) {
            result = Sylvan.MTBDD_apply(operands[0], operands[1], operatorToNumber.get(operation));
        } else if (operands.length == 3) {
            this.resultType = type;
            boolean doFree = false;
            long idOp2, idOp3;
            if (TypeBoolean.isBoolean(type)) {
                idOp2 = operands[1];
                idOp3 = operands[2];
            } else {
                // Call the identity operator to convert all the terminals to the same type.
                idOp2 = Sylvan.MTBDD_uapply(operands[1], operatorToNumber.get(OperatorId.IDENTIFIER));
                Sylvan.mtbdd_ref(idOp2);
                idOp3 = Sylvan.MTBDD_uapply(operands[2], operatorToNumber.get(OperatorId.IDENTIFIER));
                Sylvan.mtbdd_ref(idOp3);
                doFree = true;
            }
            result = Sylvan.MTBDD_ite(operands[0], idOp2, idOp3);
            if (doFree) {
                Sylvan.mtbdd_deref(idOp2);
                Sylvan.mtbdd_deref(idOp3);
            }        	
        } else {
        	assert false;
        	result = -1;
        }
        checkValueProblem();
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public long newConstant(Value value) throws EPMCException {
        assert value != null;
        long result = Sylvan.mtbdd_makeleaf(3, valueToNumber(value));
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public long newVariable() throws EPMCException {
        long result = Sylvan.mtbdd_makenode(nextVariable, falseNode, trueNode);
//        result = Sylvan.mtbdd_makenode(10, result, trueNode);
        nextVariable++;
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public boolean isLeaf(long dd) {
        return Sylvan.mtbdd_isleaf(dd) == 1;
    }

    @Override
    public Value value(long dd) {
        assert isLeaf(dd);
        return numberToValue(Sylvan.mtbdd_getvalue(dd));
    }

    @Override
    public int variable(long dd) {
        return Sylvan.mtbdd_getvar(dd);
    }

    @Override
    public void reorder() throws EPMCException {
        
    }

    @Override
    public void addGroup(int startVariable, int numVariables, boolean fixedOrder) {
        
    }

    @Override
    public long permute(long dd, PermutationLibraryDD permutation)
            throws EPMCException {
        assert permutation != null;
        assert permutation instanceof LowLevelPermutationSylvan;
        return Sylvan.mtbdd_ref(Sylvan.MTBDD_compose(dd, ((LowLevelPermutationSylvan) permutation).getMap()));
    }

    @Override
    public long clone(long uniqueId) {
        Sylvan.mtbdd_ref(uniqueId);
        return uniqueId;
    }

    @Override
    public void free(long uniqueId) {
        Sylvan.mtbdd_deref(uniqueId);
    }

    @Override
    public long getWalker(long uniqueId) {
        return uniqueId;
    }

    @Override
    public long walkerLow(long uniqueId) {
        return Sylvan.mtbdd_getlow(uniqueId);
    }

    @Override
    public long walkerHigh(long uniqueId) {
        return Sylvan.mtbdd_gethigh(uniqueId);
    }

    @Override
    public boolean isComplement(long node) {
        return (node & COMPLEMENT) == COMPLEMENT;
    }

    @Override
    public long walkerComplement(long from) {
        return from ^ COMPLEMENT;
    }

    @Override
    public long walkerRegular(long from) {
        return from & ~COMPLEMENT ;
    }

    @Override
    public long abstractExist(long dd, long cube) throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public long abstractForall(long dd, long cube) throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public long abstractSum(Type type, long dd, long cube) throws EPMCException {
        assert type != null;
        this.resultType = type;
        long result = Sylvan.MTBDD_abstract(dd, cube, operatorToNumber.get(OperatorAdd.IDENTIFIER));
        checkValueProblem();
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public long abstractProduct(Type type, long dd, long cube) throws EPMCException {
        assert type != null;
        this.resultType = type;
        long result = Sylvan.MTBDD_abstract(dd, cube, operatorToNumber.get(OperatorMultiply.IDENTIFIER));
        checkValueProblem();
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public long abstractMax(Type type, long dd, long cube) throws EPMCException {
        assert type != null;
        this.resultType = type;
        long result = Sylvan.MTBDD_abstract(dd, cube, operatorToNumber.get(OperatorMax.IDENTIFIER));
        checkValueProblem();
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public long abstractMin(Type type, long dd, long cube) throws EPMCException {
        assert type != null;
        this.resultType = type;
        long result = Sylvan.MTBDD_abstract(dd, cube, operatorToNumber.get(OperatorMin.IDENTIFIER));
        checkValueProblem();
        Sylvan.mtbdd_ref(result);
        return result;
    }

    @Override
    public long abstractAndExist(long dd1, long dd2, long cube)
            throws EPMCException {
        assert false;
        return -1;
    }

    @Override
    public PermutationLibraryDD newPermutation(int[] permutation) throws EPMCException {
        return new LowLevelPermutationSylvan(permutation, falseNode, trueNode);
    }

    @Override
    public boolean equals(long op1, long op2) {
        return op1 == op2;
    }

    @Override
    public void close() {
        if (!alive ) {
            return;
        }
        alive = false;

        if (contextDD.isDebugDD()) {
            System.out.println("BDD debugging info:");
            System.out.println("valueToNumberTime: " + valueToNumberTime * 1E-9);
            System.out.println("valueToNumberCalled: " + valueToNumberCalled);
            System.out.println("valueToNumberEqq: " + valueToNumberEqq);
            System.out.println("valueToNumberEquals: " + valueToNumberEquals);
        }
        Sylvan.sylvan_quit();
    }

    @Override
    public boolean walkerIsLeaf(long dd) {
        return isLeaf(dd);
    }

    @Override
    public Value walkerValue(long dd) {
        assert walkerIsLeaf(dd);
        return value(dd);
    }

    @Override
    public int walkerVariable(long dd) {
        return Sylvan.mtbdd_getvar(dd);
    }

    @Override
    public boolean walkerIsComplement(long from) {
        return isComplement(from);
    }

    @Override
    public boolean hasInverterArcs() {
        return true;
    }

    @Override
    public int hashCode(long uniqueId) {
        return (int) uniqueId;
    }

    @Override
    public boolean hasAndExist() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
	@Override
	public boolean canApply(String operation, Type resultType, long... operands) {
		if (operands.length > 3) {
			return false;
		}
		if (operands.length == 3 && !operation.equals(OperatorIte.IDENTIFIER)) {
			return false;
		}
		return true;
	}
}
