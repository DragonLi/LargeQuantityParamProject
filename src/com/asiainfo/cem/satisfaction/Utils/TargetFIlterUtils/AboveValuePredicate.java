package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

public class AboveValuePredicate extends ValuePredicate {
    public String lowBound;

    @Override
    public String toString() {
        return ">"+lowBound;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append(dbFieldName).append(" > ").append(lowBound);
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null && requiredCnt.contains(lowBound);
    }

    @Override
    public CemQueryNumCheck testMergedWith(ValuePredicate other) {
        return other.testOverlap(this);
    }

    @Override
    protected CemQueryNumCheck testOverlap(EqualValuePredicate left) {
        //Normal,EdgeOverlap,Overlap,MixedNumWithStr
        int leftOp;
        try{
            leftOp=Integer.parseInt(left.eqVal);
        }catch (NumberFormatException ex){
            return CemQueryNumCheck.MixedNumWithStr;
        }
        int rightLow = Integer.parseInt(lowBound);
        if (leftOp <= rightLow){
            return CemQueryNumCheck.Normal;
        }
        if (leftOp == rightLow+1)
            return CemQueryNumCheck.EdgeOverlap;
        //leftOp > rightLow+1
        return CemQueryNumCheck.Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(RangePredicate left) {
        //Normal,EdgeOverlap,Overlap
        int leftLow = Integer.parseInt(left.lowBound);
        int leftHigh = Integer.parseInt(left.upBound);
        int rightLow = Integer.parseInt(lowBound);
        if (leftHigh <= rightLow)
            return CemQueryNumCheck.Normal;
        if (leftHigh == rightLow +1)
            return CemQueryNumCheck.EdgeOverlap;
        //leftHigh > rightLow+1
        return CemQueryNumCheck.Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(AboveValuePredicate left) {
        return CemQueryNumCheck.Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(OpenCloseRangePredicate left) {
        //Normal,Overlap
        int leftHigh = Integer.parseInt(left.upBound);
        int rightLow = Integer.parseInt(lowBound);
        if (leftHigh <= rightLow)
            return CemQueryNumCheck.Normal;
        return CemQueryNumCheck.Overlap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof AboveValuePredicate){
            AboveValuePredicate other = (AboveValuePredicate) obj;
            return other.lowBound.equals(lowBound);
        }
        return false;
    }

    @Override
    public void increaseLowerBoundNumber() {
        int low = Integer.parseInt(lowBound);
        lowBound = String.valueOf(low+1);
    }

    @Override
    public boolean checkRange() {
        return true;
    }
}