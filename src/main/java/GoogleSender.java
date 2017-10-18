
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;


public class GoogleSender {
    private  Document doc;
    private  String supposedRequest = "";
    private  String topicName;
    private  ArrayList<String> similarTopics = new ArrayList<String>();
    public  ArrayList<String> getSimilarTopics() {
        return similarTopics;
    }
    public  String getTopicName() {
        return topicName;
    }

    public  Document googleIt(String searchMessage, boolean isTopic) throws Exception{
        similarTopics = new ArrayList<String>();
        String wikipediaSearch;
        String wordForSearchUrls;
        String wordForCompare;
        if (isTopic){
            wikipediaSearch = "+wikipedia";
            wordForSearchUrls = "wikipedia";
            wordForCompare = "Википедия";
        } else {
            wikipediaSearch = "+wikiquote";
            wordForSearchUrls = "wikiquote";
            wordForCompare = "Викицитатник";
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
                    System.out.println(tag.text()); // За счастье надо бороться — Викицитатник
                    String newURL = "";
                    String URL = tag.attr("href").replaceAll("/url\\?q=","");
                    for (int j = 0; j < URL.toCharArray().length; j++) {
                        if (URL.toCharArray()[j] != '&'){
                            newURL = newURL + URL.toCharArray()[j];
                        } else
                            break;
                    }
                    /* Идея для оптимизации.
                    * По сути нам надо опрашивать документ только 0 страницы.
                    * Заголовки остальных можно взять не опрашивая их. Из урла непосредственно. */
                    /*
                    newURL = URLDecoder.decode(newURL,"utf-8");
                    Document doc2 = Jsoup.connect(newURL).get();
                    String nameH1 = doc2.body().getElementsByTag("h1").text();

                    if (index == 0){

                        doc = doc2;
                        topicName = nameH1;
                    }

                    if (    nameH1.toLowerCase().contains("категория") ||
                            nameH1.toLowerCase().contains("викицитатник")){
                        continue;
                    }
                    if (!similarTopics.contains(nameH1)){
                        similarTopics.add(nameH1);
                    }
                    index++;
                    */
                    newURL = URLDecoder.decode(newURL,"utf-8");
                    String nameH1 = tag.text();
                    if (index == 0){
                        doc = Jsoup.connect(newURL).get();
                        topicName = doc.body().getElementsByTag("h1").text();
                    }
                    if (nameH1.contains(wordForCompare)){

                        String nameOfPoint = "";
                        for (int j = 0; j < nameH1.toCharArray().length; j++) {
                            if (nameH1.toCharArray()[j] != '—'){
                                nameOfPoint = nameOfPoint + nameH1.toCharArray()[j];
                            } else
                                break;
                        }
                        nameH1 = nameOfPoint;


                        if (    nameH1.toLowerCase().contains("категория") ||
                                nameH1.toLowerCase().contains("викицитатник")){
                            continue;
                        }
                        if (!similarTopics.contains(nameH1)){
                            similarTopics.add(nameH1);
                        }
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
