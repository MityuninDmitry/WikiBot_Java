import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

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
            if (!User.isUserOld(userId)){
                // создать нового пользователя
                User newUser = new User(userId);
                // пишем в консоль об этом
                System.out.println("new user");
                // сохранить его в список известных пользователей бота
                User.usersList.add(newUser);
                // сохранить ИД в список ИДов у класса пользователь
                User.addId(userId);

            }
        }
        else {
            // если это CallbackQuery запрос

            // получаем ИД чата и пользователя из апдейта
            chatId = update.getCallbackQuery().getMessage().getChat().getId();
            userId = update.getCallbackQuery().getFrom().getId();

            firstName = update.getCallbackQuery().getFrom().getFirstName();
            lastName = update.getCallbackQuery().getFrom().getLastName();
            userName = update.getCallbackQuery().getFrom().getUserName();


            // получаем последнее сообщение из апдейта
            messageFromLastUpdate = update.getCallbackQuery().getData();
        }
        // текущий пользователь из массива известных пользователей
        currentUser = User.getCurrentUserForWork(userId);
        // устанавливаем пользователю последний чатИД
        currentUser.setLastChatId(chatId);
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setUserName(userName);
        // передаем пользователю бота для отправки сообщений
        currentUser.setWikiBot(this);
        // устанавливаем пользователю последний искомый текст и параграфы
        currentUser.setLastSearchMessageAndUpdateListOfParagraphs(messageFromLastUpdate);

        // сохраняем пользователей в файл после каждого апдейта
        // p.s не уверен, что это разумно, т.к если будет много апдейтов, то будет большая нагрузка
        //User.saveUsers();
        User.saveUsersToDB();

    }
    public String getBotUsername() {
        return BotsSecretData.NAME_OF_BOT;
    }
    @Override
    public String getBotToken() {
        return BotsSecretData.TOKEN_OF_BOT;
    }
    // метод, который посылает сообщение в соответствующий чат с соотвтетсвующим сообщением и добавляем две кнопки
    public void mySendMessage(Long chatId, String messageForReply, String modeOfButtons){
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

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("\uD83D\uDCD6 Искать статьи");
            inlineKeyboardButton.setCallbackData(RESERVED_ANSWER.TOPICS);
            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            inlineKeyboardButton2.setText("\uD83D\uDCD4 Искать цитаты");
            inlineKeyboardButton2.setCallbackData(RESERVED_ANSWER.QUOTES);

            listNavigation.add(inlineKeyboardButton);
            listNavigation.add(inlineKeyboardButton2);

            InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
            inlineKeyboardButton3.setText("\uD83C\uDD98 Инструкция");
            inlineKeyboardButton3.setCallbackData(RESERVED_ANSWER.INSTRUCTION);

            listInstruction.add(inlineKeyboardButton3);

            lists.add(listInstruction);
            lists.add(listNavigation);


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
    public void mySendTocMessage(Long chatId, Map<String, String> toc, String topicName, boolean isTopic, String link){
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
            buttonLink.setUrl(link);
            list.add(buttonLink);
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
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
