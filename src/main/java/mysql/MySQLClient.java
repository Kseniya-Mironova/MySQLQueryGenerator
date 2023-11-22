package mysql;

import exceptions.*;
import exceptions.MySQLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MySQLClient {

    private String user;
    private String password;
    private String db;
    private Connection connection;
    private Statement statement;

    public MySQLClient(String user, String password, String db) {
        this.user = user;
        this.password = password;
        this.db = db;
    }

    public Connection connect() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/" + db + "?user=" + user + "&password=" + password);

        statement = connection.createStatement();
        return connection;
    }

    public void disconnect() throws SQLException {
        statement.close();
        connection.close();
    }

    public List select(Class className) throws SQLException, MySQLException {
        String query = "SELECT * FROM `" + mysql.Table.getTableName(className) + "`";
        ResultSet resultSet = statement.executeQuery(query);
        List result = mysql.ResultSetExtractor.extractResultSet(resultSet, className);
        resultSet.close();
        return result;
    }
}
