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
        // анализируем полученный апдейт
        // смотрим есть ли текст в этом сообщении или это колбэк запрос
        // если есть текст, то это сообщение от старого или нового пользователя
        if (!update.hasCallbackQuery()){
            // получаем ИД чата и пользователя из апдейта
            chatId = update.getMessage().getChat().getId();
            userId = update.getMessage().getFrom().getId();
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
            // получаем последнее сообщение из апдейта
            messageFromLastUpdate = update.getCallbackQuery().getData();
        }
        // текущий пользователь из массива известных пользователей
        currentUser = User.getCurrentUserForWork(userId);
        // устанавливаем пользователю последний чатИД
        currentUser.setLastChatId(chatId);
        // передаем пользователю бота для отправки сообщений
        currentUser.setWikiBot(this);
        // устанавливаем пользователю последний искомый текст и параграфы
        currentUser.setLastSearchMessageAndUpdateListOfParagraphs(messageFromLastUpdate);

        // сохраняем пользователей в файл после каждого апдейта
        // p.s не уверен, что это разумно, т.к если будет много апдейтов, то будет большая нагрузка
        User.saveUsers();

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
        if (modeOfButtons.equals(BUTTONS_MODE.HELP)){
            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();

            List<InlineKeyboardButton> listMenu = new ArrayList<InlineKeyboardButton>();


            InlineKeyboardButton buttonRandom = new InlineKeyboardButton();
            buttonRandom.setText("\uD83D\uDD00 Случайная статья");
            buttonRandom.setCallbackData("/random");

            InlineKeyboardButton buttonHelp = new InlineKeyboardButton();
            buttonHelp.setText("ℹ️ Сменить режим поиска");
            buttonHelp.setCallbackData("/help");

            listMenu.add(buttonHelp);
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
            List<InlineKeyboardButton> listMenu = new ArrayList<InlineKeyboardButton>();

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("⬅️ Назад");
            inlineKeyboardButton.setCallbackData("<<--");
            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            inlineKeyboardButton2.setText("Вперед ➡️");
            inlineKeyboardButton2.setCallbackData("-->>");

            listNavigation.add(inlineKeyboardButton);
            listNavigation.add(inlineKeyboardButton2);

            InlineKeyboardButton buttonHelp = new InlineKeyboardButton();
            buttonHelp.setText("ℹ️ Сменить режим поиска");
            buttonHelp.setCallbackData("/help");

            InlineKeyboardButton buttonMenu = new InlineKeyboardButton();
            buttonMenu.setText("\uD83D\uDCD7 Оглавление");
            buttonMenu.setCallbackData("/showMenu");

            listMenu.add(buttonHelp);
            listMenu.add(buttonMenu);

            lists.add(listMenu);
            lists.add(listNavigation);


            inlineKeyboardMarkup.setKeyboard(lists);

            message.setReplyMarkup(inlineKeyboardMarkup);
        }
        else if (modeOfButtons.equals(BUTTONS_MODE.CHOOSE_SEARCH_MODE)){

            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
            List<InlineKeyboardButton> listNavigation = new ArrayList<InlineKeyboardButton>();
            List<InlineKeyboardButton> listMenu = new ArrayList<InlineKeyboardButton>();

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("\uD83D\uDCD6 Искать статьи");
            inlineKeyboardButton.setCallbackData("/topics");
            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            inlineKeyboardButton2.setText("\uD83D\uDCD4 Искать цитаты");
            inlineKeyboardButton2.setCallbackData("/quotes");

            listNavigation.add(inlineKeyboardButton);
            listNavigation.add(inlineKeyboardButton2);

            lists.add(listMenu);
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
    public void mySendTocMessage(Long chatId, Map<String, String> toc, String topicName){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(String.format("Материал из Википедии — свободной энциклопедии\n" +
                "Тема поиска: %s.\n" +
                "Выберите пункт меню для просмотра информации:",topicName.toUpperCase()));

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
