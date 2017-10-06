import java.util.ArrayList;

public class User {
    private static ArrayList<Integer> usersIdsList = new ArrayList<Integer>(); // список известных ИД пользователей
    private Integer UserId; // ИД пользователя
    private String lastSearchMessage; // последнее сообщение, которое он искал
    private int indexOfParagraph = 0; // номер параграфа
    private ArrayList<String> listOfParagraphs; // список параграфов
    // метод проверяет известный это пользователь или нет
    public static boolean isUserOld(Integer userId){
        if (usersIdsList.contains(userId)){
            return true;
        }
        else return false;
    }
    // добавить ИД в список известных пользователей
    public static void addId(Integer id){
        usersIdsList.add(id);
    }
    public int getIndexOfParagraph(){
        return indexOfParagraph;
    }
    public void incrementIndex(){
        if (listOfParagraphs == null ) indexOfParagraph = 0;
        else
            if (indexOfParagraph < listOfParagraphs.size() - 1) {
                indexOfParagraph++;
            }
    }
    public void decrementIndex(){
        if (listOfParagraphs == null || indexOfParagraph <= 0) indexOfParagraph = 0;
        else
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
    public ArrayList<String> getListOfParagraphs() {
        return listOfParagraphs;
    }
    public void setListOfParagraphs(ArrayList<String> listOfParagraphs) {
        indexOfParagraph = 0; // сбрасываем счетчик каждый раз, когда записываем новый массив параграфов
        this.listOfParagraphs = listOfParagraphs;
    }
    public User(Integer userId) {
        UserId = userId;
    }
    public String getLastSearchMessage() {
        return lastSearchMessage;
    }
    public void setLastSearchMessage(String lastSearchMessage) {
        this.lastSearchMessage = lastSearchMessage;
    }
    public Integer getUserId() {
        return UserId;
    }




}
