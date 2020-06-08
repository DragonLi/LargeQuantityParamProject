package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import java.util.Objects;

public class AboveOrEqualPredicate extends ValuePredicate {
    public String lowBound;

    @Override
    public String toString() {
        return ">="+lowBound;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append(dbFieldName).append(" >= ").append(lowBound);
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null && requiredCnt.contains(lowBound);
    }

    @Override
    public CemQueryNumCheck testMergedWith(ValuePredicate other) {
        throw new RuntimeException("bug:");
    }

    @Override
    protected CemQueryNumCheck testOverlap(EqualValuePredicate left) {
        return null;
    }

    @Override
    protected CemQueryNumCheck testOverlap(RangePredicate left) {
        return null;
    }

    @Override
    protected CemQueryNumCheck testOverlap(AboveValuePredicate left) {
        return null;
    }

    @Override
    protected CemQueryNumCheck testOverlap(OpenCloseRangePredicate left) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AboveOrEqualPredicate that = (AboveOrEqualPredicate) o;
        return Objects.equals(lowBound, that.lowBound);    }

    @Override
    public boolean checkRange() {
        return true;
    }

    @Override
    public ValuePredicate merge(ValuePredicate predicate) {
        return null;
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
}
