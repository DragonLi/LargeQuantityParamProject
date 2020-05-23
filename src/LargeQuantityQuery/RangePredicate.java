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
}
