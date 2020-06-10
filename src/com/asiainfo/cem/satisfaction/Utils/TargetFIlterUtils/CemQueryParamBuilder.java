package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import java.util.*;

/**
 * must be thread safe!
 */
public class CemQueryParamBuilder {
    private final Map<String, Integer> satParamMap;
    private final Map<String,Integer> dbParamMap;
    private final CemQueryParamCfg[] satParamList;
    private final int[] requiredFieldIndLst;

    public static List<CemQueryParamCfg> compact(List<CemQueryParamCfg> pLst){
        HashMap<ValuePredicate,ValuePredicate> predSet = new HashMap<>(pLst.size());
        for (CemQueryParamCfg cfg : pLst) {
            ValuePredicate[] predLst = cfg.valueMap;
            for (int k = 0, predLen = predLst.length; k < predLen; k++) {
                ValuePredicate pred = predSet.get(predLst[k]);
                if (pred != null)
                    predLst[k] = pred;
                else
                    predSet.put(predLst[k], predLst[k]);
            }
            cfg.buildMergedPredLst();
        }
        return pLst;
    }

    public CemQueryParamBuilder(List<CemQueryParamCfg> pLst){
        pLst = compact(pLst);
        CemQueryParamCfg[] array = new CemQueryParamCfg[pLst.size()];
        pLst.toArray(array);
        this.satParamList = array;
        Map<String,Integer> map = new HashMap<>(pLst.size());
        Map<String, Integer> dbMap = new HashMap<>(pLst.size());
        List<Integer> requiredFieldIndexLst = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            CemQueryParamCfg paramCfg = array[i];
            map.put(paramCfg.name.toLowerCase(),i);
            dbMap.put(paramCfg.dbFieldName,i);
            if (paramCfg.isRequired)
                requiredFieldIndexLst.add(i);
        }
        this.satParamMap = map;
        dbParamMap = dbMap;

        int[] requiredFieldIndLst = new int[requiredFieldIndexLst.size()];
        for (int i = 0; i < requiredFieldIndexLst.size(); i++) {
            requiredFieldIndLst[i]=requiredFieldIndexLst.get(i);
        }
        this.requiredFieldIndLst=requiredFieldIndLst;
    }

    public StringBuffer CheckAndCompose(List<CemQueryParam> paramLst){
        StringBuffer buffer = new StringBuffer();
        BitSet processedParamLst = new BitSet(satParamList.length);
        for (int i = 0; i < paramLst.size(); i++) {
            CemQueryParam param = paramLst.get(i);
            String paramName = param.name.toLowerCase();
            Integer index = satParamMap.get(paramName);
            if (index == null) {
                throw new RuntimeException("无效参数名字" + paramName);
            }
            int ind = index;
            if (processedParamLst.get(ind)) {
                throw new RuntimeException("第"+(i+1)+"个参数重复了:" + paramName);
            }
            processedParamLst.set(ind);
            CemQueryParamCfg cfg = satParamList[ind];
            cfg.CheckAndCompose(param, buffer);
        }
        for (int ind : requiredFieldIndLst) {
            if (!processedParamLst.get(ind)) {
                throw new RuntimeException("缺少必填参数:"+satParamList[ind].name);
            }
        }
        return buffer;
    }

    public CemQueryParamCfg[] getNormalizerLst(List<String> outLst){
        CemQueryParamCfg[] result = new CemQueryParamCfg[outLst.size()];
        for (int i = 0; i < result.length; i++) {
            Integer index = dbParamMap.get(outLst.get(i));
            if (index == null)
                continue;
            int ind = index;
            result[i] = satParamList[ind];
        }
        return result;
    }
}
