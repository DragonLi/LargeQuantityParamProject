package LargeQuantityQuery;

import static LargeQuantityQuery.CemQueryNumValOverlap.*;

public class RangePredicate extends ValuePredicate {
    public String lowBound;
    public String upBound;

    @Override
    public String toString() {
        return lowBound+"-"+upBound;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append('(').append(dbFieldName).append(" between ").append(lowBound)
                .append(" and ").append(upBound).append(')');
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null &&
                (requiredCnt.contains(lowBound) || requiredCnt.contains(upBound));
    }

    @Override
    public CemQueryNumValOverlap testMergedWith(ValuePredicate other) {
        return other.testOverlap(this);
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(EqualValuePredicate left) {
        //Normal,EdgeOverlap,Overlap,OutOfOrder,MixedNumWithStr
        int leftOp;
        try{
            leftOp=Integer.parseInt(left.eqVal);
        }catch (NumberFormatException ex){
            return MixedNumWithStr;
        }

        int low = Integer.parseInt(lowBound);
        if (leftOp == low)
            return EdgeOverlap;

        int high = Integer.parseInt(upBound);
        if (high >= leftOp && leftOp > low)
            return Overlap;
        if (leftOp > high)
            return OutOfOrder;

        return Normal;
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(RangePredicate left) {
        //Normal,EdgeOverlap,Overlap,OutOfOrder
        int leftLow = Integer.parseInt(left.lowBound);
        int leftHigh = Integer.parseInt(left.upBound);
        int rightLow = Integer.parseInt(lowBound);
        int rightHigh= Integer.parseInt(upBound);
        if (leftHigh < rightLow)
            return Normal;
        if (leftHigh == rightLow)
            return EdgeOverlap;
        if (rightHigh < leftLow)
            return OutOfOrder;
        //leftHigh > rightLow && leftLow <= rightHigh
        return Overlap;
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(AboveValuePredicate left) {
        //Overlap,OutOfOrder
        int leftLow = Integer.parseInt(left.lowBound);
        int rightHigh= Integer.parseInt(upBound);
        if (rightHigh<=leftLow)
            return OutOfOrder;
        return Overlap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof RangePredicate){
            RangePredicate other = (RangePredicate) obj;
            return other.lowBound.equals(lowBound) && other.upBound.equals(upBound) ;
        }
        return false;
    }

    @Override
    public void increaseLowerBoundNumber() {
        int low = Integer.parseInt(lowBound);
        int high = Integer.parseInt(upBound);
        if (low < high){
            lowBound = String.valueOf(low+1);
            return;
        }
        throw new RuntimeException("lower bound is equal or greater to upper bound!");
    }

    @Override
    public boolean checkRange() {
        int low = Integer.parseInt(lowBound);
        int high = Integer.parseInt(upBound);
        return low<=high;
    }

    @Override
    public boolean isCollapsed() {
        int low = Integer.parseInt(lowBound);
        int high = Integer.parseInt(upBound);
        return low == high;
    }

    @Override
    public ValuePredicate collapse() {
        EqualValuePredicate result = new EqualValuePredicate();
        result.eqVal = lowBound;
        return result;
    }
}
