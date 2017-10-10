public class AutoSendThread implements Runnable {
    public Thread t; // нить
    private User user; // пользователь
    public AutoSendThread(User user){
        t = new Thread(this, user.getUserId().toString()); // создаем новую нить
        this.user = user; // инициализируем пользователя
        t.start(); // запускаем нить
    }
    public void run() {
        // таймер
        try {
            System.out.println(String.format("Timer thread %s was started",Thread.currentThread().getName())); // выводим в консоль
            // нить спит 12 часов
            int hours = 12;
            int minutes = 60;
            int seconds = 60;
            Thread.sleep( hours * minutes * seconds * 1000);
            // ищем случаную статью и устанавливаем ее пользователю
            user.setListOfRandomParagraphs(HttpModule.searchRandomTopicInWiki());
            // посылаем сообщение пользователю
            if (user.getListOfRandomParagraphs() != null && user.getListOfRandomParagraphs().size() > 1){
                String textForReply = user.getListOfRandomParagraphs().get(0) +
                        user.getListOfRandomParagraphs().get(1);
                user.getWikiBot().mySendMessage(user.getLastChatId(),textForReply,false);
            }
            System.out.println(String.format("Timer thread %s was ended",Thread.currentThread().getName())); // выводим в консоль
        } catch (InterruptedException e) {
            System.out.println(String.format("Timer thread %s was interrupted",Thread.currentThread().getName())); // выводим в консоль
        }
    }
}
