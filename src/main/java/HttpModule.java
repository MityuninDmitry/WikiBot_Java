import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class HttpModule {
    private Map<String, String> tocList = new LinkedHashMap<String, String>();
    private String TOPIC_NAME = "";
    private boolean isError = false;
    private String errorName;
    private String link;
    private ArrayList<String> similarTopics = new ArrayList<String>();

    public ArrayList<String> getSimilarTopics() {
        return similarTopics;
    }

    public void setLink(String link) {

        try {
            this.link = URLDecoder.decode(link,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
    public String getLink() {
        return link;
    }

    public boolean isError(){
        return isError;
    }
    public String getErrorName(){
        return errorName;
    }
    public String getTOPIC_NAME() {
        return TOPIC_NAME;
    }
    public Map getTocList(){
        return tocList;
    }
    public ArrayList<String> searchTopicInWikiWithToc(String searchMessage){
        ArrayList<String> text = new ArrayList<String>(); // результирующий список
        GoogleSender googleSender = new GoogleSender();
        // первый пункт меню добавляем сразу
        tocList.put("Просмотр статьи с начала", "0");
        // объявляем документ
        Document doc = null;
        try {
            // ищем статью в вики
            if (searchMessage.equals("/random")){

                doc = Jsoup.connect("https://ru.wikipedia.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%A1%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0").get();
                setLink(doc.getElementsByAttributeValue("rel","canonical").attr("href"));
                similarTopics = new ArrayList<String>();
                TOPIC_NAME = doc.body().getElementsByTag("h1").text();
            }
            else {
                doc = googleSender.googleIt(searchMessage.trim(),true);
                setLink(doc.getElementsByAttributeValue("rel","canonical").attr("href"));
                similarTopics = googleSender.getSimilarTopics();
                TOPIC_NAME = googleSender.getTopicName();
            }
        }
        catch (HttpStatusException e) { // если 500, 404 ошибки, то
            e.printStackTrace(); // печатаем трейс

            setLink("");
            isError = true;
            // выходим из метода
            text.add("К сожалению, по вашим ключевым словам ничего найти не удалось.\n" +
                    "Попробуйте поискать информацию в другом режиме поиска.");

            return text;

        }
        catch (Exception e){
            setLink("");
            e.printStackTrace();
            text.add("По вашему запросу ничего не найдено.");
            isError = true;
            return text;
        }
        Element tags = doc.body().getElementById("content"); // считываем контент

        // удаляем лишнее с полученной страницы
        tags.getElementsByAttributeValue("id","toc").remove(); // удаляем оглавление
        tags.getElementsByAttributeValue("class","infobox").remove(); // удаляем информационное поле
        tags.getElementsByTag("table").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("id","contentSub").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("id","siteSub").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("id","jump-to-nav").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","dablink noprint").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","thumb tright").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","thumbinner").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","metadata plainlinks navigation-box").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","printfooter").remove(); // удаляем таблички

        // забираем заголовок

        // удаляем заголовок
        tags.getElementsByTag("h1").remove();

        // формируем лист оглавления, не мапу
        ArrayList<String> tocListCopy = new ArrayList<String>();
        for (Element tagH2: tags.getAllElements()){
            if (tagH2.tagName().equals("h2")){
                tocListCopy.add(tagH2.text());
            }
        }

        // вставляем знаки для парсинга после каждого заголовка 2 уровня
        for (Element tagH2: tags.getAllElements()){
            if (tagH2.tagName().equals("h2")){
                tagH2.prepend("!parse");
                tagH2.append("!parse");
            }
            if (tagH2.tagName().equals("h3")){
                tagH2.prepend("!parse Подзаголовок: ");
                tagH2.append("!parse");
            }
        }
        // собираем весь текст
        String allText = tags.text();
        // удаляем символы переноса строк
        allText = allText.replaceAll("\n", "");
        allText = allText.replaceAll("\r", "");
        allText = allText.replaceAll("\r\n", "");
        // делим его по нашим специально вставленным знакам
        String[] allTextMas = allText.split("!parse");

        // заполняем лист кусков текста
        for (int i = 0; i < allTextMas.length; i++) {
            allTextMas[i] = allTextMas[i].trim();

            if (allTextMas[i].length() > 4096){
                allTextMas[i] = allTextMas[i].substring(0,4096);
            }
            text.add(allTextMas[i]);
        }
        // удаляем пустые элементы
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).equals("") || text.get(i) == null){
                text.remove(i);
            }
        }
        // удаляем лишние пункты меню(которые пустые)
        // идем по кускам текста, в который включены также пункты меню
        for (int i = 0; i < text.size() - 1; i++) {
            // идем по массиву списка пунктов меню
            for (int k = 0; k < tocListCopy.size() - 1; k++) {
                // если кусок текста совпадает с пунктом оглавления и след кусок также совпадает с пунктом меню
                // то это говорит о том, что предыдущий пункт меню пустой, поэтому
                if (text.get(i).trim().equals(tocListCopy.get(k).trim()) && text.get(i+1).trim().equals(tocListCopy.get(k+1).trim())) {
                    text.remove(i); // удаляем предыдущий пункт меню
                    tocListCopy.remove(k); // удаляем предыдущий пункт меню
                }
            }
        }
        // в подзаголовках текста избавляемся от лишних символов типа [править | править вики-текст]
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).matches("Подзаголовок:.*")){
                String nameOfPoint = "";
                for (int j = 0; j < text.get(i).toCharArray().length; j++) {
                    if (text.get(i).toCharArray()[j] != '['){
                        nameOfPoint = nameOfPoint + text.get(i).toCharArray()[j];
                    } else
                        break;
                }
                text.remove(i);
                text.add(i,nameOfPoint.toUpperCase());
            }
        }
        // в этом куске кода добавляем в мапу названия заголовков и ссылки на номер куска текста
        for (int i = 0; i < text.size(); i++) {
            // идем по массиву списка оглавления
            for (int k = 0; k < tocListCopy.size(); k++) {
                // если кусок текста совпадает с пунктом оглавления
                if (text.get(i).equals(tocListCopy.get(k))){
                    // удаляем лишние символы из заголовка
                    String nameOfPoint = "";
                    for (int j = 0; j < text.get(i).toCharArray().length; j++) {
                        if (text.get(i).toCharArray()[j] != '['){
                            nameOfPoint = nameOfPoint + text.get(i).toCharArray()[j];
                        } else
                            break;
                    }
                    text.remove(i);
                    text.add(i,nameOfPoint);
                    // добавляем в мапу пункт оглавления со ссылкой на номер массива
                    tocList.put(text.get(i),i + "");
                    // обнуляем элемент массива
                    text.remove(i);
                    i--;
                }

            }
        }
        // удаляем из мапы меню заголовки примечание, литература, ссылки, см. также
        // запоминая при этом их значения
        ArrayList<String> valuesList = new ArrayList<String>();
        for (Map.Entry<String,String> tocElement: tocList.entrySet()){
            if (tocElement.getKey().equals("Примечания") ||
                    tocElement.getKey().equals("См. также") ||
                    tocElement.getKey().equals("Литература") ||
                    //tocElement.getKey().equals("Источники") ||
                    tocElement.getKey().equals("Список литературы") ||
                    tocElement.getKey().equals("Пояснения") ||
                    tocElement.getKey().equals("Ссылки")) {
                valuesList.add(tocElement.getValue());

            }
        }
        tocList.remove("Примечания");
        tocList.remove("См. также");
        tocList.remove("Литература");
        tocList.remove("Ссылки");
        //tocList.remove("Источники");
        tocList.remove("Список литературы");
        tocList.remove("Пояснения");
        // находим наименьшее значение из них

        // удаляем из текста от этого значения до конца
        if (valuesList.size() > 0){
            Collections.sort(valuesList);
            while (text.size() != Integer.parseInt(valuesList.get(0))){
                text.remove(text.size() - 1);
            }
        }


        /*
        System.out.println("============ MENU =============");
        for (Map.Entry<String,String> map: tocList.entrySet()){
            System.out.println(map.getKey() + ": " + map.getValue());
        }
        System.out.println("============ TEXT =============");
        for (int i = 0; i < text.size(); i++) {
            System.out.println(text.get(i));
        }
        */
        text.add("Вы достигли конца статьи.");
        return text;
    }
    public ArrayList<String> searchQuotesInWikiWithToc(String searchMessage){
        ArrayList<String> text = new ArrayList<String>(); // результирующий список
        GoogleSender googleSender = new GoogleSender();
        tocList.put("Просмотр цитат с начала", "0");
        Document doc; // объявляем документ
        try {
            // ищем статью в вики
            if (searchMessage.equals("/random")){
                doc = Jsoup.connect("https://ru.wikiquote.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%A1%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0").get();
                setLink(doc.getElementsByAttributeValue("rel","canonical").attr("href"));
                similarTopics = new ArrayList<String>();
                TOPIC_NAME = doc.body().getElementsByTag("h1").text();
            }
            else {
                /*
                setLink("https://ru.wikiquote.org/wiki/" + searchMessage.trim());
                doc = Jsoup.connect(link).get();
                similarTopics = GoogleSender.getSimilarTopics(searchMessage, false);
                */
                doc = googleSender.googleIt(searchMessage.trim(),false);
                setLink(doc.getElementsByAttributeValue("rel","canonical").attr("href"));
                similarTopics = googleSender.getSimilarTopics();
                TOPIC_NAME = googleSender.getTopicName();
            }


        }
        catch (HttpStatusException e) { // если 500, 404 ошибки, то
            e.printStackTrace(); // печатаем трейс

            setLink("");
            isError = true;
            // выходим из метода
            text.add("К сожалению, по вашим ключевым словам ничего найти не удалось.\n" +
                    "Попробуйте поискать информацию в другом режиме поиска.");

            return text;
        }
        catch (Exception e){
            setLink("");
            e.printStackTrace();
            text.add("По вашему запросу ничего не найдено.");
            isError = true;
            return text;
        }
        Element tags = doc.body().getElementById("content"); // считываем контент

        // удаляем лишнее с полученной страницы
        tags.getElementsByAttributeValue("id","toc").remove(); // удаляем оглавление
        tags.getElementsByAttributeValue("class","infobox").remove(); // удаляем информационное поле
        //tags.getElementsByTag("table").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("id","contentSub").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("id","siteSub").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("id","jump-to-nav").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","dablink noprint").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","thumb tright").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","thumbinner").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","metadata plainlinks navigation-box").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","metadata plainlinks ambox ambox-content").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","infobox sisterproject noprint wikipedia-box").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","catlinks").remove(); // удаляем таблички
        tags.getElementsByAttributeValue("class","printfooter").remove(); // удаляем таблички

        // забираем заголовок

        // удаляем заголовок
        tags.getElementsByTag("h1").remove();

        // формируем лист оглавления, не мапу
        ArrayList<String> tocListCopy = new ArrayList<String>();
        for (Element tagH2: tags.getAllElements()){
            if (tagH2.tagName().equals("h2")){
                tocListCopy.add(tagH2.text());
            }
            if (tagH2.tagName().equals("h3")){
                tocListCopy.add(tagH2.text());
            }
        }

        // вставляем знаки для парсинга после каждого заголовка 2 уровня
        for (Element tagH2: tags.getAllElements()){
            if (tagH2.tagName().equals("h2") || tagH2.tagName().equals("h3")){
                tagH2.prepend("!parse");
                tagH2.append("!parse");
            }

            if (tagH2.tagName().equals("table") || tagH2.tagName().equals("li")){
                tagH2.prepend("!parse");
                tagH2.append("!parse");
            }
        }
        // собираем весь текст
        String allText = tags.text();
        // удаляем символы переноса строк
        allText = allText.replaceAll("\n", "");
        allText = allText.replaceAll("\r", "");
        allText = allText.replaceAll("\r\n", "");
        // делим его по нашим специально вставленным знакам
        String[] allTextMas = allText.split("!parse");

        // заполняем лист кусков текста
        for (int i = 0; i < allTextMas.length; i++) {
            // удаляем пробелы с краев куска текста
            allTextMas[i] = allTextMas[i].trim();
            if (allTextMas[i].length() > 4096){
                allTextMas[i] = allTextMas[i].substring(0,4096);
            }

            text.add(allTextMas[i]);
        }

        // удаляем пустые элементы
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).length() <= 1){
                text.remove(i);

            }
        }
        for (int i = 0; i < text.size(); i++) {

            if (text.get(i).equals("") || text.get(i) == null){
                text.remove(i);

            }
        }


        // удаляем лишние пункты меню(которые пустые)
        // идем по кускам текста, в который включены также пункты меню
        for (int i = 0; i < text.size() - 1; i++) {
            // идем по массиву списка пунктов меню
            for (int k = 0; k < tocListCopy.size() - 1; k++) {
                // если кусок текста совпадает с пунктом оглавления и след кусок также совпадает с пунктом меню
                // то это говорит о том, что предыдущий пункт меню пустой, поэтому
                if (text.get(i).trim().equals(tocListCopy.get(k).trim()) && text.get(i+1).trim().equals(tocListCopy.get(k+1).trim())) {
                    text.remove(i); // удаляем предыдущий пункт меню
                    tocListCopy.remove(k); // удаляем предыдущий пункт меню
                }
            }
        }
        // в этом куске кода добавляем в мапу названия заголовков и ссылки на номер куска текста
        for (int i = 0; i < text.size(); i++) {
            // идем по массиву списка оглавления
            for (int k = 0; k < tocListCopy.size(); k++) {
                // если кусок текста совпадает с пунктом оглавления
                if (text.get(i).equals(tocListCopy.get(k))){
                    // удаляем лишние символы из заголовка
                    String nameOfPoint = "";
                    for (int j = 0; j < text.get(i).toCharArray().length; j++) {
                        if (text.get(i).toCharArray()[j] != '['){
                            nameOfPoint = nameOfPoint + text.get(i).toCharArray()[j];
                        } else
                            break;
                    }
                    text.remove(i);
                    text.add(i,nameOfPoint);
                    // добавляем в мапу пункт оглавления со ссылкой на номер массива
                    tocList.put(text.get(i),i + "");
                    // обнуляем элемент массива
                    text.remove(i);
                    i--;
                }

            }
        }
        // удаляем из мапы меню заголовки примечание, литература, ссылки, см. также
        // запоминая при этом их значения
        ArrayList<String> valuesList = new ArrayList<String>();
        for (Map.Entry<String,String> tocElement: tocList.entrySet()){
            if (tocElement.getKey().equals("Примечания") ||
                    tocElement.getKey().equals("См. также") ||
                    tocElement.getKey().equals("Литература") ||
                    //tocElement.getKey().equals("Источники") ||
                    tocElement.getKey().equals("Ссылки")) {
                valuesList.add(tocElement.getValue());

            }
        }
        tocList.remove("Примечания");
        tocList.remove("См. также");
        tocList.remove("Литература");
        tocList.remove("Ссылки");
        //tocList.remove("Источники");
        // находим наименьшее значение из них
        if (valuesList.size() > 0){
            Collections.sort(valuesList);
            while (text.size() != Integer.parseInt(valuesList.get(0))){
                text.remove(text.size() - 1);
            }
        }

        text.add("Вы достигли конца статьи.");
        /*
        System.out.println("============ MENU =============");
        for (Map.Entry<String,String> map: tocList.entrySet()){
            System.out.println(map.getKey() + ": " + map.getValue());
        }
        System.out.println("============ TEXT =============");
        for (int i = 0; i < text.size(); i++) {

            System.out.println(i + ": " + text.get(i));
        }
        */
        return text;
    }
}
