
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;


public class GoogleSender {
    private static String supposedRequest = "";
    public static String getSupposedRequest(String searchMessage, boolean isTopic) throws Exception{
        String wikipediaSearch;
        String wordForSearchUrls;
        if (isTopic){
            wikipediaSearch = "+wikipedia";
            wordForSearchUrls = "wikipedia";
        } else {
            wikipediaSearch = "+wikiquote";
            wordForSearchUrls = "wikiquote";
        }

        searchMessage = searchMessage.replaceAll(" ", "+");
        String request = "https://www.google.com/search?q=" + searchMessage + wikipediaSearch;
        System.out.println("Sending request..." + request);
        Document doc;
        try {

            // need http protocol, set this as a Google bot agent :)
            doc = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();
            // забираем первую ссылку, которая содержит вики
            String newURL = "";
            // если статья, то ищем в гугле статью

                Elements tagsA = doc.body().select("a[href*=" + wordForSearchUrls + "]");
                for (Element tag: tagsA){
                    if (tag.attr("href").matches(".*/url\\?q=https://ru\\." + wordForSearchUrls + "\\.org/wiki/.*")){
                        System.out.println(tag);
                        String URL = tag.attr("href").replaceAll("/url\\?q=","");

                        for (int j = 0; j < URL.toCharArray().length; j++) {
                            if (URL.toCharArray()[j] != '&'){
                                newURL = newURL + URL.toCharArray()[j];
                            } else
                                break;
                        }
                        newURL = URLDecoder.decode(newURL,"utf-8");
                        System.out.println("RESULT URL:"  + newURL);
                        break;
                    }

                }

            System.out.println("Возможно, вы имели в виду:");

            supposedRequest = Jsoup.connect(newURL).get().body().getElementsByTag("h1").text();

            System.out.println(supposedRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return supposedRequest;
    }
    public static ArrayList<String> getSimilarTopics(String searchMessage, boolean isTopic){
        ArrayList<String> similarTopics = new ArrayList<String>();
        String wikipediaSearch;
        String wordForSearchUrls;
        if (isTopic){
            wikipediaSearch = "+wikipedia";
            wordForSearchUrls = "wikipedia";
        } else {
            wikipediaSearch = "+wikiquote";
            wordForSearchUrls = "wikiquote";
        }

        searchMessage = searchMessage.replaceAll(" ", "+");
        String request = "https://www.google.com/search?q=" + searchMessage + wikipediaSearch;
        System.out.println("Sending request..." + request);
        Document doc;
        try {

            // need http protocol, set this as a Google bot agent :)
            doc = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();
            // забираем первую ссылку, которая содержит вики

            // если статья, то ищем в гугле статью

            Elements tagsA = doc.body().select("a[href*=" + wordForSearchUrls + "]");
            for (Element tag: tagsA){
                if (tag.attr("href").matches(".*/url\\?q=https://ru\\." + wordForSearchUrls + "\\.org/wiki/.*")){
                    String newURL = "";
                    String URL = tag.attr("href").replaceAll("/url\\?q=","");
                    for (int j = 0; j < URL.toCharArray().length; j++) {
                        if (URL.toCharArray()[j] != '&'){
                            newURL = newURL + URL.toCharArray()[j];
                        } else
                            break;
                    }
                    newURL = URLDecoder.decode(newURL,"utf-8");

                    String nameH1 = Jsoup.connect(newURL).get().body().getElementsByTag("h1").text();
                    if (!similarTopics.contains(nameH1)){
                        similarTopics.add(nameH1);
                    }


                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return similarTopics;

    }
}
