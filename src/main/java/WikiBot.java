import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.*;

public class WikiBot extends TelegramLongPollingBot {
    private List<User> usersList = new ArrayList<User>();
    public void onUpdateReceived(Update update) {
        // выводим апдейт в консоль
        System.out.println(update.toString());
        // заводим переменные чатИд и текст для ответа, индекс пользователя из списка пользователей
        Long chatId;
        int indexOfuser;
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
                usersList.add(newUser);
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


        // индекс пользователя из массива известных пользователей
        indexOfuser = searchIndexOfuserForWork(userId);
        // устанавливаем пользователю последний чатИД
        usersList.get(indexOfuser).setLastChatId(chatId);
        // устанавливаем пользователю последний искомый текст и параграфы
        usersList.get(indexOfuser).setLastSearchMessageAndUpdateListOfParagraphs(messageFromLastUpdate);
        // передаем бота юзеру для отправки сообщения
        usersList.get(indexOfuser).sendMessageBy(this);


    }
    public String getBotUsername() {
        return BotsSecretData.NAME_OF_BOT;
    }
    @Override
    public String getBotToken() {
        return BotsSecretData.TOKEN_OF_BOT;
    }
    // метод, которые ищет индекс пользователя среди известных пользователей бота
    public int searchIndexOfuserForWork(Integer userId){
        // объявляем индекс
        int index = 50;
        // идем по списку пользователей, известых боту
        for (int i = 0; i < usersList.size(); i++) {
            // если id известного пользователя совпадает с id пользователя из апдейта, то
            if (usersList.get(i).getUserId().equals(userId)){
                // сохраняем индекс
                index = i;
                break;
            }
        }
        // возвращаем индекс
        return index;
    }
    // метод, который посылает сообщение в соответствующий чат с соотвтетсвующим сообщением и добавляем две кнопки
    public void mySendMessage(Long chatId, String messageForReply, boolean isButtonNeed){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageForReply);
        if (isButtonNeed){
            // создаем клавиатуру из двух кнопок "назад" и "вперед"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> lists = new ArrayList<List<InlineKeyboardButton>>();
            List<InlineKeyboardButton> list = new ArrayList<InlineKeyboardButton>();

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("<<--");
            inlineKeyboardButton.setCallbackData("<<--");
            InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
            inlineKeyboardButton2.setText("-->>");
            inlineKeyboardButton2.setCallbackData("-->>");

            list.add(inlineKeyboardButton);
            list.add(inlineKeyboardButton2);
            lists.add(list);
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
}
