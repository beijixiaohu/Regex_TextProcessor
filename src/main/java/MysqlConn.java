
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MysqlConn {
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/regextextprocessor?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8";
    private static final String USER = "admin";
    private static final String PASSWORD = "123456";
    private List<Connection> connections = new ArrayList<>();

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            // System.out.println("连接成功！");
            connections.add(connection);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnections() {
        for (Connection connection : connections) {
            try {
                if (connection != null) {
                    connection.close();
                    // System.out.println("连接已关闭！");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Rules表结构：
     * +-------+--------+-------------+--------+------+
     * |UserID |  regex | replacement | isOpen | note |
     * +-------+--------+-------------+--------+------+
     */
    //同步到数据库
    public void syncDataToDB() {
        List<Rule> rules = new ArrayList<>();
        try {
            rules = FileHandler.readRules();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Connection connection = getConnection()) {
            int userID = getUserID();
            // 更新本地记录到数据库
            String selectSql = "SELECT regex, replacement, isOpen, note FROM rules WHERE UserID = ?";
            String insertSql = "INSERT INTO rules (UserID, regex, replacement, isOpen, note) VALUES (?, ?, ?, ?, ?)";
            String updateSql = "UPDATE rules SET replacement = ?, isOpen = ?, note = ? WHERE UserID = ? AND regex = ?";
            String deleteSql = "DELETE FROM rules WHERE UserID = ? AND regex = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                 PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                 PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                selectStmt.setInt(1, userID);
                
                for (Rule rule : rules) {
                    boolean isExist = false;
                    selectStmt.setInt(1, userID);
                    try (ResultSet resultSet = selectStmt.executeQuery()) {
                        while (resultSet.next()) {
                            String regex = resultSet.getString("regex");
                            if (rule.getRegex().equals(regex)) {
                                isExist = true;
                                String replacement = resultSet.getString("replacement");
                                boolean isOpen = resultSet.getBoolean("isOpen");
                                String note = resultSet.getString("note");
                                if (!rule.getReplacement().equals(replacement) || rule.isOpen() != isOpen || !rule.getNote().equals(note)) {
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                    if (!isExist) {
                        insertStmt.setInt(1, userID);
                        insertStmt.setString(2, rule.getRegex());
                        insertStmt.setString(3, rule.getReplacement());
                        insertStmt.setBoolean(4, rule.isOpen());
                        insertStmt.setString(5, rule.getNote());
                        insertStmt.executeUpdate();
                    } else {
                        updateStmt.setString(1, rule.getReplacement());
                        updateStmt.setBoolean(2, rule.isOpen());
                        updateStmt.setString(3, rule.getNote());
                        updateStmt.setInt(4, userID);
                        updateStmt.setString(5, rule.getRegex());
                        updateStmt.executeUpdate();
                    }
                }
                
                // 删除数据库中多余的记录
                selectStmt.setInt(1, userID);
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    while (resultSet.next()) {
                        String regex = resultSet.getString("regex");
                        boolean isExist = false;
                        for (Rule rule : rules) {
                            if (rule.getRegex().equals(regex)) {
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            deleteStmt.setInt(1, userID);
                            deleteStmt.setString(2, regex);
                            deleteStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 同步rule到本地
    public void syncDataToLocal() {
        List<Rule> rules = new ArrayList<>();
        try (Connection connection = getConnection()) {
            int userID = getUserID();
            String selectSql = "SELECT regex, replacement, isOpen, note FROM rules WHERE UserID = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userID);
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    while (resultSet.next()) {
                        String regex = resultSet.getString("regex");
                        String replacement = resultSet.getString("replacement");
                        boolean isOpen = resultSet.getBoolean("isOpen");
                        String note = resultSet.getString("note");
                        rules.add(new Rule(regex, replacement, isOpen, note));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Rule> localRules = new ArrayList<>();
        try {
            localRules = FileHandler.readRules();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Rule rule : rules) {
            boolean isExist = false;
            for (Rule localRule : localRules) {
                if (rule.getRegex().equals(localRule.getRegex())) {
                    isExist = true;
                    if(localRule.replacement()!=rule.getReplacement()||localRule.getNote()!=rule.getNote()||localRule.isOpen()!=rule.isOpen()){
                        localRules.remove(localRule);
                        localRules.add(rule);
                    }
                    break;
                }
            }
            if (!isExist) {
                localRules.add(rule);
            }
        }
        FileHandler.writeRules(localRules);
    }

    /**
     * exRules表结构：
     * +-------+--------+-------------+--------+
     * |UserID |  note  | regex       | isOpen |
     * +-------+--------+-------------+--------+
     */
    //同步到数据库
    public void syncExDataToDB() {
        List<Ex> rules = new ArrayList<>();
        try {
            rules = FileHandler.readExRules();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Connection connection = getConnection()) {
            int userID = getUserID();
            // 更新本地记录到数据库
            String selectSql = "SELECT note, regex, isOpen FROM exRules WHERE UserID = ?";
            String insertSql = "INSERT INTO exRules (UserID, note, regex, isOpen) VALUES (?, ?, ?, ?)";
            String updateSql = "UPDATE exRules SET note = ?, isOpen = ? WHERE UserID = ? AND regex = ?";
            String deleteSql = "DELETE FROM exRules WHERE UserID = ? AND regex = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                 PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                 PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                selectStmt.setInt(1, userID);
                
                for (Ex rule : rules) {
                    boolean isExist = false;
                    selectStmt.setInt(1, userID);
                    try (ResultSet resultSet = selectStmt.executeQuery()) {
                        while (resultSet.next()) {
                            String regex = resultSet.getString("regex");
                            if (rule.getRegex().equals(regex)) {
                                isExist = true;
                                String note = resultSet.getString("note");
                                boolean isOpen = resultSet.getBoolean("isOpen");
                                if (!rule.getNote().equals(note) || rule.isOpen() != isOpen) {
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                    if (!isExist) {
                        insertStmt.setInt(1, userID);
                        insertStmt.setString(2, rule.getNote());
                        insertStmt.setString(3, rule.getRegex());
                        insertStmt.setBoolean(4, rule.isOpen());
                        insertStmt.executeUpdate();
                    } else {
                        updateStmt.setString(1, rule.getNote());
                        updateStmt.setBoolean(2, rule.isOpen());
                        updateStmt.setInt(3, userID);
                        updateStmt.setString(4, rule.getRegex());
                        updateStmt.executeUpdate();
                    }
                }
                
                // 删除数据库中多余的记录
                selectStmt.setInt(1, userID);
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    while (resultSet.next()) {
                        String regex = resultSet.getString("regex");
                        boolean isExist = false;
                        for (Ex rule : rules) {
                            if (rule.getRegex().equals(regex)) {
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            deleteStmt.setInt(1, userID);
                            deleteStmt.setString(2, regex);
                            deleteStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 同步到本地
    public void syncExDataToLocal() {
        List<Ex> rules = new ArrayList<>();
        try (Connection connection = getConnection()) {
            int userID = getUserID();
            String selectSql = "SELECT note, regex, isOpen FROM exRules WHERE UserID = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setInt(1, userID);
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    while (resultSet.next()) {
                        String note = resultSet.getString("note");
                        String regex = resultSet.getString("regex");
                        boolean isOpen = resultSet.getBoolean("isOpen");
                        rules.add(new Ex(isOpen, regex, note));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Ex> localRules = new ArrayList<>();
        try {
            localRules = FileHandler.readExRules();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Ex rule : rules) {
            boolean isExist = false;
            for (Ex localRule : localRules) {
                if (rule.getRegex().equals(localRule.getRegex())) {
                    isExist = true;
                    if(localRule.getNote()!=rule.getNote()||localRule.getIsOpen()!=rule.getIsOpen()){
                        localRules.remove(localRule);
                        localRules.add(rule);
                    }
                    break;
                }
            }
            if (!isExist) {
                localRules.add(rule);
            }
        }
        FileHandler.writeExRules(localRules);
    }



    // 用户ID获取
    public int getUserID() {
        List<User> users = new ArrayList<>();
        try {
            users = FileHandler.readUser();
        } catch (IOException e) {
            e.printStackTrace();
        }
        User user = users.get(users.size()-1);
        String userNameText = user.username();
        String passWordText = user.password();

        try (Connection connection = getConnection()) {
            String selectSql = "SELECT UserID FROM users WHERE UserName = ? AND PassWord = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setString(1, userNameText);
                selectStmt.setString(2, passWordText);
                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("UserID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 用户注册
    public int registerUser(String userNameText, String passWordText) {

        try (Connection connection = getConnection()) {
            String insertSql = "INSERT INTO users (UserName, PassWord) VALUES (?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, userNameText);
                insertStmt.setString(2, passWordText);
                
                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}


