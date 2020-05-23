package LargeQuantityQuery;

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
    public CemQueryNumValOverlap testMergedWith(ValuePredicate other) {
        return other.testOverlap(this);
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(EqualValuePredicate left) {
        return null;
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(RangePredicate left) {
        return null;
    }

    @Override
    protected CemQueryNumValOverlap testOverlap(AboveValuePredicate left) {
        return null;
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
    public void increaseLowerBoundNumber() {}

    @Override
    public boolean checkRange() {
        return true;
    }
}
