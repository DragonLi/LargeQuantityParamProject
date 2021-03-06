package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import java.util.Objects;

import static com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.CemQueryNumCheck.*;

public class OpenCloseRangePredicate extends ValuePredicate {
    public String lowBound;
    public String upBound;
    public boolean isLeftOpen;
    public boolean isRightOpen;

    @Override
    public String toString() {
        return (isLeftOpen? "(":"[")+ lowBound+"-"+upBound+(isRightOpen?")":"]");
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append('(')
                .append(dbFieldName).append(isLeftOpen?">":">=").append(lowBound)
                .append(" and ")
                .append(dbFieldName).append(isRightOpen?"<":"<=").append(upBound)
                .append(')');
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null &&
                requiredCnt.contains(lowBound+"-"+upBound);
    }

    @Override
    public CemQueryNumCheck testMergedWith(ValuePredicate other) {
        return other.testOverlap(this);
    }

    @Override
    protected CemQueryNumCheck testOverlap(EqualValuePredicate left) {
        //Normal,EdgeOverlap,Overlap,OutOfOrder,MixedNumWithStr
        int leftOp;
        try{
            leftOp=Integer.parseInt(left.eqVal);
        }catch (NumberFormatException ex){
            return MixedNumWithStr;
        }

        int low = Integer.parseInt(lowBound);
        if (!isLeftOpen && leftOp == low)
            return EdgeOverlap;

        int high = Integer.parseInt(upBound);
        if ((isLeftOpen?leftOp>low:leftOp>=low)
                && (isRightOpen?leftOp<high:leftOp<=high))
            return Overlap;

        if (isRightOpen? leftOp>= high : leftOp > high)
            return OutOfOrder;

        return leftOp == low? Normal:LeakFloatGap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(RangePredicate left) {
        return MixedFloatRangeWithIntRange;
    }

    @Override
    protected CemQueryNumCheck testOverlap(AboveValuePredicate left) {
        //Overlap,OutOfOrder
        int leftLow = Integer.parseInt(left.lowBound);
        int rightHigh= Integer.parseInt(upBound);
        if (isRightOpen){
            if (rightHigh<leftLow)
                return OutOfOrder;
        }else{
            if (rightHigh<=leftLow)
                return OutOfOrder;
        }
        return Overlap;
    }

    @Override
    protected CemQueryNumCheck testOverlap(OpenCloseRangePredicate left) {
        //Normal,EdgeOverlap,Overlap,OutOfOrder
        int leftLow = Integer.parseInt(left.lowBound);
        int leftHigh = Integer.parseInt(left.upBound);
        int rightLow = Integer.parseInt(lowBound);
        int rightHigh= Integer.parseInt(upBound);
        if (leftHigh < rightLow
                || leftHigh == rightLow && (isLeftOpen || left.isRightOpen))
            return leftHigh == rightLow?Normal:LeakFloatGap;

        if (leftHigh == rightLow)//when reached isLeftOpen and left.isRightOpen are both false
            return EdgeOverlap;

        if (rightHigh < leftLow)
            return OutOfOrder;
        //leftHigh > rightLow && leftLow <= rightHigh
        return Overlap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenCloseRangePredicate that = (OpenCloseRangePredicate) o;
        return isLeftOpen == that.isLeftOpen &&
                isRightOpen == that.isRightOpen &&
                Objects.equals(lowBound, that.lowBound) &&
                Objects.equals(upBound, that.upBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowBound, upBound, isLeftOpen, isRightOpen);
    }

    @Override
    public void increaseLowerBoundNumber() {
        if (isLeftOpen)
            throw new RuntimeException("can not increase lower bound of float range!");
        isLeftOpen = true;
    }

    @Override
    public ValuePredicate merge(ValuePredicate predicate) {
        return predicate.mergeWith(this);
    }

    @Override
    protected ValuePredicate mergeWith(EqualValuePredicate left) {
        OpenCloseRangePredicate result = new OpenCloseRangePredicate();
        result.isLeftOpen=false;
        result.lowBound=left.eqVal;
        result.isRightOpen=this.isRightOpen;
        result.upBound=this.upBound;
        return result;
    }

    @Override
    protected ValuePredicate mergeWith(RangePredicate left) {
        throw new RuntimeException("bug: mix float range with integer range");
    }

    @Override
    protected ValuePredicate mergeWith(AboveValuePredicate left) {
        throw new RuntimeException("bug: range overlap or out of order config");
    }

    @Override
    protected ValuePredicate mergeWith(OpenCloseRangePredicate left) {
        OpenCloseRangePredicate result = new OpenCloseRangePredicate();
        result.isLeftOpen=left.isLeftOpen;
        result.lowBound=left.lowBound;
        result.isRightOpen=this.isRightOpen;
        result.upBound=this.upBound;
        return result;
    }

    @Override
    protected ValuePredicate mergeWith(AboveOrEqualPredicate left) {
        throw new RuntimeException("bug: range overlap or out of order config");
    }

    @Override
    public boolean checkRange() {
        int low = Integer.parseInt(lowBound);
        int high = Integer.parseInt(upBound);
        //theoretically when left or right is open, low is allowed to equal to high, but it is meaningless in practice
        return low<high;
    }
}
