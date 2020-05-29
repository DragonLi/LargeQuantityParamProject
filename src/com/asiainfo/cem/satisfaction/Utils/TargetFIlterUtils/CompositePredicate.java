package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

public class CompositePredicate extends ValuePredicate {
    public ValuePredicate[] lst;
    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        if (lst == null || lst.length == 0)
            return;

        if (lst.length>1)
            buffer.append('(');
        lst[0].generateCondition(dbFieldName, buffer);
        for (int i = 1, lstLength = lst.length; i < lstLength; i++) {
            buffer.append(" and ");
            ValuePredicate pred = lst[i];
            pred.generateCondition(dbFieldName, buffer);
        }
        if (lst.length>1)
            buffer.append(')');
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return false;
    }

    @Override
    public CemQueryNumCheck testMergedWith(ValuePredicate other) {
        return null;
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
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public boolean checkRange() {
        return false;
    }

    @Override
    public ValuePredicate merge(ValuePredicate predicate) {
        return predicate.mergeWith(this);
    }

    @Override
    protected ValuePredicate mergeWith(EqualValuePredicate left) {
        return null;
    }

    @Override
    protected ValuePredicate mergeWith(RangePredicate left) {
        return null;
    }

    @Override
    protected ValuePredicate mergeWith(AboveValuePredicate left) {
        return null;
    }

    @Override
    protected ValuePredicate mergeWith(OpenCloseRangePredicate left) {
        return null;
    }

    @Override
    protected ValuePredicate mergeWith(CompositePredicate left) {
        return null;
    }
}
