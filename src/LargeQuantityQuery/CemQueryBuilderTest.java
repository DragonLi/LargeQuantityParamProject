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
                                above.lowBound = match.group(1);
                                cfg.valueMap[k]=above;
                            }else if ((match=rangePat.matcher(rawPred)).find()){
                                RangePredicate range = new RangePredicate();
                                range.lowBound=match.group(1);
                                range.upBound=match.group(2);
                                cfg.valueMap[k]=range;
                            }else{
                                EqualValuePredicate eq = new EqualValuePredicate();
                                eq.eqVal=rawPred;
                                cfg.valueMap[k]=eq;
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
        }

        for (CemQueryParamCfg cfg : cfgLst) {
            System.out.println(cfg);
        }

        ObjectMapper json = CemQueryParamManager.getQueryJsonHelper();
        WriteJson(json, cfgLst,"SatParamConfig.json");
        CemQueryParamManager mgr = new CemQueryParamManager();
        mgr.init();
        System.out.println("finished");
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
