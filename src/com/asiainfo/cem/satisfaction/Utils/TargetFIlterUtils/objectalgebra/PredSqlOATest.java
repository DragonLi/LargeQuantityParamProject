package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.objectalgebra;

import com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.AboveValuePredicate;
import com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.EqualValuePredicate;
import org.junit.Test;

public class PredSqlOATest {
    @Test
    public void Test(){
        AboveValuePredicate p = new AboveValuePredicate();
        p.lowBound = "0";
        EqualValuePredicate p2= new EqualValuePredicate();
        p2.eqVal="1";
        p2.replace="女";
        PredSqlBuilder alg = new PredSqlBuilder();
        SqlGenerator aboveZero= PredSqlBuilder.convert(alg,p);
        SqlGenerator eqFemale = PredSqlBuilder.convert(alg,p2);
        QueryBuilder queryGen = new QueryBuilder();
        SqlStatement stmt= queryGen.and(
                queryGen.queryWith("age",aboveZero),
                queryGen.queryWith("sex",eqFemale));
        System.out.println(stmt.generate());

        stmt= queryGen.or(
                queryGen.queryWith("age",aboveZero),
                stmt);
        System.out.println(stmt.generate());

    }
}
