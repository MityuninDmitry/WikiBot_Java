import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Db {
    private static String fileNameDb;
    private static String path = "C:/Users/Ирина/IdeaProjects/WikiBot/";
    private static String table_Users = "Users";
    private static String table_UserSettings = "UserSettings";
    private static Connection conn = null;
    // запрос на создание таблицы Users с данными пользователей
    private static String CREATE_USERS_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + table_Users + " (\n"
            + "	id integer PRIMARY KEY,\n"
            + "	UserId text ,\n"
            + "	firstName text ,\n"
            + "	lastName text ,\n"
            + "	userName text \n"
            + ");";
    /** запрос на создание таблицы UsersSettings с данными о настройках пользователей
     *  Поле UsersSettings.id_in_table_Users ссылается на поле Users.id */
    private static String CREATE_USER_SETTINGS_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + table_UserSettings + " (\n"
            + "	id integer PRIMARY KEY,\n"
            + "	id_in_table_Users integer ,\n"
            + "	lastChatId text ,\n"
            + "	lastSearchMessage text ,\n"
            + "	indexOfParagraph text ,\n"
            + "	listOfParagraphs text ,\n"
            + "	modeOfButtons text ,\n"
            + "	toc text ,\n"
            + "	listOfCases ,\n"
            + "	topic_name text ,\n"
            + "	isNeedShowToc text ,\n"
            + "	isTopicsMode text, \n"
            + "	firstName text ,\n"
            + "	lastName text ,\n"
            + "	userName text ,\n" +
            "FOREIGN KEY (id_in_table_Users) REFERENCES Users(id)"
            + ");";
    /** Запрос на вставку новых данных в таблицу Users
     * */
    private static String INSERT_SQL_STATEMENT_INTO_USERS = "INSERT INTO " + table_Users + "(" +
            "UserId," +
            "firstName," +
            "lastName," +
            "userName) VALUES(?,?,?,?)";
    /** Запрос на вставку новых данных в таблицу UserSettings
     * При этом данные поля UserSettings.id_in_table_Users заполняются данными из соответствующей записи
     * в поля Users.UserId
     * */
    private static String INSERT_SQL_STATEMENT_INTO_USERS_SETTINGS = "INSERT INTO " + table_UserSettings + "(" +
            "id_in_table_Users," +
            "lastChatId," +
            "lastSearchMessage," +
            "indexOfParagraph," +
            "listOfParagraphs," +
            "modeOfButtons," +
            "toc," +
            "listOfCases," +
            "topic_name," +
            "isNeedShowToc," +
            "isTopicsMode)" +
            "VALUES((SELECT Users.id FROM Users WHERE Users.UserId = ?),?,?,?,?,?,?,?,?,?,?) ";
    /** Обновление таблицы Users */
    private static String UPDATE_SQL_STATEMENT_IN_USERS = "UPDATE " + table_Users + " "
            + "SET UserId = ? , " +
            "firstName = ? , " +
            "lastName = ?, " +
            "userName = ?" +
            "WHERE id = ?";
    /** Обновление таблицы UserSettings */
    private static String UPDATE_SQL_STATEMENT_IN_USER_SETTINGS = "UPDATE " + table_UserSettings + " "
            + "SET " +
            "lastChatId = ? , " +
            "lastSearchMessage = ? , " +
            "indexOfParagraph = ? , " +
            "listOfParagraphs = ?, " +
            "modeOfButtons = ?, " +
            "toc = ? , " +
            "listOfCases = ?,  " +
            "topic_name = ? , " +
            "isNeedShowToc = ?, " +
            "isTopicsMode = ?" +
            "WHERE UserSettings.id_in_table_Users = ?";
    // ID пользователей
    private static ArrayList<Integer> loadedUsersIdsList = new ArrayList<Integer>();
    // Возвращаем список ID пользователей
    public static ArrayList<Integer> getLoadedUsersIdsList() {
        return loadedUsersIdsList;
    }
    public static void NotMain() {
        // создаем БД
        createNewDatabase("testUser.db");
        // создаем табличку User
        createNewTable();
        // коннектимся к ней
        connect();
        // заполняем табличку

        // обновляем пользователя

        System.out.println(isUserExist(5));

        // закрываем соединение
        close();

    }
    // Загружаем данные из таблицы и преобразуем в нужный формат
    public static ArrayList<String> loadListOfParagraphs(String stringList){
        ArrayList<String> result = new ArrayList<String>();
        String[] mas = stringList.split("parse");
        for (String part: mas){
            result.add(part);
        }
        return result;
    }
    public static Map<String, String> loadToc(String stringToc){
        Map<String, String> tocList = new LinkedHashMap<String, String>();
        String[] mas = stringToc.split("parse2");
        for (String pos: mas){
            String[] innerMas = pos.split("parse1");
            tocList.put(innerMas[0],innerMas[1]);
        }

        return tocList;
    }
    public static ArrayList<String> loadListOfCases(String stringList){
        ArrayList<String> result = new ArrayList<String>();
        String[] mas = stringList.split("parse");
        for (String part: mas){
            result.add(part);
        }
        return result;
    }
    // загружаем пользователей из базы данных.
    public static ArrayList<User> loadUsers(){
        ArrayList<User> users = new ArrayList<User>();
        // запрос в БД для получения сразу всех полей
        String sql = "SELECT * " +
                "FROM Users, UserSettings " +
                "WHERE Users.id = UserSettings.id_in_table_Users";
        try {
            // выполняем запрос
            Statement stmt  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // идем по записям полученной таблицы
            while (rs.next()){
                // создаем нового пользователя
                User newUser = new User( Integer.parseInt(rs.getString("UserId")));
                // заполняем данные пользователя
                newUser.setFirstName(rs.getString("firstName"));
                newUser.setLastName(rs.getString("lastName"));
                newUser.setUserName(rs.getString("userName"));

                newUser.setLastChatId(Long.parseLong(rs.getString("lastChatId")));
                newUser.setListOfParagraphs(loadListOfParagraphs(rs.getString("listOfParagraphs")));
                newUser.setIndexOfParagraph(Integer.parseInt(rs.getString("indexOfParagraph")));

                newUser.setButtonsMode(rs.getString("modeOfButtons"));
                newUser.setToc(loadToc(rs.getString("toc")));
                newUser.setListOfCases(loadListOfCases(rs.getString("listOfCases")));
                newUser.setTopic_name(rs.getString("topic_name"));
                newUser.setNeedToShowToc(Boolean.parseBoolean(rs.getString("isNeedShowToc")));
                newUser.setTopicsMode(Boolean.parseBoolean(rs.getString("isTopicsMode")));
                // добавляем в список ID пользователя
                loadedUsersIdsList.add(newUser.getUserId());
                // добавляем пользователя в список пользователей
                users.add(newUser);
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        // возвращаем пользователей
        return users;
    }
    // проверка на то, есть ли в БД пользователь или нет
    public static boolean isUserExist(int id){
        boolean isExist = false;
        String sql = "SELECT id " +
                "FROM " + table_Users + " " +
                "WHERE id = ?";
        try {
            PreparedStatement stmt  = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            //System.out.println(rs.next());

            isExist = rs.next();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return isExist;
    }
    // обновляем данные пользователя в двух таблицах
    public static void updateUser(int id, User user) {
        try {
            // обновление в таблице Users
            PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL_STATEMENT_IN_USERS);
            // список полей для обновления
            pstmt.setString(1, user.getUserId()+"");
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getLastName());
            pstmt.setString(4, user.getUserName());
            // по id понимаем, какую запись обновлять
            pstmt.setInt(5, id);
            // выполняем запрос
            pstmt.executeUpdate();
            // обновление в таблице UserSettings
            pstmt = conn.prepareStatement(UPDATE_SQL_STATEMENT_IN_USER_SETTINGS);
            // список полей для обновления
            pstmt.setString(1, user.getLastChatId()+"");
            pstmt.setString(2, user.getLastSearchMessage());
            pstmt.setString(3, user.getIndexOfParagraph() + "");
            pstmt.setString(4, getPreparedDataListOfParagraphs(user));
            pstmt.setString(5, user.getModeButtons());
            pstmt.setString(6, getPreparedDataToc(user));
            pstmt.setString(7, getPreparedDataListOfCases(user));
            pstmt.setString(8, user.getTopic_name());
            pstmt.setString(9, user.isNeedShowToc()+"");
            pstmt.setString(10, user.getTopicMode() + "");
            // по id понимаем, какую запись обновлять
            pstmt.setInt(11, id);
            // выполняем апдейт
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //System.out.println("=== User was updated ===");
    }
    // готовим данные к записи в БД - преобразуем их в строки
    public static String getPreparedDataListOfParagraphs(User user){
        // преобразуем данные в строку
        // чтобы декодировать данные, надо их считать и разделить по слову parse
        if (user.getListOfParagraphs() == null){
            return "";
        }
        StringBuilder listOfParargraphsString = new StringBuilder();
        for (String paragraph: user.getListOfParagraphs()){
            listOfParargraphsString.append(paragraph + "parse");
        }
        return listOfParargraphsString.toString();
    }
    public static String getPreparedDataToc(User user){
        // чтобы распарсить, надо поделить на parse2, так получим список.
        // потом поделить по parse1
        if (user.getToc() == null){
            return "";
        }
        StringBuilder tocString = new StringBuilder();
        for (Map.Entry<String, String> pair: user.getToc().entrySet()){
            tocString.append(pair.getKey() + "parse1" + pair.getValue() + "parse2");
        }
        return tocString.toString();
    }
    public static String getPreparedDataListOfCases(User user){
        if (user.getListOfCases() == null){
            return "";
        }
        // преобразуем данные в строку
        // чтобы декодировать данные, надо их считать и разделить по слову parse
        StringBuilder listOfCasesString = new StringBuilder();
        for (String paragraph: user.getListOfCases()){
            listOfCasesString.append(paragraph + "parse");
        }
        return listOfCasesString.toString();
    }
    // вставляем нового пользователя в БД
    public static void insertNewUser(User user){
        try {
            // готовим запрос
            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL_STATEMENT_INTO_USERS);
            // данные для вставки
            pstmt.setString(1, user.getUserId()+"");
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getLastName());
            pstmt.setString(4, user.getUserName());
            // выполняем запрос
            pstmt.executeUpdate();

            // аналогично готовим запрос
            pstmt = conn.prepareStatement(INSERT_SQL_STATEMENT_INTO_USERS_SETTINGS);
            // данные для вставки
            pstmt.setString(1, user.getUserId() + "");
            pstmt.setString(2, user.getLastChatId()+"");
            pstmt.setString(3, user.getLastSearchMessage());
            pstmt.setString(4, user.getIndexOfParagraph() + "");
            pstmt.setString(5, getPreparedDataListOfParagraphs(user));
            pstmt.setString(6, user.getModeButtons());
            pstmt.setString(7, getPreparedDataToc(user));
            pstmt.setString(8, getPreparedDataListOfCases(user));
            pstmt.setString(9, user.getTopic_name());
            pstmt.setString(10, user.isNeedShowToc()+"");
            pstmt.setString(11, user.getTopicMode() + "");
            // выполняем запрос
            pstmt.executeUpdate();
            //System.out.println("=== User was inserted ===");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    public static void close(){
        if (conn != null) {
            try {
                conn.close();
                //System.out.println("=== Connection to database was closed ===");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:" + path + fileNameDb;

        try {
            conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            // create a new tables
            stmt.execute(CREATE_USERS_TABLE_STATEMENT);
            stmt.execute(CREATE_USER_SETTINGS_TABLE_STATEMENT);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //System.out.println("=== Table was created ===");
    }
    /**
     * Connect to a sample database
     */
    public static void connect() {

        try {
            // db parameters
            String url = "jdbc:sqlite:" + path +  fileNameDb;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            //System.out.println("=== Connection was established. ===");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
    public static void createNewDatabase(String fileName) {
        fileNameDb = fileName;
        String url = "jdbc:sqlite:" + path + fileNameDb;
        try{
            conn = DriverManager.getConnection(url);
            //System.out.println("=== DataBase was created === ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}