package LargeQuantityQuery;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

//TODO import org.springframework.core.io.ClassPathResource;
//TODO import com.google.common.io.CharStreams;

//TODO spring boot @component !
public class CemQueryParamManager {
    private CemQueryParamBuilder builder;

    //@postConstruct
    public void init(){
        ObjectMapper jsonHelper = getQueryJsonHelper();
        //TODO convert to property config file path
        String filename = "SatParamConfig.json";
        try (InputStream inputStream = getResource(filename);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            List<CemQueryParamCfg> x = jsonHelper.readValue(inputStreamReader, new TypeReference<List<CemQueryParamCfg>>() {});
            this.builder = new CemQueryParamBuilder(x);
        } catch (Exception e) {
            //TODO e.printStackTrace();
            //TODO log.info("read json file error: "+filename);
        }
    }

    public StringBuffer compose(List<CemQueryParam> paramLst){
        return builder.CheckAndCompose(paramLst);
    }

    public static ObjectMapper getQueryJsonHelper(){
        ObjectMapper json = new ObjectMapper();
        json.configure(SerializationFeature.INDENT_OUTPUT, true);
        json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        json.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        json.registerSubtypes(
                new NamedType(EqualValuePredicate.class, "eq")
                , new NamedType(RangePredicate.class, "range")
                , new NamedType(OpenCloseRangePredicate.class, "floatRange")
                , new NamedType(AboveValuePredicate.class, "above")
        );
        return json;
    }

    //get resource
    public static InputStream getResource(String resName) throws FileNotFoundException {
        String pathToUse = cleanPath(resName);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        ClassLoader loader = getDefaultClassLoader();
        InputStream is;
        if (loader != null) {
            is = loader.getResourceAsStream(pathToUse);
        } else {
            is = ClassLoader.getSystemResourceAsStream(pathToUse);
        }

        if (is == null){
            //TODO try read directly from file system, it may be a security hole!
            is = new FileInputStream(resName);
        }
        return is;
    }

    public static String getDescription(String pathToUse) {
        StringBuilder builder = new StringBuilder("class path resource [");

        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }

        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }

    //ClassUtils.getDefaultClassLoader
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;

        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable var3) {
        }

        if (cl == null) {
            cl = CemQueryParamManager.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable var2) {
                }
            }
        }

        return cl;
    }

    //StringUtils.cleanPath
    public static String cleanPath(String path) {
        if (!hasLength(path)) {
            return path;
        } else {
            String pathToUse = replace(path, "\\", "/");
            int prefixIndex = pathToUse.indexOf(58);
            String prefix = "";
            if (prefixIndex != -1) {
                prefix = pathToUse.substring(0, prefixIndex + 1);
                if (prefix.contains("/")) {
                    prefix = "";
                } else {
                    pathToUse = pathToUse.substring(prefixIndex + 1);
                }
            }

            if (pathToUse.startsWith("/")) {
                prefix = prefix + "/";
                pathToUse = pathToUse.substring(1);
            }

            String[] pathArray = delimitedListToStringArray(pathToUse, "/");
            LinkedList<String> pathElements = new LinkedList();
            int tops = 0;

            int i;
            for(i = pathArray.length - 1; i >= 0; --i) {
                String element = pathArray[i];
                if (!".".equals(element)) {
                    if ("..".equals(element)) {
                        ++tops;
                    } else if (tops > 0) {
                        --tops;
                    } else {
                        pathElements.add(0, element);
                    }
                }
            }

            for(i = 0; i < tops; ++i) {
                pathElements.add(0, "..");
            }

            if (pathElements.size() == 1 && "".equals(pathElements.getLast()) && !prefix.endsWith("/")) {
                pathElements.add(0, ".");
            }

            return prefix + collectionToDelimitedString(pathElements, "/");
        }
    }

    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static String replace(String inString, String oldPattern, String newPattern) {
        if (hasLength(inString) && hasLength(oldPattern) && newPattern != null) {
            int index = inString.indexOf(oldPattern);
            if (index == -1) {
                return inString;
            } else {
                int capacity = inString.length();
                if (newPattern.length() > oldPattern.length()) {
                    capacity += 16;
                }

                StringBuilder sb = new StringBuilder(capacity);
                int pos = 0;

                for(int patLen = oldPattern.length(); index >= 0; index = inString.indexOf(oldPattern, pos)) {
                    sb.append(inString.substring(pos, index));
                    sb.append(newPattern);
                    pos = index + patLen;
                }

                sb.append(inString.substring(pos));
                return sb.toString();
            }
        } else {
            return inString;
        }
    }

    public static String deleteAny(String inString, String charsToDelete) {
        if (hasLength(inString) && hasLength(charsToDelete)) {
            StringBuilder sb = new StringBuilder(inString.length());

            for(int i = 0; i < inString.length(); ++i) {
                char c = inString.charAt(i);
                if (charsToDelete.indexOf(c) == -1) {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return inString;
        }
    }

    public static String[] delimitedListToStringArray( String str,  String delimiter) {
        return delimitedListToStringArray(str, delimiter, (String)null);
    }

    public static String[] delimitedListToStringArray( String str,  String delimiter,  String charsToDelete) {
        if (str == null) {
            return new String[0];
        } else if (delimiter == null) {
            return new String[]{str};
        } else {
            List<String> result = new ArrayList();
            int pos;
            if ("".equals(delimiter)) {
                for(pos = 0; pos < str.length(); ++pos) {
                    result.add(deleteAny(str.substring(pos, pos + 1), charsToDelete));
                }
            } else {
                int delPos;
                for(pos = 0; (delPos = str.indexOf(delimiter, pos)) != -1; pos = delPos + delimiter.length()) {
                    result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                }

                if (str.length() > 0 && pos <= str.length()) {
                    result.add(deleteAny(str.substring(pos), charsToDelete));
                }
            }

            return toStringArray((Collection)result);
        }
    }

    public static String[] toStringArray(Collection<String> collection) {
        return (String[])collection.toArray(new String[0]);
    }

    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {
        if (isEmpty(coll)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator it = coll.iterator();

            while(it.hasNext()) {
                sb.append(prefix).append(it.next()).append(suffix);
                if (it.hasNext()) {
                    sb.append(delim);
                }
            }

            return sb.toString();
        }
    }

}
