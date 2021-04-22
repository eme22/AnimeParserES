package com.eme22.animeparseres.Util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class Util {

    public static int parseEp(String episodenumtemp) {
        Log.d(TAG, episodenumtemp);
        try {
            return Integer.parseInt(episodenumtemp.replaceAll("[\\D]",""));
        } catch (NumberFormatException e){
            return 1;
        }
    }

    public static String[] parseAlternatives(String alternatives){
        List<String> temp = new ArrayList<>();
        for (String s: alternatives.split("\\s{2,}")) {
            if (s.length() >= 2) temp.add(s);
        }
        return temp.toArray(new String[0]);
    }
}
