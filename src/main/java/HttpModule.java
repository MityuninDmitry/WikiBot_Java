import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.util.*;

public class HttpModule {
    private Map<String, String> tocList = new LinkedHashMap<String, String>();
    private String TOPIC_NAME = "";

    public String getTOPIC_NAME() {
        return TOPIC_NAME;
    }
    public Map getTocList(){
        return tocList;
    }
    public ArrayList<String> searchTopicInWikiWithToc(String searchMessage){
        ArrayList<String> text = new ArrayList<String>(); // результирующий список
        tocList.put("Просмотр статьи с начала", "0");
        Document doc; // объявляем документ
        try {
            // ищем статью в вики
            if (searchMessage.equals("/random")){
                doc = Jsoup.connect("https://ru.wikipedia.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%A1%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0").get();
            }
            else {
                doc = Jsoup.connect("https://ru.wikipedia.org/wiki/" + searchMessage.trim()).get();
            }


        }
        catch (HttpStatusException e) { // если 500, 404 ошибки, то
            e.printStackTrace(); // печатаем трейс
            // в результирующий лист добавляем текст
            text.add("У меня не получилось найти что-либо по этому запросу.");
            // выходим из метода
            return text;
        }
        catch (IOException e) { // если любая другая ошибка
            e.printStackTrace();
            text.add("Прошу прощения, но с интернет соединением что-то не так. Пожалуйста, попробуйте позже.");
            return text;
        }
        Element tags = doc.body().getElementById("content"); // считываем контент
        // удаляем лишнее с полученной страницы

        tags.getElementsByAttributeValue("id","toc").remove();
        tags.getElementsByAttributeValue("class","infobox").remove();
        tags.getElementsByTag("a").unwrap();
        //tags.getElementsByTag("span").remove();
        tags.getElementsByTag("table").remove();
        tags.getElementsByTag("sup").remove();
        tags.getElementsByTag("b").unwrap();
        tags.getElementsByTag("i").unwrap();
        // идем по всем оставшимся тегам

        int index = 0;
        for (Element tag: tags.getAllElements()){
            String result = null;
            if (tag.tagName().equals("h1")) {
                TOPIC_NAME = tag.text();
            }
            if (tag.tagName().equals("h2")) {

                String t = tag.text();
                String nameOfPoint = "";
                for (int i = 0; i < t.toCharArray().length; i++) {
                    if (t.toCharArray()[i] != '['){
                        nameOfPoint = nameOfPoint + t.toCharArray()[i];
                    } else
                        break;
                }

                tocList.put(nameOfPoint,index+"");
            }



            // если тег p и в нем есть текст
            if (tag.tagName().equals("p") && tag.hasText() ){
                // удаляем корявые символы и обрезаем пробелы по краям
                result = tag.text().replaceAll("(&nbsp;)","").trim();

            }
            // если это список и он не пустой
            if (tag.tagName().equals("li") && tag.hasText()){
                // удаляем корявые символы и обрезаем пробелы по краям
                result = tag.text().replaceAll("&nbsp;","").trim(); // избавляемся от лишнего в тексте

            }
            if (result != null){ // если кусок текста с вики не пустой, то
                text.add(result); // добавляем в результирующий список
                index++;
            }


        }

        return text;
    }
}
