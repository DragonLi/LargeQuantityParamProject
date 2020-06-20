package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.objectalgebra;

import com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.AboveValuePredicate;
import com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.EqualValuePredicate;

public class PredSqlBuilder implements PredicateAlg<SqlGenerator> {

    public static <T> T convert(PredicateAlg<T> alg,AboveValuePredicate p){
        return alg.aboveInt(Integer.parseInt(p.lowBound));
    }

    public static <T> T convert(PredicateAlg<T> alg, EqualValuePredicate p){
        return alg.eqEnum(p.eqVal,p.replace);
    }

    @Override
    public SqlGenerator aboveInt(int lowerBound) {
        return new SqlGenerator() {
            @Override
            public String gen(String dbField) {
                return dbField+" > "+lowerBound;
            }
        };
    }

    @Override
    public SqlGenerator aboveEqInt(int lowerBound) {
        return null;
    }

    @Override
    public SqlGenerator rangeInt(int lowerBoundInclusive, int upperBoundExclusive) {
        return null;
    }

    @Override
    public SqlGenerator eqInt(int val) {
        return null;
    }

    @Override
    public SqlGenerator aboveFloat(float lowerBound) {
        return null;
    }

    @Override
    public SqlGenerator aboveEqFloat(float lowerBound) {
        return null;
    }

    @Override
    public SqlGenerator rangeFloat(float lowerBoundInclusive, float upperBoundExclusive) {
        return null;
    }

    @Override
    public SqlGenerator eqFloat(float val) {
        return null;
    }

    @Override
    public SqlGenerator eqEnum(String val, String replacement) {
        return new SqlGenerator() {
            @Override
            public String gen(String dbField) {
                return dbField+ " = " + val;
            }
        };
    }
}
