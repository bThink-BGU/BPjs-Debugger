package il.ac.bgu.se.bp.common;

import il.ac.bgu.se.bp.utils.Pair;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Utils {

    public static boolean isNull(String str) {
        return str.equals("null");
    }

    public static List<Pair<String, String>> strToStringVarsList(String stringVars) {
        return StringUtils.isEmpty(stringVars) ? new LinkedList<>() :
                Arrays.stream(stringVars.split(",")).map(strVar -> Pair.of(getVar(strVar), getStrVal(strVar))).collect(Collectors.toList());
    }

    public static List<Pair<String, Double>> strToDoubleVarsList(String doubleVars) {
        return StringUtils.isEmpty(doubleVars) ? new LinkedList<>() :
                Arrays.stream(doubleVars.split(",")).map(strVar -> Pair.of(getVar(strVar), getDoubleVal(strVar))).collect(Collectors.toList());
    }

    public static String getVar(String str) {
        return splitAndGetBy(str, "=", 0);
    }

    public static String getStrVal(String str) {
        return splitAndGetBy(str, "=", 1);
    }

    public static Double getDoubleVal(String str) {
        return strToDouble(splitAndGetBy(str, "=", 1));
    }

    public static String splitAndGetBy(String str, String regex, int i) {
        return str.split(regex)[i];
    }

    public static boolean strToBoolean(String str) {
        return Boolean.parseBoolean(str);
    }

    public static String strToString(String str) {
        str = str.charAt(0) == '\"' ? str.substring(1) : str;
        str = str.charAt(str.length() - 1) == '\"' ? str.substring(0, str.length() -1) : str;
        return str;
    }

    public static Double strToDouble(String str) {
        return Double.parseDouble(str);
    }

    public static List<String> strToStringList(String strings) {
        return Arrays.stream(strings.split(",")).collect(Collectors.toList());
    }

    public static List<Double> strToDoubleList(String doubles) {
        return Arrays.stream(doubles.split(",")).map(Double::parseDouble).collect(Collectors.toList());
    }

    public static List<Integer> strToIntList(String breakpoints) {
        return Arrays.stream(breakpoints.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    public static void waitUntilPredicateSatisfied(Callable<Boolean> predicate, int timeToSleep, int maxToTry) {
        try {
            for (int i = 0; i < maxToTry; i++) {
                if (predicate.call()) {
                    return;
                }
                sleep(timeToSleep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sleep(int timeToSleep) {
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
