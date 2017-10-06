import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        // инициализация контекста апи
        ApiContextInitializer.init();

        // создать объект взаимодействия нашего бота с апи телеграма
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // в этом блоке кода регистрируется наш вновь созданный бот
        WikiBot wikiBot = new WikiBot();
        try {
            botsApi.registerBot(wikiBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }
}