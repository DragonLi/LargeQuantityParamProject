package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import java.util.List;

public class CemQueryParamCfg {
    public String name;
    public boolean isRequired;

    public String dbFieldName;
    public ValuePredicate[] valueMap;
    public CemQueryParamCompositionMode mode;
    public QueryFieldType fTy;
    private ValuePredicate[][] mergedPredLst;

    @Override
    public String toString() {
        return "[,"+name+","+isRequired+","+dbFieldName+","+show(valueMap)+","+mode+",]";
    }

    private String show(ValuePredicate[] valueMap) {
        String result = "";
        for (ValuePredicate predicate : valueMap) {
            result += predicate+";";
        }
        return result;
    }

    //for json
    public CemQueryParamCfg(){}

    public CemQueryParamCfg(String name, String dbFieldName, ValuePredicate[] valueMap, CemQueryParamCompositionMode mode, boolean isRequired) {
        this.name = name;
        this.dbFieldName = dbFieldName;
        this.valueMap = valueMap;
        this.mode = mode;
        this.isRequired = isRequired;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public void CheckAndCompose(CemQueryParam param, StringBuffer buffer){
        List<Integer> values = param.values;
        int valCount = values.size();
        if (valCount == 0 && isRequired){
            throw new RuntimeException("parameter value is required:"+name);
        }
        if (valCount == 1 && values.get(0) == -1){
            //-1 means skip this field filtering
            return;
        }
        if (valCount > 0){
            if (buffer.length() > 0)
                buffer.append(" and ");
            buffer.append('(');
        }
        switch (mode){
            case SingleAnd:{
                if (valCount > 1){
                    throw new RuntimeException("multiple parameter for single value mode!");
                }
                processOneParameter(buffer, values.get(0));
            }
                break;
            case OrMultipartAnd:{
                if (mergedPredLst != null){
                    //TODO using long as bit mask
                }
                if (valueMap.length < valCount){
                    valCount = valueMap.length;
                }
                for (int i = 0; i < valCount; i++) {
                    if (i != 0)
                        buffer.append(" or ");
                    processOneParameter(buffer, values.get(i));
                }
            }
                break;
            default:
                throw new RuntimeException("bug! unsupported mode:"+mode);
        }
        if (valCount > 0){
            buffer.append(')');
        }
    }

    private void processOneParameter(StringBuffer buffer, Integer value) {
        if (value == null) {
            throw new RuntimeException("No value for index:" + value);
        }
        int ind = value;
        ValuePredicate fieldPred = valueMap[ind];
        fieldPred.generateCondition(dbFieldName,buffer);
    }

    public String normalizeFieldVal(String val){
        if (fTy != QueryFieldType.TyEnumReplace)
            return val;
        for (ValuePredicate valuePredicate : valueMap) {
            if (valuePredicate.isReplaced(val))
                return valuePredicate.normalizedVal(val);
        }
        return val;
    }

    public void buildMergedPredLst(){
        if (mergedPredLst != null)
            return;
        if (mode != CemQueryParamCompositionMode.OrMultipartAnd)
            return;
        if (fTy != QueryFieldType.TyInt && fTy != QueryFieldType.TyFloat)
            return;
        int valCount = valueMap.length;
        if (valCount <= 1)
            return;
        ValuePredicate[][] merged = new ValuePredicate[valCount][];
        for (int i = 0; i < valCount; i++) {
            ValuePredicate[] current = merged[i] = new ValuePredicate[valCount - i];
            ValuePredicate accumulator = current[0]=valueMap[i];
            for (int j = 1,len=current.length; j < len; j++) {
                current[j]=accumulator=accumulator.merge(valueMap[j+i]);
            }
        }
        mergedPredLst = merged;
    }

    public String showMergedPredLst(){
        if (mergedPredLst == null)
            return "NA";
        String result = "";
        for (ValuePredicate[] lst : mergedPredLst) {
            for (ValuePredicate pred : lst) {
                result += lst+";";
            }
            result += "|";
        }
        return result;
    }
}
