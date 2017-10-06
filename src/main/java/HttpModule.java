import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.util.ArrayList;

public class HttpModule {

    public static ArrayList<String> searchTextInWiki(String searchMessage){

        ArrayList<String> text = new ArrayList<String>(); // результирующий список

        Document doc; // объявляем документ


        try {
            // ищем статью в вики
            doc = Jsoup.connect("https://ru.wikipedia.org/wiki/" + searchMessage.trim()).get();

        }
        catch (HttpStatusException e) { // если 500, 404 ошибки, то
            e.printStackTrace(); // печатаем трейс
            // в результирующий лист добавляем текст
            text.add("I can't find such topic. Try to find something else.");
            // выходим из метода
            return text;
        }
        catch (IOException e) { // если любая другая ошибка
            e.printStackTrace();
            text.add("I am sorry, but it was something wrong with connection. Please, try again.");
            return text;
        }
        Element tags = doc.body().getElementById("content"); // считываем контент

        // удаляем лишнее с полученной страницы
        tags.getElementsByAttributeValue("id","toc").remove();
        tags.getElementsByAttributeValue("class","infobox").remove();
        tags.getElementsByTag("a").unwrap();
        tags.getElementsByTag("span").remove();
        tags.getElementsByTag("table").remove();
        tags.getElementsByTag("sup").remove();
        tags.getElementsByTag("b").unwrap();
        tags.getElementsByTag("i").unwrap();

        // идем по всем оставшимся тегам
        for (Element tag: tags.getAllElements()){
            // если это параграф
            String result = null;
            // если тег p и в нем есть текст
            if (tag.tagName().equals("p") && tag.hasText() ){
                // удаляем корявые символы и обрезаем пробелы по краям
                result = tag.text().replaceAll("(&nbsp;)","").trim();
            }
            // если это список и он не пустой
            if (tag.tagName().equals("li") && tag.hasText()){
                result = tag.text(); // выделяем текст
                result = result.replaceAll("&nbsp;","").trim(); // избавляемся от лишнего в тексте
            }
            if (result != null){ // если текст не пустой
                text.add(result); // добавляем в список текст
            }

        }
        for (int i = 0; i < text.size(); i++) {
            System.out.println(text.get(i));
        }
        return text;
    }
}
