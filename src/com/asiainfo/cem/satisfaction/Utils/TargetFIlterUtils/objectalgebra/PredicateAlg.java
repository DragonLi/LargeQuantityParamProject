package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.objectalgebra;

public interface PredicateAlg<T> {
    T aboveInt(int lowerBound);
    T aboveEqInt(int lowerBound);
    T rangeInt(int lowerBoundInclusive, int upperBoundExclusive);
    T eqInt(int val);
    T aboveFloat(float lowerBound);
    T aboveEqFloat(float lowerBound);
    T rangeFloat(float lowerBoundInclusive, float upperBoundExclusive);
    T eqFloat(float val);
    T eqEnum(String val,String replacement);
}
