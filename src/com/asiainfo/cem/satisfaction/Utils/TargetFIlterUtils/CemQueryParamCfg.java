package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
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
        StringBuilder result = new StringBuilder();
        for (ValuePredicate predicate : valueMap) {
            result.append(predicate).append(";");
        }
        return result.toString();
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
                if (valCount == 1){
                    processOneParameter(buffer,values.get(0));
                }else{
                    int[] tmp = new int[values.size()];
                    valCount=0;
                    for (int i = 0; i < tmp.length; i++) {
                        Integer ind = values.get(i);
                        if (ind == null) {
                            //log.warn("前端传输的参数是空的："+name+" 索引是："+i);
                            continue;
                        }
                        int k = ind;
                        if (k<0 || k >= valueMap.length) {
                            //log.warn("前端传输的参数超出范围："+name+" 索引是："+k);
                            continue;
                        }
                        if (contains(tmp,valCount,k)){
                            //log.warn("前端传输了重复的值："+k+" 字段是："+name);
                            continue;
                        }
                        tmp[valCount] = k;
                        ++valCount;
                    }
                    Arrays.sort(tmp,0,valCount);
                    if (mergedPredLst != null){
                        genViaMergedPredLst(buffer, tmp, valCount);
                    }else{
                        ValuePredicate fieldPred = valueMap[tmp[0]];
                        fieldPred.generateCondition(dbFieldName,buffer);
                        for (int i = 1; i < valCount; i++) {
                            buffer.append(" or ");
                            fieldPred = valueMap[tmp[i]];
                            fieldPred.generateCondition(dbFieldName,buffer);
                        }
                    }
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

    private boolean contains(int[] lst, int size, int v){
        for (int i = size - 1; i >= 0; i--) {
            if (lst[i] == v)
                return true;
        }
        return false;
    }

    private void genViaMergedPredLst(StringBuffer buffer, int[] tmp, int valCount) {
        int startInd = 0;
        int endInd = 1;
        while (endInd < valCount){
            int y = tmp[endInd - 1];
            if (tmp[endInd] != y +1){
                genHelper(buffer, startInd, y, tmp[startInd]);
                startInd = endInd;
            }
            endInd++;
        }
        genHelper(buffer, startInd, tmp[endInd - 1], tmp[startInd]);
    }

    private void genHelper(StringBuffer buffer, int startInd, int y, int x) {
        ValuePredicate pred = mergedPredLst[x][y - x];
        if (startInd != 0)
            buffer.append(" or ");
        pred.generateCondition(dbFieldName, buffer);
    }

    private void processOneParameter(StringBuffer buffer, Integer value) {
        if (value == null) {
            //log.warn("前端传输的参数是空的："+name);
            return;
        }
        int ind = value;
        if (ind <0 || ind >= valueMap.length){
            //log.warn("前端传输的参数超出范围："+name+" 索引是："+k);
            return;
        }
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
        System.out.print(show(this.valueMap)+"|");
        System.out.println(showMergedPredLst());
    }

    public String showMergedPredLst(){
        if (mergedPredLst == null)
            return "NA";
        StringBuilder result = new StringBuilder();
        for (ValuePredicate[] lst : mergedPredLst) {
            for (ValuePredicate pred : lst) {
                result.append(pred).append(";");
            }
            result.append("|");
        }
        return result.toString();
    }
}
