package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

import java.util.List;

public class CemQueryParam {
    public String name;
    public List<Integer> values;

    @Override
    public String toString() {
        return name+":"+show(values);
    }

    private String show(List<Integer> values) {
        String result="";
        for (Integer value : values) {
            result += value+";";
        }
        return result;
    }
}
