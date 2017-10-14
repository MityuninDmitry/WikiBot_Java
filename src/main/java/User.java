import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User implements Serializable{
    public static List<User> usersList = new ArrayList<User>();
    private static ArrayList<Integer> usersIdsList = new ArrayList<Integer>(); // список известных ИД пользователей
    private Integer UserId; // ИД пользователя
    private Long lastChatId; // чат ИД пользователя
    private String lastSearchMessage; // последнее сообщение, которое он искал
    private int indexOfParagraph = 0; // номер параграфа
    private ArrayList<String> listOfParagraphs; // список параграфов
    private ArrayList<String> listOfRandomParagraphs; // список параграфов случайной статьи
    private String modeOfButtons; // нужны кнопки при отправке сообщения поьзователю или нет
    private Map<String, String> toc; // меню статьи
    private ArrayList<String> listOfCases = new ArrayList<String>();
    private String topic_name; // название статьи в Википедии
    private String topic_random_name; // название случайной статьи в Википедии(задается в отдельной нити)
    private boolean isNeedShowToc; // надо ли показывать меню
    private boolean isTopicsMode = true; // true - Topics, false - Quotes
    private transient Thread autoSendRandomMessage; // дополнительная нить пользователя для отправки сообщений по таймеру
    private transient WikiBot wikiBot; // инстанс бота для отправки сообщений
    public void setTopicsMode(boolean isTopicsMode){
        this.isTopicsMode = isTopicsMode;
    }
    public void setTopic_name(String TOPIC_NAME){
        topic_name = TOPIC_NAME;
    }
    public String getTopic_name(){
        return topic_name;
    }
    public void setTopic_random_name(String TOPIC_NAME){
        topic_random_name = TOPIC_NAME;
    }
    public String getTopic_random_name(){
        return topic_random_name;
    }
    public void setToc(Map<String, String> toc){
        this.toc = toc;
        if (this.toc == null || this.toc.size() == 1) {
            setNeedToShowToc(false);
        } else {
            setNeedToShowToc(true);
            for (Map.Entry<String,String> toc_item: this.toc.entrySet()){
                listOfCases.add(toc_item.getValue());
            }
        }

    }
    public void setNeedToShowToc(boolean needToShowToc){
        if (toc != null)
            this.isNeedShowToc = needToShowToc;
    }
    public boolean isNeedShowToc(){
        return isNeedShowToc;
    }
    public void setWikiBot(WikiBot wikiBot){
        // если вики бот еще не был проинициализирован у пользователя, то проинициализировать
        if (this.wikiBot == null){
            this.wikiBot = wikiBot;
        }
    }
    // конструктор
    public User(Integer userId) {
        UserId = userId;
    }
    // метод сохраняет список пользователей
    public static void saveUsers(){
        FileOutputStream fos;
        ObjectOutputStream out;
        try{
            // создаем файлик для сохранения пользователей
            File file = new File("src/main/resources/SavedUsers.dat");
            // открываем потоки сохранения объекта
            fos = new FileOutputStream(file);
            out = new ObjectOutputStream(fos);
            // сохраняем объекты
            out.writeObject(usersList);
            out.writeObject(usersIdsList);
            // закрываем поток записи
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    // метод загружает известных пользователей из файла
    public static void loadUsers(){
        FileInputStream fis;
        ObjectInputStream in;
        try {
            fis = new FileInputStream("src/main/resources/SavedUsers.dat");
            in = new ObjectInputStream(fis);
            usersList = (ArrayList<User>) in.readObject();
            usersIdsList = (ArrayList<Integer>) in.readObject();
            in.close();
            System.out.println("Users was loaded");
        } catch (IOException e){
            // e.printStackTrace();
            System.out.println("There is no file for loading users. Or another error.");
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        for (int i = 0; i < usersList.size(); i++) {
            usersList.get(i).activateNewTimerThread();
        }
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
    public WikiBot getWikiBot() {
        return wikiBot;
    }
    // метод посылает сообщение пользователю с помощью бота
    public void activateNewTimerThread(){
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
    public String getModeButtons() {
        return modeOfButtons;
    }
    public void setButtonsMode(String modeOfButtons) {
        this.modeOfButtons = modeOfButtons;
    }
    // метод проверяет известный это пользователь или нет
    public static boolean isUserOld(Integer userId){
        return usersIdsList.contains(userId);
    }
    // добавить ИД в список известных пользователей
    public static void addId(Integer id){
        usersIdsList.add(id);
    }
    public int getIndexOfParagraph(){
        return indexOfParagraph;
    }
    public void setIndexOfParagraph(int indexOfParagraph){
        if (indexOfParagraph > getListOfParagraphs().size()){
            this.indexOfParagraph = getListOfParagraphs().size() - 1;
        }
        else {
            this.indexOfParagraph = indexOfParagraph;
        }
    }
    public void incrementIndex(){
        // если список параграфов пустой, то индекс всегда ноль
        if (listOfParagraphs == null ) {
            indexOfParagraph = 0;
            setButtonsMode(BUTTONS_MODE.NONE);
        }
        else // иначе если еще не достигли предела списка параграфов, то увеличиваем индекс
            if (indexOfParagraph < listOfParagraphs.size() - 1) {
                indexOfParagraph++;
            }
            else {
                indexOfParagraph = listOfParagraphs.size() - 1;
            }
    }
    public void decrementIndex(){
        // если список параграфов пустой или индекс стал меньше 0, то обнуляем его
        if (listOfParagraphs == null){
            setButtonsMode(BUTTONS_MODE.NONE);
        }
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
    public ArrayList<String> getListOfRandomParagraphs() {
        return listOfRandomParagraphs;
    }
    public void setListOfRandomParagraphs(ArrayList<String> listOfParagraphs) {
        this.listOfRandomParagraphs = listOfParagraphs;
    }
    public ArrayList<String> getListOfParagraphs() {
        return listOfParagraphs;
    }
    public void setListOfParagraphs(ArrayList<String> listOfParagraphs) {
        indexOfParagraph = 0; // сбрасываем счетчик каждый раз, когда записываем новый массив параграфов
        this.listOfParagraphs = listOfParagraphs;
        if (this.listOfParagraphs == null || this.listOfParagraphs.size() == 1){
            setButtonsMode(BUTTONS_MODE.NONE);
        } else {
            setButtonsMode(BUTTONS_MODE.DEFAULT);
        }
    }
    public String getLastSearchMessage() {
        return lastSearchMessage;
    }
    // при установке нового последнего сообщения пользователя, сразу же обнволяем список параграфов для вывода
    public void setLastSearchMessageAndUpdateListOfParagraphs(String lastSearchMessage) {
        setNeedToShowToc(false);
        // если сообщение null, то пользователь послал документ или картинку
        if (lastSearchMessage == null) lastSearchMessage = "";
        // если пользователь ввел старт или помощь, то соответствующее сообщение
        if (lastSearchMessage.equals(RESERVED_ANSWER.START)){
            this.lastSearchMessage = null;
            setToc(null);
            ArrayList<String> text = new ArrayList<String>();
            text.add("Привет. Меня зовут WikiBot.\n" +
                    "Если ты хочешь найти что-то в WikiPedia или WikiQuotes, скажи мне.\n" +
                    "Я попробую найти это для тебя.\n" +
                    "Для начала, выбери режим поиска:");
            setListOfParagraphs(text);
            setButtonsMode(BUTTONS_MODE.START);

        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.INSTRUCTION)){
            this.lastSearchMessage = null;
            setToc(null);
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
                    "6.3 Также внизу будут кнопки поиска случайной статьи и смены режима поиска \n" +
                    "7. Во время просмотра статьи в вашем распоряжении будут три кнопки:\n" +
                    "7.1 ⬅️ - посмотреть предыдущий параграф статьи\n" +
                    "7.2 \uD83D\uDCD7 - вернуться к оглавлению статьи\n" +
                    "7.3 ➡️ - посмотреть следующий параграф статьи\n" +
                    "8. Вот и все, приятного пользования.");
            setListOfParagraphs(text);
            setButtonsMode(BUTTONS_MODE.INSTRUCTIONS);

        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.TOPICS)){
            setTopicsMode(true);
            this.lastSearchMessage = null;
            setToc(null);
            ArrayList<String> text = new ArrayList<String>();
            text.add("Напиши, что мне найти в Wikipedia");
            setListOfParagraphs(text);
            setButtonsMode(BUTTONS_MODE.SEARCH_MODE);
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.QUOTES)){
            setTopicsMode(false);
            this.lastSearchMessage = null;
            setToc(null);
            ArrayList<String> text = new ArrayList<String>();
            text.add("В режиме поиска цитат вы можете искать цитаты по ключевым словам.\n" +
                    "Например:\n" +
                    "- Шрек\n" +
                    "- Преступление и наказание\n" +
                    "- Счастье\n" +
                    "- Эйнштейн");
            setListOfParagraphs(text);
            setButtonsMode(BUTTONS_MODE.SEARCH_MODE);
        }
        else if (lastSearchMessage.equals(RESERVED_ANSWER.RANDOM)){
            // случай, когда пользователь нажал показать случайную статью
            this.lastSearchMessage = null;
            HttpModule httpModule = new HttpModule();

            if (isTopicsMode){
                setListOfParagraphs(httpModule.searchTopicInWikiWithToc("/random"));
            } else {
                setListOfParagraphs(httpModule.searchQuotesInWikiWithToc("/random"));
            }

            setToc(httpModule.getTocList());
            setTopic_name(httpModule.getTOPIC_NAME());
            setButtonsMode(BUTTONS_MODE.DEFAULT);

            if (httpModule.isError()){
                setButtonsMode(BUTTONS_MODE.NONE);
            }

        }
        else if (lastSearchMessage.equals("")){
            // случай, когда пользователь послал картинку или документ
            this.lastSearchMessage = null;
            setListOfParagraphs(null); // список параграфов обнуляем
            setToc(null);
            setTopic_name(null);
            setButtonsMode(BUTTONS_MODE.NONE);
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
            setNeedToShowToc(true);
        }
        else if (lastSearchMessage.equals("/image")){
            ArrayList<String> image = new ArrayList<String>();
            image.add("https://c1.staticflickr.com/7/6107/6381966401_032df5fe1e_b.jpg");
            setListOfParagraphs(image);
        }
        else if (listOfCases.contains(lastSearchMessage)){ // если страница меню совпадает с текущей менюшкой
            setIndexOfParagraph(Integer.parseInt(lastSearchMessage));
            setButtonsMode(BUTTONS_MODE.DEFAULT);
        }
        else {
            // если новое сообщение ок, то ищем по этому сообщению статью в вики
            this.lastSearchMessage = lastSearchMessage;

            // инициализируем http модуль для отпавки запросов
            HttpModule httpModule = new HttpModule();

            // ищем новые параграфы
            if (isTopicsMode){
                setListOfParagraphs(httpModule.searchTopicInWikiWithToc(this.lastSearchMessage));
            } else {
                setListOfParagraphs(httpModule.searchQuotesInWikiWithToc(this.lastSearchMessage));
            }

            // устанавливаем меню
            setToc(httpModule.getTocList());
            setTopic_name(httpModule.getTOPIC_NAME());

            if (httpModule.isError()){
                setButtonsMode(BUTTONS_MODE.NONE);
            }

        }

        // если надо показать меню, то показываем меню. иначе отправляем сообщение
        if (isNeedShowToc()){
            wikiBot.mySendTocMessage(getLastChatId(),toc,getTopic_name(),isTopicsMode);
        } else {
            wikiBot.mySendMessage(getLastChatId(),getMessageForReply(),getModeButtons());
        }


        // запуск новой нити для автоматической отправки случайной статьи по истечении таймера
        activateNewTimerThread();
    }
    public Integer getUserId() {
        return UserId;
    }




}
