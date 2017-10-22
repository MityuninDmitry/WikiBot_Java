
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;


public class GoogleSender {
    private  Document doc;
    private  String topicName;
    private  ArrayList<String> similarTopics = new ArrayList<String>();
    public  ArrayList<String> getSimilarTopics() {
        return similarTopics;
    }
    public  String getTopicName() {
        return topicName;
    }

    public  Document googleIt(String searchMessage, boolean isTopic) throws Exception{
        // похожие статьи
        similarTopics = new ArrayList<String>();
        String wikipediaSearch;
        String wordForSearchUrls;
        String wordForCompare;
        // если режим статей, то википедия, иначе цитатник
        if (isTopic){
            wikipediaSearch = "+wikipedia";
            wordForSearchUrls = "wikipedia";
            wordForCompare = "Википедия";
        } else {
            wikipediaSearch = "+wikiquote";
            wordForSearchUrls = "wikiquote";
            wordForCompare = "Викицитатник";
        }
        // в ключевых словах вставляем +
        searchMessage = searchMessage.replaceAll(" ", "+");
        // запрос
        String request = "https://www.google.com/search?q=" + searchMessage + wikipediaSearch;
        System.out.println("Sending request..." + request);
        try {

            // need http protocol, set this as a Google bot agent :)
            // посылаем запрос в гугл и получаем страницу
            Document docFirstSearch;
            docFirstSearch = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();
            // забираем список тегов, которые ведут на википедию
            Elements tagsA = docFirstSearch.body().select("a[href*=" + wordForSearchUrls + "]");
            int index = 0;
            // идем по списку тегов
            for (Element tag: tagsA){
                // если тег удовлетворяет условию, что он ведет на википедию
                if (tag.attr("href").matches(".*/url\\?q=https://ru\\." + wordForSearchUrls + "\\.org/wiki/.*")){
                    // забираем урл и убираем оттуда лишнее
                    String newURL = "";
                    String URL = tag.attr("href").replaceAll("/url\\?q=","");
                    for (int j = 0; j < URL.toCharArray().length; j++) {
                        if (URL.toCharArray()[j] != '&'){
                            newURL = newURL + URL.toCharArray()[j];
                        } else
                            break;
                    }
                    // кодируем урл в нужную кодировку
                    newURL = URLDecoder.decode(newURL,"utf-8");
                    // забираем текст урла
                    String nameH1 = tag.text();
                    // если это первая ссылка валидная, то
                    if (index == 0){
                        // забираем ее документ
                        doc = Jsoup.connect(newURL).get();
                        // забираем оттуда заголовок статьи
                        topicName = doc.body().getElementsByTag("h1").text();
                    }
                    // если заголовок содержит слово Википедия или Викицитатник
                    if (nameH1.contains(wordForCompare)){
                        // удаляем лишнее
                        String nameOfPoint = "";
                        for (int j = 0; j < nameH1.toCharArray().length; j++) {
                            if (nameH1.toCharArray()[j] != '—'){
                                nameOfPoint = nameOfPoint + nameH1.toCharArray()[j];
                            } else
                                break;
                        }
                        nameH1 = nameOfPoint;

                        // если имя содержит названия всякие лишние, то не включаем их в список
                        if (    nameH1.toLowerCase().contains("категория") ||
                                nameH1.toLowerCase().contains("викицитатник")){
                            continue;
                        }
                        // если такого заголовка еще нет в списке, то добавляем его
                        if (!similarTopics.contains(nameH1)){
                            similarTopics.add(nameH1);
                        }
                    }
                    index++;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;

    }
}
