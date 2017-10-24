import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class User implements Serializable{
    public User(Integer userId) {
        UserId = userId;
    }

    public static List<User> usersList = new ArrayList<User>();
    private static ArrayList<Integer> usersIdsList = new ArrayList<Integer>(); // список известных ИД пользователей

    // метод проверяет известный это пользователь или нет
    public static boolean isUserOld(Integer userId){
        return usersIdsList.contains(userId);
    }
    // добавить ИД в список известных пользователей
    public static void addId(Integer id){
        usersIdsList.add(id);
    }
    // метод возвращает пользователя из списка пользователей по его ИД
    public static User getCurrentUserForWork(Integer userId){
        // объявляем индекс
        int index = 0;
        // идем по списку пользователей, известых боту
        for (int i = 0; i < usersList.size(); i++) {
            // если id известного пользователя совпадает с id пользователя из апдейта, то
            if (User.usersList.get(i).getUserId().equals(userId)){
                // сохраняем индекс
                index = i;
                break;
            }
        }
        // возвращаем индекс
        return usersList.get(index);
    }

    private Integer UserId; // ИД пользователя
    private String firstName;
    private String lastName;
    private String userName;
    private Long lastChatId; // чат ИД пользователя

    public Integer getUserId() {
        return UserId;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public Long getLastChatId() {
        return lastChatId;
    }
    public void setLastChatId(Long lastChatId) {
        this.lastChatId = lastChatId;
    }

    private String lastSearchMessage; // последнее сообщение, которое он искал
    private int indexOfParagraph = 0; // номер параграфа
    private ArrayList<String> listOfParagraphs; // список параграфов
    private String modeOfButtons; // нужны кнопки при отправке сообщения поьзователю или нет
    private Map<String, String> toc; // меню статьи
    private ArrayList<String> listOfCases = new ArrayList<String>();
    private String topic_name; // название статьи в Википедии
    private boolean isNeedShowToc; // надо ли показывать меню
    private boolean isTopicMode = true; // true - Topics, false - Quotes
    private String lastWebLink;
    private ArrayList<String> similarTopics = new ArrayList<String>();
    private Date lastDateUpdate;



    public void setLastSearchMessage(String lastSearchMessage){
        this.lastSearchMessage = lastSearchMessage;
    }
    public String getLastSearchMessage() {
        return lastSearchMessage;
    }
    public void setIndexOfParagraph(int indexOfParagraph){
        if (indexOfParagraph > getListOfParagraphs().size()){
            this.indexOfParagraph = getListOfParagraphs().size() - 1;
        }
        else {
            this.indexOfParagraph = indexOfParagraph;
        }
    }
    public int getIndexOfParagraph(){
        return indexOfParagraph;
    }
    public void setListOfParagraphs(ArrayList<String> listOfParagraphs) {
        indexOfParagraph = 0; // сбрасываем счетчик каждый раз, когда записываем новый массив параграфов
        this.listOfParagraphs = listOfParagraphs;
    }
    public ArrayList<String> getListOfParagraphs() {
        return listOfParagraphs;
    }
    public void setButtonsMode(String modeOfButtons) {
        this.modeOfButtons = modeOfButtons;
    }
    public String getModeButtons() {
        return modeOfButtons;
    }
    public void setToc(Map<String, String> toc){
        this.toc = toc;
    }
    public Map<String, String> getToc() {
        return toc;
    }
    public void setListOfCases(Map<String,String> toc){
        listOfCases = new ArrayList<String>();
        if (toc != null){
            listOfCases.addAll(toc.values());
        }
    }
    public void setListOfCases(ArrayList<String> listOfCases) {
        this.listOfCases = listOfCases;
    }
    public ArrayList<String> getListOfCases(){
        return listOfCases;
    }
    public void setTopic_name(String TOPIC_NAME){
        topic_name = TOPIC_NAME;
    }
    public String getTopic_name(){
        return topic_name;
    }
    public void setNeedToShowToc(boolean needToShowToc){
        if (toc == null){
            this.isNeedShowToc = false;
        } else {
            this.isNeedShowToc = needToShowToc;
        }

    }
    public boolean isNeedShowToc(){
        return isNeedShowToc;
    }
    public void setTopicMode(boolean isTopicMode){
        this.isTopicMode = isTopicMode;
    }
    public boolean getTopicMode(){
        return isTopicMode;
    }
    public void setLastWebLink(String lastWebLink) {
        this.lastWebLink = lastWebLink;
    }
    public String getLastWebLink() {
        return lastWebLink;
    }
    public void setSimilarTopics(ArrayList<String> similarTopics) {
        if (similarTopics == null) this.similarTopics = new ArrayList<String>();
        else {
            this.similarTopics = similarTopics;
        }
    }
    public ArrayList<String> getSimilarTopics() {
        return similarTopics;
    }
    public Date getLastDateUpdate() {
        return lastDateUpdate;
    }
    public void setLastDateUpdate(String lastDateUpdate) {
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {
            this.lastDateUpdate = format.parse(lastDateUpdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void updateLastDateUpdate(){
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date lastUpdate = new Date();
        String string = format.format(lastUpdate);

        try {
            lastDateUpdate = format.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //System.out.println(lastDateUpdate);
    }
    public void incrementIndex(){
        if (indexOfParagraph < listOfParagraphs.size() - 1) {
            indexOfParagraph++;
        }
        else {
            indexOfParagraph = listOfParagraphs.size() - 1;

        }
    }
    public void decrementIndex(){
        if (indexOfParagraph <= 0) indexOfParagraph = 0;
        else // иначе уменьшаем индекс
            indexOfParagraph--;
    }
    public String getMessageForReply(){
        // если список параграфов пустой(не ссылается ни на какой список), то возвращаем соответствующее сообщение
        if (listOfParagraphs == null){
            setButtonsMode(BUTTONS_MODE.NONE);
            return "У меня не получилось найти что-либо по этому запросу.";

            // иначе возвращаем соответствубщий параграф
        } else  {
            return listOfParagraphs.get(indexOfParagraph);
        }
    }
    public boolean needToShowSimilarTopics(){
        boolean needShowButtonSimilarTopic = false;
        if (getSimilarTopics().size() > 0){
            needShowButtonSimilarTopic = true;
        }
        return needShowButtonSimilarTopic;
    }
    public boolean checkContainsListOfCases(String lastSearchMessage){
        try {
            return listOfCases.contains(lastSearchMessage);

        } catch (Exception e){
            return false;
        }
    }
    // при установке нового последнего сообщения пользователя, сразу же обнволяем список параграфов для вывода
    public void parseLastMessageAndSendReply(String lastSearchMessage) {
        updateLastDateUpdate();
        // если сообщение null, то пользователь послал документ или картинку
        if (lastSearchMessage == null) lastSearchMessage = "";
        // если пользователь ввел старт или помощь, то соответствующее сообщение
        if (lastSearchMessage.equals(RESERVED_ANSWER.START)){
            // последнее сообщение null
            setLastSearchMessage(null);
            // устанавливаем лист параграфов
            ArrayList<String> text = new ArrayList<String>();
            text.add("Привет. Меня зовут WikiBot.\n" +
                    "Если ты хочешь найти что-то в WikiPedia или WikiQuotes, скажи мне.\n" +
                    "Я попробую найти это для тебя.\n" +
                    "Для начала, выбери режим поиска:");
            setListOfParagraphs(text);
            // обнуляем меню
            setToc(null);
            setListOfCases(getToc());
            setTopic_name(null);
            setLastWebLink(null);
            setButtonsMode(BUTTONS_MODE.START);
            setNeedToShowToc(false);
            setSimilarTopics(null);

        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.INSTRUCTION)){
            // устанавливаем последнее сообщение
            setLastSearchMessage(null);
            // устанавливаем лист параграфов
            ArrayList<String> text = new ArrayList<String>();
            text.add("Краткая инструкция пользования WikiBot:\n" +
                    "1. Для начала вам необходимо задать боту режим поиска - либо статьи в Википедии, либо цитаты в Викицитатнице.\n" +
                    "2. После того, как вы зададите режим, бот будет искать информацию в соответствующем источнике.\n" +
                    "3. Далее вы можете:\n" +
                    "3.1 Ввести ключевое слово для поиска статьи\n" +
                    "3.2 Сменить режим поиска\n" +
                    "3.3 Нажать кнопку \"случайная статья\" после которой бот выберет и предложит вам случайную статью\n" +
                    "4. Если бот не найдет искомую информацию, то он так и напишет\n" +
                    "5. А если найдет, то он постарается сформировать оглавление\n" +
                    "6. В оглавлении вы можете начать просмотр статьи:\n" +
                    "6.1 Либо с самого начала \n" +
                    "6.2 Либо с соответстствующего пункта оглавления\n" +
                    "6.3 Ниже будут кнопки для поиска случайной статьи, смены режима поиска," +
                    " открытия статьи в браузере и просмотра похожих статей по найденной теме.\n" +
                    "7. Во время просмотра статьи в вашем распоряжении будут три кнопки:\n" +
                    "7.1 ⬅️ - посмотреть предыдущий параграф статьи\n" +
                    "7.2 \uD83D\uDCD7 - вернуться к оглавлению статьи\n" +
                    "7.3 ➡️ - посмотреть следующий параграф статьи\n" +
                    "8. Вот и все, приятного пользования.");
            setListOfParagraphs(text);
            // обнуляем меню
            setToc(null);
            setListOfCases(getToc());
            //
            /* если строку ниже раскоментировать, то будет баг непонятно почему:
            * сменить режим, инструкция, начать пользоваться ботом, выбрать режим, нажать на "случайная статья"
            * и будет ошибка */
            //setListOfCases(null);
            setTopic_name(null);
            setLastWebLink(null);

            setButtonsMode(BUTTONS_MODE.INSTRUCTIONS);
            setNeedToShowToc(false);
            setSimilarTopics(null);

        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.ABOUT)){
            // устанавливаем последнее сообщение
            setLastSearchMessage(null);
            // устанавливаем лист параграфов
            ArrayList<String> text = new ArrayList<String>();
            text.add("Привет. \uD83D\uDE0A \n" +
                    "Немножко о боте:\n" +
                    "\uD83C\uDFAF Бот предназначен для поиска статей в Википедии и Викицитатнике по ключевым словам, которые вы ему пишете. \n" +
                    "При этом если статья не найдена, то он так и напишет. В ином случае бот попытается сформировать меню для навигации и получить соответствующий текст. \n" +
                    "В большинстве своем он ищет и получает информацию корректно, но как говорил А.С.Пушкин - \"нет правил без исключений\". \n" +
                    "Это связано с тем, что структура найденной страницы википедии/цитатника отличается от той, которую бот может понять.\n" +
                    "\uD83C\uDFAF Также хотелось бы добавить немного о постоянном процессе оптимизации скорости отклика бота на действие пользователя. \n" +
                    "Если вы заметили замедление работы, это может быть связано либо со скоростью работы интернета, либо с большой нагрузкой пользователей.\n" +
                    "\uD83C\uDFAF И наконец, желаю приятного пользования. Не стесняйтесь оставлять оценку и отзыв о боте, нажав на кнопку со звездами в главном меню. Спасибо за внимание.");
            setListOfParagraphs(text);
            // обнуляем меню
            setToc(null);
            setListOfCases(getToc());
            setTopic_name(null);
            setLastWebLink(null);

            setButtonsMode(BUTTONS_MODE.INSTRUCTIONS);
            setNeedToShowToc(false);
            setSimilarTopics(null);

        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.TOPICS)){
            // обнуляем последнее сообщение
            setLastSearchMessage(null);
            // вносим лист текст
            ArrayList<String> text = new ArrayList<String>();
            text.add("Напиши, что мне найти в Wikipedia");
            setListOfParagraphs(text);
            // обнуляем меню
            setToc(null);
            setListOfCases(getToc());
            setButtonsMode(BUTTONS_MODE.SEARCH_MODE);

            setTopicMode(true);
            setNeedToShowToc(false);
            setSimilarTopics(null);
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.QUOTES)){
            setLastSearchMessage(null);

            ArrayList<String> text = new ArrayList<String>();
            text.add("В режиме поиска цитат вы можете искать цитаты по ключевым словам.\n" +
                    "Например:\n" +
                    "- Шрек\n" +
                    "- Преступление и наказание\n" +
                    "- Счастье\n" +
                    "- Эйнштейн");
            setListOfParagraphs(text);

            setToc(null);
            setListOfCases(getToc());
            setButtonsMode(BUTTONS_MODE.SEARCH_MODE);

            setTopicMode(false);
            setNeedToShowToc(false);
            setSimilarTopics(null);
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.RANDOM)){
            // случай, когда пользователь нажал показать случайную статью
            setLastSearchMessage(null);

            HttpModule httpModule = new HttpModule();
            if (isTopicMode){
                setListOfParagraphs(httpModule.searchTopicInWikiWithToc("/random"));
            } else {
                setListOfParagraphs(httpModule.searchQuotesInWikiWithToc("/random"));
            }

            setToc(httpModule.getTocList());
            setListOfCases(getToc());
            setTopic_name(httpModule.getTOPIC_NAME());

            setButtonsMode(BUTTONS_MODE.DEFAULT);
            if (httpModule.isError()){
                setButtonsMode(BUTTONS_MODE.NONE);
            }

            setLastWebLink(httpModule.getLink());

            setNeedToShowToc(true);
            setSimilarTopics(null);

        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.SIMILAR_TOPICS)){
            // если пользователь нажал кнопку посмотреть похожие статьи,

            ArrayList<String> text = new ArrayList<String>();
            text.add("Ниже представлен список похожих статей.\n" +
                    "Выберете статью для просмотра:");
            setListOfParagraphs(text);
            setToc(null);
            setListOfCases(getToc());
            setTopic_name(null);
            setButtonsMode(BUTTONS_MODE.SIMILAR_TOPICS);
            setNeedToShowToc(false);
            setLastWebLink(null);
            //setTopicMode(true);
        }
        else if (lastSearchMessage.equals("")){
            // случай, когда пользователь послал картинку или документ
            setLastSearchMessage(null);
            setListOfParagraphs(null); // список параграфов обнуляем
            setToc(null);
            setListOfCases(getToc());
            setTopic_name(null);
            setButtonsMode(BUTTONS_MODE.NONE);
            setNeedToShowToc(false);
            setSimilarTopics(null);
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.COUNT_USERS)){
            // случай, когда пользователь послал картинку или документ
            //setLastSearchMessage(null);
            ArrayList<String> text = new ArrayList<String>();
            int size = Db.countOfUsers();
            text.add("Текущее количество пользователей: " + size);
            setListOfParagraphs(text);
            setToc(null);
            setListOfCases(getToc());
            setTopic_name(null);
            setButtonsMode(BUTTONS_MODE.NONE);
            setNeedToShowToc(false);
            setSimilarTopics(null);
            setLastWebLink(null);
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.NEXT)){
            // в зависимости от того, какую кнопку нажал пользователь мы увеличиваем или уменьшаем счетчик страницы
            incrementIndex();
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.PREV)){
            // в зависимости от того, какую кнопку нажал пользователь мы увеличиваем или уменьшаем счетчик страницы
            decrementIndex();
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.SHOW_MENU)){
            // если пользователь нажал кнопку посмотреть меню, ничего не меняем, кроме функции показать меню
            setNeedToShowToc(true);
        }
        else if (lastSearchMessage.equals("/image")){
            ArrayList<String> image = new ArrayList<String>();
            image.add("https://c1.staticflickr.com/7/6107/6381966401_032df5fe1e_b.jpg");
            setListOfParagraphs(image);
        }
        else if (checkContainsListOfCases(lastSearchMessage)){ // если страница меню совпадает с текущей менюшкой
            setIndexOfParagraph(Integer.parseInt(lastSearchMessage));
            setButtonsMode(BUTTONS_MODE.DEFAULT);
            setNeedToShowToc(false);
        }
        else {
            // если новое сообщение ок, то ищем по этому сообщению статью в вики
            setLastSearchMessage(lastSearchMessage);

            // инициализируем http модуль для отпавки запросов
            HttpModule httpModule = new HttpModule();

            // ищем новые параграфы
            if (isTopicMode){
                setListOfParagraphs(httpModule.searchTopicInWikiWithToc(this.lastSearchMessage));
            } else {
                setListOfParagraphs(httpModule.searchQuotesInWikiWithToc(this.lastSearchMessage));
            }

            // устанавливаем меню
            setToc(httpModule.getTocList());
            setListOfCases(getToc());
            setTopic_name(httpModule.getTOPIC_NAME());
            setLastWebLink(httpModule.getLink());

            setButtonsMode(BUTTONS_MODE.DEFAULT);
            if (httpModule.isError()){
                setButtonsMode(BUTTONS_MODE.NONE);
            }
            setNeedToShowToc(true);

            setSimilarTopics(httpModule.getSimilarTopics());
        }

        // если надо показать меню, то показываем меню. иначе отправляем сообщение
        if (isNeedShowToc()){
            wikiBot.mySendTocMessage(getLastChatId(),toc,getTopic_name(), isTopicMode, getLastWebLink(),needToShowSimilarTopics());
        } else {
            wikiBot.mySendMessage(getLastChatId(),getMessageForReply(),getModeButtons(), getSimilarTopics());
        }

        // запуск новой нити для автоматической отправки случайной статьи по истечении таймера
        //activateNewTimerThread();
    }

    private transient WikiBot wikiBot; // инстанс бота для отправки сообщений
    public void setWikiBot(WikiBot wikiBot){
        // если вики бот еще не был проинициализирован у пользователя, то проинициализировать
        if (this.wikiBot == null){
            this.wikiBot = wikiBot;
        }
    }
    public WikiBot getWikiBot() {
        return wikiBot;
    }
    // метод посылает сообщение пользователю с помощью бота

    private transient Thread autoSendRandomMessage; // дополнительная нить пользователя для отправки сообщений по таймеру
    private String topic_random_name; // название случайной статьи в Википедии(задается в отдельной нити)
    private ArrayList<String> listOfRandomParagraphs; // список параграфов случайной статьи

    public void setTopic_random_name(String TOPIC_NAME){
        topic_random_name = TOPIC_NAME;
    }
    public String getTopic_random_name(){
        return topic_random_name;
    }
    public ArrayList<String> getListOfRandomParagraphs() {
        return listOfRandomParagraphs;
    }
    public void setListOfRandomParagraphs(ArrayList<String> listOfParagraphs) {
        this.listOfRandomParagraphs = listOfParagraphs;
    }
    public void activateNewTimerThread(){
        // если есть текущая запущенная нить, то прерываем ее
        if (autoSendRandomMessage != null){
            autoSendRandomMessage.interrupt();
        }
        // создаем и запускаем новую нить отправки автоматического сообщения
        AutoSendThread autoSendThread = new AutoSendThread(this);
        autoSendRandomMessage = autoSendThread.t; // инициализируем переменную

    }
    public void saveUserToDBv2(){
        // создаем БД
        Db.createNewDatabase("Users.db");
        // создаем табличку UserTable
        Db.createNewTable();
        // коннектимся к ней
        Db.connect();
        // проверяем существует ли пользователь в БД
        if (Db.isUserExist(this.getUserId().toString())){
            // если существует, то апдейтим пользователя
            int index = Db.getUserIndex(this.getUserId().toString());
            Db.updateUser(index,this);
        } else {
            // если не сущестсует, то инсертим пользователя
            Db.insertNewUser(this);
        }
        // закрываем соединение к БД
        Db.close();
    }
    public void saveUserToDB(){
        // создаем БД
        Db.createNewDatabase("Users.db");
        // создаем табличку UserTable
        Db.createNewTable();
        // коннектимся к ней
        Db.connect();
        // получаем id пользователя в списке
        int index = usersList.indexOf(this) + 1;
        // проверяем существует ли пользователь в БД
        if (Db.isUserExist(index)){
            // если существует, то апдейтим пользователя
            Db.updateUser(index,this);
        } else {
            // если не сущестсует, то инсертим пользователя
            Db.insertNewUser(this);
        }
        // закрываем соединение к БД
        Db.close();
    }
    public static void saveUsersToDB(){
        // создаем БД
        Db.createNewDatabase("Users.db");
        // создаем табличку UserTable
        Db.createNewTable();
        // коннектимся к ней
        Db.connect();
        // идем по всем пользователям
        int index;
        for (int i = 0; i < usersList.size(); i++) {
            index = i + 1;
            // проверяем существует ли пользователь в БД
            if (Db.isUserExist(index)){
                // если существует, то апдейтим пользователя
                Db.updateUser(index,usersList.get(i));
            } else {
                // если не сущестсует, то инсертим пользователя
                Db.insertNewUser(usersList.get(i));
            }
        }
        // закрываем соединение к БД
        Db.close();
    }
    public static void loadUsersFromDB(){
        // создаем БД
        Db.createNewDatabase("Users.db");
        // создаем табличку UserTable
        Db.createNewTable();
        // коннектимся к ней
        Db.connect();
        // загружаем пользователей
        usersList = Db.loadUsers();
        usersIdsList = Db.getLoadedUsersIdsList();
        // закрываем соединение к БД
        Db.close();
        System.out.println("Users was loaded");
    }
}
