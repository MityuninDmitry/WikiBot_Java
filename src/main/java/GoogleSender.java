
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;


public class GoogleSender {
    private static Document doc;
    private static String supposedRequest = "";
    private static String topicName;
    private static ArrayList<String> similarTopics = new ArrayList<String>();
    public static ArrayList<String> getSimilarTopics() {
        return similarTopics;
    }
    public static String getTopicName() {
        return topicName;
    }

    public static Document googleIt(String searchMessage, boolean isTopic) throws Exception{
        similarTopics = new ArrayList<String>();
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
        try {

            // need http protocol, set this as a Google bot agent :)
            Document docFirstSearch;
            docFirstSearch = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();
            // забираем первую ссылку, которая содержит вики

            // если статья, то ищем в гугле статью

            Elements tagsA = docFirstSearch.body().select("a[href*=" + wordForSearchUrls + "]");
            int index = 0;
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
                    Document doc2 = Jsoup.connect(newURL).get();
                    String nameH1 = doc2.body().getElementsByTag("h1").text();
                    if (nameH1.toLowerCase().contains("категория")){
                        continue;
                    }
                    if (!similarTopics.contains(nameH1)){
                        similarTopics.add(nameH1);
                    }

                    if (index == 0){
                        doc = doc2;
                        topicName = nameH1;
                    }

                    index++;
                }

            }
            try{
                supposedRequest = similarTopics.get(0);
            } catch (IndexOutOfBoundsException e){
                supposedRequest = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;

    }
}
