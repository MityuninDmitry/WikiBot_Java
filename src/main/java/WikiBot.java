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
        String textForReply;
        int indexOfuser;
        // анализируем полученный апдейт
        // смотрим есть ли текст в этом сообщении или это колбэк запрос
        // если есть текст, то это сообщение от старого или нового пользователя
        if (!update.hasCallbackQuery()){
            // инициализируем переменные, объявленные раннее
            chatId = update.getMessage().getChat().getId();
            String messageFromLastUpdate = update.getMessage().getText();
            // проверить новый это или старый пользователь
            // получаем ИД пользователя из апдейта
            Integer userId = update.getMessage().getFrom().getId();
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
            // индекс пользователя из массива известных пользователей
            indexOfuser = searchIndexOfuserForWork(userId);
            // если в обновлении есть документ или фото, то сохраняем соответствующее сообщение для пользователя
            if (update.getMessage().hasDocument() || update.getMessage().hasPhoto()){
                // устанавливаем последнее сообщение пользователя null
                usersList.get(indexOfuser).setLastSearchMessage(null);
                // текст статьи тоже null
                usersList.get(indexOfuser).setListOfParagraphs(null);
            }
            // иначе
            else {
                // устанавливаем последнее сообщение пользователя
                usersList.get(indexOfuser).setLastSearchMessage(messageFromLastUpdate);
                // ищем в википедии текст статьи и сохраняем в лист параграфоф пользователя
                usersList.get(indexOfuser).setListOfParagraphs(HttpModule.searchTextInWiki(messageFromLastUpdate));
            }
        }
        else {
            // если апдейт это inlineQuery
            // инициализируем ранее объявленные переменные
            chatId = update.getCallbackQuery().getMessage().getChat().getId();
            Integer userId = update.getCallbackQuery().getFrom().getId();
            // если это inlineQuery, то пользователь уже известен. поэтому ищем его по id
            indexOfuser = searchIndexOfuserForWork(userId);
            // в зависимости от того, какую кнопку нажал пользователь мы увеличиваем или уменьшаем счетчик страницы
            if (update.getCallbackQuery().getData().equals("-->>")){
                usersList.get(indexOfuser).incrementIndex();
            }
            else if (update.getCallbackQuery().getData().equals("<<--")){
                usersList.get(indexOfuser).decrementIndex();
            }
        }
        // собираем текст для отправки
        textForReply = usersList.get(indexOfuser).getMessageForReply();
        // посылаем сообщение
        mySendMessage(chatId,textForReply);
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
        int index = 0;
        // идем по списку пользователей, известых боту
        for (int i = 0; i < usersList.size(); i++) {
            // если id известного пользователя совпадает с id пользователя из апдейта, то
            if (userId == usersList.get(i).getUserId()){
                // сохраняем индекс
                index = i;
            }
        }
        // возвращаем индекс
        return index;
    }
    // метод, который посылает сообщение в соответствующий чат с соотвтетсвующим сообщением и добавляем две кнопки
    public void mySendMessage(Long chatId, String messageForReply){

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageForReply);

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

        // посылаем сообщение
        try {
            sendApiMethod(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
