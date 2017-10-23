import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.*;

public class WikiBot extends TelegramLongPollingBot {

    public void onUpdateReceived(Update update) {
        // выводим апдейт в консоль
        System.out.println(update.toString());
        // заводим переменные чатИд и текст для ответа, индекс пользователя из списка пользователей
        Long chatId;
        User currentUser;
        Integer userId;
        String messageFromLastUpdate;
        String firstName;
        String lastName;
        String userName;
        // анализируем полученный апдейт
        // смотрим есть ли текст в этом сообщении или это колбэк запрос
        // если есть текст, то это сообщение от старого или нового пользователя
        if (!update.hasCallbackQuery()){
            // получаем ИД чата и пользователя из апдейта
            chatId = update.getMessage().getChat().getId();
            userId = update.getMessage().getFrom().getId();

            firstName = update.getMessage().getFrom().getFirstName();
            lastName = update.getMessage().getFrom().getLastName();
            userName = update.getMessage().getFrom().getUserName();

            // получаем последнее сообщение из апдейта
            messageFromLastUpdate = update.getMessage().getText();
            // смотрим изветнсый это пользователь или нет
        }
        else {
            // если это CallbackQuery запрос
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery().setText("Идет загрузка данных... \uD83D\uDC38");
            answerCallbackQuery.setShowAlert(false);
            answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            try {
                sendApiMethod(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            // получаем ИД чата и пользователя из апдейта
            chatId = update.getCallbackQuery().getMessage().getChat().getId();
            userId = update.getCallbackQuery().getFrom().getId();

            firstName = update.getCallbackQuery().getFrom().getFirstName();
            lastName = update.getCallbackQuery().getFrom().getLastName();
            userName = update.getCallbackQuery().getFrom().getUserName();

            // получаем последнее сообщение из апдейта
            messageFromLastUpdate = update.getCallbackQuery().getData();
        }
        // создаем ДБ
        Db.createNewDatabase("Users.db");
        // создаем табличку UserTable
        Db.createNewTable();
        // коннектимся к ней
        Db.connect();
        // Если пользователь есть в БД с таким userId, то выгружаем в него данные
        if (Db.isUserExist(userId.toString())){
            currentUser = Db.loadUser(userId.toString());
        }
        // иначе создаем нового пользователя
        else {
            currentUser = new User(userId);
            // пишем в консоль об этом
            System.out.println("new user");
        }
        // закрываем соединение к БД
        Db.close();

        // если нет, то создаем нового пользователя
        // текущий пользователь из массива известных пользователей
        //currentUser = User.getCurrentUserForWork(userId);
        // устанавливаем пользователю последний чатИД
        currentUser.setLastChatId(chatId);
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setUserName(userName);
        // передаем пользователю бота для отправки сообщений
        currentUser.setWikiBot(this);
        // устанавливаем пользователю последний искомый текст и параграфы
        currentUser.parseLastMessageAndSendReply(messageFromLastUpdate);

        // сохраняем пользователей в файл после каждого апдейта
        // p.s не уверен, что это разумно, т.к если будет много апдейтов, то будет большая нагрузка
        //User.saveUsers();
        //User.saveUsersToDB();
        //currentUser.saveUserToDB();
        currentUser.saveUserToDBv2();

    }
    public String getBotUsername() {
        return BotsSecretData.NAME_OF_BOT;
    }
    @Override
    public String getBotToken() {
        return BotsSecretData.TOKEN_OF_BOT;
    }
    // метод, который посылает сообщение в соответствующий чат с соотвтетсвующим сообщением и добавляем две кнопки
    public void mySendMessage(Long chatId, String messageForReply, String modeOfButtons, ArrayList<String> similarTopics){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageForReply);

        if (modeOfButtons.equals(BUTTONS_MODE.SEARCH_MODE)){
            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();

            List<InlineKeyboardButton> listMenu = new ArrayList<InlineKeyboardButton>();


            InlineKeyboardButton buttonRandom = new InlineKeyboardButton();
            buttonRandom.setText("\uD83D\uDD00 Случайная статья");
            buttonRandom.setCallbackData(RESERVED_ANSWER.RANDOM);

            InlineKeyboardButton buttonHelp = new InlineKeyboardButton();
            buttonHelp.setText("ℹ️ Сменить режим поиска");
            buttonHelp.setCallbackData(RESERVED_ANSWER.START);

            listMenu.add(buttonHelp);
            listMenu.add(buttonRandom);

            lists.add(listMenu);

            inlineKeyboardMarkup.setKeyboard(lists);

            message.setReplyMarkup(inlineKeyboardMarkup);

        }
        else if (modeOfButtons.equals(BUTTONS_MODE.INSTRUCTIONS)){
            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();

            List<InlineKeyboardButton> listMenu = new ArrayList<InlineKeyboardButton>();

            InlineKeyboardButton buttonRandom = new InlineKeyboardButton();
            buttonRandom.setText("\uD83C\uDFC1 Начать пользоваться ботом");
            buttonRandom.setCallbackData(RESERVED_ANSWER.START);

            listMenu.add(buttonRandom);

            lists.add(listMenu);

            inlineKeyboardMarkup.setKeyboard(lists);

            message.setReplyMarkup(inlineKeyboardMarkup);
        }
        else if (modeOfButtons.equals(BUTTONS_MODE.DEFAULT)){

            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
            List<InlineKeyboardButton> listNavigation = new ArrayList<InlineKeyboardButton>();

            InlineKeyboardButton buttonBack = new InlineKeyboardButton();
            buttonBack.setText("⬅️");
            buttonBack.setCallbackData(RESERVED_ANSWER.PREV);
            InlineKeyboardButton buttonNext = new InlineKeyboardButton();
            buttonNext.setText("➡️");
            buttonNext.setCallbackData(RESERVED_ANSWER.NEXT);

            InlineKeyboardButton buttonMenu = new InlineKeyboardButton();
            buttonMenu.setText("\uD83D\uDCD7");
            buttonMenu.setCallbackData(RESERVED_ANSWER.SHOW_MENU);

            listNavigation.add(buttonBack);
            listNavigation.add(buttonMenu);
            listNavigation.add(buttonNext);

            lists.add(listNavigation);


            inlineKeyboardMarkup.setKeyboard(lists);

            message.setReplyMarkup(inlineKeyboardMarkup);
        }
        else if (modeOfButtons.equals(BUTTONS_MODE.START)){

            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
            List<InlineKeyboardButton> listNavigation = new ArrayList<InlineKeyboardButton>();
            List<InlineKeyboardButton> listInstruction = new ArrayList<InlineKeyboardButton>();
            List<InlineKeyboardButton> listStars = new ArrayList<InlineKeyboardButton>();

            InlineKeyboardButton buttonTopic = new InlineKeyboardButton();
            buttonTopic.setText("\uD83D\uDCD6 Искать статьи");
            buttonTopic.setCallbackData(RESERVED_ANSWER.TOPICS);

            InlineKeyboardButton buttonQuotes = new InlineKeyboardButton();
            buttonQuotes.setText("\uD83D\uDCD4 Искать цитаты");
            buttonQuotes.setCallbackData(RESERVED_ANSWER.QUOTES);

            listNavigation.add(buttonTopic);
            listNavigation.add(buttonQuotes);

            InlineKeyboardButton buttonInstruction = new InlineKeyboardButton();
            buttonInstruction.setText("\uD83C\uDD98 Инструкция");
            buttonInstruction.setCallbackData(RESERVED_ANSWER.INSTRUCTION);

            InlineKeyboardButton buttonStars = new InlineKeyboardButton();
            buttonStars.setText("⭐ ⭐ ⭐ ⭐ ⭐");
            buttonStars.setUrl("https://telegram.me/storebot?start=mity_wiki_bot");
            listStars.add(buttonStars);

            listInstruction.add(buttonInstruction);

            lists.add(listNavigation);
            lists.add(listInstruction);
            lists.add(listStars);

            inlineKeyboardMarkup.setKeyboard(lists);

            message.setReplyMarkup(inlineKeyboardMarkup);
        }
        else if (modeOfButtons.equals(BUTTONS_MODE.SIMILAR_TOPICS)){
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
            List<InlineKeyboardButton> list;
            for (String topicName: similarTopics){

                list = new ArrayList<InlineKeyboardButton>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(topicName);

                String topic = topicName;
                topic = topic.replaceAll("—", "");
                if (topic.length() > 30){
                    topic = topic.substring(0,30);
                }
                inlineKeyboardButton.setCallbackData(topic);
                list.add(inlineKeyboardButton);
                lists.add(list);
            }
            inlineKeyboardMarkup.setKeyboard(lists);

            message.setReplyMarkup(inlineKeyboardMarkup);

        }

        // посылаем сообщение
        try {
            sendApiMethod(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    // метод посылает меню пользователю
    public void mySendTocMessage(Long chatId, Map<String, String> toc, String topicName, boolean isTopic, String link, boolean needButtonSimilarTopic){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        String modeText;
        if (isTopic){
            modeText = "Материал из Википедии — свободной энциклопедии.";
        } else {
            modeText = "Материал из Викицитатника.";
        }
        message.setText(String.format("%s\n" +
                "Тема поиска: %s.\n" +
                "Выберите пункт меню для просмотра информации:",modeText,topicName.toUpperCase()));

        // создаем клавиатуру из содержания
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
        List<InlineKeyboardButton> list;
        for (Map.Entry<String,String> toc_item: toc.entrySet()){
            list = new ArrayList<InlineKeyboardButton>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(toc_item.getKey());
            inlineKeyboardButton.setCallbackData(toc_item.getValue());
            list.add(inlineKeyboardButton);
            lists.add(list);
        }
        if (!link.equals("")){
            list = new ArrayList<InlineKeyboardButton>();
            InlineKeyboardButton buttonLink = new InlineKeyboardButton();
            buttonLink.setText("\uD83C\uDF10 Открыть в браузере");
            link = link.replaceAll("—", "-");
            //link = link.replaceAll("’", "'");
            buttonLink.setUrl(link);
            list.add(buttonLink);
            lists.add(list);

        }
        if (needButtonSimilarTopic){
            list = new ArrayList<InlineKeyboardButton>();
            InlineKeyboardButton buttonSimilarTopics = new InlineKeyboardButton();
            buttonSimilarTopics.setText("\uD83D\uDD0D Похожие статьи");
            buttonSimilarTopics.setCallbackData(RESERVED_ANSWER.SIMILAR_TOPICS);
            list.add(buttonSimilarTopics);
            lists.add(list);
        }


        list = new ArrayList<InlineKeyboardButton>();
        InlineKeyboardButton buttonHelp = new InlineKeyboardButton();
        buttonHelp.setText("ℹ️ Сменить режим поиска");
        buttonHelp.setCallbackData(RESERVED_ANSWER.START);
        list.add(buttonHelp);
        lists.add(list);

        list = new ArrayList<InlineKeyboardButton>();
        InlineKeyboardButton buttonRandom = new InlineKeyboardButton();
        buttonRandom.setText("\uD83D\uDD00 Случайная статья");
        buttonRandom.setCallbackData(RESERVED_ANSWER.RANDOM);

        list.add(buttonRandom);
        lists.add(list);

        inlineKeyboardMarkup.setKeyboard(lists);

        message.setReplyMarkup(inlineKeyboardMarkup);

        // посылаем сообщение
        try {
            sendApiMethod(message);
        }
        catch (TelegramApiRequestException e) {
            // если по каким-то причинам урл невалидный, и из-за этого не может отправиться пользователю сообщение
            // то посылать ему сообщение без урла
            // Пример: из-за этого символа ’, который нельзя заменить на ', т.к ссылка станет другой и скорее всего 404
            if (e.getApiResponse().contains("Bad Request: BUTTON_URL_INVALID")){
                mySendTocMessage(chatId, toc, topicName,isTopic, "", needButtonSimilarTopic);
                System.out.println("ОШИБКА: " + e.getApiResponse() + "\nПОЛЬЗОВАТЕЛЬ с chatId = " + chatId + "\nURL: " + link);
            } else {
                e.printStackTrace();
            }

        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
