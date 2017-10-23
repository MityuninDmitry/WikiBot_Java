import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Db {
    private static String fileNameDb;
    private static String path = "C:\\Users\\Ирина\\IdeaProjects\\WikiBot\\";
    private static String table_Users = "Users";
    private static String table_UserStates = "UserStates";
    private static Connection conn = null;
    // запрос на создание таблицы Users с данными пользователей
    private static String CREATE_USERS_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + table_Users + " (\n"
            + "	id integer PRIMARY KEY,\n"
            + "	UserId text ,\n"
            + "	firstName text ,\n"
            + "	lastName text ,\n"
            + "	userName text ,\n"
            + "	lastChatId text \n"
            + ");";
    /** запрос на создание таблицы UsersSettings с данными о настройках пользователей
     *  Поле UsersSettings.id_in_table_Users ссылается на поле Users.id */
    private static String CREATE_USER_SETTINGS_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + table_UserStates + " (\n"
            + "	id integer PRIMARY KEY,\n"
            + "	id_in_table_Users integer ,\n"
            + "	lastSearchMessage text ,\n"
            + "	indexOfParagraph text ,\n"
            + "	listOfParagraphs text ,\n"
            + "	modeOfButtons text ,\n"
            + "	toc text ,\n"
            + "	listOfCases ,\n"
            + "	topic_name text ,\n"
            + "	isNeedShowToc text ,\n"
            + "	isTopicsMode text, \n"
            + "	lastWebLink text ,\n"
            + "	similarTopics text ,\n"
            + "	lastDateUpdate text ,\n"
            + "FOREIGN KEY (id_in_table_Users) REFERENCES Users(id)"
            + ");";
    /** Запрос на вставку новых данных в таблицу Users
     * */
    private static String INSERT_SQL_STATEMENT_INTO_USERS = "INSERT INTO " + table_Users + "(" +
            "UserId," +
            "firstName," +
            "lastName," +
            "userName," +
            "lastChatId) VALUES(?,?,?,?,?)";
    /** Запрос на вставку новых данных в таблицу UserSettings
     * При этом данные поля UserSettings.id_in_table_Users заполняются данными из соответствующей записи
     * в поля Users.UserId
     * */
    private static String INSERT_SQL_STATEMENT_INTO_USERS_SETTINGS = "INSERT INTO " + table_UserStates + "(" +
            "id_in_table_Users," +
            "lastSearchMessage," +
            "indexOfParagraph," +
            "listOfParagraphs," +
            "modeOfButtons," +
            "toc," +
            "listOfCases," +
            "topic_name," +
            "isNeedShowToc," +
            "isTopicsMode," +
            "lastWebLink," +
            "similarTopics," +
            "lastDateUpdate)" +
            "VALUES((SELECT Users.id FROM Users WHERE Users.UserId = ?),?,?,?,?,?,?,?,?,?,?,?,?) ";
    /** Обновление таблицы Users */
    private static String UPDATE_SQL_STATEMENT_IN_USERS = "UPDATE " + table_Users + " "
            + "SET UserId = ? , " +
            "firstName = ? , " +
            "lastName = ?, " +
            "userName = ?," +
            "lastChatId = ?" +
            "WHERE id = ?";
    /** Обновление таблицы UserSettings */
    private static String UPDATE_SQL_STATEMENT_IN_USER_SETTINGS = "UPDATE " + table_UserStates + " "
            + "SET " +
            "lastSearchMessage = ? , " +
            "indexOfParagraph = ? , " +
            "listOfParagraphs = ?, " +
            "modeOfButtons = ?, " +
            "toc = ? , " +
            "listOfCases = ?,  " +
            "topic_name = ? , " +
            "isNeedShowToc = ?, " +
            "isTopicsMode = ?," +
            "lastWebLink = ?," +
            "similarTopics = ?," +
            "lastDateUpdate = ?" +
            "WHERE UserStates.id_in_table_Users = ?";
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
    public static ArrayList<String> loadSimilarTopics(String stringList){
        ArrayList<String> result = new ArrayList<String>();
        if (stringList.equals("")) return result;
        String[] mas = stringList.split("parse");
        for (String part: mas){
            result.add(part);
        }
        return result;
    }
    public static ArrayList<String> loadListOfParagraphs(String stringList){
        ArrayList<String> result = new ArrayList<String>();
        if (stringList.equals("")) return result;
        String[] mas = stringList.split("parse");
        for (String part: mas){
            result.add(part);
        }
        return result;
    }
    public static Map<String, String> loadToc(String stringToc){
        Map<String, String> tocList = new LinkedHashMap<String, String>();
        if (stringToc.equals("")) return tocList;
        String[] mas = stringToc.split("parse2");
        for (String pos: mas){
            String[] innerMas = pos.split("parse1");
            tocList.put(innerMas[0],innerMas[1]);
        }

        return tocList;
    }
    public static ArrayList<String> loadListOfCases(String stringList){
        ArrayList<String> result = new ArrayList<String>();
        if (stringList.equals("")) return result;
        String[] mas = stringList.split("parse");
        for (String part: mas){
            result.add(part);
        }
        return result;
    }
    // загружаем пользователей из базы данных.
    public static Integer countOfUsers(){
        connect();
        String sql = "SELECT id " +
                "FROM " + table_Users;
        int index = 1;
        try {

            PreparedStatement stmt  = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            //System.out.println(rs.next());
            while (rs.next()){
                index = rs.getInt("id");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        close();
        return index;
    }
    public static User loadUser(String UserId){
        User newUser = null;
        // запрос в БД для получения сразу всех полей
        String sql = "SELECT * " +
                "FROM Users, UserStates " +
                "WHERE Users.id = UserStates.id_in_table_Users and Users.UserId = " + UserId;
        try {
            // выполняем запрос
            Statement stmt  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // идем по записям полученной таблицы

            // создаем нового пользователя
            newUser = new User( Integer.parseInt(rs.getString("UserId")));
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
            newUser.setTopicMode(Boolean.parseBoolean(rs.getString("isTopicsMode")));
            newUser.setLastWebLink(rs.getString("lastWebLink"));

            newUser.setSimilarTopics(loadSimilarTopics(rs.getString("similarTopics")));

            newUser.setLastDateUpdate(rs.getString("lastDateUpdate"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        // возвращаем пользователей
        return newUser;
    }
    public static ArrayList<User> loadUsers(){
        ArrayList<User> users = new ArrayList<User>();
        // запрос в БД для получения сразу всех полей
        String sql = "SELECT * " +
                "FROM Users, UserStates " +
                "WHERE Users.id = UserStates.id_in_table_Users";
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
                newUser.setTopicMode(Boolean.parseBoolean(rs.getString("isTopicsMode")));
                newUser.setLastWebLink(rs.getString("lastWebLink"));

                newUser.setSimilarTopics(loadSimilarTopics(rs.getString("similarTopics")));

                newUser.setLastDateUpdate(rs.getString("lastDateUpdate"));
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
    public static Integer getUserIndex(String UserId){
        String sql = "SELECT id " +
                "FROM " + table_Users + " " +
                "WHERE UserId = ?";
        int index = 0;
        try {
            PreparedStatement stmt  = conn.prepareStatement(sql);
            stmt.setString(1, UserId);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            //System.out.println(rs.next());
            index = rs.getInt("id");



        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return index;
    }
    // проверка на то, есть ли в БД пользователь или нет
    public static boolean isUserExist(String UserId){
        boolean isExist = false;
        String sql = "SELECT UserId " +
                "FROM " + table_Users + " " +
                "WHERE UserId = ?";
        try {
            PreparedStatement stmt  = conn.prepareStatement(sql);
            stmt.setString(1, UserId);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            //System.out.println(rs.next());

            isExist = rs.next();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return isExist;
    }
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
            pstmt.setString(5, user.getLastChatId() + "");
            // по id понимаем, какую запись обновлять
            pstmt.setInt(6, id);
            // выполняем запрос
            pstmt.executeUpdate();
            // обновление в таблице UserSettings
            pstmt = conn.prepareStatement(UPDATE_SQL_STATEMENT_IN_USER_SETTINGS);
            // список полей для обновления
            pstmt.setString(1, user.getLastSearchMessage());
            pstmt.setString(2, user.getIndexOfParagraph() + "");
            pstmt.setString(3, getPreparedDataListOfParagraphs(user));
            pstmt.setString(4, user.getModeButtons());
            pstmt.setString(5, getPreparedDataToc(user));
            pstmt.setString(6, getPreparedDataListOfCases(user));
            pstmt.setString(7, user.getTopic_name());
            pstmt.setString(8, user.isNeedShowToc()+"");
            pstmt.setString(9, user.getTopicMode() + "");
            pstmt.setString(10, user.getLastWebLink());
            pstmt.setString(11, getPreparedDataSimilarTopics(user));
            pstmt.setString(12, getPreparedLastDateUpdate(user));
            // по id понимаем, какую запись обновлять
            pstmt.setInt(13, id);
            // выполняем апдейт
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //System.out.println("=== User was updated ===");
    }
    // готовим данные к записи в БД - преобразуем их в строки
    public static String getPreparedDataSimilarTopics(User user){
        // преобразуем данные в строку
        // чтобы декодировать данные, надо их считать и разделить по слову parse
        if (user.getSimilarTopics() == null){
            return "";
        }
        StringBuilder listOfSimilarTopicsString = new StringBuilder();
        for (String topic: user.getSimilarTopics()){
            listOfSimilarTopicsString.append(topic + "parse");
        }
        return listOfSimilarTopicsString.toString();
    }
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
    public static String getPreparedLastDateUpdate(User user){
        DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        return format.format(user.getLastDateUpdate());
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
            pstmt.setString(5, user.getLastChatId() + "");
            // выполняем запрос
            pstmt.executeUpdate();

            // аналогично готовим запрос
            pstmt = conn.prepareStatement(INSERT_SQL_STATEMENT_INTO_USERS_SETTINGS);
            // данные для вставки
            pstmt.setString(1, user.getUserId() + "");
            pstmt.setString(2, user.getLastSearchMessage());
            pstmt.setString(3, user.getIndexOfParagraph() + "");
            pstmt.setString(4, getPreparedDataListOfParagraphs(user));
            pstmt.setString(5, user.getModeButtons());
            pstmt.setString(6, getPreparedDataToc(user));
            pstmt.setString(7, getPreparedDataListOfCases(user));
            pstmt.setString(8, user.getTopic_name());
            pstmt.setString(9, user.isNeedShowToc()+"");
            pstmt.setString(10, user.getTopicMode() + "");
            pstmt.setString(11, user.getLastWebLink());
            pstmt.setString(12, getPreparedDataSimilarTopics(user));
            pstmt.setString(13, getPreparedLastDateUpdate(user));
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
