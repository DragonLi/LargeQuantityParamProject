package LargeQuantityQuery;

public class AboveValuePredicate extends ValuePredicate {
    public String lowBound;

    @Override
    public String toString() {
        return ">"+lowBound;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append('(').append(dbFieldName).append(" > ").append(lowBound).append(')');
    }
}
