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
            return Normal;
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
            return Normal;
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
        if (leftHigh <= rightLow)
            return Normal;
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
    public boolean checkRange() {
        return true;
    }
}
