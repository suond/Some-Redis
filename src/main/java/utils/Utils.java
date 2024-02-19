package utils;

import constants.Constants;

public class Utils {

    public static String toRESP(String[] s){
        StringBuilder builder = new StringBuilder();
        String len = Integer.toString(s.length);
        builder.append("*"+len+ Constants.R_N);
        for (String t: s){
            builder.append("$"+ Integer.toString(t.length())+ Constants.R_N + t + Constants.R_N);
        }
        return builder.toString();
    }
}
