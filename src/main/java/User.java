import java.util.ArrayList;

public class User {
    private static ArrayList<Integer> usersIdsList = new ArrayList<Integer>(); // список известных ИД пользователей
    private Integer UserId; // ИД пользователя
    private String lastSearchMessage; // последнее сообщение, которое он искал
    private int indexOfParagraph = 0; // номер параграфа
    private ArrayList<String> listOfParagraphs; // список параграфов

    public boolean isButtonsNeed() {
        return isButtonsNeed;
    }

    public void setButtonsNeed(boolean buttonsNeed) {
        isButtonsNeed = buttonsNeed;
    }

    private boolean isButtonsNeed;

    // конструктор
    public User(Integer userId) {
        UserId = userId;
    }
    // метод проверяет известный это пользователь или нет
    public static boolean isUserOld(Integer userId){
        if (usersIdsList.contains(userId)){
            return true;
        }
        else {
            return false;
        }
    }
    // добавить ИД в список известных пользователей
    public static void addId(Integer id){
        usersIdsList.add(id);
    }
    public int getIndexOfParagraph(){
        return indexOfParagraph;
    }
    public void incrementIndex(){
        // если список параграфов пустой, то индекс всегда ноль
        if (listOfParagraphs == null ) {
            indexOfParagraph = 0;
            setButtonsNeed(false);
        }
        else // иначе если еще не достигли предела списка параграфов, то увеличиваем индекс
            if (indexOfParagraph < listOfParagraphs.size() - 1) {
                indexOfParagraph++;
            }
    }
    public void decrementIndex(){
        // если список параграфов пустой или индекс стал меньше 0, то обнуляем его
        if (listOfParagraphs == null){
            setButtonsNeed(false);
        }
        if (indexOfParagraph <= 0) indexOfParagraph = 0;
        else // иначе уменьшаем индекс
            indexOfParagraph--;
    }
    public String getMessageForReply(){
        // если список параграфов пустой(не ссылается ни на какой список), то возвращаем соответствующее сообщение
        if (listOfParagraphs == null){
            return "There is no text for you. Try to find something else.";
            // иначе возвращаем соответствубщий параграф
        } else  {
            return listOfParagraphs.get(indexOfParagraph);
        }
    }
    public ArrayList<String> getListOfParagraphs() {
        return listOfParagraphs;
    }
    public void setListOfParagraphs(ArrayList<String> listOfParagraphs) {
        indexOfParagraph = 0; // сбрасываем счетчик каждый раз, когда записываем новый массив параграфов
        this.listOfParagraphs = listOfParagraphs;
    }

    public String getLastSearchMessage() {
        return lastSearchMessage;
    }
    // при установке нового последнего сообщения пользователя, сразу же обнволяем список параграфов для вывода
    public void setLastSearchMessageAndUpdateListOfParagraphs(String lastSearchMessage) {

        // если сообщение null, то пользователь послал документ или картинку
        if (lastSearchMessage == null) lastSearchMessage = "";
        // если пользователь ввел старт или помощь, то соответствующее сообщение
        if (lastSearchMessage.equals("/start") || lastSearchMessage.equals("/help")){
            this.lastSearchMessage = null;
            ArrayList<String> text = new ArrayList<String>();
            text.add("Hello. I am WikiBot. Nice to meet you.\n" +
                    "If you want to search something in WikiPedia, tell me.\n" +
                    "I will try to find it for you.\n" +
                    "For example, type 'Tesla'");
            setListOfParagraphs(text);
            setButtonsNeed(false);
        }
        else if (lastSearchMessage.equals("")){
            // случай, когда пользователь послал картинку или документ
            this.lastSearchMessage = null;
            setListOfParagraphs(null); // список параграфов обнуляем
            setButtonsNeed(false);
        }
        else if (lastSearchMessage.equals("-->>")){
            // в зависимости от того, какую кнопку нажал пользователь мы увеличиваем или уменьшаем счетчик страницы
            incrementIndex();
        }
        else if (lastSearchMessage.equals("<<--")){
            // в зависимости от того, какую кнопку нажал пользователь мы увеличиваем или уменьшаем счетчик страницы
            decrementIndex();
        }
        else {
            // если сообщение ок, то ищем по этому сообщению статью в вики
            this.lastSearchMessage = lastSearchMessage;
            setListOfParagraphs(HttpModule.searchTextInWiki(this.lastSearchMessage));
            // если список параграфов состоит из 1 элемента, то нет смысла выводить кнопки
            if (getListOfParagraphs().size() == 1){
                setButtonsNeed(false);
            } else {
                setButtonsNeed(true);
            }

        }
    }
    public Integer getUserId() {
        return UserId;
    }




}
