package LargeQuantityQuery;

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

    public abstract CemQueryNumValOverlap testMergedWith(ValuePredicate other);
    //double dispatch
    protected abstract CemQueryNumValOverlap testOverlap(EqualValuePredicate left);
    protected abstract CemQueryNumValOverlap testOverlap(RangePredicate left);
    protected abstract CemQueryNumValOverlap testOverlap(AboveValuePredicate left);

    @Override
    public abstract boolean equals(Object obj);

    public abstract void increaseLowerBoundNumber();

    public abstract boolean checkRange();
}
