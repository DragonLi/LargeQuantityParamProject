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
}
