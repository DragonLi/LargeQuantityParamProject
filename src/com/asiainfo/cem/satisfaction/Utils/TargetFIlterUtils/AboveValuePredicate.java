package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import java.util.Objects;

import static com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.CemQueryNumCheck.*;

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
            return MixedNumWithStr;
        }
        int rightLow = Integer.parseInt(lowBound);
        if (leftOp <= rightLow){
            return leftOp == rightLow? Normal:LeakFloatGap;
        }
        if (leftOp == rightLow+1)
            return EdgeOverlap;
        //leftOp > rightLow+1
        return Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(RangePredicate left) {
        //Normal,EdgeOverlap,Overlap
        int leftHigh = Integer.parseInt(left.upBound);
        int rightLow = Integer.parseInt(lowBound);
        if (leftHigh <= rightLow)
            return leftHigh == rightLow? Normal:LeakFloatGap;
        if (leftHigh == rightLow +1)
            return EdgeOverlap;
        //leftHigh > rightLow+1
        return Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(AboveValuePredicate left) {
        return Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(OpenCloseRangePredicate left) {
        //Normal,Overlap
        int leftHigh = Integer.parseInt(left.upBound);
        int rightLow = Integer.parseInt(lowBound);
        if (leftHigh <= rightLow){
            if (left.isRightOpen)
                return LeakFloatGap;
            return leftHigh == rightLow? Normal:LeakFloatGap;
        }
        return Overlap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AboveValuePredicate that = (AboveValuePredicate) o;
        return Objects.equals(lowBound, that.lowBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowBound);
    }

    @Override
    public void increaseLowerBoundNumber() {
        int low = Integer.parseInt(lowBound);
        lowBound = String.valueOf(low+1);
    }

    @Override
    public ValuePredicate merge(ValuePredicate predicate) {
        return predicate.mergeWith(this);
    }

    @Override
    protected ValuePredicate mergeWith(EqualValuePredicate left) {
        AboveOrEqualPredicate result = new AboveOrEqualPredicate();
        result.lowBound=left.eqVal;
        return result;
    }

    @Override
    protected ValuePredicate mergeWith(RangePredicate left) {
        AboveOrEqualPredicate result = new AboveOrEqualPredicate();
        result.lowBound=left.lowBound;
        return result;
    }

    @Override
    protected ValuePredicate mergeWith(AboveValuePredicate left) {
        throw new RuntimeException("bug: overlap range");
    }

    @Override
    protected ValuePredicate mergeWith(OpenCloseRangePredicate left) {
        if (left.isLeftOpen){
            AboveValuePredicate r = new AboveValuePredicate();
            r.lowBound = left.lowBound;
            return r;
        }
        AboveOrEqualPredicate r = new AboveOrEqualPredicate();
        r.lowBound = left.lowBound;
        return r;
    }

    @Override
    protected ValuePredicate mergeWith(AboveOrEqualPredicate left) {
        throw new RuntimeException("bug: overlap range");
    }

    @Override
    public boolean checkRange() {
        return true;
    }
}
