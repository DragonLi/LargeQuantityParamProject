package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.objectalgebra;

public interface QueryAlg<SQL, PRED> extends PredicateAlg<PRED> {
    SQL and(SQL left, SQL right);
    SQL or(SQL left, SQL right);
    SQL queryWith(String dbField, PRED pred);
}
