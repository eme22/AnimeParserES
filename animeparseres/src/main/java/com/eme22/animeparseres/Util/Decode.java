package com.eme22.animeparseres.Util;

import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Decode {

    public static Pair<String,String> decodeLink(String link) throws IOException {
        if (link.contains("mega.nz") || link.contains("mega.co.nz")) return new Pair<>("mega",link);
        else if (link.contains("zippyshare.com")) return new Pair<>("zippyshare",link);
        else if (link.contains("streamtape.com")) return new Pair<>("stape",link);
        else if (link.contains("netu.php")){
            link = Jsoup.connect(link).timeout(3000).get().select("iframe").attr("src");
            return new Pair<>("netu",link);
        }
        else if (link.contains("animeid.tv/?vid=")){
            link = Jsoup.connect(link).timeout(3000).get().select("IFRAME").attr("src");
            return new Pair<>("clipwatching",link);
        }
        else if (link.contains("um.php")){
            link = Jsoup.connect(link).timeout(3000).get().outerHtml();
            Pattern pattern = Pattern.compile("swarmId: '(.*?)'");
            Matcher matcher = pattern.matcher(link);
            if (matcher.find()){
                link = matcher.group(1);
                return new Pair<>("um",link);

            }
        }
        else if (link.contains("um2.php")){
            link = Jsoup.connect(link).header("Referer", "https://jkanime.net/").execute().url().toString();
            return new Pair<>("um2", link);

        }
        else if (link.contains("jkfembed.php")){
            Document document =  Jsoup.connect(link).timeout(3000).get();
            link = document.select("iframe").first().attr("src");
            return new Pair<>("fembed",link);
        }
        else if (link.contains("jkokru.php")){
            Document document =  Jsoup.connect(link).timeout(3000).get();
            link = document.select("iframe").first().attr("src");
            return new Pair<>("okru",link);

        }
        else if (link.contains("jkvmixdrop.php")){
            Document document =  Jsoup.connect(link).timeout(3000).get();
            link = document.select("iframe").first().attr("src");
            return new Pair<>("mixdrop",link);

        }
        else if (link.contains("jk.php")){
            Document document =  Jsoup.connect(link).timeout(3000).get();
            link = document.select("video[id=jkvideo]").select("source").attr("src");
            return new Pair<>("jkmedia",link);
        }

        return null;

    }
}
