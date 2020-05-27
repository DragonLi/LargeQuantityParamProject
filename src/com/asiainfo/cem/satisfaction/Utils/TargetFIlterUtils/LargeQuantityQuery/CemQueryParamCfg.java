package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.LargeQuantityQuery;

import java.util.List;

public class CemQueryParamCfg {
    public String name;
    public boolean isRequired;

    public String dbFieldName;
    public ValuePredicate[] valueMap;
    public CemQueryParamCompositionMode mode;
    public QueryFieldType fTy;

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
        if (fTy == QueryFieldType.TyBool){
            switch (val){
                case "1":
                    return "是";
                case "0":
                    return "否";
            }
        }
        if (dbFieldName.equals("sex")){
            switch (val){//男=1、女=2
                case "1":
                    return "男";
                case "2":
                    return "女";
            }
        }
        return val;
    }
}
