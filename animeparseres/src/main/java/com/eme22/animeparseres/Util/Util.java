package com.eme22.animeparseres.Util;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class Util {

    public static class AsyncRequest implements Callable<ANResponse> {
        private final String link;
        private final Pair<String,String> header;
        public AsyncRequest(String link, Pair<String,String> header) {
            this.link = link;
            this.header = header;
        }
        public ANResponse call() {
            if (header != null)
                return AndroidNetworking.get(link).addHeaders(header.first,header.second).build().executeForString();
            else return AndroidNetworking.get(link).build().executeForString();
        }
    }

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

    public static ANResponse asyncCall(String url, Pair<String,String> header) throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Callable<ANResponse> callable = new AsyncRequest(url, header);
        Future<ANResponse> future = pool.submit(callable);
        return future.get();
    }
}
