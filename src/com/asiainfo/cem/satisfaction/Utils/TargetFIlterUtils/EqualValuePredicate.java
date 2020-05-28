package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

public class EqualValuePredicate extends ValuePredicate {
    public String eqVal;

    @Override
    public String toString() {
        return "="+eqVal;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append(dbFieldName).append(" = '").append(eqVal).append("'");
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null && requiredCnt.contains(eqVal);
    }

    @Override
    public CemQueryNumCheck testMergedWith(ValuePredicate other) {
        return other.testOverlap(this);
    }

    @Override
    protected CemQueryNumCheck testOverlap(EqualValuePredicate left) {
        //Normal,EdgeOverlap,OutOfOrder,MixedNumWithStr
        if (this.eqVal.equals(left.eqVal)){
            return CemQueryNumCheck.Overlap;
        }
        int rightOp=Integer.MIN_VALUE,leftOp=Integer.MIN_VALUE;
        try{
            rightOp=Integer.parseInt(eqVal);
        }catch (NumberFormatException ignored){
        }
        try{
            leftOp = Integer.parseInt(left.eqVal);
        }catch (NumberFormatException ignored){
        }
        if (rightOp != Integer.MIN_VALUE && leftOp != Integer.MIN_VALUE){
            if (leftOp > rightOp)
                return CemQueryNumCheck.OutOfOrder;
        }
        if (rightOp == Integer.MIN_VALUE && leftOp != Integer.MIN_VALUE
                || rightOp != Integer.MIN_VALUE && leftOp == Integer.MIN_VALUE){
            return CemQueryNumCheck.MixedNumWithStr;
        }
        return CemQueryNumCheck.Normal;
    }

    @Override
    protected CemQueryNumCheck testOverlap(RangePredicate left) {
        //Normal,OutOfOrder,MixedNumWithStr
        int rightOp;
        try{
            rightOp=Integer.parseInt(eqVal);
        }catch (NumberFormatException ex){
            return CemQueryNumCheck.MixedNumWithStr;
        }

        int high = Integer.parseInt(left.upBound);
        if (high >= rightOp)
            return CemQueryNumCheck.OutOfOrder;
        return CemQueryNumCheck.Normal;
    }

    @Override
    protected CemQueryNumCheck testOverlap(OpenCloseRangePredicate left) {
        //Normal,Overlap,OutOfOrder,MixedNumWithStr
        int rightOp;
        try{
            rightOp=Integer.parseInt(eqVal);
        }catch (NumberFormatException ex){
            return CemQueryNumCheck.MixedNumWithStr;
        }
        int leftLow = Integer.parseInt(left.lowBound);
        int leftHigh = Integer.parseInt(left.upBound);
        if (left.isLeftOpen && rightOp <= leftLow
                || !left.isLeftOpen && rightOp < leftLow){
            return CemQueryNumCheck.OutOfOrder;
        }
        if (left.isRightOpen && rightOp < leftHigh
                || !left.isRightOpen &&rightOp <= leftHigh){
            return CemQueryNumCheck.Overlap;
        }

        return CemQueryNumCheck.Normal;
    }

    @Override
    protected CemQueryNumCheck testOverlap(AboveValuePredicate left) {
        //Overlap,OutOfOrder,MixedNumWithStr
        int rightOp;
        try{
            rightOp=Integer.parseInt(eqVal);
        }catch (NumberFormatException ex){
            return CemQueryNumCheck.MixedNumWithStr;
        }
        int leftOp = Integer.parseInt(left.lowBound);
        if (leftOp < rightOp)
            return CemQueryNumCheck.Overlap;

        return CemQueryNumCheck.OutOfOrder;
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
    public boolean checkRange() {
        return true;
    }
}
