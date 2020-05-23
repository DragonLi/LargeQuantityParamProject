package LargeQuantityQuery;

import static LargeQuantityQuery.CemQueryNumValOverlap.*;

public class EqualValuePredicate extends ValuePredicate {
    public String eqVal;

    @Override
    public String toString() {
        return "="+eqVal;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append(dbFieldName).append(" = '").append(eqVal);
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null && requiredCnt.contains(eqVal);
    }

    @Override
    public CemQueryNumValOverlap testMergedWith(ValuePredicate other) {
        return other.testOverlap(this);
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(EqualValuePredicate left) {
        //Normal,EdgeOverlap,OutOfOrder,MixedNumWithStr
        if (this.eqVal.equals(left.eqVal)){
            return EdgeOverlap;
        }
        int rightOp=Integer.MIN_VALUE,leftOp=Integer.MIN_VALUE;
        try{
            rightOp=Integer.valueOf(eqVal);
        }catch (NumberFormatException ex){
        }
        try{
            leftOp = Integer.valueOf(left.eqVal);
        }catch (NumberFormatException ex){
        }
        if (rightOp != Integer.MIN_VALUE && leftOp != Integer.MIN_VALUE){
            if (leftOp > rightOp)
                return OutOfOrder;
        }
        if (rightOp == Integer.MIN_VALUE && leftOp != Integer.MIN_VALUE
                || rightOp != Integer.MIN_VALUE && leftOp == Integer.MIN_VALUE){
            return MixedNumWithStr;
        }
        return Normal;
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(RangePredicate left) {
        //Normal,OutOfOrder,MixedNumWithStr
        int rightOp;
        try{
            rightOp=Integer.valueOf(eqVal);
        }catch (NumberFormatException ex){
            return MixedNumWithStr;
        }

        int high = Integer.valueOf(left.upBound);
        if (high >= rightOp)
            return OutOfOrder;
        return Normal;
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(AboveValuePredicate left) {
        //Overlap,OutOfOrder,MixedNumWithStr
        int rightOp;
        try{
            rightOp=Integer.valueOf(eqVal);
        }catch (NumberFormatException ex){
            return MixedNumWithStr;
        }
        int leftOp = Integer.valueOf(left.lowBound);
        if (leftOp < rightOp)
            return Overlap;

        return OutOfOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof EqualValuePredicate){
            EqualValuePredicate other = (EqualValuePredicate) obj;
            return other.eqVal.equals(eqVal);
        }
        return false;
    }

    @Override
    public void increaseLowerBoundNumber() {}

    @Override
    public boolean checkRange() {
        return true;
    }
}
