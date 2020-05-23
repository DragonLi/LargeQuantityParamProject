package LargeQuantityQuery;

public class EqualValuePredicate extends ValuePredicate {
    public String eqVal;

    @Override
    public String toString() {
        return "="+eqVal;
    }

    @Override
    public void generateCondition(String dbFieldName, StringBuffer buffer) {
        buffer.append('(').append(dbFieldName).append(" = '").append(eqVal).append("')");
    }
}
