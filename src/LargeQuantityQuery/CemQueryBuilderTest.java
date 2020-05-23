package LargeQuantityQuery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CemQueryBuilderTest {
    @Test
    public void testExcel() throws IOException {
        XSSFWorkbook excel = new XSSFWorkbook("重庆多条件组合查询-代码生成配置.xlsx");
        XSSFSheet sheet = excel.getSheetAt(0);
        String[] nameLst = new String[5];
        {
            nameLst[0]="前端名字";
            nameLst[1]="是否必选";
            nameLst[2]="数据库字段名字";
            nameLst[3]="字段分段";
            nameLst[4]="字段间的值";
        }
        int[] indexLst = new int[5];
        XSSFRow headerRow = sheet.getRow(0);
        for (Cell cell : headerRow) {
            String cnt = cell.getStringCellValue();
            for (int i = 0; i < nameLst.length; i++) {
                String targetName = nameLst[i];
                if (targetName.equals(cnt)) {
                    indexLst[i]=cell.getColumnIndex();
                }
            }
        }

        Pattern rangePat = Pattern.compile("(\\d+)\\-(\\d+)");
        Pattern abovePat = Pattern.compile("(\\d+)\\+");
        int rowCount = sheet.getLastRowNum();
        CemQueryParamCfg[] cfgLst = new CemQueryParamCfg[rowCount-1];

        for (int i = 1; i < rowCount; i++) {
            XSSFRow row = sheet.getRow(i);
            CemQueryParamCfg cfg = new CemQueryParamCfg();
            cfgLst[i-1] = cfg;
            String requiredCnt = null;
            for (int j = 0; j < indexLst.length; j++) {
                int targetIndex = indexLst[j];
                XSSFCell cell = row.getCell(targetIndex);
                if (cell == null){
                    throw new RuntimeException("empty cell: "+nameLst[j]);
                }
                switch (j){
                    case 0:{
                        cfg.name=cell.getStringCellValue();
                        break;
                    }
                    case 1:{
                        String cnt = cell.getStringCellValue();
                        cfg.isRequired=!cnt.equals("否");
                        if (cfg.isRequired)
                            requiredCnt = cnt;
                        break;
                    }
                    case 2:{
                        cfg.dbFieldName=cell.getStringCellValue();
                        break;
                    }
                    case 3:{
                        String cnt = cell.getStringCellValue();
                        String cleaned = cnt.replaceAll("以上","+").replaceAll("、",",").replaceAll("，",",");
                        String[] groups = cleaned.split(",");
                        cfg.valueMap=new ValuePredicate[groups.length];
                        for (int k = 0; k < groups.length; k++) {
                            String rawPred = groups[k].trim();
                            Matcher match;
                            if ((match=abovePat.matcher(rawPred)).find()){
                                AboveValuePredicate above = new AboveValuePredicate();
                                cfg.valueMap[k]=above;
                                above.lowBound = match.group(1);
                            }else if ((match=rangePat.matcher(rawPred)).find()){
                                RangePredicate range = new RangePredicate();
                                cfg.valueMap[k]=range;
                                range.lowBound=match.group(1);
                                range.upBound=match.group(2);
                            }else{
                                EqualValuePredicate eq = new EqualValuePredicate();
                                cfg.valueMap[k]=eq;
                                eq.eqVal=rawPred;
                                if (rawPred.indexOf('-') != -1){
                                    throw new RuntimeException("non range value should not contains \"-\", or range value must be numbers");
                                }
                            }
                        }
                        break;
                    }
                    case 4:{
                        String cnt = cell.getStringCellValue();
                        switch (cnt){
                            case "单选":{
                                cfg.mode=CemQueryParamCompositionMode.SingleAnd;
                                break;
                            }
                            case "多选":{
                                cfg.mode=CemQueryParamCompositionMode.OrMultipartAnd;
                                break;
                            }
                            default:{
                                throw new RuntimeException("unsupported mode: "+cnt);
                            }
                        }
                        break;
                    }
                }
            }
            CheckRequired(cfg,requiredCnt);
        }

        for (CemQueryParamCfg cfg : cfgLst) {
            System.out.println(cfg);
        }

        ObjectMapper json = CemQueryParamManager.getQueryJsonHelper();
        WriteJson(json, cfgLst,"SatParamConfig.json");
        System.out.println("SatParamConfig.json written");
        CemQueryParamManager mgr = new CemQueryParamManager();
        mgr.init();
        System.out.println("finished");
        /**
         * TODO
         * 1 多选参数重复
         * 2 多选参数顺序和覆盖检查
         * 3 多选参数组合时分段的合并
         * 4 测试:多选参数重复,顺序,边缘重叠和覆盖等(CemQueryNumValOverlap); 默认参数设置;多选参数组合时分段的合并
         */
    }

    private static void CheckRequired(CemQueryParamCfg cfg, String requiredCnt) {
        if (cfg.valueMap.length == 0){
            throw new RuntimeException("no value list found!"+cfg);
        }

        ValuePredicate[] valueMap = cfg.valueMap;
        HashSet<ValuePredicate> testSet = new HashSet<>(valueMap.length);
        for (ValuePredicate predicate : valueMap) {
            if (testSet.contains(predicate)){
                throw new RuntimeException("duplicated choice: "+cfg);
            }
            if (!predicate.checkRange())
                throw new RuntimeException("invalid range bound:"+cfg);

            testSet.add(predicate);
        }

        ValuePredicate merged = valueMap[0];
        for (int i = 1; i < valueMap.length; i++) {
            ValuePredicate other = valueMap[i];
            CemQueryNumValOverlap overlap = merged.testMergedWith(other);
            switch (overlap){
                case Normal:
                    break;
                case EdgeOverlap:
                    if (other instanceof RangePredicate){
                        System.out.println("range bound is overlap, will be adjusted:"+cfg);
                        other.increaseLowerBoundNumber();
                        break;
                    }else
                        throw new RuntimeException("value range bound is overlap but failed to adjust:" + cfg);
                case Overlap:
                    throw new RuntimeException("range overlap:"+cfg);
                case OutOfOrder:
                    throw new RuntimeException("values are not in order:"+cfg);
                case MixedNumWithStr:
                    throw new RuntimeException("mixing number choice with string choice is not supported:"+cfg);
            }
            merged = other;
        }

        if (cfg.isRequired){
            boolean hasReq = Arrays.stream(cfg.valueMap).anyMatch(predicate ->
                    predicate.containsVal(requiredCnt));
            if (!hasReq){
                throw new RuntimeException("default value is not described!\n"+cfg);
            }
            if (!cfg.valueMap[0].containsVal(requiredCnt)){
                throw new RuntimeException("default value must be the first choice!\n"+cfg);
            }
        }
    }

    @Test
    public void testSatParamCfg() {
        List<CemQueryParamCfg> pLst=new ArrayList<>();
        AboveValuePredicate above;
        EqualValuePredicate single;
        RangePredicate range;
        {
            ValuePredicate[] valMap=new ValuePredicate[2];
            single = new EqualValuePredicate();
            single.eqVal="男";
            valMap[0]= single;
            single = new EqualValuePredicate();
            single.eqVal="女";
            valMap[1]= single;
            CemQueryParamCfg cfg = new CemQueryParamCfg("性别","gender"
                    ,valMap, CemQueryParamCompositionMode.SingleAnd,false);
            pLst.add(cfg);
        }
        {
            ValuePredicate[] valMap=new ValuePredicate[4];
            range = new RangePredicate();
            range.lowBound="0";
            range.upBound="19";
            valMap[0]= range;
            range = new RangePredicate();
            range.lowBound="20";
            range.upBound="39";
            valMap[1]= range;
            range = new RangePredicate();
            range.lowBound="40";
            range.upBound="60";
            valMap[2]= range;
            above = new AboveValuePredicate();
            above.lowBound="60";
            valMap[3]= above;
            CemQueryParamCfg cfg = new CemQueryParamCfg("年龄","age"
                    ,valMap, CemQueryParamCompositionMode.OrMultipartAnd,false);
            pLst.add(cfg);
        }
        ObjectMapper json = CemQueryParamManager.getQueryJsonHelper();
        WriteJson(json, pLst,"SatParamConfig.json");
        List<CemQueryParamCfg> result = ReadJson(json, "SatParamConfig.json", new TypeReference<List<CemQueryParamCfg>>() {});
        CemQueryParamBuilder builder = new CemQueryParamBuilder(result);
        CemQueryParamManager mgr = new CemQueryParamManager();
        mgr.init();
    }

    public static <T> T ReadJson(ObjectMapper json, String fn, TypeReference<T> objType) {
        try {
            return json.readValue(new File(fn), objType);
        } catch (IOException e) {
            //TODO e.printStackTrace();
        }
        return null;
    }

    public static <T> T ReadJson(ObjectMapper json, String fn, Class<T> objType) {
        try {
            return json.readValue(new File(fn), objType);
        } catch (IOException e) {
            //TODO e.printStackTrace();
        }
        return null;
    }

    public static void WriteJson(ObjectMapper json, Object obj, String fn) {
        try {
            json.writeValue(new File(fn), obj);
        } catch (IOException e) {
            //TODO e.printStackTrace();
        }
    }
}
