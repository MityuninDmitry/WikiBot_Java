import java.util.ArrayList;

public class User {
    private static ArrayList<Integer> usersIdsList = new ArrayList<Integer>(); // список известных ИД пользователей
    private Integer UserId; // ИД пользователя
    private Long lastChatId; // ИД пользователя
    private String lastSearchMessage; // последнее сообщение, которое он искал
    private int indexOfParagraph = 0; // номер параграфа
    private int indexOfRandomParagraph = 0; // номер параграфа
    private ArrayList<String> listOfParagraphs; // список параграфов
    private ArrayList<String> listOfRandomParagraphs; // список параграфов
    private boolean isButtonsNeed; // нужны кнопки при отправке сообщения поьзователю или нет
    private Thread autoSendRandomMessage;
    private WikiBot wikiBot;
    // конструктор
    public User(Integer userId) {
        UserId = userId;
    }
    public WikiBot getWikiBot() {
        return wikiBot;
    }
    // метод посылает сообщение пользователю с помощью бота
    public void sendMessageBy(WikiBot wikiBot){
        // если вики бот еще не был проинициализирован у пользователя, то проинициализировать
        if (this.wikiBot == null){
            this.wikiBot = wikiBot;
        }
        // посылаем сообщение на запрос пользователя
        wikiBot.mySendMessage(getLastChatId(),getMessageForReply(), isButtonsNeed());

        // если есть текущая запущенная нить, то прерываем ее
        if (autoSendRandomMessage != null){
            autoSendRandomMessage.interrupt();
        }
        // создаем и запускаем новую нить отправки автоматического сообщения
        AutoSendThread autoSendThread = new AutoSendThread(this);
        autoSendRandomMessage = autoSendThread.t; // инициализируем переменную
    }

    public Long getLastChatId() {
        return lastChatId;
    }

    public void setLastChatId(Long lastChatId) {
        this.lastChatId = lastChatId;
    }

    public boolean isButtonsNeed() {
        return isButtonsNeed;
    }

    public void setButtonsNeed(boolean buttonsNeed) {
        isButtonsNeed = buttonsNeed;
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
    public ArrayList<String> getListOfRandomParagraphs() {
        return listOfRandomParagraphs;
    }
    public void setListOfRandomParagraphs(ArrayList<String> listOfParagraphs) {
        indexOfRandomParagraph = 0; // сбрасываем счетчик каждый раз, когда записываем новый массив параграфов
        this.listOfRandomParagraphs = listOfParagraphs;
        if (listOfRandomParagraphs != null && listOfRandomParagraphs.size() >= 1){
            this.listOfRandomParagraphs.add(0,"Hello again. You didn't search anyhting long time ago.\n" +
                    "That's why i found random topic in Wikipedia. I hope it will be interesting for you:\n\n");
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
                    "For example, type 'Tesla'\n" +
                    "Also you can type:\n" +
                    "/random for searching random topic\n" +
                    "/help for seeing this instruction");
            setListOfParagraphs(text);
            setButtonsNeed(false);
        }
        else if (lastSearchMessage.equals("/random")){
            // случай, когда пользователь послал картинку или документ
            this.lastSearchMessage = null;

            setListOfParagraphs(HttpModule.searchRandomTopicInWiki()); // список параграфов обнуляем
            setButtonsNeed(true);
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
            setListOfParagraphs(HttpModule.searchTopicInWikiBy(this.lastSearchMessage));
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
