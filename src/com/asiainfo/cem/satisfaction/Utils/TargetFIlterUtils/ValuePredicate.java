package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Must register all subclasses,see CemQueryParamManager
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "typeTag")
public abstract class ValuePredicate {
    public abstract void generateCondition(String dbFieldName, StringBuffer buffer);

    public abstract boolean containsVal(String requiredCnt);

    public abstract CemQueryNumCheck testMergedWith(ValuePredicate other);
    //double dispatch
    protected abstract CemQueryNumCheck testOverlap(EqualValuePredicate left);
    protected abstract CemQueryNumCheck testOverlap(RangePredicate left);
    protected abstract CemQueryNumCheck testOverlap(AboveValuePredicate left);
    protected abstract CemQueryNumCheck testOverlap(OpenCloseRangePredicate left);

    @Override
    public abstract boolean equals(Object obj);

    public abstract boolean checkRange();

    public void increaseLowerBoundNumber(){}

    public boolean isCollapsed(){
        return false;
    }

    public ValuePredicate collapse(){
        return this;
    }

    public String normalizedVal(String val){return val;}

    public boolean isReplaced(String val) {
        return false;
    }

    public abstract ValuePredicate merge(ValuePredicate predicate);
    //double dispatch
    protected abstract ValuePredicate mergeWith(EqualValuePredicate left);
    protected abstract ValuePredicate mergeWith(RangePredicate left);
    protected abstract ValuePredicate mergeWith(AboveValuePredicate left);
    protected abstract ValuePredicate mergeWith(OpenCloseRangePredicate left);
    protected abstract ValuePredicate mergeWith(CompositePredicate left);
}
