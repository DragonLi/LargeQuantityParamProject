package LargeQuantityQuery;

public class RangePredicate extends ValuePredicate {
    public String lowBound;
    public String upBound;

    @Override
    public String toString() {
        return lowBound+"-"+upBound;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append('(').append(dbFieldName).append(" between ").append(lowBound)
                .append(" and ").append(upBound).append(')');
    }

    @Override
    public boolean containsVal(String requiredCnt) {
        return requiredCnt!= null &&
                (requiredCnt.contains(lowBound) || requiredCnt.contains(upBound));
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
        if (obj instanceof RangePredicate){
            RangePredicate other = (RangePredicate) obj;
            return other.lowBound.equals(lowBound) && other.upBound.equals(upBound) ;
        }
        return false;
    }

    @Override
    public void increaseLowerBoundNumber() {
        int low = Integer.valueOf(lowBound);
        int high = Integer.valueOf(upBound);
        if (low < high){
            lowBound = String.valueOf(low+1);
            return;
        }
        throw new RuntimeException("lower bound is equal or greater to upper bound!");
    }

    @Override
    public boolean checkRange() {
        int low = Integer.valueOf(lowBound);
        int high = Integer.valueOf(upBound);
        return low<=high;
    }
}
